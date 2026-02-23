/*
 * Copyright 2023-2025 Attila Fazekas & contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.kolibrium.api.ksp.processors

import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Modifier
import dev.kolibrium.api.ksp.annotations.Auth
import dev.kolibrium.api.ksp.annotations.AuthType
import dev.kolibrium.api.ksp.annotations.Header
import dev.kolibrium.api.ksp.annotations.Path
import dev.kolibrium.api.ksp.annotations.Query
import dev.kolibrium.api.ksp.annotations.Returns

internal fun validateRequestClass(
    requestClass: KSClassDeclaration,
    apiInfo: ApiSpecInfo,
    errors: MutableList<Diagnostic>,
    warnings: MutableList<Diagnostic>,
): RequestClassInfo? {
    val className = requestClass.getClassName()

    val simpleName = requestClass.simpleName.asString()
    val functionName = deriveFunctionName(simpleName)

    if (!simpleName.endsWith("Request") || simpleName == "Request") {
        errors +=
            Diagnostic(
                "Request class '$simpleName' must end with 'Request' suffix and have a descriptive name (e.g., 'GetUserRequest', 'CreateOrderRequest')",
                requestClass,
            )
        return null
    }

    if (!functionName.isValidKotlinIdentifier()) {
        errors +=
            Diagnostic(
                "Request class name '$className' generates invalid function name '$functionName'. " +
                    "Function name must be a valid Kotlin identifier.",
                requestClass,
            )
        return null
    }

    if (requestClass.modifiers.contains(Modifier.ABSTRACT) ||
        requestClass.modifiers.contains(Modifier.SEALED) ||
        requestClass.modifiers.contains(Modifier.INNER)
    ) {
        errors += Diagnostic("Request class $className cannot be abstract, sealed, or inner", requestClass)
        return null
    }

    val httpAnnotations = requestClass.getHttpMethodAnnotations()
    if (httpAnnotations.size != 1) {
        errors += Diagnostic("Class $className has multiple HTTP method annotations", requestClass)
        return null
    }

    val methodAnnotation = httpAnnotations.single()
    val httpMethod =
        methodAnnotation.toHttpMethod()
            ?: run {
                errors += Diagnostic("Class $className has an unsupported HTTP method annotation", requestClass)
                return null
            }

    val path = methodAnnotation.getArgumentValue("path")?.let { it as? String }

    if (path.isNullOrBlank()) {
        errors += Diagnostic("HTTP method annotation on $className must specify a path", requestClass)
        return null
    }

    // Validate path format (malformed paths, brace syntax)
    if (!validatePathFormat(path, requestClass, errors)) {
        return null
    }

    // Normalize trailing slash: strip it for consistency (except root "/")
    val normalizedPath =
        if (path.length > 1 && path.endsWith("/")) {
            warnings +=
                Diagnostic(
                    "Trailing slash in path '$path' on $className was stripped for consistency",
                    requestClass,
                )
            path.trimEnd('/')
        } else {
            path
        }

    val returnsAnnotation = requestClass.getAnnotation(Returns::class)
    if (returnsAnnotation == null) {
        errors +=
            Diagnostic(
                "Request class $className with HTTP method annotation must have @Returns annotation",
                requestClass,
            )
        return null
    }

    val returnType = returnsAnnotation.getKClassTypeArgument("success")
    if (returnType == null || returnType.isError) {
        errors += Diagnostic("Success type for $className could not be resolved", requestClass)
        return null
    }

    val isEmptyResponse = returnType.declaration.qualifiedName?.asString() == KOTLIN_UNIT

    // Extract optional error type
    val errorType = returnsAnnotation.getKClassTypeArgument("error")
    val errorQualifiedName = errorType?.declaration?.qualifiedName?.asString()
    // KSP resolves Nothing::class defaults as java.lang.Void
    val resolvedErrorType =
        if (errorType != null &&
            !errorType.isError &&
            errorQualifiedName != KOTLIN_NOTHING &&
            errorQualifiedName != "java.lang.Void"
        ) {
            errorType
        } else {
            null
        }

    // Collect parameters
    val properties = requestClass.getAllProperties().toList()
    val ctorDefaults =
        requestClass.primaryConstructor
            ?.parameters
            ?.associateBy(
                keySelector = { it.name?.asString().orEmpty() },
                valueTransform = { it.hasDefault },
            ).orEmpty()

    val pathProperties = mutableListOf<KSPropertyDeclaration>()
    val queryProperties = mutableListOf<KSPropertyDeclaration>()
    val headerProperties = mutableListOf<KSPropertyDeclaration>()
    val bodyProperties = mutableListOf<KSPropertyDeclaration>()

    properties.forEach { property ->
        val isPath = property.hasAnnotation(Path::class)
        val isQuery = property.hasAnnotation(Query::class)
        val isHeader = property.hasAnnotation(Header::class)
        val annotationCount = listOf(isPath, isQuery, isHeader).count { it }
        if (annotationCount > 1) {
            errors +=
                Diagnostic(
                    "Property '${property.simpleName.asString()}' in $className cannot have multiple parameter annotations (@Path, @Query, @Header)",
                    property,
                )
            return@forEach
        }
        when {
            isPath -> pathProperties += property
            isQuery -> queryProperties += property
            isHeader -> headerProperties += property
            else -> bodyProperties += property
        }
    }

    // Object declarations must not have properties — use a data class for requests with parameters.
    if (requestClass.classKind == ClassKind.OBJECT && properties.isNotEmpty()) {
        errors +=
            Diagnostic(
                "Object request class $className cannot have properties — use a data class for requests with parameters",
                requestClass,
            )
        return null
    }

    // Marker request classes (no properties) must be object declarations.
    // Request classes with properties must be data classes.
    if (properties.isEmpty() && requestClass.classKind != ClassKind.OBJECT) {
        errors += Diagnostic("Marker request class $className with no properties must be an object declaration", requestClass)
        return null
    }
    if (properties.isNotEmpty() && !requestClass.modifiers.contains(Modifier.DATA)) {
        errors += Diagnostic("Request class $className must be a data class", requestClass)
        return null
    }

    val authType = extractAuthType(requestClass, errors) ?: return null
    val apiKeyHeader = extractApiKeyHeader(requestClass)

    if (authType == AuthType.API_KEY && !apiKeyHeader.isValidHttpHeaderName()) {
        errors +=
            Diagnostic(
                "Invalid API key header name: '$apiKeyHeader' in request class $className",
                requestClass,
            )
        return null
    }

    // Validate that headerName is only used with API_KEY auth type
    val authAnnotation = requestClass.getAnnotation(Auth::class)
    if (authAnnotation != null && authType != AuthType.API_KEY) {
        val headerNameValue = authAnnotation.getArgumentValue("headerName") as? String
        // Only error if headerName was explicitly provided (not the default)
        if (headerNameValue != null && headerNameValue != "X-API-Key") {
            errors +=
                Diagnostic(
                    "headerName parameter can only be used with AuthType.API_KEY, but request uses AuthType.$authType",
                    requestClass,
                )
        }
    }

    return RequestClassInfo(
        requestClass = requestClass,
        httpMethod = httpMethod,
        path = normalizedPath,
        group = extractGroupByApiPrefix(normalizedPath),
        returnType = returnType,
        errorType = resolvedErrorType,
        isEmptyResponse = isEmptyResponse,
        pathProperties = pathProperties,
        queryProperties = queryProperties,
        headerProperties = headerProperties,
        bodyProperties = bodyProperties,
        ctorDefaults = ctorDefaults,
        apiPackage = apiInfo.packageName,
        authType = authType,
        apiKeyHeader = apiKeyHeader,
        endpointName = requestClass.simpleName.asString().removeSuffix("Request"),
    )
}

private fun extractAuthType(
    requestClass: KSClassDeclaration,
    errors: MutableList<Diagnostic>,
): AuthType? {
    val annotation = requestClass.getAnnotation(Auth::class) ?: return AuthType.NONE

    val enumName =
        when (val authArg = annotation.getArgumentValue("type")) {
            is KSType -> {
                authArg.declaration.simpleName.asString()
            }

            is KSClassDeclaration -> {
                authArg.simpleName.asString()
            }

            else -> {
                errors +=
                    Diagnostic(
                        "Could not resolve auth type on ${requestClass.getClassName()}",
                        requestClass,
                    )
                return null
            }
        }
    return AuthType.entries.firstOrNull { it.name == enumName }
        ?: run {
            errors +=
                Diagnostic(
                    "Unknown auth type '$enumName' on ${requestClass.getClassName()}",
                    requestClass,
                )
            null
        }
}

private fun extractApiKeyHeader(requestClass: KSClassDeclaration): String {
    val annotation = requestClass.getAnnotation(Auth::class) ?: return "X-API-Key"
    return annotation.getArgumentValue("headerName") as? String ?: "X-API-Key"
}

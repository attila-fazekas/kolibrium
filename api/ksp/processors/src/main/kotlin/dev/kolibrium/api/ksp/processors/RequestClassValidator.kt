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
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Modifier
import dev.kolibrium.api.ksp.annotations.Auth
import dev.kolibrium.api.ksp.annotations.AuthType
import dev.kolibrium.api.ksp.annotations.Path
import dev.kolibrium.api.ksp.annotations.Query
import dev.kolibrium.api.ksp.annotations.Returns

internal fun validateRequestClass(requestClass: KSClassDeclaration): ValidationResult<RequestClassInfo> {
    val errors = mutableListOf<Diagnostic>()
    val warnings = mutableListOf<Diagnostic>()
    val className = requestClass.getClassName()

    val simpleName = requestClass.simpleName.asString()
    val functionName = deriveFunctionName(simpleName)

    if (!simpleName.endsWith("Request") || simpleName == "Request") {
        return ValidationResult.Invalid(
            listOf(
                Diagnostic(
                    "Request class '$simpleName' must end with 'Request' suffix and have a descriptive name (e.g., 'GetUserRequest', 'CreateOrderRequest')",
                    requestClass,
                ),
            ),
        )
    }

    if (!functionName.isValidKotlinIdentifier()) {
        return ValidationResult.Invalid(
            listOf(
                Diagnostic(
                    "Request class name '$className' generates invalid function name '$functionName'. " +
                        "Function name must be a valid Kotlin identifier.",
                    requestClass,
                ),
            ),
        )
    }

    if (requestClass.modifiers.contains(Modifier.ABSTRACT) ||
        requestClass.modifiers.contains(Modifier.SEALED) ||
        requestClass.modifiers.contains(Modifier.INNER)
    ) {
        return ValidationResult.Invalid(
            listOf(Diagnostic("Request class $className cannot be abstract, sealed, or inner", requestClass)),
        )
    }

    val httpAnnotations = requestClass.getHttpMethodAnnotations()
    if (httpAnnotations.size != 1) {
        return ValidationResult.Invalid(
            listOf(Diagnostic("Class $className has multiple HTTP method annotations", requestClass)),
        )
    }

    val methodAnnotation = httpAnnotations.single()

    val httpMethod = methodAnnotation.toHttpMethod()!!

    val path = methodAnnotation.getArgumentValue("path")?.let { it as? String }

    if (path.isNullOrBlank()) {
        return ValidationResult.Invalid(
            listOf(Diagnostic("HTTP method annotation on $className must specify a path", requestClass)),
        )
    }

    // Validate path format (malformed paths, brace syntax)
    val pathErrors = validatePathFormat(path, requestClass)
    if (pathErrors.isNotEmpty()) return ValidationResult.Invalid(pathErrors)

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

    val returnsAnnotation =
        requestClass.getAnnotation(Returns::class) ?: return ValidationResult.Invalid(
            listOf(
                Diagnostic(
                    "Request class $className with HTTP method annotation must have @Returns annotation",
                    requestClass,
                ),
            ),
        )

    val returnType = returnsAnnotation.getKClassTypeArgument("success")
    if (returnType == null || returnType.isError) {
        return ValidationResult.Invalid(
            listOf(Diagnostic("Success type for $className could not be resolved", requestClass)),
        )
    }

    val successQualifiedName = returnType.declaration.qualifiedName?.asString()

    // KSP represents Nothing::class as java.lang.Void at the symbol level
    if (successQualifiedName == KOTLIN_NOTHING || successQualifiedName == JAVA_LANG_VOID) {
        return ValidationResult.Invalid(
            listOf(
                Diagnostic(
                    "Nothing::class is not allowed as a success type in @Returns on $className. Use Unit::class for empty responses",
                    requestClass,
                ),
            ),
        )
    }

    val isEmptyResponse = successQualifiedName == KOTLIN_UNIT

    // Extract optional error type
    val errorType = returnsAnnotation.getKClassTypeArgument("error")
    val errorQualifiedName = errorType?.declaration?.qualifiedName?.asString()
    // KSP resolves Nothing::class defaults as java.lang.Void
    val resolvedErrorType =
        if (errorType != null &&
            !errorType.isError &&
            errorQualifiedName != KOTLIN_NOTHING &&
            errorQualifiedName != JAVA_LANG_VOID &&
            errorQualifiedName != KOTLIN_UNIT
        ) {
            errorType
        } else if (errorQualifiedName == KOTLIN_UNIT) {
            return ValidationResult.Invalid(
                listOf(
                    Diagnostic(
                        "Unit::class is not allowed as an error type in @Returns on $className. Use Nothing::class (the default) to omit the error type",
                        requestClass,
                    ),
                ),
            )
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
    val bodyProperties = mutableListOf<KSPropertyDeclaration>()

    properties.forEach { property ->
        val isPath = property.hasAnnotation(Path::class)
        val isQuery = property.hasAnnotation(Query::class)
        val annotationCount = listOf(isPath, isQuery).count { it }
        if (annotationCount > 1) {
            errors +=
                Diagnostic(
                    "Property '${property.simpleName.asString()}' in $className cannot have " +
                        "multiple parameter annotations (@Path, @Query) $property",
                )
            return@forEach
        }
        when {
            isPath -> pathProperties += property
            isQuery -> queryProperties += property
            else -> bodyProperties += property
        }
    }

    // Request classes with properties must be data classes.
    if (requestClass.classKind == ClassKind.OBJECT && properties.isNotEmpty()) {
        return ValidationResult.Invalid(
            listOf(
                Diagnostic(
                    "Object request class $className cannot have properties — use a data class for requests with parameters",
                    requestClass,
                ),
            ),
        )
    }

    // Marker request classes (no properties) must be object declarations.
    if (properties.isEmpty() && requestClass.classKind != ClassKind.OBJECT) {
        return ValidationResult.Invalid(
            listOf(
                Diagnostic(
                    "Marker request class $className with no properties must be an object declaration",
                    requestClass,
                ),
            ),
        )
    }
    if (properties.isNotEmpty() && !requestClass.modifiers.contains(Modifier.DATA)) {
        return ValidationResult.Invalid(
            listOf(Diagnostic("Request class $className must be a data class", requestClass)),
        )
    }

    val authAnnotation = requestClass.getAnnotation(Auth::class)
    val authType = extractAuthType(authAnnotation)

    val apiKeyHeader = extractApiKeyHeader(requestClass)

    if (authType == AuthType.API_KEY && !apiKeyHeader.isValidHttpHeaderName()) {
        return ValidationResult.Invalid(
            listOf(
                Diagnostic(
                    "Invalid API key header name: '$apiKeyHeader' in request class $className",
                    requestClass,
                ),
            ),
        )
    }

    // Validate that headerName is only used with API_KEY auth type
    if (authAnnotation != null && authType != AuthType.API_KEY) {
        val headerNameValue = authAnnotation.getArgumentValue("headerName") as? String
        // Only error if headerName was explicitly provided (not the default)
        if (headerNameValue != null && headerNameValue != API_KEY_HEADER) {
            errors +=
                Diagnostic(
                    "headerName parameter can only be used with AuthType.API_KEY, but request uses AuthType.$authType",
                    requestClass,
                )
        }
    }

    if (errors.isNotEmpty()) {
        return ValidationResult.Invalid(errors, warnings)
    }

    return ValidationResult.Valid(
        value =
            RequestClassInfo(
                requestClass = requestClass,
                httpMethod = httpMethod,
                path = normalizedPath,
                returnType = returnType,
                errorType = resolvedErrorType,
                pathProperties = pathProperties,
                queryProperties = queryProperties,
                bodyProperties = bodyProperties,
                ctorDefaults = ctorDefaults,
                authType = authType,
            ),
        warnings = warnings,
    )
}

private fun validatePathFormat(
    path: String,
    requestClass: KSClassDeclaration,
): List<Diagnostic> {
    val errors = mutableListOf<Diagnostic>()

    val className = requestClass.simpleName.asString()

    if (!path.startsWith("/")) {
        errors += Diagnostic("Path '$path' in $className must start with '/'", requestClass)
    }

    if (path.contains('?') || path.contains('&') || path.contains('#')) {
        errors +=
            Diagnostic(
                "Path '$path' in $className must not contain query strings or fragment identifiers ('?', '&', '#'). Use @Query parameters instead",
                requestClass,
            )
    }

    if (path.contains("//")) {
        errors += Diagnostic("Path '$path' in $className contains empty segments (double slashes)", requestClass)
    }

    if (path.contains("{}")) {
        errors += Diagnostic("Path '$path' in $className contains empty braces", requestClass)
    }

    // Nested braces: /users/{{id}}
    if (Regex("""\{[^}]*\{""").containsMatchIn(path)) {
        errors += Diagnostic("Path '$path' in $className contains nested braces", requestClass)
    }

    // Reversed braces: /users/}id{  — match }...{ within the same segment (no / between)
    if (Regex("""}[^{/]*\{""").containsMatchIn(path)) {
        errors += Diagnostic("Path '$path' in $className contains reversed braces", requestClass)
    }

    // Unclosed/mismatched braces
    val openCount = path.count { it == '{' }
    val closeCount = path.count { it == '}' }
    if (openCount != closeCount) {
        errors += Diagnostic("Path '$path' in $className has mismatched braces", requestClass)
    }

    return errors
}

private fun extractAuthType(annotation: KSAnnotation?): AuthType {
    annotation ?: return AuthType.NONE
    val enumName = (annotation.getArgumentValue("type") as KSClassDeclaration).simpleName.asString()
    return AuthType.entries.first { it.name == enumName }
}

private fun extractApiKeyHeader(requestClass: KSClassDeclaration): String {
    val annotation = requestClass.getAnnotation(Auth::class) ?: return API_KEY_HEADER
    return annotation.getArgumentValue("headerName") as? String ?: API_KEY_HEADER
}

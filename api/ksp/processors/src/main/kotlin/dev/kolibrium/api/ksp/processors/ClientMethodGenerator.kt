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

import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Nullability
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.ExperimentalKotlinPoetApi
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import dev.kolibrium.api.core.AuthType
import io.ktor.http.HttpMethod

internal class ClientMethodGenerator {
    @OptIn(ExperimentalKotlinPoetApi::class)
    fun generateClientMethod(
        apiInfo: ApiSpecInfo,
        info: RequestClassInfo,
        clientPackage: String,
    ): FunSpec {
        val functionName = info.requestClass.toFunctionName()
        val modelsPackage = info.requestClass.packageName.asString()
        val requestClassName = ClassName(modelsPackage, info.requestClass.simpleName.asString())
        val returnTypeName = getReturnTypeName(info, clientPackage)

        val funBuilder =
            FunSpec
                .builder(functionName)
                .addModifiers(KModifier.SUSPEND)
                .returns(returnTypeName)

        // Add context parameters based on resolved auth type
        when (info.authType) {
            AuthType.BEARER -> {
                funBuilder.contextParameter("token", String::class)
            }

            AuthType.BASIC -> {
                funBuilder
                    .contextParameter("username", String::class)
                    .contextParameter("password", String::class)
            }

            AuthType.API_KEY -> {
                funBuilder.contextParameter("apiKey", String::class)
            }

            AuthType.CUSTOM -> {
                val authContextClass = ClassName(apiInfo.packageName, "AuthContext", "Custom")
                funBuilder.contextParameter("auth", authContextClass)
            }

            AuthType.NONE -> {
                // No context parameters
            }
        }

        // Add path parameters as function parameters
        info.pathProperties.forEach { property ->
            val paramName = property.simpleName.asString()
            val paramType = property.type.resolve().toTypeName()
            funBuilder.addParameter(paramName, paramType)
        }

        // Add query parameters as optional function parameters
        info.queryProperties.forEach { property ->
            val paramName = property.simpleName.asString()
            val paramType = property.type.resolve().toTypeName()
            funBuilder.addParameter(
                ParameterSpec
                    .builder(paramName, paramType)
                    .defaultValue("null")
                    .build(),
            )
        }

        // Add DSL builder parameter for body requests
        val hasBody = info.bodyProperties.isNotEmpty()
        if (hasBody) {
            val lambdaType =
                LambdaTypeName.get(
                    receiver = requestClassName,
                    returnType = Unit::class.asTypeName(),
                )
            funBuilder.addParameter("block", lambdaType)
        }

        // Generate function body
        val codeBlock = generateClientMethodBody(info, requestClassName, hasBody, clientPackage)
        funBuilder.addCode(codeBlock)

        return funBuilder.build()
    }

    fun generateClientMethodBody(
        info: RequestClassInfo,
        requestClassName: ClassName,
        hasBody: Boolean,
        clientPackage: String,
    ): CodeBlock {
        val builder = CodeBlock.builder()

        // For requests with body, create a request object
        if (hasBody) {
            if (info.pathProperties.isNotEmpty()) {
                // Pass path params to constructor
                val pathParams =
                    info.pathProperties.joinToString(", ") {
                        "${it.simpleName.asString()} = ${it.simpleName.asString()}"
                    }
                builder.addStatement("val request = %T($pathParams).apply(block)", requestClassName)
            } else {
                builder.addStatement("val request = %T().apply(block)", requestClassName)
            }
            builder.add("\n")
        }

        // Build URL with path parameters
        val urlPath = buildUrlPath(info)

        // Get the HTTP method name
        val httpMethodName =
            when (info.httpMethod) {
                HttpMethod.Get -> "get"
                HttpMethod.Post -> "post"
                HttpMethod.Put -> "put"
                HttpMethod.Delete -> "delete"
                HttpMethod.Patch -> "patch"
                else -> "get"
            }

        val hasQueryParams = info.queryProperties.isNotEmpty()
        val needsAuth = info.authType != AuthType.NONE

        // Generate HTTP request
        if (hasBody || hasQueryParams || needsAuth) {
            builder.addStatement($$"val httpResponse = client.%L(\"$baseUrl%L\") {", httpMethodName, urlPath)
            builder.indent()

            // Apply authentication based on resolved type
            when (info.authType) {
                AuthType.BEARER -> {
                    builder.addStatement("%M(token)", BEARER_AUTH_MEMBER)
                }

                AuthType.BASIC -> {
                    builder.addStatement("%M(username, password)", BASIC_AUTH_MEMBER)
                }

                AuthType.API_KEY -> {
                    builder.addStatement("header(%S, apiKey)", info.apiKeyHeader)
                }

                AuthType.CUSTOM -> {
                    builder.addStatement("auth.configure(this)")
                }

                AuthType.NONE -> {
                    // No authentication
                }
            }

            // Content type and body for non-GET/DELETE requests
            if (hasBody) {
                builder.addStatement("%M(%T.Application.Json)", CONTENT_TYPE_MEMBER, CONTENT_TYPE_CLASS)
                builder.addStatement("%M(request)", SET_BODY_MEMBER)
            }

            // Query parameters
            if (hasQueryParams) {
                info.queryProperties.forEach { property ->
                    val paramName = property.simpleName.asString()
                    builder.addStatement("$paramName?.let { %M(%S, it) }", PARAMETER_MEMBER, paramName)
                }
            }

            builder.unindent()
            builder.addStatement("}")
        } else {
            // Simple request with no body, query params, or auth
            builder.addStatement($$"val httpResponse = client.%L(\"$baseUrl%L\")", httpMethodName, urlPath)
        }

        builder.add("\n")

        // If error type is specified, return sealed result type
        if (info.errorType != null) {
            val resultTypeName = getResultTypeName(info.requestClass.simpleName.asString())
            val resultClass = ClassName(clientPackage, resultTypeName)
            val successClass = resultClass.nestedClass("Success")
            val errorClass = resultClass.nestedClass("Error")

            builder.addStatement("return if (httpResponse.status.isSuccess()) {")
            builder.indent()
            builder.addStatement("%T(", successClass)
            builder.indent()
            builder.addStatement("data = httpResponse.%M(),", BODY_MEMBER)
            builder.addStatement("response = httpResponse,")
            builder.unindent()
            builder.addStatement(")")
            builder.unindent()
            builder.addStatement("} else {")
            builder.indent()
            builder.beginControlFlow("try")
            builder.addStatement("%T(", errorClass)
            builder.indent()
            builder.addStatement("data = httpResponse.%M(),", BODY_MEMBER)
            builder.addStatement("response = httpResponse,")
            builder.unindent()
            builder.addStatement(")")
            builder.nextControlFlow("catch (e: %T)", EXCEPTION_CLASS)

            // Get the error type for the fallback
            val errorTypeDeclaration = info.errorType.declaration
            val errorClassName =
                ClassName(
                    errorTypeDeclaration.packageName.asString(),
                    errorTypeDeclaration.simpleName.asString(),
                )
            builder.addStatement(
                "throw %T(%S + %T::class.simpleName + %S + httpResponse.status.value + %S + (e.message ?: %S))",
                ILLEGAL_STATE_EXCEPTION_CLASS,
                "Failed to parse error response as ",
                errorClassName,
                " (HTTP ",
                "): ",
                "unknown error",
            )
            builder.endControlFlow()
            builder.unindent()
            builder.addStatement("}")
        } else {
            // Return ApiResponse
            builder.addStatement("return %T(", API_RESPONSE_CLASS)
            builder.indent()
            builder.addStatement("status = httpResponse.status,")
            builder.addStatement("headers = httpResponse.headers,")
            builder.addStatement("contentType = httpResponse.%M(),", CONTENT_TYPE_MEMBER)

            if (info.isEmptyResponse) {
                builder.addStatement("body = Unit,")
            } else {
                builder.addStatement("body = httpResponse.%M(),", BODY_MEMBER)
            }
            builder.unindent()
            builder.addStatement(")")
        }

        return builder.build()
    }

    fun getReturnTypeName(
        info: RequestClassInfo,
        clientPackage: String,
    ): TypeName {
        // If error type is specified, return the sealed result type
        if (info.errorType != null) {
            val resultTypeName = getResultTypeName(info.requestClass.simpleName.asString())
            return ClassName(clientPackage, resultTypeName)
        }

        // Standard return type without error handling
        return if (info.isEmptyResponse) {
            EMPTY_RESPONSE_CLASS
        } else {
            val returnTypeDeclaration = info.returnType.declaration
            val returnClassName =
                ClassName(
                    returnTypeDeclaration.packageName.asString(),
                    returnTypeDeclaration.simpleName.asString(),
                )
            API_RESPONSE_CLASS.parameterizedBy(returnClassName)
        }
    }

    fun getResultTypeName(requestClassName: String): String = requestClassName.removeSuffix("Request") + "Result"

    fun buildUrlPath(info: RequestClassInfo): String {
        var path = info.path
        info.pathProperties.forEach { property ->
            val paramName = property.simpleName.asString()
            path = path.replace("{$paramName}", $$"$$$paramName")
        }
        return path
    }

    fun collectHttpMethodNames(requests: List<RequestClassInfo>): List<String> =
        requests
            .map { info ->
                when (info.httpMethod) {
                    HttpMethod.Get -> "get"
                    HttpMethod.Post -> "post"
                    HttpMethod.Put -> "put"
                    HttpMethod.Delete -> "delete"
                    HttpMethod.Patch -> "patch"
                    else -> "get"
                }
            }.distinct()
}

internal fun KSType.toTypeName(): TypeName {
    val declaration = this.declaration
    val baseClassName =
        ClassName(
            declaration.packageName.asString(),
            declaration.simpleName.asString(),
        )

    // Handle generic types (e.g., List<String>)
    val typeArguments = this.arguments
    val typeName =
        if (typeArguments.isNotEmpty()) {
            val typeArgumentNames =
                typeArguments.mapNotNull { typeArg ->
                    typeArg.type?.resolve()?.toTypeName()
                }
            if (typeArgumentNames.size == typeArguments.size) {
                baseClassName.parameterizedBy(typeArgumentNames)
            } else {
                // Fallback if we can't resolve all type arguments
                baseClassName
            }
        } else {
            baseClassName
        }

    // Apply nullability
    return if (this.nullability == Nullability.NULLABLE) {
        typeName.copy(nullable = true)
    } else {
        typeName
    }
}

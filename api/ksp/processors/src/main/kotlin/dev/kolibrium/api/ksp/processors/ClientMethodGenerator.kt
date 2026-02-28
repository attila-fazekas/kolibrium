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
import com.squareup.kotlinpoet.ksp.toTypeName
import dev.kolibrium.api.ksp.annotations.AuthType
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

        funBuilder.addKdoc("Performs a %L request to %L.", info.httpMethod.value, info.path)

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
                val customAuthType =
                    LambdaTypeName.get(
                        receiver = HTTP_REQUEST_BUILDER_CLASS,
                        returnType = Unit::class.asTypeName(),
                    )
                funBuilder.contextParameter("customAuth", customAuthType)
            }

            AuthType.NONE -> {
                // No context parameters
            }
        }

        // Add path parameters as function parameters
        info.pathProperties.forEach { property ->
            val paramName = property.simpleName.asString()
            val paramType = property.type.resolve().toTypeName()
            val paramBuilder =
                ParameterSpec
                    .builder(paramName, paramType)
                    .addKdoc("path parameter — substituted into `%L`", info.path)
            funBuilder.addParameter(paramBuilder.build())
        }

        // Add headers parameter (unconditional — applies to all request types)
        val headersLambdaType =
            LambdaTypeName.get(
                receiver = HEADERS_BUILDER_CLASS,
                returnType = Unit::class.asTypeName(),
            )
        val headersBuilder =
            ParameterSpec
                .builder("headers", headersLambdaType)
                .defaultValue("{}")
                .addKdoc("custom headers builder")
        funBuilder.addParameter(headersBuilder.build())

        // Add query parameters as DSL block parameter
        val hasQueryParams = info.queryProperties.isNotEmpty()

        // Add DSL builder parameter for body or query requests
        val hasBody = info.bodyProperties.isNotEmpty()
        if (hasBody) {
            val lambdaType =
                LambdaTypeName.get(
                    receiver = requestClassName,
                    returnType = Unit::class.asTypeName(),
                )
            val blockBuilder =
                ParameterSpec
                    .builder("block", lambdaType)
                    .addKdoc("request body builder")
            funBuilder.addParameter(blockBuilder.build())
        } else if (hasQueryParams) {
            val lambdaType =
                LambdaTypeName.get(
                    receiver = requestClassName,
                    returnType = Unit::class.asTypeName(),
                )
            val blockBuilder =
                ParameterSpec
                    .builder("block", lambdaType)
                    .defaultValue("{}")
                    .addKdoc("query parameters builder")
            funBuilder.addParameter(blockBuilder.build())
        }

        // Generate function body
        val codeBlock = generateClientMethodBody(info, requestClassName, hasBody, clientPackage)
        funBuilder.addCode(codeBlock)

        return funBuilder.build()
    }

    private fun generateClientMethodBody(
        info: RequestClassInfo,
        requestClassName: ClassName,
        hasBody: Boolean,
        clientPackage: String,
    ): CodeBlock {
        val builder = CodeBlock.builder()
        val hasQueryParams = info.queryProperties.isNotEmpty()

        // For requests with body or query params, create a request object via DSL
        if (hasBody || hasQueryParams) {
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
                else -> error("Unsupported HTTP method: ${info.httpMethod}")
            }

        // Always generate the lambda form since every function has a `headers` parameter
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
                // handled separately below
            }

            AuthType.CUSTOM -> {
                builder.addStatement("customAuth()")
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
                val resolvedType = property.type.resolve()
                val typeQualifiedName = resolvedType.declaration.qualifiedName?.asString()
                if (typeQualifiedName == KOTLIN_COLLECTIONS_LIST) {
                    val elementType =
                        resolvedType.arguments
                            .firstOrNull()
                            ?.type
                            ?.resolve()
                            ?.declaration
                            ?.qualifiedName
                            ?.asString()
                    val mapSuffix = if (elementType == "kotlin.String") "" else ".map { it.toString() }"
                    builder.addStatement(
                        "request.$paramName?.let { url.parameters.appendAll(%S, it$mapSuffix) }",
                        paramName,
                    )
                } else {
                    builder.addStatement("request.$paramName?.let { %M(%S, it) }", PARAMETER_MEMBER, paramName)
                }
            }
        }

        builder.unindent()
        builder.addStatement("}")

        builder.add("\n")

        // If error type is specified, return sealed result type
        if (info.errorType != null) {
            val resultTypeName = getResultTypeName(info)
            val resultClass = ClassName(clientPackage, resultTypeName)
            val successClass = resultClass.nestedClass("Success")
            val errorClass = resultClass.nestedClass("Error")

            builder.addStatement("return if (httpResponse.status.isSuccess()) {")
            builder.indent()
            builder.addStatement("%T(", successClass)
            builder.indent()
            if (info.isEmptyResponse) {
                builder.addStatement("body = Unit,")
            } else {
                builder.addStatement("body = httpResponse.%M(),", BODY_MEMBER)
            }
            builder.addStatement("response = httpResponse,")
            builder.unindent()
            builder.addStatement(")")
            builder.unindent()
            builder.addStatement("} else {")
            builder.indent()
            builder.beginControlFlow("try")
            builder.addStatement("%T(", errorClass)
            builder.indent()
            builder.addStatement("body = httpResponse.%M(),", BODY_MEMBER)
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
                "val rawBody = try { httpResponse.%M() } catch (_: %T) { %S }",
                BODY_AS_TEXT_MEMBER,
                EXCEPTION_CLASS,
                "<unavailable>",
            )
            builder.addStatement(
                "throw %T(%S + %T::class.simpleName + %S + httpResponse.status.value + %S + (e.message ?: %S) + %S + rawBody.take($ERROR_RESPONSE_BODY_MAX_LENGTH))",
                ILLEGAL_STATE_EXCEPTION_CLASS,
                "Failed to deserialize error response as ",
                errorClassName,
                " (HTTP ",
                "): ",
                "unknown error",
                ". Response body: ",
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
            val resultTypeName = getResultTypeName(info)
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

    private fun buildUrlPath(info: RequestClassInfo): String {
        var path = info.path
        info.pathProperties.forEach { property ->
            val paramName = property.simpleName.asString()
            val typeQualifiedName =
                property.type
                    .resolve()
                    .declaration.qualifiedName
                    ?.asString()
            val toStringCall = if (typeQualifiedName == "kotlin.String") "" else ".toString()"
            path = path.replace("{$paramName}", $$"${$$paramName$$toStringCall.encodeURLPathPart()}")
        }
        return path
    }

    fun hasPathParameters(requests: List<RequestClassInfo>): Boolean = requests.any { it.pathProperties.isNotEmpty() }

    fun collectHttpMethodNames(requests: List<RequestClassInfo>): List<String> =
        requests
            .map { info ->
                when (info.httpMethod) {
                    HttpMethod.Get -> "get"
                    HttpMethod.Post -> "post"
                    HttpMethod.Put -> "put"
                    HttpMethod.Delete -> "delete"
                    HttpMethod.Patch -> "patch"
                    else -> error("Unsupported HTTP method: ${info.httpMethod}")
                }
            }.distinct()
}

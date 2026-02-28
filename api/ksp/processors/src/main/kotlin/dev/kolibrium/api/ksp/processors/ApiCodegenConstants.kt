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
import com.squareup.kotlinpoet.MemberName

internal const val KOTLIN_UNIT: String = "kotlin.Unit"
internal const val KOTLIN_NOTHING: String = "kotlin.Nothing"
internal const val JAVA_LANG_VOID: String = "java.lang.Void"
internal const val API_SPEC_BASE_CLASS: String = "dev.kolibrium.api.core.ApiSpec"
internal const val KOTLIN_COLLECTIONS_LIST: String = "kotlin.collections.List"
internal const val KOTLINX_SERIALIZATION_TRANSIENT: String = "kotlinx.serialization.Transient"
internal const val KOTLIN_JVM_TRANSIENT: String = "kotlin.jvm.Transient"
internal const val ROOT_GROUP_NAME: String = "root"
internal const val ERROR_RESPONSE_BODY_MAX_LENGTH: Int = 500

internal const val API_KEY_HEADER = "X-API-Key"

internal val ALLOWED_PARAMETER_TYPES: Set<String> =
    setOf(
        "kotlin.String",
        "kotlin.Int",
        "kotlin.Long",
        "kotlin.Short",
        "kotlin.Float",
        "kotlin.Double",
        "kotlin.Boolean",
    )

internal val API_RESPONSE_CLASS: ClassName = ClassName("dev.kolibrium.api.core", "ApiResponse")
internal val EMPTY_RESPONSE_CLASS: ClassName = ClassName("dev.kolibrium.api.core", "EmptyResponse")
internal val CONTENT_TYPE_CLASS: ClassName = ClassName("io.ktor.http", "ContentType")
internal val HTTP_CLIENT_CLASS: ClassName = ClassName("io.ktor.client", "HttpClient")
internal val HTTP_REQUEST_BUILDER_CLASS: ClassName = ClassName("io.ktor.client.request", "HttpRequestBuilder")
internal val HTTP_RESPONSE_CLASS: ClassName = ClassName("io.ktor.client.statement", "HttpResponse")
internal val EXCEPTION_CLASS: ClassName = ClassName("kotlin", "Exception")
internal val ILLEGAL_STATE_EXCEPTION_CLASS: ClassName = ClassName("kotlin", "IllegalStateException")
internal val API_TEST_MEMBER: MemberName = MemberName("dev.kolibrium.api.core", "apiTest")

// Ktor request builder functions
internal val SET_BODY_MEMBER: MemberName = MemberName("io.ktor.client.request", "setBody")
internal val PARAMETER_MEMBER: MemberName = MemberName("io.ktor.client.request", "parameter")

// Ktor response functions
internal val BODY_MEMBER: MemberName = MemberName("io.ktor.client.call", "body")
internal val BODY_AS_TEXT_MEMBER: MemberName = MemberName("io.ktor.client.statement", "bodyAsText")

// Ktor HTTP functions
internal val CONTENT_TYPE_MEMBER: MemberName = MemberName("io.ktor.http", "contentType")
internal val BEARER_AUTH_MEMBER: MemberName = MemberName("io.ktor.client.request", "bearerAuth")
internal val BASIC_AUTH_MEMBER: MemberName = MemberName("io.ktor.client.request", "basicAuth")

// Ktor headers
internal val HEADERS_BUILDER_CLASS: ClassName = ClassName("io.ktor.http", "HeadersBuilder")

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

@file:OptIn(ExperimentalCompilerApi::class)

package dev.kolibrium.api.ksp.processors

import com.tschuchort.compiletesting.KotlinCompilation.ExitCode.COMPILATION_ERROR
import com.tschuchort.compiletesting.KotlinCompilation.ExitCode.OK
import com.tschuchort.compiletesting.SourceFile.Companion.kotlin
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Test

class HeaderParameterValidationTest : ApiBaseTest() {
    @Test
    fun `Valid header with custom name generates header call`() {
        val request =
            kotlin(
                "Requests.kt",
                """
                package dev.kolibrium.api.ksp.test.models
                import dev.kolibrium.api.ksp.annotations.*
                import kotlinx.serialization.Serializable
                import kotlinx.serialization.Transient
                @Serializable
                data class UserDto(val id: Int)
                @GET("/users")
                @Returns(success = UserDto::class)
                @Serializable
                data class ListUsersRequest(
                    @Header(name = "X-Correlation-ID") @Transient val correlationId: String? = null
                )
                """.trimIndent(),
            )
        val kotlinCompilation = getCompilation(validApiSpec, request)
        val compilation = kotlinCompilation.compile()
        compilation.exitCode shouldBe OK
        val source = kotlinCompilation.getGeneratedSource("TestClient.kt")
        source shouldContain "correlationId: String? = null"
        source shouldContain """correlationId?.let { header("X-Correlation-ID", it) }"""
        source shouldContain "import io.ktor.client.request.`header`"
    }

    @Test
    fun `Header with default property name uses property name as header name`() {
        val request =
            kotlin(
                "Requests.kt",
                """
                package dev.kolibrium.api.ksp.test.models
                import dev.kolibrium.api.ksp.annotations.*
                import kotlinx.serialization.Serializable
                import kotlinx.serialization.Transient
                @Serializable
                data class UserDto(val id: Int)
                @GET("/users")
                @Returns(success = UserDto::class)
                @Serializable
                data class ListUsersRequest(
                    @Header @Transient val accept: String? = null
                )
                """.trimIndent(),
            )
        val kotlinCompilation = getCompilation(validApiSpec, request)
        val compilation = kotlinCompilation.compile()
        compilation.exitCode shouldBe OK
        val source = kotlinCompilation.getGeneratedSource("TestClient.kt")
        source shouldContain "accept: String? = null"
        source shouldContain """accept?.let { header("accept", it) }"""
    }

    @Test
    fun `Header missing Transient rejected`() {
        val request =
            kotlin(
                "Requests.kt",
                """
                package dev.kolibrium.api.ksp.test.models
                import dev.kolibrium.api.ksp.annotations.*
                import kotlinx.serialization.Serializable
                @Serializable
                data class UserDto(val id: Int)
                @GET("/users")
                @Returns(success = UserDto::class)
                @Serializable
                data class ListUsersRequest(
                    @Header val accept: String? = null
                )
                """.trimIndent(),
            )
        val compilation = getCompilation(validApiSpec, request).compile()
        compilation.exitCode shouldBe COMPILATION_ERROR
        compilation.messages shouldContain "@Header parameter 'accept' must be annotated with @Transient"
    }

    @Test
    fun `Non-nullable header rejected`() {
        val request =
            kotlin(
                "Requests.kt",
                """
                package dev.kolibrium.api.ksp.test.models
                import dev.kolibrium.api.ksp.annotations.*
                import kotlinx.serialization.Serializable
                import kotlinx.serialization.Transient
                @Serializable
                data class UserDto(val id: Int)
                @GET("/users")
                @Returns(success = UserDto::class)
                @Serializable
                data class ListUsersRequest(
                    @Header @Transient val accept: String = ""
                )
                """.trimIndent(),
            )
        val compilation = getCompilation(validApiSpec, request).compile()
        compilation.exitCode shouldBe COMPILATION_ERROR
        compilation.messages shouldContain "@Header parameter 'accept' must be nullable"
    }

    @Test
    fun `Unsupported header type rejected`() {
        val request =
            kotlin(
                "Requests.kt",
                """
                package dev.kolibrium.api.ksp.test.models
                import dev.kolibrium.api.ksp.annotations.*
                import kotlinx.serialization.Serializable
                import kotlinx.serialization.Transient
                @Serializable
                data class UserDto(val id: Int)
                @GET("/users")
                @Returns(success = UserDto::class)
                @Serializable
                data class ListUsersRequest(
                    @Header @Transient val limit: List<String>? = null
                )
                """.trimIndent(),
            )
        val compilation = getCompilation(validApiSpec, request).compile()
        compilation.exitCode shouldBe COMPILATION_ERROR
        compilation.messages shouldContain "@Header parameter 'limit' must be String, Int, Long, Short, Float, Double, or Boolean"
    }

    @Test
    fun `Invalid HTTP header name rejected`() {
        val request =
            kotlin(
                "Requests.kt",
                """
                package dev.kolibrium.api.ksp.test.models
                import dev.kolibrium.api.ksp.annotations.*
                import kotlinx.serialization.Serializable
                import kotlinx.serialization.Transient
                @Serializable
                data class UserDto(val id: Int)
                @GET("/users")
                @Returns(success = UserDto::class)
                @Serializable
                data class ListUsersRequest(
                    @Header(name = "Invalid Header Name") @Transient val myHeader: String? = null
                )
                """.trimIndent(),
            )
        val compilation = getCompilation(validApiSpec, request).compile()
        compilation.exitCode shouldBe COMPILATION_ERROR
        compilation.messages shouldContain "Invalid HTTP header name"
    }

    @Test
    fun `Header combined with other parameter annotations rejected`() {
        val pathRequest =
            kotlin(
                "Requests.kt",
                """
                package dev.kolibrium.api.ksp.test.models
                import dev.kolibrium.api.ksp.annotations.*
                import kotlinx.serialization.Serializable
                import kotlinx.serialization.Transient
                @Serializable
                data class UserDto(val id: Int)
                @GET("/users/{id}")
                @Returns(success = UserDto::class)
                @Serializable
                data class GetUserRequest(
                    @Path @Header @Transient val id: String? = null
                )
                """.trimIndent(),
            )
        val pathCompilation = getCompilation(validApiSpec, pathRequest).compile()
        pathCompilation.exitCode shouldBe COMPILATION_ERROR
        pathCompilation.messages shouldContain "cannot have multiple parameter annotations (@Path, @Query, @Header)"

        val queryRequest =
            kotlin(
                "Requests.kt",
                """
                package dev.kolibrium.api.ksp.test.models
                import dev.kolibrium.api.ksp.annotations.*
                import kotlinx.serialization.Serializable
                import kotlinx.serialization.Transient
                @Serializable
                data class UserDto(val id: Int)
                @GET("/users")
                @Returns(success = UserDto::class)
                @Serializable
                data class ListUsersRequest(
                    @Query @Header @Transient val search: String? = null
                )
                """.trimIndent(),
            )
        val queryCompilation = getCompilation(validApiSpec, queryRequest).compile()
        queryCompilation.exitCode shouldBe COMPILATION_ERROR
        queryCompilation.messages shouldContain "cannot have multiple parameter annotations (@Path, @Query, @Header)"
    }

    @Test
    fun `Headers work on POST requests`() {
        val request =
            kotlin(
                "Requests.kt",
                """
                package dev.kolibrium.api.ksp.test.models
                import dev.kolibrium.api.ksp.annotations.*
                import kotlinx.serialization.Serializable
                import kotlinx.serialization.Transient
                @Serializable
                data class UserDto(val id: Int)
                @POST("/users")
                @Returns(success = UserDto::class)
                @Serializable
                data class CreateUserRequest(
                    @Header(name = "X-Request-Source") @Transient val source: String? = null,
                    var name: String? = null
                )
                """.trimIndent(),
            )
        val kotlinCompilation = getCompilation(validApiSpec, request)
        val compilation = kotlinCompilation.compile()
        compilation.exitCode shouldBe OK
        val generatedSource = kotlinCompilation.getGeneratedSource("TestClient.kt")
        generatedSource shouldContain "source: String? = null"
        generatedSource shouldContain """source?.let { header("X-Request-Source", it) }"""
    }

    @Test
    fun `Multiple headers generate multiple header calls`() {
        val request =
            kotlin(
                "Requests.kt",
                """
                package dev.kolibrium.api.ksp.test.models
                import dev.kolibrium.api.ksp.annotations.*
                import kotlinx.serialization.Serializable
                import kotlinx.serialization.Transient
                @Serializable
                data class UserDto(val id: Int)
                @GET("/users")
                @Returns(success = UserDto::class)
                @Serializable
                data class ListUsersRequest(
                    @Header(name = "X-Correlation-ID") @Transient val correlationId: String? = null,
                    @Header(name = "Accept") @Transient val accept: String? = null
                )
                """.trimIndent(),
            )
        val kotlinCompilation = getCompilation(validApiSpec, request)
        val compilation = kotlinCompilation.compile()
        compilation.exitCode shouldBe OK
        val source = kotlinCompilation.getGeneratedSource("TestClient.kt")
        source shouldContain """correlationId?.let { header("X-Correlation-ID", it) }"""
        source shouldContain """accept?.let { header("Accept", it) }"""
    }

    @Test
    fun `Full generated output for header request is correct`() {
        val request =
            kotlin(
                "Requests.kt",
                """
                package dev.kolibrium.api.ksp.test.models
                import dev.kolibrium.api.ksp.annotations.*
                import kotlinx.serialization.Serializable
                import kotlinx.serialization.Transient
                @Serializable
                data class UserDto(val id: Int)
                @GET("/users")
                @Returns(success = UserDto::class)
                @Serializable
                data class ListUsersRequest(
                    @Header(name = "X-Correlation-ID") @Transient val correlationId: String? = null
                )
                """.trimIndent(),
            )
        val kotlinCompilation = getCompilation(validApiSpec, request)
        val compilation = kotlinCompilation.compile()
        compilation.exitCode shouldBe OK
        assertSourceEquals(
            $$"""
                // Code generated by kolibrium-codegen. Do not edit.
                package dev.kolibrium.api.ksp.test.generated

                import dev.kolibrium.api.core.ApiResponse
                import dev.kolibrium.api.ksp.test.models.UserDto
                import io.ktor.client.HttpClient
                import io.ktor.client.call.body
                import io.ktor.client.request.`get`
                import io.ktor.client.request.`header`
                import io.ktor.http.contentType
                import kotlin.String

                /**
                 * HTTP client for the Test API.
                 */
                public class TestClient(
                  private val client: HttpClient,
                  private val baseUrl: String,
                ) {
                  /**
                   * Performs a GET request to /users.
                   *
                   * @param correlationId header: `X-Correlation-ID`
                   */
                  public suspend fun listUsers(correlationId: String? = null): ApiResponse<UserDto> {
                    val httpResponse = client.get("$baseUrl/users") {
                      correlationId?.let { header("X-Correlation-ID", it) }
                    }

                    return ApiResponse(
                      status = httpResponse.status,
                      headers = httpResponse.headers,
                      contentType = httpResponse.contentType(),
                      body = httpResponse.body(),
                    )
                  }
                }
                """,
            "TestClient.kt",
            kotlinCompilation,
        )
    }

    @Test
    fun `JVM Transient on header rejected with helpful message`() {
        val request =
            kotlin(
                "Requests.kt",
                """
                package dev.kolibrium.api.ksp.test.models
                import dev.kolibrium.api.ksp.annotations.*
                import kotlinx.serialization.Serializable
                @Serializable
                data class UserDto(val id: Int)
                @GET("/users")
                @Returns(success = UserDto::class)
                @Serializable
                data class ListUsersRequest(
                    @Header @kotlin.jvm.Transient val accept: String? = null
                )
                """.trimIndent(),
            )
        val compilation = getCompilation(validApiSpec, request).compile()
        compilation.exitCode shouldBe COMPILATION_ERROR
        compilation.messages shouldContain "uses @kotlin.jvm.Transient, which does not affect kotlinx.serialization"
    }

    @Test
    fun `Duplicate header names rejected`() {
        val identicalRequest =
            kotlin(
                "Requests.kt",
                """
                package dev.kolibrium.api.ksp.test.models
                import dev.kolibrium.api.ksp.annotations.*
                import kotlinx.serialization.Serializable
                import kotlinx.serialization.Transient
                @Serializable
                data class UserDto(val id: Int)
                @GET("/users")
                @Returns(success = UserDto::class)
                @Serializable
                data class ListUsersRequest(
                    @Header(name = "X-Foo") @Transient val foo: String? = null,
                    @Header(name = "X-Foo") @Transient val bar: String? = null,
                )
                """.trimIndent(),
            )
        val identicalCompilation = getCompilation(validApiSpec, identicalRequest).compile()
        identicalCompilation.exitCode shouldBe COMPILATION_ERROR
        identicalCompilation.messages shouldContain "Duplicate HTTP header name 'X-Foo'"

        val caseInsensitiveRequest =
            kotlin(
                "Requests.kt",
                """
                package dev.kolibrium.api.ksp.test.models
                import dev.kolibrium.api.ksp.annotations.*
                import kotlinx.serialization.Serializable
                import kotlinx.serialization.Transient
                @Serializable
                data class UserDto(val id: Int)
                @GET("/users")
                @Returns(success = UserDto::class)
                @Serializable
                data class ListUsersRequest(
                    @Header(name = "x-foo") @Transient val first: String? = null,
                    @Header(name = "X-Foo") @Transient val second: String? = null,
                )
                """.trimIndent(),
            )
        val caseCompilation = getCompilation(validApiSpec, caseInsensitiveRequest).compile()
        caseCompilation.exitCode shouldBe COMPILATION_ERROR
        caseCompilation.messages shouldContain "Duplicate HTTP header name 'X-Foo'"
        caseCompilation.messages shouldContain "resolve to the same header (case-insensitive)"

        val defaultCollisionRequest =
            kotlin(
                "Requests.kt",
                """
                package dev.kolibrium.api.ksp.test.models
                import dev.kolibrium.api.ksp.annotations.*
                import kotlinx.serialization.Serializable
                import kotlinx.serialization.Transient
                @Serializable
                data class UserDto(val id: Int)
                @GET("/users")
                @Returns(success = UserDto::class)
                @Serializable
                data class ListUsersRequest(
                    @Header @Transient val accept: String? = null,
                    @Header(name = "accept") @Transient val myAccept: String? = null,
                )
                """.trimIndent(),
            )
        val defaultCompilation = getCompilation(validApiSpec, defaultCollisionRequest).compile()
        defaultCompilation.exitCode shouldBe COMPILATION_ERROR
        defaultCompilation.messages shouldContain "Duplicate HTTP header name 'accept'"
    }

    @Test
    fun `Reserved header names emit warnings`() {
        val request =
            kotlin(
                "Requests.kt",
                """
                package dev.kolibrium.api.ksp.test.models
                import dev.kolibrium.api.ksp.annotations.*
                import kotlinx.serialization.Serializable
                import kotlinx.serialization.Transient
                @Serializable
                data class UserDto(val id: Int)
                @GET("/users")
                @Returns(success = UserDto::class)
                @Serializable
                data class ListUsersRequest(
                    @Header(name = "Host") @Transient val host: String? = null,
                    @Header(name = "Content-Length") @Transient val contentLength: String? = null,
                    @Header(name = "Transfer-Encoding") @Transient val transferEncoding: String? = null,
                )
                """.trimIndent(),
            )
        val compilation = getCompilation(validApiSpec, request).compile()
        compilation.exitCode shouldBe OK
        compilation.messages shouldContain "uses reserved HTTP header name 'Host'"
        compilation.messages shouldContain "uses reserved HTTP header name 'Content-Length'"
        compilation.messages shouldContain "uses reserved HTTP header name 'Transfer-Encoding'"
    }

    @Test
    fun `Normal custom header does not emit reserved warning`() {
        val request =
            kotlin(
                "Requests.kt",
                """
                package dev.kolibrium.api.ksp.test.models
                import dev.kolibrium.api.ksp.annotations.*
                import kotlinx.serialization.Serializable
                import kotlinx.serialization.Transient
                @Serializable
                data class UserDto(val id: Int)
                @GET("/users")
                @Returns(success = UserDto::class)
                @Serializable
                data class ListUsersRequest(
                    @Header(name = "X-Custom-Header") @Transient val custom: String? = null
                )
                """.trimIndent(),
            )
        val compilation = getCompilation(validApiSpec, request).compile()
        compilation.exitCode shouldBe OK
        compilation.messages shouldNotContain "reserved HTTP header name"
    }

    @Test
    fun `Non-String header types generate toString conversion and String does not`() {
        val intRequest =
            kotlin(
                "Requests.kt",
                """
                package dev.kolibrium.api.ksp.test.models
                import dev.kolibrium.api.ksp.annotations.*
                import kotlinx.serialization.Serializable
                import kotlinx.serialization.Transient
                @Serializable
                data class UserDto(val id: Int)
                @GET("/users")
                @Returns(success = UserDto::class)
                @Serializable
                data class ListUsersRequest(
                    @Header(name = "X-Limit") @Transient val limit: Int? = null
                )
                """.trimIndent(),
            )
        val intKotlinCompilation = getCompilation(validApiSpec, intRequest)
        val intCompilation = intKotlinCompilation.compile()
        intCompilation.exitCode shouldBe OK
        val intSource = intKotlinCompilation.getGeneratedSource("TestClient.kt")
        intSource shouldContain "limit: Int? = null"
        intSource shouldContain """limit?.let { header("X-Limit", it.toString()) }"""

        val booleanRequest =
            kotlin(
                "Requests.kt",
                """
                package dev.kolibrium.api.ksp.test.models
                import dev.kolibrium.api.ksp.annotations.*
                import kotlinx.serialization.Serializable
                import kotlinx.serialization.Transient
                @Serializable
                data class UserDto(val id: Int)
                @GET("/users")
                @Returns(success = UserDto::class)
                @Serializable
                data class ListUsersRequest(
                    @Header(name = "X-Debug") @Transient val debug: Boolean? = null
                )
                """.trimIndent(),
            )
        val booleanKotlinCompilation = getCompilation(validApiSpec, booleanRequest)
        val booleanCompilation = booleanKotlinCompilation.compile()
        booleanCompilation.exitCode shouldBe OK
        val booleanSource = booleanKotlinCompilation.getGeneratedSource("TestClient.kt")
        booleanSource shouldContain "debug: Boolean? = null"
        booleanSource shouldContain """debug?.let { header("X-Debug", it.toString()) }"""

        val stringRequest =
            kotlin(
                "Requests.kt",
                """
                package dev.kolibrium.api.ksp.test.models
                import dev.kolibrium.api.ksp.annotations.*
                import kotlinx.serialization.Serializable
                import kotlinx.serialization.Transient
                @Serializable
                data class UserDto(val id: Int)
                @GET("/users")
                @Returns(success = UserDto::class)
                @Serializable
                data class ListUsersRequest(
                    @Header(name = "X-Trace") @Transient val trace: String? = null
                )
                """.trimIndent(),
            )
        val stringKotlinCompilation = getCompilation(validApiSpec, stringRequest)
        val stringCompilation = stringKotlinCompilation.compile()
        stringCompilation.exitCode shouldBe OK
        val stringSource = stringKotlinCompilation.getGeneratedSource("TestClient.kt")
        stringSource shouldContain """trace?.let { header("X-Trace", it) }"""
        stringSource shouldNotContain """trace?.let { header("X-Trace", it.toString()) }"""
    }

    @Test
    fun `Header and Query without default value rejected`() {
        val headerRequest =
            kotlin(
                "Requests.kt",
                """
                package dev.kolibrium.api.ksp.test.models
                import dev.kolibrium.api.ksp.annotations.*
                import kotlinx.serialization.Serializable
                import kotlinx.serialization.Transient
                @Serializable
                data class UserDto(val id: Int)
                @GET("/users")
                @Returns(success = UserDto::class)
                @Serializable
                data class ListUsersRequest(
                    @Header @Transient val accept: String?
                )
                """.trimIndent(),
            )
        val headerCompilation = getCompilation(validApiSpec, headerRequest).compile()
        headerCompilation.exitCode shouldBe COMPILATION_ERROR
        headerCompilation.messages shouldContain "@Header parameter 'accept' must have a default value (e.g., = null)"

        val queryRequest =
            kotlin(
                "Requests.kt",
                """
                package dev.kolibrium.api.ksp.test.models
                import dev.kolibrium.api.ksp.annotations.*
                import kotlinx.serialization.Serializable
                import kotlinx.serialization.Transient
                @Serializable
                data class UserDto(val id: Int)
                @GET("/users")
                @Returns(success = UserDto::class)
                @Serializable
                data class ListUsersRequest(
                    @Query @Transient val search: String?
                )
                """.trimIndent(),
            )
        val queryCompilation = getCompilation(validApiSpec, queryRequest).compile()
        queryCompilation.exitCode shouldBe COMPILATION_ERROR
        queryCompilation.messages shouldContain "@Query parameter 'search' must have a default value (e.g., = null)"
    }

    @Test
    fun `Path, Header, and body together on POST`() {
        val request =
            kotlin(
                "Requests.kt",
                """
                package dev.kolibrium.api.ksp.test.models
                import dev.kolibrium.api.ksp.annotations.*
                import kotlinx.serialization.Serializable
                import kotlinx.serialization.Transient
                @Serializable
                data class ItemDto(val id: Int)
                @POST("/users/{userId}/items")
                @Returns(success = ItemDto::class)
                @Serializable
                data class CreateUserItemRequest(
                    @Path @Transient val userId: Int = 0,
                    @Header(name = "X-Request-ID") @Transient val requestId: String? = null,
                    @Header(name = "X-Priority") @Transient val priority: Int? = null,
                    var name: String? = null,
                    var quantity: Int? = null,
                )
                """.trimIndent(),
            )
        val kotlinCompilation = getCompilation(validApiSpec, request)
        val compilation = kotlinCompilation.compile()
        compilation.exitCode shouldBe OK
        assertSourceEquals(
            $$"""
                // Code generated by kolibrium-codegen. Do not edit.
                package dev.kolibrium.api.ksp.test.generated

                import dev.kolibrium.api.core.ApiResponse
                import dev.kolibrium.api.ksp.test.models.CreateUserItemRequest
                import dev.kolibrium.api.ksp.test.models.ItemDto
                import io.ktor.client.HttpClient
                import io.ktor.client.call.body
                import io.ktor.client.request.`header`
                import io.ktor.client.request.post
                import io.ktor.client.request.setBody
                import io.ktor.http.ContentType
                import io.ktor.http.contentType
                import io.ktor.http.encodeURLPathPart
                import kotlin.Int
                import kotlin.String
                import kotlin.Unit

                /**
                 * HTTP client for the Test API.
                 */
                public class TestClient(
                  private val client: HttpClient,
                  private val baseUrl: String,
                ) {
                  /**
                   * Performs a POST request to /users/{userId}/items.
                   *
                   * @param userId path parameter — substituted into `/users/{userId}/items`
                   * @param requestId header: `X-Request-ID`
                   * @param priority header: `X-Priority`
                   * @param block request body builder
                   */
                  public suspend fun createUserItem(
                    userId: Int,
                    requestId: String? = null,
                    priority: Int? = null,
                    block: CreateUserItemRequest.() -> Unit,
                  ): ApiResponse<ItemDto> {
                    val request = CreateUserItemRequest(userId = userId).apply(block)

                    val httpResponse = client.post("$baseUrl/users/${userId.toString().encodeURLPathPart()}/items") {
                      contentType(ContentType.Application.Json)
                      setBody(request)
                      requestId?.let { header("X-Request-ID", it) }
                      priority?.let { header("X-Priority", it.toString()) }
                    }

                    return ApiResponse(
                      status = httpResponse.status,
                      headers = httpResponse.headers,
                      contentType = httpResponse.contentType(),
                      body = httpResponse.body(),
                    )
                  }
                }
                """,
            "TestClient.kt",
            kotlinCompilation,
        )
    }

    @Test
    fun `Path, Query, and Header together on GET`() {
        val request =
            kotlin(
                "Requests.kt",
                """
                package dev.kolibrium.api.ksp.test.models
                import dev.kolibrium.api.ksp.annotations.*
                import kotlinx.serialization.Serializable
                import kotlinx.serialization.Transient
                @Serializable
                data class ItemDto(val id: Int)
                @GET("/users/{userId}/items")
                @Returns(success = ItemDto::class)
                @Serializable
                data class GetUserItemsRequest(
                    @Path @Transient val userId: Int = 0,
                    @Query @Transient val status: String? = null,
                    @Query @Transient val limit: Int? = null,
                    @Header(name = "X-Correlation-ID") @Transient val correlationId: String? = null,
                    @Header(name = "X-Page-Size") @Transient val pageSize: Int? = null,
                )
                """.trimIndent(),
            )
        val kotlinCompilation = getCompilation(validApiSpec, request)
        val compilation = kotlinCompilation.compile()
        compilation.exitCode shouldBe OK
        assertSourceEquals(
            $$"""
                // Code generated by kolibrium-codegen. Do not edit.
                package dev.kolibrium.api.ksp.test.generated

                import dev.kolibrium.api.core.ApiResponse
                import dev.kolibrium.api.ksp.test.models.ItemDto
                import io.ktor.client.HttpClient
                import io.ktor.client.call.body
                import io.ktor.client.request.`get`
                import io.ktor.client.request.`header`
                import io.ktor.client.request.parameter
                import io.ktor.http.contentType
                import io.ktor.http.encodeURLPathPart
                import kotlin.Int
                import kotlin.String

                /**
                 * HTTP client for the Test API.
                 */
                public class TestClient(
                  private val client: HttpClient,
                  private val baseUrl: String,
                ) {
                  /**
                   * Performs a GET request to /users/{userId}/items.
                   *
                   * @param userId path parameter — substituted into `/users/{userId}/items`
                   * @param status query parameter
                   * @param limit query parameter
                   * @param correlationId header: `X-Correlation-ID`
                   * @param pageSize header: `X-Page-Size`
                   */
                  public suspend fun getUserItems(
                    userId: Int,
                    status: String? = null,
                    limit: Int? = null,
                    correlationId: String? = null,
                    pageSize: Int? = null,
                  ): ApiResponse<ItemDto> {
                    val httpResponse = client.get("$baseUrl/users/${userId.toString().encodeURLPathPart()}/items") {
                      status?.let { parameter("status", it) }
                      limit?.let { parameter("limit", it) }
                      correlationId?.let { header("X-Correlation-ID", it) }
                      pageSize?.let { header("X-Page-Size", it.toString()) }
                    }

                    return ApiResponse(
                      status = httpResponse.status,
                      headers = httpResponse.headers,
                      contentType = httpResponse.contentType(),
                      body = httpResponse.body(),
                    )
                  }
                }
                """,
            "TestClient.kt",
            kotlinCompilation,
        )
    }
}

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

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.KotlinCompilation.ExitCode.OK
import com.tschuchort.compiletesting.SourceFile.Companion.kotlin
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Test

class ClientMethodKDocTest : ApiBaseTest() {
    @Test
    fun `Simple GET generates function-level KDoc with HTTP method and path`() {
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
                object GetUsersRequest
                """.trimIndent(),
            )
        val kotlinCompilation = getCompilation(validApiSpec, request)
        val compilation = kotlinCompilation.compile()
        compilation.exitCode shouldBe OK
        val source = kotlinCompilation.getGeneratedSource("TestClient.kt")
        source shouldContain "/**\n   * Performs a GET request to /users.\n   */"
    }

    @Test
    fun `SingleClient class gets KDoc with API name`() {
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
                object GetUsersRequest
                """.trimIndent(),
            )
        val kotlinCompilation = getCompilation(validApiSpec, request)
        val compilation = kotlinCompilation.compile()
        compilation.exitCode shouldBe OK
        val source = kotlinCompilation.getGeneratedSource("TestClient.kt")
        source shouldContain "HTTP client for the Test API."
    }

    @Test
    fun `ByPrefix group clients and root aggregator get KDoc`() {
        val apiSpec =
            kotlin(
                "GroupedApiSpec.kt",
                """
                package dev.kolibrium.api.ksp.test
                import dev.kolibrium.api.core.ApiSpec
                import dev.kolibrium.api.ksp.annotations.ClientGrouping
                import dev.kolibrium.api.ksp.annotations.GenerateApi
                @GenerateApi(grouping = ClientGrouping.ByPrefix)
                object GroupedApiSpec : ApiSpec() {
                    override val baseUrl = "https://test.api"
                }
                """.trimIndent(),
            )
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
                object GetUsersRequest
                @Serializable
                data class OrderDto(val id: Int)
                @GET("/orders")
                @Returns(success = OrderDto::class)
                @Serializable
                object GetOrdersRequest
                """.trimIndent(),
            )
        val kotlinCompilation = getCompilation(apiSpec, request)
        val compilation = kotlinCompilation.compile()
        compilation.exitCode shouldBe OK
        val usersSource = kotlinCompilation.getGeneratedSource("UsersClient.kt")
        usersSource shouldContain "HTTP client for the Grouped API."
        val ordersSource = kotlinCompilation.getGeneratedSource("OrdersClient.kt")
        ordersSource shouldContain "HTTP client for the Grouped API."
        val rootSource = kotlinCompilation.getGeneratedSource("GroupedClient.kt")
        rootSource shouldContain "Aggregator client for the Grouped API, grouping endpoints by resource."
    }

    @Test
    fun `Path parameters get param KDoc with path template`() {
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
                @GET("/users/{userId}")
                @Returns(success = UserDto::class)
                @Serializable
                data class GetUserRequest(@Path @Transient val userId: Int = 0)
                """.trimIndent(),
            )
        val kotlinCompilation = getCompilation(validApiSpec, request)
        val compilation = kotlinCompilation.compile()
        compilation.exitCode shouldBe OK
        val source = kotlinCompilation.getGeneratedSource("TestClient.kt")
        source shouldContain "@param userId path parameter — substituted into `/users/{userId}`"
    }

    @Test
    fun `Query parameters get param KDoc`() {
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
                data class SearchUsersRequest(@Query @Transient val search: String? = null)
                """.trimIndent(),
            )
        val kotlinCompilation = getCompilation(validApiSpec, request)
        val compilation = kotlinCompilation.compile()
        compilation.exitCode shouldBe OK
        val source = kotlinCompilation.getGeneratedSource("TestClient.kt")
        source shouldContain "@param search query parameter"
    }

    @Test
    fun `All parameter types combined produce correct param tags`() {
        val getRequest =
            kotlin(
                "GetRequests.kt",
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
                )
                """.trimIndent(),
            )
        val postRequest =
            kotlin(
                "PostRequests.kt",
                """
                package dev.kolibrium.api.ksp.test.models
                import dev.kolibrium.api.ksp.annotations.*
                import kotlinx.serialization.Serializable
                import kotlinx.serialization.Transient
                @POST("/users/{userId}/items")
                @Returns(success = ItemDto::class)
                @Serializable
                data class CreateUserItemRequest(
                    @Path @Transient val userId: Int = 0,
                    var name: String? = null,
                )
                """.trimIndent(),
            )
        val kotlinCompilation = getCompilation(validApiSpec, getRequest, postRequest)
        val compilation = kotlinCompilation.compile()
        compilation.exitCode shouldBe OK
        val source = kotlinCompilation.getGeneratedSource("TestClient.kt")
        // GET method KDoc
        source shouldContain "Performs a GET request to /users/{userId}/items."
        source shouldContain "@param userId path parameter — substituted into `/users/{userId}/items`"
        source shouldContain "@param status query parameter"
        // POST method KDoc
        source shouldContain "Performs a POST request to /users/{userId}/items."
        source shouldContain "@param block request body builder"
    }

    @Test
    fun `API spec without ApiSpec suffix produces sensible KDoc`() {
        val apiSpec =
            kotlin(
                "MyApi.kt",
                """
                package dev.kolibrium.api.ksp.test
                import dev.kolibrium.api.core.ApiSpec
                import dev.kolibrium.api.ksp.annotations.GenerateApi
                @GenerateApi
                object MyApi : ApiSpec() {
                    override val baseUrl = "https://test.api"
                }
                """.trimIndent(),
            )
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
                object GetUsersRequest
                """.trimIndent(),
            )
        val kotlinCompilation = getCompilation(apiSpec, request)
        val compilation = kotlinCompilation.compile()
        compilation.exitCode shouldBe OK
        val source = kotlinCompilation.getGeneratedSource("MyClient.kt")
        source shouldContain "HTTP client for the My API."
    }

    @Test
    fun `API spec named exactly ApiSpec generates KDoc with ApiSpec display name`() {
        val apiSpec =
            kotlin(
                "ApiSpec.kt",
                """
                package dev.kolibrium.api.ksp.test
                import dev.kolibrium.api.ksp.annotations.GenerateApi
                @GenerateApi
                object ApiSpec : dev.kolibrium.api.core.ApiSpec() {
                    override val baseUrl = "https://test.api"
                }
                """.trimIndent(),
            )
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
                object GetUsersRequest
                """.trimIndent(),
            )
        val kotlinCompilation = getCompilation(apiSpec, request)
        val compilation = kotlinCompilation.compile()
        compilation.exitCode shouldBe OK
        val source = kotlinCompilation.getGeneratedSource("ApiSpecClient.kt")
        source shouldContain "HTTP client for the ApiSpec API."
    }

    @Test
    fun `Explicit displayName overrides derived display name in KDoc but not class name`() {
        val apiSpec =
            kotlin(
                "PetStoreApiSpec.kt",
                """
                package dev.kolibrium.api.ksp.test
                import dev.kolibrium.api.core.ApiSpec
                import dev.kolibrium.api.ksp.annotations.GenerateApi
                @GenerateApi(displayName = "My Awesome Store")
                object PetStoreApiSpec : ApiSpec() {
                    override val baseUrl = "https://test.api"
                }
                """.trimIndent(),
            )
        val request =
            kotlin(
                "Requests.kt",
                """
                package dev.kolibrium.api.ksp.test.models
                import dev.kolibrium.api.ksp.annotations.*
                import kotlinx.serialization.Serializable
                @Serializable
                data class PetDto(val id: Int)
                @GET("/pets")
                @Returns(success = PetDto::class)
                @Serializable
                object GetPetsRequest
                """.trimIndent(),
            )
        val kotlinCompilation = getCompilation(apiSpec, request)
        val compilation = kotlinCompilation.compile()
        compilation.exitCode shouldBe OK
        val source = kotlinCompilation.getGeneratedSource("PetStoreClient.kt")
        source shouldContain "HTTP client for the My Awesome Store API."
    }

    @Test
    fun `displayName with special characters works for KDoc but does not affect class name`() {
        val apiSpec =
            kotlin(
                "PetStoreApiSpec.kt",
                """
                package dev.kolibrium.api.ksp.test
                import dev.kolibrium.api.core.ApiSpec
                import dev.kolibrium.api.ksp.annotations.GenerateApi
                @GenerateApi(displayName = "My API! (v2)")
                object PetStoreApiSpec : ApiSpec() {
                    override val baseUrl = "https://test.api"
                }
                """.trimIndent(),
            )
        val request =
            kotlin(
                "Requests.kt",
                """
                package dev.kolibrium.api.ksp.test.models
                import dev.kolibrium.api.ksp.annotations.*
                import kotlinx.serialization.Serializable
                @Serializable
                data class PetDto(val id: Int)
                @GET("/pets")
                @Returns(success = PetDto::class)
                @Serializable
                object GetPetsRequest
                """.trimIndent(),
            )
        val kotlinCompilation = getCompilation(apiSpec, request)
        val compilation = kotlinCompilation.compile()
        compilation.exitCode shouldBe OK
        val source = kotlinCompilation.getGeneratedSource("PetStoreClient.kt")
        source shouldContain "HTTP client for the My API! (v2) API."
    }

    @Test
    fun `Request class named exactly Request produces empty function name and fails compilation`() {
        val request =
            kotlin(
                "Requests.kt",
                """
                package dev.kolibrium.api.ksp.test.models
                import dev.kolibrium.api.ksp.annotations.*
                import kotlinx.serialization.Serializable
                @Serializable
                data class SuccessDto(val id: Int)
                @Serializable
                data class ErrorDto(val message: String)
                @POST("/action")
                @Returns(success = SuccessDto::class, error = ErrorDto::class)
                @Serializable
                class Request
                """.trimIndent(),
            )
        val kotlinCompilation = getCompilation(validApiSpec, request)
        val compilation = kotlinCompilation.compile()
        compilation.exitCode shouldBe KotlinCompilation.ExitCode.COMPILATION_ERROR
    }

    @Test
    fun `Multiple path parameters each get param KDoc with full path`() {
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
                @GET("/users/{userId}/items/{itemId}")
                @Returns(success = ItemDto::class)
                @Serializable
                data class GetUserItemRequest(
                    @Path @Transient val userId: Int = 0,
                    @Path @Transient val itemId: Int = 0,
                )
                """.trimIndent(),
            )
        val kotlinCompilation = getCompilation(validApiSpec, request)
        val compilation = kotlinCompilation.compile()
        compilation.exitCode shouldBe OK
        val source = kotlinCompilation.getGeneratedSource("TestClient.kt")
        source shouldContain "@param userId path parameter — substituted into `/users/{userId}/items/{itemId}`"
        source shouldContain "@param itemId path parameter — substituted into `/users/{userId}/items/{itemId}`"
    }
}

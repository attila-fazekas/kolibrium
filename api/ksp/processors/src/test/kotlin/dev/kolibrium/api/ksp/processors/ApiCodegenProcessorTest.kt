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
import com.tschuchort.compiletesting.kspSourcesDir
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ApiCodegenProcessorTest : ApiBaseTest() {
    private val validApiSpec =
        kotlin(
            "TestApiSpec.kt",
            """
            package dev.kolibrium.api.ksp.test

            import dev.kolibrium.api.core.ApiSpec

            object TestApiSpec : ApiSpec() {
                override val baseUrl = "https://test.api"
            }
            """.trimIndent(),
        )

    @Nested
    inner class `1 — No-Op Behaviour` {
        @Test
        fun `1_1 — No ApiSpec in source`() {
            val source =
                kotlin(
                    "PlainClass.kt",
                    """
                    package dev.kolibrium.api.ksp.test
                    class PlainClass
                    """.trimIndent(),
                )
            val kotlinCompilation = getCompilation(source)
            val compilation = kotlinCompilation.compile()
            compilation.exitCode shouldBe OK
            kotlinCompilation.kspSourcesDir
                .walkTopDown()
                .filter { it.isFile }
                .toList() shouldBe emptyList()
        }

        @Test
        fun `1_2 — ApiSpec present but no request classes in scan package`() {
            val kotlinCompilation = getCompilation(validApiSpec)
            val compilation = kotlinCompilation.compile()
            compilation.exitCode shouldBe OK
            compilation.messages shouldContain "No request classes found"
            shouldThrow<IllegalArgumentException> {
                kotlinCompilation.getGeneratedSource("TestClient.kt")
            }
            shouldThrow<IllegalArgumentException> {
                kotlinCompilation.getGeneratedSource("TestTestHarness.kt")
            }
        }
    }

    @Nested
    inner class `2 — ApiSpec Subclass Validation` {
        @Test
        fun `2_1 — Abstract class rejected`() {
            val source =
                kotlin(
                    "PetApiSpec.kt",
                    """
                    package dev.kolibrium.api.ksp.test
                    import dev.kolibrium.api.core.ApiSpec
                    abstract class PetApiSpec : ApiSpec()
                    """.trimIndent(),
                )
            val compilation = getCompilation(source).compile()
            compilation.exitCode shouldBe COMPILATION_ERROR
            compilation.messages shouldContain "must be an object declaration or a concrete class"
        }

        @Test
        fun `2_2 — Named exactly ApiSpec rejected`() {
            val source =
                kotlin(
                    "ApiSpec.kt",
                    """
                    package dev.kolibrium.api.ksp.test
                    import dev.kolibrium.api.core.ApiSpec
                    object ApiSpec : ApiSpec()
                    """.trimIndent(),
                )
            val compilation = getCompilation(source).compile()
            compilation.exitCode shouldBe COMPILATION_ERROR
            compilation.messages shouldContain "must not be named exactly 'ApiSpec'"
        }

        @Test
        fun `2_3 — Invalid package name rejected`() {
            val source =
                kotlin(
                    "PetApiSpec.kt",
                    """
                    package com.`123invalid`
                    import dev.kolibrium.api.core.ApiSpec
                    object PetApiSpec : ApiSpec() {
                        override val baseUrl = "https://test.api"
                    }
                    """.trimIndent(),
                )
            val compilation = getCompilation(source).compile()
            compilation.exitCode shouldBe COMPILATION_ERROR
            compilation.messages shouldContain "invalid package name"
        }

        @Test
        fun `2_4 — Duplicate apiName in same package rejected`() {
            val source1 =
                kotlin(
                    "PetApiSpec.kt",
                    """
                    package dev.kolibrium.api.ksp.test
                    import dev.kolibrium.api.core.ApiSpec
                    object PetApiSpec : ApiSpec() {
                        override val baseUrl = "https://test.api"
                    }
                    """.trimIndent(),
                )
            val source2 =
                kotlin(
                    "PetApiSpec2.kt",
                    """
                    package dev.kolibrium.api.ksp.test
                    import dev.kolibrium.api.core.ApiSpec
                    object PetApiSpec : ApiSpec() {
                        override val baseUrl = "https://test2.api"
                    }
                    """.trimIndent(),
                )
            val compilation = getCompilation(source1, source2).compile()
            compilation.exitCode shouldBe COMPILATION_ERROR
            compilation.messages shouldContain "Duplicate API name"
        }

        @Test
        fun `2_5 — Same apiName in different packages is allowed`() {
            val spec1 =
                kotlin(
                    "TestApiSpec1.kt",
                    """
                    package dev.kolibrium.api.ksp.test.one
                    import dev.kolibrium.api.core.ApiSpec
                    object TestApiSpec : ApiSpec() {
                        override val baseUrl = "https://test1.api"
                    }
                    """.trimIndent(),
                )
            val spec2 =
                kotlin(
                    "TestApiSpec2.kt",
                    """
                    package dev.kolibrium.api.ksp.test.two
                    import dev.kolibrium.api.core.ApiSpec
                    object TestApiSpec : ApiSpec() {
                        override val baseUrl = "https://test2.api"
                    }
                    """.trimIndent(),
                )
            val request1 =
                kotlin(
                    "Request1.kt",
                    """
                    package dev.kolibrium.api.ksp.test.one.models
                    import dev.kolibrium.api.ksp.annotations.GET
                    import dev.kolibrium.api.ksp.annotations.Returns
                    import kotlinx.serialization.Serializable
                    @Serializable
                    data class UserDto(val id: Int)
                    @GET("/users")
                    @Returns(success = UserDto::class)
                    @Serializable
                    class GetUsersRequest
                    """.trimIndent(),
                )
            val request2 =
                kotlin(
                    "Request2.kt",
                    """
                    package dev.kolibrium.api.ksp.test.two.models
                    import dev.kolibrium.api.ksp.annotations.GET
                    import dev.kolibrium.api.ksp.annotations.Returns
                    import kotlinx.serialization.Serializable
                    @Serializable
                    data class ItemDto(val id: Int)
                    @GET("/items")
                    @Returns(success = ItemDto::class)
                    @Serializable
                    class GetItemsRequest
                    """.trimIndent(),
                )
            val kotlinCompilation = getCompilation(spec1, spec2, request1, request2)
            val compilation = kotlinCompilation.compile()
            compilation.exitCode shouldBe OK
            kotlinCompilation.getGeneratedSource("TestClient.kt")
            kotlinCompilation.getGeneratedSource("TestTestHarness.kt")
        }
    }

    @Nested
    inner class `3 — Request Class Discovery` {
        @Test
        fun `3_1 — Convention scan package`() {
            val request =
                kotlin(
                    "GetUsersRequest.kt",
                    """
                    package dev.kolibrium.api.ksp.test.models
                    import dev.kolibrium.api.ksp.annotations.GET
                    import dev.kolibrium.api.ksp.annotations.Returns
                    import kotlinx.serialization.Serializable
                    @Serializable
                    data class UserDto(val id: Int)
                    @GET("/users")
                    @Returns(success = UserDto::class)
                    @Serializable
                    class GetUsersRequest
                    """.trimIndent(),
                )
            val kotlinCompilation = getCompilation(validApiSpec, request)
            val compilation = kotlinCompilation.compile()
            compilation.exitCode shouldBe OK
            val source = kotlinCompilation.getGeneratedSource("TestClient.kt")
            source shouldContain "fun getUsers()"
        }

        @Test
        fun `3_2 — Subpackage of scan package is included`() {
            val request =
                kotlin(
                    "GetUsersRequest.kt",
                    """
                    package dev.kolibrium.api.ksp.test.models.users
                    import dev.kolibrium.api.ksp.annotations.GET
                    import dev.kolibrium.api.ksp.annotations.Returns
                    import kotlinx.serialization.Serializable
                    @Serializable
                    data class UserDto(val id: Int)
                    @GET("/users")
                    @Returns(success = UserDto::class)
                    @Serializable
                    class GetUsersRequest
                    """.trimIndent(),
                )
            val kotlinCompilation = getCompilation(validApiSpec, request)
            val compilation = kotlinCompilation.compile()
            compilation.exitCode shouldBe OK
            val source = kotlinCompilation.getGeneratedSource("TestClient.kt")
            source shouldContain "fun getUsers()"
        }

        @Test
        fun `3_3 — Class outside scan package is ignored`() {
            val request =
                kotlin(
                    "GetUsersRequest.kt",
                    """
                    package dev.kolibrium.api.ksp.other
                    import dev.kolibrium.api.ksp.annotations.GET
                    import dev.kolibrium.api.ksp.annotations.Returns
                    import kotlinx.serialization.Serializable
                    @Serializable
                    data class UserDto(val id: Int)
                    @GET("/users")
                    @Returns(success = UserDto::class)
                    @Serializable
                    class GetUsersRequest
                    """.trimIndent(),
                )
            val kotlinCompilation = getCompilation(validApiSpec, request)
            val compilation = kotlinCompilation.compile()
            compilation.exitCode shouldBe OK
            compilation.messages shouldContain "No request classes found"
        }

        @Test
        fun `3_4 — Class without @Serializable is ignored`() {
            val request =
                kotlin(
                    "GetUsersRequest.kt",
                    """
                    package dev.kolibrium.api.ksp.test.models
                    import dev.kolibrium.api.ksp.annotations.GET
                    import dev.kolibrium.api.ksp.annotations.Returns
                    import kotlinx.serialization.Serializable
                    @Serializable
                    data class UserDto(val id: Int)
                    @GET("/users")
                    @Returns(success = UserDto::class)
                    class GetUsersRequest
                    """.trimIndent(),
                )
            val kotlinCompilation = getCompilation(validApiSpec, request)
            val compilation = kotlinCompilation.compile()
            compilation.exitCode shouldBe OK
            compilation.messages shouldContain "No request classes found"
        }

        @Test
        fun `3_5 — Class without HTTP method annotation is ignored`() {
            val request =
                kotlin(
                    "GetUsersRequest.kt",
                    """
                    package dev.kolibrium.api.ksp.test.models
                    import kotlinx.serialization.Serializable
                    @Serializable
                    class GetUsersRequest
                    """.trimIndent(),
                )
            val kotlinCompilation = getCompilation(validApiSpec, request)
            val compilation = kotlinCompilation.compile()
            compilation.exitCode shouldBe OK
            compilation.messages shouldContain "No request classes found"
        }
    }

    @Nested
    inner class `4 — Request Class Structural Validation` {
        @Test
        fun `4_1 — Name not ending in Request rejected`() {
            val request =
                kotlin(
                    "GetUser.kt",
                    """
                    package dev.kolibrium.api.ksp.test.models
                    import dev.kolibrium.api.ksp.annotations.GET
                    import dev.kolibrium.api.ksp.annotations.Returns
                    import kotlinx.serialization.Serializable
                    @Serializable
                    data class UserDto(val id: Int)
                    @GET("/users")
                    @Returns(success = UserDto::class)
                    @Serializable
                    class GetUser
                    """.trimIndent(),
                )
            val compilation = getCompilation(validApiSpec, request).compile()
            compilation.exitCode shouldBe COMPILATION_ERROR
            compilation.messages shouldContain "must end with 'Request' suffix and have a descriptive name"
        }

        @Test
        fun `4_2 — Name exactly Request rejected`() {
            val request =
                kotlin(
                    "Request.kt",
                    """
                    package dev.kolibrium.api.ksp.test.models
                    import dev.kolibrium.api.ksp.annotations.GET
                    import dev.kolibrium.api.ksp.annotations.Returns
                    import kotlinx.serialization.Serializable
                    @Serializable
                    data class UserDto(val id: Int)
                    @GET("/users")
                    @Returns(success = UserDto::class)
                    @Serializable
                    class Request
                    """.trimIndent(),
                )
            val compilation = getCompilation(validApiSpec, request).compile()
            compilation.exitCode shouldBe COMPILATION_ERROR
            compilation.messages shouldContain "must end with 'Request' suffix and have a descriptive name"
        }

        @Test
        fun `4_3 — Non-data class with properties rejected`() {
            val request =
                kotlin(
                    "GetUserRequest.kt",
                    """
                    package dev.kolibrium.api.ksp.test.models
                    import dev.kolibrium.api.ksp.annotations.GET
                    import dev.kolibrium.api.ksp.annotations.Returns
                    import kotlinx.serialization.Serializable
                    @Serializable
                    data class UserDto(val id: Int)
                    @GET("/users")
                    @Returns(success = UserDto::class)
                    @Serializable
                    class GetUserRequest(val page: Int = 0)
                    """.trimIndent(),
                )
            val compilation = getCompilation(validApiSpec, request).compile()
            compilation.exitCode shouldBe COMPILATION_ERROR
            compilation.messages shouldContain "must be a data class"
        }

        @Test
        fun `4_4 — Non-data class with no properties accepted (marker class)`() {
            val request =
                kotlin(
                    "DeleteSessionRequest.kt",
                    """
                    package dev.kolibrium.api.ksp.test.models
                    import dev.kolibrium.api.ksp.annotations.DELETE
                    import dev.kolibrium.api.ksp.annotations.Returns
                    import kotlinx.serialization.Serializable
                    @DELETE("/sessions")
                    @Returns(success = Unit::class)
                    @Serializable
                    class DeleteSessionRequest
                    """.trimIndent(),
                )
            val kotlinCompilation = getCompilation(validApiSpec, request)
            val compilation = kotlinCompilation.compile()
            compilation.exitCode shouldBe OK
            assertSourceEquals(
                """
                // Code generated by kolibrium-codegen. Do not edit.
                package dev.kolibrium.api.ksp.test.generated

                import dev.kolibrium.api.core.ApiResponse
                import dev.kolibrium.api.core.EmptyResponse
                import io.ktor.client.HttpClient
                import io.ktor.client.request.delete
                import io.ktor.http.contentType
                import kotlin.String

                public class TestClient(
                  private val client: HttpClient,
                  private val baseUrl: String,
                ) {
                  public suspend fun deleteSession(): EmptyResponse {
                    val httpResponse = client.delete("${'$'}baseUrl/sessions")

                    return ApiResponse(
                      status = httpResponse.status,
                      headers = httpResponse.headers,
                      contentType = httpResponse.contentType(),
                      body = Unit,
                    )
                  }
                }
                """,
                "TestClient.kt",
                kotlinCompilation,
            )
        }

        @Test
        fun `4_5 — Abstract and sealed request classes rejected`() {
            val abstractRequest =
                kotlin(
                    "AbstractRequest.kt",
                    """
                    package dev.kolibrium.api.ksp.test.models
                    import dev.kolibrium.api.ksp.annotations.GET
                    import dev.kolibrium.api.ksp.annotations.Returns
                    import kotlinx.serialization.Serializable
                    @Serializable
                    data class UserDto(val id: Int)
                    @GET("/users")
                    @Returns(success = UserDto::class)
                    @Serializable
                    abstract class GetUserRequest
                    """.trimIndent(),
                )
            val compilation1 = getCompilation(validApiSpec, abstractRequest).compile()
            compilation1.exitCode shouldBe COMPILATION_ERROR
            compilation1.messages shouldContain "cannot be abstract or sealed"

            val sealedRequest =
                kotlin(
                    "SealedRequest.kt",
                    """
                    package dev.kolibrium.api.ksp.test.models
                    import dev.kolibrium.api.ksp.annotations.GET
                    import dev.kolibrium.api.ksp.annotations.Returns
                    import kotlinx.serialization.Serializable
                    @Serializable
                    data class UserDto(val id: Int)
                    @GET("/users")
                    @Returns(success = UserDto::class)
                    @Serializable
                    sealed class GetUserRequest
                    """.trimIndent(),
                )
            val compilation2 = getCompilation(validApiSpec, sealedRequest).compile()
            compilation2.exitCode shouldBe COMPILATION_ERROR
            compilation2.messages shouldContain "cannot be abstract or sealed"
        }

        @Test
        fun `4_6 — Multiple HTTP method annotations rejected`() {
            val request =
                kotlin(
                    "GetUserRequest.kt",
                    """
                    package dev.kolibrium.api.ksp.test.models
                    import dev.kolibrium.api.ksp.annotations.*
                    import kotlinx.serialization.Serializable
                    @Serializable
                    data class UserDto(val id: Int)
                    @GET("/users")
                    @POST("/users")
                    @Returns(success = UserDto::class)
                    @Serializable
                    class GetUserRequest
                    """.trimIndent(),
                )
            val compilation = getCompilation(validApiSpec, request).compile()
            compilation.exitCode shouldBe COMPILATION_ERROR
            compilation.messages shouldContain "multiple HTTP method annotations"
        }

        @Test
        fun `4_7 — Blank path rejected`() {
            val emptyPath =
                kotlin(
                    "EmptyPath.kt",
                    """
                    package dev.kolibrium.api.ksp.test.models
                    import dev.kolibrium.api.ksp.annotations.GET
                    import dev.kolibrium.api.ksp.annotations.Returns
                    import kotlinx.serialization.Serializable
                    @Serializable
                    data class UserDto(val id: Int)
                    @GET("")
                    @Returns(success = UserDto::class)
                    @Serializable
                    class GetUsersRequest
                    """.trimIndent(),
                )
            val compilation1 = getCompilation(validApiSpec, emptyPath).compile()
            compilation1.exitCode shouldBe COMPILATION_ERROR
            compilation1.messages shouldContain "must specify a path"

            val blankPath =
                kotlin(
                    "BlankPath.kt",
                    """
                    package dev.kolibrium.api.ksp.test.models
                    import dev.kolibrium.api.ksp.annotations.GET
                    import dev.kolibrium.api.ksp.annotations.Returns
                    import kotlinx.serialization.Serializable
                    @Serializable
                    data class UserDto(val id: Int)
                    @GET("   ")
                    @Returns(success = UserDto::class)
                    @Serializable
                    class GetUsersRequest
                    """.trimIndent(),
                )
            val compilation2 = getCompilation(validApiSpec, blankPath).compile()
            compilation2.exitCode shouldBe COMPILATION_ERROR
            compilation2.messages shouldContain "must specify a path"
        }

        @Test
        fun `4_8 — Missing @Returns annotation rejected`() {
            val request =
                kotlin(
                    "GetUsersRequest.kt",
                    """
                    package dev.kolibrium.api.ksp.test.models
                    import dev.kolibrium.api.ksp.annotations.GET
                    import kotlinx.serialization.Serializable
                    @GET("/users")
                    @Serializable
                    class GetUsersRequest
                    """.trimIndent(),
                )
            val compilation = getCompilation(validApiSpec, request).compile()
            compilation.exitCode shouldBe COMPILATION_ERROR
            compilation.messages shouldContain "must have @Returns annotation"
        }
    }

    @Nested
    inner class `5 — Return Type Validation` {
        @Test
        fun `5_1 — Returns Unit generates EmptyResponse`() {
            val request =
                kotlin(
                    "Requests.kt",
                    """
                    package dev.kolibrium.api.ksp.test.models
                    import dev.kolibrium.api.ksp.annotations.DELETE
                    import dev.kolibrium.api.ksp.annotations.Returns
                    import kotlinx.serialization.Serializable
                    @DELETE("/sessions")
                    @Returns(success = Unit::class)
                    @Serializable
                    class DeleteSessionRequest
                    """.trimIndent(),
                )
            val kotlinCompilation = getCompilation(validApiSpec, request)
            val compilation = kotlinCompilation.compile()
            compilation.exitCode shouldBe OK
            assertSourceEquals(
                """
                // Code generated by kolibrium-codegen. Do not edit.
                package dev.kolibrium.api.ksp.test.generated

                import dev.kolibrium.api.core.ApiResponse
                import dev.kolibrium.api.core.EmptyResponse
                import io.ktor.client.HttpClient
                import io.ktor.client.request.delete
                import io.ktor.http.contentType
                import kotlin.String

                public class TestClient(
                  private val client: HttpClient,
                  private val baseUrl: String,
                ) {
                  public suspend fun deleteSession(): EmptyResponse {
                    val httpResponse = client.delete("${'$'}baseUrl/sessions")

                    return ApiResponse(
                      status = httpResponse.status,
                      headers = httpResponse.headers,
                      contentType = httpResponse.contentType(),
                      body = Unit,
                    )
                  }
                }
                """,
                "TestClient.kt",
                kotlinCompilation,
            )
        }

        @Test
        fun `5_2 — Success type not Serializable is rejected`() {
            val request =
                kotlin(
                    "Requests.kt",
                    """
                    package dev.kolibrium.api.ksp.test.models
                    import dev.kolibrium.api.ksp.annotations.GET
                    import dev.kolibrium.api.ksp.annotations.Returns
                    import kotlinx.serialization.Serializable
                    class NotSerializable(val id: Int)
                    @GET("/users")
                    @Returns(success = NotSerializable::class)
                    @Serializable
                    class GetUsersRequest
                    """.trimIndent(),
                )
            val compilation = getCompilation(validApiSpec, request).compile()
            compilation.exitCode shouldBe COMPILATION_ERROR
            compilation.messages shouldContain "is not @Serializable"
        }

        @Test
        fun `5_3 — Returns error Nothing is treated as no error type`() {
            val request =
                kotlin(
                    "Requests.kt",
                    """
                    package dev.kolibrium.api.ksp.test.models
                    import dev.kolibrium.api.ksp.annotations.GET
                    import dev.kolibrium.api.ksp.annotations.Returns
                    import kotlinx.serialization.Serializable
                    @Serializable
                    data class UserDto(val id: Int)
                    @GET("/users")
                    @Returns(success = UserDto::class, error = Nothing::class)
                    @Serializable
                    class GetUsersRequest
                    """.trimIndent(),
                )
            val kotlinCompilation = getCompilation(validApiSpec, request)
            val compilation = kotlinCompilation.compile()
            compilation.exitCode shouldBe OK
            assertSourceEquals(
                """
                // Code generated by kolibrium-codegen. Do not edit.
                package dev.kolibrium.api.ksp.test.generated

                import dev.kolibrium.api.core.ApiResponse
                import dev.kolibrium.api.ksp.test.models.UserDto
                import io.ktor.client.HttpClient
                import io.ktor.client.call.body
                import io.ktor.client.request.`get`
                import io.ktor.http.contentType
                import kotlin.String

                public class TestClient(
                  private val client: HttpClient,
                  private val baseUrl: String,
                ) {
                  public suspend fun getUsers(): ApiResponse<UserDto> {
                    val httpResponse = client.get("${'$'}baseUrl/users")

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
        fun `5_4 — Error type not Serializable is rejected`() {
            val request =
                kotlin(
                    "Requests.kt",
                    """
                    package dev.kolibrium.api.ksp.test.models
                    import dev.kolibrium.api.ksp.annotations.GET
                    import dev.kolibrium.api.ksp.annotations.Returns
                    import kotlinx.serialization.Serializable
                    @Serializable
                    data class SuccessDto(val id: Int)
                    class NotSerializable(val msg: String)
                    @GET("/users")
                    @Returns(success = SuccessDto::class, error = NotSerializable::class)
                    @Serializable
                    class GetUsersRequest
                    """.trimIndent(),
                )
            val compilation = getCompilation(validApiSpec, request).compile()
            compilation.exitCode shouldBe COMPILATION_ERROR
            compilation.messages shouldContain "is not @Serializable"
        }

        @Test
        fun `5_5 — Valid success and error types generate sealed result type`() {
            val request =
                kotlin(
                    "Requests.kt",
                    """
                    package dev.kolibrium.api.ksp.test.models
                    import dev.kolibrium.api.ksp.annotations.*
                    import kotlinx.serialization.Serializable
                    import kotlinx.serialization.Transient
                    @Serializable
                    data class SuccessDto(val id: Int)
                    @Serializable
                    data class ApiError(val message: String)
                    @GET("/users/{id}")
                    @Returns(success = SuccessDto::class, error = ApiError::class)
                    @Serializable
                    data class GetUserRequest(
                        @Path @Transient val id: Int = 0
                    )
                    """.trimIndent(),
                )
            val kotlinCompilation = getCompilation(validApiSpec, request)
            val compilation = kotlinCompilation.compile()
            compilation.exitCode shouldBe OK
            val source = kotlinCompilation.getGeneratedSource("TestClient.kt")
            source shouldContain "public sealed interface GetUserResult"
            source shouldContain "public fun requireSuccess(): Success"
            source shouldContain "public fun requireError(): Error"
            source shouldContain "public data class Success("
            source shouldContain "public val `data`: SuccessDto,"
            source shouldContain "public val response: HttpResponse,"
            source shouldContain ") : GetUserResult"
            source shouldContain "public data class Error("
            source shouldContain "public val `data`: ApiError,"
            source shouldContain "public suspend fun getUser(id: Int): GetUserResult"
            source shouldContain "GetUserResult.Success("
            source shouldContain "GetUserResult.Error("
            source shouldContain "httpResponse.bodyAsText()"
            source shouldContain "rawBody.take(500)"
        }
    }

    @Nested
    inner class `6 — Path Parameter Validation` {
        @Test
        fun `6_1 — Valid Path property resolves path variable`() {
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
                    @GET("/users/{id}")
                    @Returns(success = UserDto::class)
                    @Serializable
                    data class GetUserRequest(@Path @Transient val id: Int = 0)
                    """.trimIndent(),
                )
            val kotlinCompilation = getCompilation(validApiSpec, request)
            val compilation = kotlinCompilation.compile()
            compilation.exitCode shouldBe OK
            assertSourceEquals(
                """
                // Code generated by kolibrium-codegen. Do not edit.
                package dev.kolibrium.api.ksp.test.generated

                import dev.kolibrium.api.core.ApiResponse
                import dev.kolibrium.api.ksp.test.models.UserDto
                import io.ktor.client.HttpClient
                import io.ktor.client.call.body
                import io.ktor.client.request.`get`
                import io.ktor.http.contentType
                import io.ktor.http.encodeURLPathPart
                import kotlin.Int
                import kotlin.String

                public class TestClient(
                  private val client: HttpClient,
                  private val baseUrl: String,
                ) {
                  public suspend fun getUser(id: Int): ApiResponse<UserDto> {
                    val httpResponse = client.get("${'$'}baseUrl/users/${'$'}{id.toString().encodeURLPathPart()}")

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
        fun `6_2 — Path variable with no matching Path property rejected`() {
            val request =
                kotlin(
                    "Requests.kt",
                    """
                    package dev.kolibrium.api.ksp.test.models
                    import dev.kolibrium.api.ksp.annotations.*
                    import kotlinx.serialization.Serializable
                    @Serializable
                    data class UserDto(val id: Int)
                    @GET("/users/{id}")
                    @Returns(success = UserDto::class)
                    @Serializable
                    class GetUserRequest
                    """.trimIndent(),
                )
            val compilation = getCompilation(validApiSpec, request).compile()
            compilation.exitCode shouldBe COMPILATION_ERROR
            compilation.messages shouldContain "has no matching @Path parameter"
        }

        @Test
        fun `6_3 — Path property not present in path rejected`() {
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
                    @GET("/users/{id}")
                    @Returns(success = UserDto::class)
                    @Serializable
                    data class GetUserRequest(
                        @Path @Transient val id: Int = 0,
                        @Path @Transient val foo: String = ""
                    )
                    """.trimIndent(),
                )
            val compilation = getCompilation(validApiSpec, request).compile()
            compilation.exitCode shouldBe COMPILATION_ERROR
            compilation.messages shouldContain "not found in path"
        }

        @Test
        fun `6_4 — Invalid path variable name rejected`() {
            val request =
                kotlin(
                    "Requests.kt",
                    """
                    package dev.kolibrium.api.ksp.test.models
                    import dev.kolibrium.api.ksp.annotations.*
                    import kotlinx.serialization.Serializable
                    @Serializable
                    data class UserDto(val id: Int)
                    @GET("/users/{123bad}")
                    @Returns(success = UserDto::class)
                    @Serializable
                    class GetUserRequest
                    """.trimIndent(),
                )
            val compilation = getCompilation(validApiSpec, request).compile()
            compilation.exitCode shouldBe COMPILATION_ERROR
            compilation.messages shouldContain "is not a valid Kotlin identifier"
        }

        @Test
        fun `6_5 — Missing Transient on Path property rejected`() {
            val request =
                kotlin(
                    "Requests.kt",
                    """
                    package dev.kolibrium.api.ksp.test.models
                    import dev.kolibrium.api.ksp.annotations.*
                    import kotlinx.serialization.Serializable
                    @Serializable
                    data class UserDto(val id: Int)
                    @GET("/users/{id}")
                    @Returns(success = UserDto::class)
                    @Serializable
                    data class GetUserRequest(@Path val id: Int = 0)
                    """.trimIndent(),
                )
            val compilation = getCompilation(validApiSpec, request).compile()
            compilation.exitCode shouldBe COMPILATION_ERROR
            compilation.messages shouldContain "must be annotated with @Transient"
        }

        @Test
        fun `6_6 — Property annotated with both Path and Query rejected`() {
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
                    @GET("/users/{id}")
                    @Returns(success = UserDto::class)
                    @Serializable
                    data class GetUserRequest(@Path @Query @Transient val id: Int = 0)
                    """.trimIndent(),
                )
            val compilation = getCompilation(validApiSpec, request).compile()
            compilation.exitCode shouldBe COMPILATION_ERROR
            compilation.messages shouldContain "cannot be annotated with both @Path and @Query"
        }

        @Test
        fun `6_7 — Valid path parameter types accepted`() {
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
                    @GET("/a/{s}/{i}/{l}/{sh}/{f}/{d}/{b}")
                    @Returns(success = UserDto::class)
                    @Serializable
                    data class GetAllTypesRequest(
                        @Path @Transient val s: String = "",
                        @Path @Transient val i: Int = 0,
                        @Path @Transient val l: Long = 0L,
                        @Path @Transient val sh: Short = 0,
                        @Path @Transient val f: Float = 0f,
                        @Path @Transient val d: Double = 0.0,
                        @Path @Transient val b: Boolean = false
                    )
                    """.trimIndent(),
                )
            val compilation = getCompilation(validApiSpec, request).compile()
            compilation.exitCode shouldBe OK
        }

        @Test
        fun `6_8 — Invalid path parameter type rejected`() {
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
                    data class CustomType(val value: String)
                    @GET("/users/{id}")
                    @Returns(success = UserDto::class)
                    @Serializable
                    data class GetUserRequest(@Path @Transient val id: CustomType = CustomType(""))
                    """.trimIndent(),
                )
            val compilation = getCompilation(validApiSpec, request).compile()
            compilation.exitCode shouldBe COMPILATION_ERROR
            compilation.messages shouldContain "must be String, Int, Long, Short, Float, Double, or Boolean"
        }
    }

    @Nested
    inner class `7 — Query Parameter Validation` {
        @Test
        fun `7_1 — Valid Query on GET and DELETE generates optional function parameters`() {
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
                    @DELETE("/users")
                    @Returns(success = Unit::class)
                    @Serializable
                    data class DeleteUsersRequest(@Query @Transient val filter: String? = null)
                    """.trimIndent(),
                )
            val kotlinCompilation = getCompilation(validApiSpec, request)
            val compilation = kotlinCompilation.compile()
            compilation.exitCode shouldBe OK
            val source = kotlinCompilation.getGeneratedSource("TestClient.kt")
            source shouldContain "search: String? = null"
            source shouldContain """search?.let { parameter("search", it) }"""
            source shouldContain "filter: String? = null"
            source shouldContain """filter?.let { parameter("filter", it) }"""
        }

        @Test
        fun `7_2 — Query on POST, PUT, PATCH rejected`() {
            val postRequest =
                kotlin(
                    "PostRequest.kt",
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
                    data class CreateUserRequest(@Query @Transient val q: String? = null)
                    """.trimIndent(),
                )
            val compilation1 = getCompilation(validApiSpec, postRequest).compile()
            compilation1.exitCode shouldBe COMPILATION_ERROR
            compilation1.messages shouldContain "@Query parameters not allowed on"

            val putRequest =
                kotlin(
                    "PutRequest.kt",
                    """
                    package dev.kolibrium.api.ksp.test.models
                    import dev.kolibrium.api.ksp.annotations.*
                    import kotlinx.serialization.Serializable
                    import kotlinx.serialization.Transient
                    @Serializable
                    data class UserDto(val id: Int)
                    @PUT("/users")
                    @Returns(success = UserDto::class)
                    @Serializable
                    data class UpdateUserRequest(@Query @Transient val q: String? = null)
                    """.trimIndent(),
                )
            val compilation2 = getCompilation(validApiSpec, putRequest).compile()
            compilation2.exitCode shouldBe COMPILATION_ERROR
            compilation2.messages shouldContain "@Query parameters not allowed on"

            val patchRequest =
                kotlin(
                    "PatchRequest.kt",
                    """
                    package dev.kolibrium.api.ksp.test.models
                    import dev.kolibrium.api.ksp.annotations.*
                    import kotlinx.serialization.Serializable
                    import kotlinx.serialization.Transient
                    @Serializable
                    data class UserDto(val id: Int)
                    @PATCH("/users")
                    @Returns(success = UserDto::class)
                    @Serializable
                    data class PatchUserRequest(@Query @Transient val q: String? = null)
                    """.trimIndent(),
                )
            val compilation3 = getCompilation(validApiSpec, patchRequest).compile()
            compilation3.exitCode shouldBe COMPILATION_ERROR
            compilation3.messages shouldContain "@Query parameters not allowed on"
        }

        @Test
        fun `7_3 — Non-nullable Query property rejected`() {
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
                    data class SearchUsersRequest(@Query @Transient val page: Int = 0)
                    """.trimIndent(),
                )
            val compilation = getCompilation(validApiSpec, request).compile()
            compilation.exitCode shouldBe COMPILATION_ERROR
            compilation.messages shouldContain "must be nullable"
        }

        @Test
        fun `7_4 — Missing Transient on Query property rejected`() {
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
                    data class SearchUsersRequest(@Query val search: String? = null)
                    """.trimIndent(),
                )
            val compilation = getCompilation(validApiSpec, request).compile()
            compilation.exitCode shouldBe COMPILATION_ERROR
            compilation.messages shouldContain "must be annotated with @Transient"
        }

        @Test
        fun `7_5 — List String query parameter type is valid`() {
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
                    data class SearchUsersRequest(@Query @Transient val tags: List<String>? = null)
                    """.trimIndent(),
                )
            val kotlinCompilation = getCompilation(validApiSpec, request)
            val compilation = kotlinCompilation.compile()
            compilation.exitCode shouldBe OK
            val source = kotlinCompilation.getGeneratedSource("TestClient.kt")
            source shouldContain "tags"
        }

        @Test
        fun `7_6 — List with unsupported element type rejected`() {
            val request =
                kotlin(
                    "Requests.kt",
                    """
                    package dev.kolibrium.api.ksp.test.models
                    import dev.kolibrium.api.ksp.annotations.*
                    import kotlinx.serialization.Serializable
                    import kotlinx.serialization.Transient
                    import java.math.BigDecimal
                    @Serializable
                    data class UserDto(val id: Int)
                    @GET("/users")
                    @Returns(success = UserDto::class)
                    @Serializable
                    data class SearchUsersRequest(@Query @Transient val tags: List<BigDecimal>? = null)
                    """.trimIndent(),
                )
            val compilation = getCompilation(validApiSpec, request).compile()
            compilation.exitCode shouldBe COMPILATION_ERROR
            compilation.messages shouldContain "must be String, Int, Long, Short, Float, Double, Boolean, or List of these types"
        }
    }

    @Nested
    inner class `8 — Body Parameter Validation` {
        @Test
        fun `8_1 — Valid body parameters on POST generate DSL lambda`() {
            val request =
                kotlin(
                    "Requests.kt",
                    """
                    package dev.kolibrium.api.ksp.test.models
                    import dev.kolibrium.api.ksp.annotations.*
                    import kotlinx.serialization.Serializable
                    @Serializable
                    data class UserDto(val id: Int)
                    @POST("/users")
                    @Returns(success = UserDto::class)
                    @Serializable
                    data class CreateUserRequest(var name: String? = null)
                    """.trimIndent(),
                )
            val kotlinCompilation = getCompilation(validApiSpec, request)
            val compilation = kotlinCompilation.compile()
            compilation.exitCode shouldBe OK
            val source = kotlinCompilation.getGeneratedSource("TestClient.kt")
            source shouldContain "block: CreateUserRequest.() -> Unit"
            source shouldContain "CreateUserRequest().apply(block)"
        }

        @Test
        fun `8_2 — Body parameters on GET and DELETE rejected`() {
            val getRequest =
                kotlin(
                    "GetRequest.kt",
                    """
                    package dev.kolibrium.api.ksp.test.models
                    import dev.kolibrium.api.ksp.annotations.*
                    import kotlinx.serialization.Serializable
                    @Serializable
                    data class UserDto(val id: Int)
                    @GET("/users")
                    @Returns(success = UserDto::class)
                    @Serializable
                    data class GetUsersRequest(var name: String? = null)
                    """.trimIndent(),
                )
            val compilation1 = getCompilation(validApiSpec, getRequest).compile()
            compilation1.exitCode shouldBe COMPILATION_ERROR
            compilation1.messages shouldContain "Request body parameters not allowed on GET/DELETE"

            val deleteRequest =
                kotlin(
                    "DeleteRequest.kt",
                    """
                    package dev.kolibrium.api.ksp.test.models
                    import dev.kolibrium.api.ksp.annotations.*
                    import kotlinx.serialization.Serializable
                    @DELETE("/users")
                    @Returns(success = Unit::class)
                    @Serializable
                    data class DeleteUsersRequest(var name: String? = null)
                    """.trimIndent(),
                )
            val compilation2 = getCompilation(validApiSpec, deleteRequest).compile()
            compilation2.exitCode shouldBe COMPILATION_ERROR
            compilation2.messages shouldContain "Request body parameters not allowed on GET/DELETE"
        }

        @Test
        fun `8_3 — Non-nullable body parameter without default rejected`() {
            val request =
                kotlin(
                    "Requests.kt",
                    """
                    package dev.kolibrium.api.ksp.test.models
                    import dev.kolibrium.api.ksp.annotations.*
                    import kotlinx.serialization.Serializable
                    @Serializable
                    data class UserDto(val id: Int)
                    @POST("/users")
                    @Returns(success = UserDto::class)
                    @Serializable
                    data class CreateUserRequest(val name: String)
                    """.trimIndent(),
                )
            val compilation = getCompilation(validApiSpec, request).compile()
            compilation.exitCode shouldBe COMPILATION_ERROR
            compilation.messages shouldContain "must be nullable or have a default value"
        }

        @Test
        fun `8_4 — val body parameter emits warning but compiles`() {
            val request =
                kotlin(
                    "Requests.kt",
                    """
                    package dev.kolibrium.api.ksp.test.models
                    import dev.kolibrium.api.ksp.annotations.*
                    import kotlinx.serialization.Serializable
                    @Serializable
                    data class UserDto(val id: Int)
                    @POST("/users")
                    @Returns(success = UserDto::class)
                    @Serializable
                    data class CreateUserRequest(val name: String? = null)
                    """.trimIndent(),
                )
            val kotlinCompilation = getCompilation(validApiSpec, request)
            val compilation = kotlinCompilation.compile()
            compilation.exitCode shouldBe OK
            compilation.messages shouldContain "should be var for DSL builder pattern"
            val source = kotlinCompilation.getGeneratedSource("TestClient.kt")
            source shouldContain "block: CreateUserRequest.() -> Unit"
        }

        @Test
        fun `8_5 — Body POST with path parameters creates request object passing path params`() {
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
                    @POST("/users/{id}/items")
                    @Returns(success = ItemDto::class)
                    @Serializable
                    data class CreateUserItemRequest(
                        @Path @Transient val id: Int = 0,
                        var name: String? = null
                    )
                    """.trimIndent(),
                )
            val kotlinCompilation = getCompilation(validApiSpec, request)
            val compilation = kotlinCompilation.compile()
            compilation.exitCode shouldBe OK
            val source = kotlinCompilation.getGeneratedSource("TestClient.kt")
            source shouldContain "CreateUserItemRequest(id = id).apply(block)"
        }
    }

    @Nested
    inner class `9 — Authentication Code Generation` {
        @Test
        fun `9_1 — BEARER generates token context parameter and bearerAuth call`() {
            val request =
                kotlin(
                    "Requests.kt",
                    """
                    package dev.kolibrium.api.ksp.test.models
                    import dev.kolibrium.api.ksp.annotations.*
                    import dev.kolibrium.api.core.AuthType
                    import kotlinx.serialization.Serializable
                    @Serializable
                    data class UserDto(val id: Int)
                    @GET("/users")
                    @Returns(success = UserDto::class)
                    @Auth(type = AuthType.BEARER)
                    @Serializable
                    class GetUsersRequest
                    """.trimIndent(),
                )
            val kotlinCompilation = getCompilation(validApiSpec, request)
            val compilation = kotlinCompilation.compile()
            compilation.exitCode shouldBe OK
            assertSourceEquals(
                """
                // Code generated by kolibrium-codegen. Do not edit.
                package dev.kolibrium.api.ksp.test.generated

                import dev.kolibrium.api.core.ApiResponse
                import dev.kolibrium.api.ksp.test.models.UserDto
                import io.ktor.client.HttpClient
                import io.ktor.client.call.body
                import io.ktor.client.request.`get`
                import io.ktor.client.request.bearerAuth
                import io.ktor.http.contentType
                import kotlin.String

                public class TestClient(
                  private val client: HttpClient,
                  private val baseUrl: String,
                ) {
                  context(token: String)
                  public suspend fun getUsers(): ApiResponse<UserDto> {
                    val httpResponse = client.get("${'$'}baseUrl/users") {
                      bearerAuth(token)
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
        fun `9_2 — BASIC generates username and password context parameters and basicAuth call`() {
            val request =
                kotlin(
                    "Requests.kt",
                    """
                    package dev.kolibrium.api.ksp.test.models
                    import dev.kolibrium.api.ksp.annotations.*
                    import dev.kolibrium.api.core.AuthType
                    import kotlinx.serialization.Serializable
                    @Serializable
                    data class UserDto(val id: Int)
                    @GET("/users")
                    @Returns(success = UserDto::class)
                    @Auth(type = AuthType.BASIC)
                    @Serializable
                    class GetUsersRequest
                    """.trimIndent(),
                )
            val kotlinCompilation = getCompilation(validApiSpec, request)
            val compilation = kotlinCompilation.compile()
            compilation.exitCode shouldBe OK
            assertSourceEquals(
                """
                // Code generated by kolibrium-codegen. Do not edit.
                package dev.kolibrium.api.ksp.test.generated

                import dev.kolibrium.api.core.ApiResponse
                import dev.kolibrium.api.ksp.test.models.UserDto
                import io.ktor.client.HttpClient
                import io.ktor.client.call.body
                import io.ktor.client.request.`get`
                import io.ktor.client.request.basicAuth
                import io.ktor.http.contentType
                import kotlin.String

                public class TestClient(
                  private val client: HttpClient,
                  private val baseUrl: String,
                ) {
                  context(username: String, password: String)
                  public suspend fun getUsers(): ApiResponse<UserDto> {
                    val httpResponse = client.get("${'$'}baseUrl/users") {
                      basicAuth(username, password)
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
        fun `9_3 — API_KEY generates apiKey context parameter with default X-API-Key header`() {
            val request =
                kotlin(
                    "Requests.kt",
                    """
                    package dev.kolibrium.api.ksp.test.models
                    import dev.kolibrium.api.ksp.annotations.*
                    import dev.kolibrium.api.core.AuthType
                    import kotlinx.serialization.Serializable
                    @Serializable
                    data class UserDto(val id: Int)
                    @GET("/users")
                    @Returns(success = UserDto::class)
                    @Auth(type = AuthType.API_KEY)
                    @Serializable
                    class GetUsersRequest
                    """.trimIndent(),
                )
            val kotlinCompilation = getCompilation(validApiSpec, request)
            val compilation = kotlinCompilation.compile()
            compilation.exitCode shouldBe OK
            assertSourceEquals(
                """
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

                public class TestClient(
                  private val client: HttpClient,
                  private val baseUrl: String,
                ) {
                  context(apiKey: String)
                  public suspend fun getUsers(): ApiResponse<UserDto> {
                    val httpResponse = client.get("${'$'}baseUrl/users") {
                      header("X-API-Key", apiKey)
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
        fun `9_4 — API_KEY with custom valid headerName`() {
            val request =
                kotlin(
                    "Requests.kt",
                    """
                    package dev.kolibrium.api.ksp.test.models
                    import dev.kolibrium.api.ksp.annotations.*
                    import dev.kolibrium.api.core.AuthType
                    import kotlinx.serialization.Serializable
                    @Serializable
                    data class UserDto(val id: Int)
                    @GET("/users")
                    @Returns(success = UserDto::class)
                    @Auth(type = AuthType.API_KEY, headerName = "Authorization")
                    @Serializable
                    class GetUsersRequest
                    """.trimIndent(),
                )
            val kotlinCompilation = getCompilation(validApiSpec, request)
            val compilation = kotlinCompilation.compile()
            compilation.exitCode shouldBe OK
            assertSourceEquals(
                """
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

                public class TestClient(
                  private val client: HttpClient,
                  private val baseUrl: String,
                ) {
                  context(apiKey: String)
                  public suspend fun getUsers(): ApiResponse<UserDto> {
                    val httpResponse = client.get("${'$'}baseUrl/users") {
                      header("Authorization", apiKey)
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
        fun `9_5 — API_KEY with invalid headerName rejected`() {
            val request =
                kotlin(
                    "Requests.kt",
                    """
                    package dev.kolibrium.api.ksp.test.models
                    import dev.kolibrium.api.ksp.annotations.*
                    import dev.kolibrium.api.core.AuthType
                    import kotlinx.serialization.Serializable
                    @Serializable
                    data class UserDto(val id: Int)
                    @GET("/users")
                    @Returns(success = UserDto::class)
                    @Auth(type = AuthType.API_KEY, headerName = "Invalid Header Name")
                    @Serializable
                    class GetUsersRequest
                    """.trimIndent(),
                )
            val compilation = getCompilation(validApiSpec, request).compile()
            compilation.exitCode shouldBe COMPILATION_ERROR
            compilation.messages shouldContain "Invalid API key header name"
        }

        @Test
        fun `9_6 — CUSTOM generates AuthContext Custom context parameter and configure call`() {
            val authContext =
                kotlin(
                    "AuthContext.kt",
                    """
                    package dev.kolibrium.api.ksp.test
                    import io.ktor.client.request.HttpRequestBuilder
                    sealed interface AuthContext {
                        interface Custom {
                            fun configure(builder: HttpRequestBuilder)
                        }
                    }
                    """.trimIndent(),
                )
            val request =
                kotlin(
                    "Requests.kt",
                    """
                    package dev.kolibrium.api.ksp.test.models
                    import dev.kolibrium.api.ksp.annotations.*
                    import dev.kolibrium.api.core.AuthType
                    import kotlinx.serialization.Serializable
                    @Serializable
                    data class UserDto(val id: Int)
                    @GET("/users")
                    @Returns(success = UserDto::class)
                    @Auth(type = AuthType.CUSTOM)
                    @Serializable
                    class GetUsersRequest
                    """.trimIndent(),
                )
            val kotlinCompilation = getCompilation(validApiSpec, authContext, request)
            val compilation = kotlinCompilation.compile()
            compilation.exitCode shouldBe OK
            assertSourceEquals(
                """
                // Code generated by kolibrium-codegen. Do not edit.
                package dev.kolibrium.api.ksp.test.generated

                import dev.kolibrium.api.core.ApiResponse
                import dev.kolibrium.api.ksp.test.AuthContext
                import dev.kolibrium.api.ksp.test.models.UserDto
                import io.ktor.client.HttpClient
                import io.ktor.client.call.body
                import io.ktor.client.request.`get`
                import io.ktor.http.contentType
                import kotlin.String

                public class TestClient(
                  private val client: HttpClient,
                  private val baseUrl: String,
                ) {
                  context(auth: AuthContext.Custom)
                  public suspend fun getUsers(): ApiResponse<UserDto> {
                    val httpResponse = client.get("${'$'}baseUrl/users") {
                      auth.configure(this)
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
        fun `9_7 — headerName used with non-API_KEY auth type rejected`() {
            val request =
                kotlin(
                    "Requests.kt",
                    """
                    package dev.kolibrium.api.ksp.test.models
                    import dev.kolibrium.api.ksp.annotations.*
                    import dev.kolibrium.api.core.AuthType
                    import kotlinx.serialization.Serializable
                    @Serializable
                    data class UserDto(val id: Int)
                    @GET("/users")
                    @Returns(success = UserDto::class)
                    @Auth(type = AuthType.BEARER, headerName = "X-Custom")
                    @Serializable
                    class GetUsersRequest
                    """.trimIndent(),
                )
            val compilation = getCompilation(validApiSpec, request).compile()
            compilation.exitCode shouldBe COMPILATION_ERROR
            compilation.messages shouldContain "headerName parameter can only be used with AuthType.API_KEY"
        }

        @Test
        fun `9_8 — Multiple requests with different auth types generate all required auth imports`() {
            val request =
                kotlin(
                    "Requests.kt",
                    """
                    package dev.kolibrium.api.ksp.test.models
                    import dev.kolibrium.api.ksp.annotations.*
                    import dev.kolibrium.api.core.AuthType
                    import kotlinx.serialization.Serializable
                    @Serializable
                    data class UserDto(val id: Int)
                    @GET("/users")
                    @Returns(success = UserDto::class)
                    @Auth(type = AuthType.BEARER)
                    @Serializable
                    class GetUsersRequest
                    @GET("/orders")
                    @Returns(success = UserDto::class)
                    @Auth(type = AuthType.BASIC)
                    @Serializable
                    class GetOrdersRequest
                    """.trimIndent(),
                )
            val kotlinCompilation = getCompilation(validApiSpec, request)
            val compilation = kotlinCompilation.compile()
            compilation.exitCode shouldBe OK
            val source = kotlinCompilation.getGeneratedSource("TestClient.kt")
            source shouldContain "import io.ktor.client.request.bearerAuth"
            source shouldContain "import io.ktor.client.request.basicAuth"
        }

        @Test
        fun `9_9 — NONE generates no context parameters or auth logic`() {
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
                    class GetUsersRequest
                    """.trimIndent(),
                )
            val kotlinCompilation = getCompilation(validApiSpec, request)
            val compilation = kotlinCompilation.compile()
            compilation.exitCode shouldBe OK
            assertSourceEquals(
                """
                // Code generated by kolibrium-codegen. Do not edit.
                package dev.kolibrium.api.ksp.test.generated

                import dev.kolibrium.api.core.ApiResponse
                import dev.kolibrium.api.ksp.test.models.UserDto
                import io.ktor.client.HttpClient
                import io.ktor.client.call.body
                import io.ktor.client.request.`get`
                import io.ktor.http.contentType
                import kotlin.String

                public class TestClient(
                  private val client: HttpClient,
                  private val baseUrl: String,
                ) {
                  public suspend fun getUsers(): ApiResponse<UserDto> {
                    val httpResponse = client.get("${'$'}baseUrl/users")

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

    @Nested
    inner class `10 — Function Name Collision Detection` {
        @Test
        fun `10_1 — Two request classes that derive the same function name in SingleClient rejected`() {
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
                    class GetUserRequest
                    @GET("/users/active")
                    @Returns(success = UserDto::class)
                    @Serializable
                    class GetUserRequest2
                    """.trimIndent(),
                )
            // GetUserRequest -> getUser, GetUserRequest2 won't collide. Need actual collision.
            val request2 =
                kotlin(
                    "Requests2.kt",
                    """
                    package dev.kolibrium.api.ksp.test.models
                    import dev.kolibrium.api.ksp.annotations.*
                    import kotlinx.serialization.Serializable
                    @Serializable
                    data class UserDto(val id: Int)
                    @GET("/users/a")
                    @Returns(success = UserDto::class)
                    @Serializable
                    class GetUsersRequest
                    @GET("/users/b")
                    @Returns(success = UserDto::class)
                    @Serializable
                    class GetUsersRequest2
                    """.trimIndent(),
                )
            // GetUsersRequest -> getUsers, GetUsersRequest2 won't collide either.
            // Need two classes that produce same function name. Use same simple name in different files.
            val requestA =
                kotlin(
                    "RequestA.kt",
                    """
                    package dev.kolibrium.api.ksp.test.models
                    import dev.kolibrium.api.ksp.annotations.*
                    import kotlinx.serialization.Serializable
                    @Serializable
                    data class UserDto(val id: Int)
                    @GET("/users")
                    @Returns(success = UserDto::class)
                    @Serializable
                    class GetUserRequest
                    """.trimIndent(),
                )
            val requestB =
                kotlin(
                    "RequestB.kt",
                    """
                    package dev.kolibrium.api.ksp.test.models.sub
                    import dev.kolibrium.api.ksp.annotations.*
                    import dev.kolibrium.api.ksp.test.models.UserDto
                    import kotlinx.serialization.Serializable
                    @GET("/users/active")
                    @Returns(success = UserDto::class)
                    @Serializable
                    class GetUserRequest
                    """.trimIndent(),
                )
            val compilation = getCompilation(validApiSpec, requestA, requestB).compile()
            compilation.exitCode shouldBe COMPILATION_ERROR
            compilation.messages shouldContain "conflicts with another function"
        }
    }

    @Nested
    inner class `12 — Generated Client Class Structure` {
        @Test
        fun `12_1 — Client class name, package, and file header`() {
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
                    class GetUsersRequest
                    """.trimIndent(),
                )
            val kotlinCompilation = getCompilation(validApiSpec, request)
            val compilation = kotlinCompilation.compile()
            compilation.exitCode shouldBe OK
            val source = kotlinCompilation.getGeneratedSource("TestClient.kt")
            source shouldContain "// Code generated by kolibrium-codegen. Do not edit."
            source shouldContain "package dev.kolibrium.api.ksp.test.generated"
            source shouldContain "public class TestClient"
        }

        @Test
        fun `12_2 — Constructor parameters stored as private properties`() {
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
                    class GetUsersRequest
                    """.trimIndent(),
                )
            val kotlinCompilation = getCompilation(validApiSpec, request)
            val compilation = kotlinCompilation.compile()
            compilation.exitCode shouldBe OK
            val source = kotlinCompilation.getGeneratedSource("TestClient.kt")
            source shouldContain "private val client: HttpClient"
            source shouldContain "private val baseUrl: String"
        }

        @Test
        fun `12_3 — All five HTTP methods generate suspend functions with correct verb`() {
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
                    class GetUsersRequest
                    @POST("/users")
                    @Returns(success = UserDto::class)
                    @Serializable
                    class CreateUserRequest
                    @PUT("/users")
                    @Returns(success = UserDto::class)
                    @Serializable
                    class UpdateUserRequest
                    @PATCH("/users")
                    @Returns(success = UserDto::class)
                    @Serializable
                    class PatchUserRequest
                    @DELETE("/users")
                    @Returns(success = Unit::class)
                    @Serializable
                    class DeleteUserRequest
                    """.trimIndent(),
                )
            val kotlinCompilation = getCompilation(validApiSpec, request)
            val compilation = kotlinCompilation.compile()
            compilation.exitCode shouldBe OK
            val source = kotlinCompilation.getGeneratedSource("TestClient.kt")
            source shouldContain "public suspend fun getUsers()"
            source shouldContain "client.get("
            source shouldContain "public suspend fun createUser()"
            source shouldContain "client.post("
            source shouldContain "public suspend fun updateUser()"
            source shouldContain "client.put("
            source shouldContain "public suspend fun patchUser()"
            source shouldContain "client.patch("
            source shouldContain "public suspend fun deleteUser()"
            source shouldContain "client.delete("
        }

        @Test
        fun `12_4 — Simple request uses terse single-line call form`() {
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
                    class GetUsersRequest
                    """.trimIndent(),
                )
            val kotlinCompilation = getCompilation(validApiSpec, request)
            val compilation = kotlinCompilation.compile()
            compilation.exitCode shouldBe OK
            val source = kotlinCompilation.getGeneratedSource("TestClient.kt")
            source shouldContain """val httpResponse = client.get("${'$'}baseUrl/users")"""
        }

        @Test
        fun `12_5 — Request with query params uses block-builder call form`() {
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
            source shouldContain """client.get("${'$'}baseUrl/users") {"""
            source shouldContain "parameter("
        }
    }

    @Nested
    inner class `13 — Generated Test Harness` {
        @Test
        fun `13_1 — Test harness file name, package, and header`() {
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
                    class GetUsersRequest
                    """.trimIndent(),
                )
            val kotlinCompilation = getCompilation(validApiSpec, request)
            val compilation = kotlinCompilation.compile()
            compilation.exitCode shouldBe OK
            val source = kotlinCompilation.getGeneratedSource("TestTestHarness.kt")
            source shouldContain "// Code generated by kolibrium-codegen. Do not edit."
            source shouldContain "package dev.kolibrium.api.ksp.test.generated"
        }

        @Test
        fun `13_2 — Simple and setUp-tearDown overload signatures and delegation`() {
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
                    class GetUsersRequest
                    """.trimIndent(),
                )
            val kotlinCompilation = getCompilation(validApiSpec, request)
            val compilation = kotlinCompilation.compile()
            compilation.exitCode shouldBe OK
            val source = kotlinCompilation.getGeneratedSource("TestTestHarness.kt")
            source shouldContain "fun testApiTest("
            source shouldContain "baseUrl: String = TestApiSpec.baseUrl"
            source shouldContain "client: HttpClient = TestApiSpec.httpClient"
            source shouldContain "block: suspend TestClient.() -> Unit"
            source shouldContain "apiTest(api = TestClient(client, baseUrl), block = block)"
            source shouldContain "fun <T> testApiTest("
            source shouldContain "setUp: suspend TestClient.() -> T"
            source shouldContain "tearDown: suspend TestClient.(T) -> Unit = {}"
            source shouldContain "block: suspend TestClient.(T) -> Unit"
            source shouldContain "apiTest(api = TestClient(client, baseUrl), setUp = setUp, tearDown = tearDown, block = block)"
        }

        @Test
        fun `13_3 — generateTestHarness = false skips test harness generation`() {
            val noHarnessApiSpec =
                kotlin(
                    "NoHarnessApiSpec.kt",
                    """
                    package dev.kolibrium.api.ksp.test
                    import dev.kolibrium.api.core.ApiSpec
                    import dev.kolibrium.api.ksp.annotations.GenerateApi
                    @GenerateApi(generateTestHarness = false)
                    object NoHarnessApiSpec : ApiSpec() {
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
                    class GetUsersRequest
                    """.trimIndent(),
                )
            val kotlinCompilation = getCompilation(noHarnessApiSpec, request)
            val compilation = kotlinCompilation.compile()
            compilation.exitCode shouldBe OK
            // Client should still be generated
            val clientSource = kotlinCompilation.getGeneratedSource("NoHarnessClient.kt")
            clientSource shouldContain "class NoHarnessClient"
            // But test harness should NOT be generated
            shouldThrow<IllegalArgumentException> {
                kotlinCompilation.getGeneratedSource("NoHarnessTestHarness.kt")
            }
        }

        @Test
        fun `13_4 — GenerateApi with custom scanPackages discovers request classes in specified package`() {
            val apiSpec =
                kotlin(
                    "CustomScanApiSpec.kt",
                    """
                    package dev.kolibrium.api.ksp.test
                    import dev.kolibrium.api.core.ApiSpec
                    import dev.kolibrium.api.ksp.annotations.GenerateApi
                    @GenerateApi(scanPackages = ["dev.kolibrium.api.ksp.test.custom"])
                    object CustomScanApiSpec : ApiSpec() {
                        override val baseUrl = "https://test.api"
                    }
                    """.trimIndent(),
                )
            val request =
                kotlin(
                    "CustomRequests.kt",
                    """
                    package dev.kolibrium.api.ksp.test.custom
                    import dev.kolibrium.api.ksp.annotations.*
                    import kotlinx.serialization.Serializable
                    @Serializable
                    data class UserDto(val id: Int)
                    @GET("/users")
                    @Returns(success = UserDto::class)
                    @Serializable
                    class GetUsersRequest
                    """.trimIndent(),
                )
            val kotlinCompilation = getCompilation(apiSpec, request)
            val compilation = kotlinCompilation.compile()
            compilation.exitCode shouldBe OK
            val clientSource = kotlinCompilation.getGeneratedSource("CustomScanClient.kt")
            clientSource shouldContain "class CustomScanClient"
            clientSource shouldContain "suspend fun getUsers"
        }

        @Test
        fun `13_5 — GenerateApi with ByPrefix grouping produces grouped clients`() {
            val apiSpec =
                kotlin(
                    "GroupedApiSpec.kt",
                    """
                    package dev.kolibrium.api.ksp.test
                    import dev.kolibrium.api.core.ApiSpec
                    import dev.kolibrium.api.core.ClientGrouping
                    import dev.kolibrium.api.ksp.annotations.GenerateApi
                    @GenerateApi(grouping = ClientGrouping.ByPrefix)
                    object GroupedApiSpec : ApiSpec() {
                        override val baseUrl = "https://test.api"
                    }
                    """.trimIndent(),
                )
            val request =
                kotlin(
                    "GroupedRequests.kt",
                    """
                    package dev.kolibrium.api.ksp.test.models
                    import dev.kolibrium.api.ksp.annotations.*
                    import kotlinx.serialization.Serializable
                    @Serializable
                    data class UserDto(val id: Int)
                    @GET("/users")
                    @Returns(success = UserDto::class)
                    @Serializable
                    class GetUsersRequest
                    @Serializable
                    data class OrderDto(val id: Int)
                    @GET("/orders")
                    @Returns(success = OrderDto::class)
                    @Serializable
                    class GetOrdersRequest
                    """.trimIndent(),
                )
            val kotlinCompilation = getCompilation(apiSpec, request)
            val compilation = kotlinCompilation.compile()
            compilation.exitCode shouldBe OK
            val usersClient = kotlinCompilation.getGeneratedSource("UsersClient.kt")
            usersClient shouldContain "class UsersClient"
            usersClient shouldContain "suspend fun getUsers"
            val ordersClient = kotlinCompilation.getGeneratedSource("OrdersClient.kt")
            ordersClient shouldContain "class OrdersClient"
            ordersClient shouldContain "suspend fun getOrders"
            // Root aggregator client should reference group clients
            val rootClient = kotlinCompilation.getGeneratedSource("GroupedClient.kt")
            rootClient shouldContain "class GroupedClient"
        }

        @Test
        fun `13_6 — GenerateApi absent uses all defaults`() {
            val apiSpec =
                kotlin(
                    "DefaultApiSpec.kt",
                    """
                    package dev.kolibrium.api.ksp.test
                    import dev.kolibrium.api.core.ApiSpec
                    object DefaultApiSpec : ApiSpec() {
                        override val baseUrl = "https://test.api"
                    }
                    """.trimIndent(),
                )
            val request =
                kotlin(
                    "DefaultRequests.kt",
                    """
                    package dev.kolibrium.api.ksp.test.models
                    import dev.kolibrium.api.ksp.annotations.*
                    import kotlinx.serialization.Serializable
                    @Serializable
                    data class UserDto(val id: Int)
                    @GET("/users")
                    @Returns(success = UserDto::class)
                    @Serializable
                    class GetUsersRequest
                    """.trimIndent(),
                )
            val kotlinCompilation = getCompilation(apiSpec, request)
            val compilation = kotlinCompilation.compile()
            compilation.exitCode shouldBe OK
            // SingleClient: single client class
            val clientSource = kotlinCompilation.getGeneratedSource("DefaultClient.kt")
            clientSource shouldContain "class DefaultClient"
            // Test harness generated by default
            val harnessSource = kotlinCompilation.getGeneratedSource("DefaultTestHarness.kt")
            harnessSource shouldContain "fun defaultApiTest"
        }

        @Test
        fun `13_7 — GenerateApi with empty scanPackages uses convention default`() {
            val apiSpec =
                kotlin(
                    "EmptyScanApiSpec.kt",
                    """
                    package dev.kolibrium.api.ksp.test
                    import dev.kolibrium.api.core.ApiSpec
                    import dev.kolibrium.api.ksp.annotations.GenerateApi
                    @GenerateApi(scanPackages = [])
                    object EmptyScanApiSpec : ApiSpec() {
                        override val baseUrl = "https://test.api"
                    }
                    """.trimIndent(),
                )
            val request =
                kotlin(
                    "EmptyScanRequests.kt",
                    """
                    package dev.kolibrium.api.ksp.test.models
                    import dev.kolibrium.api.ksp.annotations.*
                    import kotlinx.serialization.Serializable
                    @Serializable
                    data class UserDto(val id: Int)
                    @GET("/users")
                    @Returns(success = UserDto::class)
                    @Serializable
                    class GetUsersRequest
                    """.trimIndent(),
                )
            val kotlinCompilation = getCompilation(apiSpec, request)
            val compilation = kotlinCompilation.compile()
            compilation.exitCode shouldBe OK
            val clientSource = kotlinCompilation.getGeneratedSource("EmptyScanClient.kt")
            clientSource shouldContain "class EmptyScanClient"
            clientSource shouldContain "suspend fun getUsers"
        }
    }

    @Nested
    inner class `14 — Error Isolation` {
        @Test
        fun `14_1 — Any single validation error suppresses all code generation`() {
            val invalidRequest =
                kotlin(
                    "InvalidRequest.kt",
                    """
                    package dev.kolibrium.api.ksp.test.models
                    import dev.kolibrium.api.ksp.annotations.*
                    import kotlinx.serialization.Serializable
                    @Serializable
                    data class UserDto(val id: Int)
                    @GET("")
                    @Returns(success = UserDto::class)
                    @Serializable
                    class BadRequest
                    """.trimIndent(),
                )
            val validRequest =
                kotlin(
                    "ValidRequest.kt",
                    """
                    package dev.kolibrium.api.ksp.test.models
                    import dev.kolibrium.api.ksp.annotations.*
                    import kotlinx.serialization.Serializable
                    @Serializable
                    data class ItemDto(val id: Int)
                    @GET("/items")
                    @Returns(success = ItemDto::class)
                    @Serializable
                    class GetItemsRequest
                    """.trimIndent(),
                )
            val validRequest2 =
                kotlin(
                    "ValidRequest2.kt",
                    """
                    package dev.kolibrium.api.ksp.test.models
                    import dev.kolibrium.api.ksp.annotations.*
                    import kotlinx.serialization.Serializable
                    @Serializable
                    data class OrderDto(val id: Int)
                    @GET("/orders")
                    @Returns(success = OrderDto::class)
                    @Serializable
                    class GetOrdersRequest
                    """.trimIndent(),
                )
            val kotlinCompilation = getCompilation(validApiSpec, invalidRequest, validRequest, validRequest2)
            val compilation = kotlinCompilation.compile()
            compilation.exitCode shouldBe COMPILATION_ERROR
            val generatedFiles =
                kotlinCompilation.kspSourcesDir
                    .walkTopDown()
                    .filter { it.isFile && it.extension == "kt" }
                    .toList()
            generatedFiles shouldBe emptyList()
        }
    }

    @Nested
    inner class `15 — Path Format Edge Cases` {
        @Test
        fun `15_1 — Double slashes in path rejected`() {
            val request =
                kotlin(
                    "Requests.kt",
                    """
                    package dev.kolibrium.api.ksp.test.models
                    import dev.kolibrium.api.ksp.annotations.*
                    import kotlinx.serialization.Serializable
                    @Serializable
                    data class UserDto(val id: Int)
                    @GET("//users")
                    @Returns(success = UserDto::class)
                    @Serializable
                    class GetUsersRequest
                    """.trimIndent(),
                )
            val compilation = getCompilation(validApiSpec, request).compile()
            compilation.exitCode shouldBe COMPILATION_ERROR
            compilation.messages shouldContain "contains empty segments"
        }

        @Test
        fun `15_2 — Query string in path rejected`() {
            val request =
                kotlin(
                    "Requests.kt",
                    """
                    package dev.kolibrium.api.ksp.test.models
                    import dev.kolibrium.api.ksp.annotations.*
                    import kotlinx.serialization.Serializable
                    @Serializable
                    data class UserDto(val id: Int)
                    @GET("/users?foo=bar")
                    @Returns(success = UserDto::class)
                    @Serializable
                    class GetUsersRequest
                    """.trimIndent(),
                )
            val compilation = getCompilation(validApiSpec, request).compile()
            compilation.exitCode shouldBe COMPILATION_ERROR
            compilation.messages shouldContain "must not contain query strings"
        }

        @Test
        fun `15_3 — Fragment identifier in path rejected`() {
            val request =
                kotlin(
                    "Requests.kt",
                    """
                    package dev.kolibrium.api.ksp.test.models
                    import dev.kolibrium.api.ksp.annotations.*
                    import kotlinx.serialization.Serializable
                    @Serializable
                    data class UserDto(val id: Int)
                    @GET("/users#section")
                    @Returns(success = UserDto::class)
                    @Serializable
                    class GetUsersRequest
                    """.trimIndent(),
                )
            val compilation = getCompilation(validApiSpec, request).compile()
            compilation.exitCode shouldBe COMPILATION_ERROR
            compilation.messages shouldContain "must not contain fragment identifiers"
        }

        @Test
        fun `15_4 — Empty path segments rejected`() {
            val request =
                kotlin(
                    "Requests.kt",
                    """
                    package dev.kolibrium.api.ksp.test.models
                    import dev.kolibrium.api.ksp.annotations.*
                    import kotlinx.serialization.Serializable
                    @Serializable
                    data class UserDto(val id: Int)
                    @GET("/users//profile")
                    @Returns(success = UserDto::class)
                    @Serializable
                    class GetUsersRequest
                    """.trimIndent(),
                )
            val compilation = getCompilation(validApiSpec, request).compile()
            compilation.exitCode shouldBe COMPILATION_ERROR
            compilation.messages shouldContain "contains empty segments"
        }

        @Test
        fun `15_5 — Nested braces in path rejected`() {
            val request =
                kotlin(
                    "Requests.kt",
                    """
                    package dev.kolibrium.api.ksp.test.models
                    import dev.kolibrium.api.ksp.annotations.*
                    import kotlinx.serialization.Serializable
                    @Serializable
                    data class UserDto(val id: Int)
                    @GET("/users/{foo{bar}}")
                    @Returns(success = UserDto::class)
                    @Serializable
                    class GetUsersRequest
                    """.trimIndent(),
                )
            val compilation = getCompilation(validApiSpec, request).compile()
            compilation.exitCode shouldBe COMPILATION_ERROR
            compilation.messages shouldContain "contains nested braces"
        }

        @Test
        fun `15_6 — Unclosed brace in path rejected`() {
            val request =
                kotlin(
                    "Requests.kt",
                    """
                    package dev.kolibrium.api.ksp.test.models
                    import dev.kolibrium.api.ksp.annotations.*
                    import kotlinx.serialization.Serializable
                    @Serializable
                    data class UserDto(val id: Int)
                    @GET("/users/{id")
                    @Returns(success = UserDto::class)
                    @Serializable
                    class GetUsersRequest
                    """.trimIndent(),
                )
            val compilation = getCompilation(validApiSpec, request).compile()
            compilation.exitCode shouldBe COMPILATION_ERROR
            compilation.messages shouldContain "contains unclosed brace"
        }

        @Test
        fun `15_7 — Empty braces in path rejected`() {
            val request =
                kotlin(
                    "Requests.kt",
                    """
                    package dev.kolibrium.api.ksp.test.models
                    import dev.kolibrium.api.ksp.annotations.*
                    import kotlinx.serialization.Serializable
                    @Serializable
                    data class UserDto(val id: Int)
                    @GET("/users/{}")
                    @Returns(success = UserDto::class)
                    @Serializable
                    class GetUsersRequest
                    """.trimIndent(),
                )
            val compilation = getCompilation(validApiSpec, request).compile()
            compilation.exitCode shouldBe COMPILATION_ERROR
            compilation.messages shouldContain "contains empty braces"
        }

        @Test
        fun `15_8 — Trailing slash is normalized`() {
            val request =
                kotlin(
                    "Requests.kt",
                    """
                    package dev.kolibrium.api.ksp.test.models
                    import dev.kolibrium.api.ksp.annotations.*
                    import kotlinx.serialization.Serializable
                    @Serializable
                    data class UserDto(val id: Int)
                    @GET("/users/")
                    @Returns(success = UserDto::class)
                    @Serializable
                    class GetUsersRequest
                    """.trimIndent(),
                )
            val kotlinCompilation = getCompilation(validApiSpec, request)
            val compilation = kotlinCompilation.compile()
            compilation.exitCode shouldBe OK
            val source = kotlinCompilation.getGeneratedSource("TestClient.kt")
            source shouldContain "\"${'$'}baseUrl/users\""
        }

        @Test
        fun `15_9 — List query parameter generates forEach loop`() {
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
                    data class SearchUsersRequest(
                        @Query @Transient val tags: List<String>? = null
                    )
                    """.trimIndent(),
                )
            val kotlinCompilation = getCompilation(validApiSpec, request)
            val compilation = kotlinCompilation.compile()
            compilation.exitCode shouldBe OK
            val source = kotlinCompilation.getGeneratedSource("TestClient.kt")
            source shouldContain """tags?.let { url.parameters.appendAll("tags", it) }"""
        }
    }

    @Nested
    inner class `16 — Concurrent Processing` {
        @Test
        fun `16_1 — Multiple ApiSpec objects with overlapping scan packages generate separate clients`() {
            val spec1 =
                kotlin(
                    "Spec1.kt",
                    """
                    package dev.kolibrium.api.ksp.test
                    import dev.kolibrium.api.core.ApiSpec
                    object PetApiSpec : ApiSpec() {
                        override val baseUrl = "https://pet.api"
                    }
                    """.trimIndent(),
                )
            val spec2 =
                kotlin(
                    "Spec2.kt",
                    """
                    package dev.kolibrium.api.ksp.test
                    import dev.kolibrium.api.core.ApiSpec
                    object StoreApiSpec : ApiSpec() {
                        override val baseUrl = "https://store.api"
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
                    data class ItemDto(val id: Int)
                    @GET("/items")
                    @Returns(success = ItemDto::class)
                    @Serializable
                    class GetItemsRequest
                    """.trimIndent(),
                )
            val kotlinCompilation = getCompilation(spec1, spec2, request)
            val compilation = kotlinCompilation.compile()
            compilation.exitCode shouldBe OK
            kotlinCompilation.getGeneratedSource("PetClient.kt")
            kotlinCompilation.getGeneratedSource("StoreClient.kt")
        }
    }

    @Nested
    inner class `17 — Error Recovery` {
        @Test
        fun `17_1 — Validation errors prevent all code generation, no partial output`() {
            val validRequest =
                kotlin(
                    "ValidRequest.kt",
                    """
                    package dev.kolibrium.api.ksp.test.models
                    import dev.kolibrium.api.ksp.annotations.*
                    import kotlinx.serialization.Serializable
                    @Serializable
                    data class UserDto(val id: Int)
                    @GET("/users")
                    @Returns(success = UserDto::class)
                    @Serializable
                    class GetUsersRequest
                    """.trimIndent(),
                )
            val invalidRequest =
                kotlin(
                    "InvalidRequest.kt",
                    """
                    package dev.kolibrium.api.ksp.test.models
                    import dev.kolibrium.api.ksp.annotations.*
                    import kotlinx.serialization.Serializable
                    @Serializable
                    data class OrderDto(val id: Int)
                    @GET("/orders")
                    @GET("/orders2")
                    @Returns(success = OrderDto::class)
                    @Serializable
                    class GetOrdersRequest
                    """.trimIndent(),
                )
            val kotlinCompilation = getCompilation(validApiSpec, validRequest, invalidRequest)
            val compilation = kotlinCompilation.compile()
            compilation.exitCode shouldBe COMPILATION_ERROR
            val generatedFiles =
                kotlinCompilation.kspSourcesDir
                    .walkTopDown()
                    .filter { it.isFile && it.extension == "kt" }
                    .toList()
            generatedFiles shouldBe emptyList()
        }

        @Test
        fun `17_2 — Multiple validation errors are all reported`() {
            val request =
                kotlin(
                    "Requests.kt",
                    """
                    package dev.kolibrium.api.ksp.test.models
                    import dev.kolibrium.api.ksp.annotations.*
                    import kotlinx.serialization.Serializable
                    @Serializable
                    data class UserDto(val id: Int)
                    @GET("//users?bad#path")
                    @Returns(success = UserDto::class)
                    @Serializable
                    class GetUsersRequest
                    """.trimIndent(),
                )
            val compilation = getCompilation(validApiSpec, request).compile()
            compilation.exitCode shouldBe COMPILATION_ERROR
            compilation.messages shouldContain "contains empty segments"
            compilation.messages shouldContain "must not contain query strings"
            compilation.messages shouldContain "must not contain fragment identifiers"
        }
    }
}

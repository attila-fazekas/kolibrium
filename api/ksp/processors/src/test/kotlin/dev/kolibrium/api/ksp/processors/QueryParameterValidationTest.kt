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
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Test

class QueryParameterValidationTest : ApiBaseTest() {
    @Test
    fun `Valid Query on GET and DELETE generates optional function parameters`() {
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
        source shouldContain "block: SearchUsersRequest.() -> Unit = {}"
        source shouldContain """request.search?.let { parameter("search", it) }"""
        source shouldContain "block: DeleteUsersRequest.() -> Unit = {}"
        source shouldContain """request.filter?.let { parameter("filter", it) }"""
    }

    @Test
    fun `Query on POST, PUT, PATCH rejected`() {
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
    fun `Non-nullable Query property rejected`() {
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
    fun `Missing Transient on Query property rejected`() {
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
    fun `List String query parameter type is valid`() {
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
    fun `List with unsupported element type rejected`() {
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

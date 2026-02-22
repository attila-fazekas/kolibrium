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

class BodyParameterValidationTest : ApiBaseTest() {
    @Test
    fun `Valid body parameters on POST generate DSL lambda`() {
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
    fun `Body parameters on GET and DELETE rejected`() {
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
    fun `Non-nullable body parameter without default rejected`() {
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
    fun `val body parameter emits warning but compiles`() {
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
    fun `Body POST with path parameters creates request object passing path params`() {
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

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

class PathFormatEdgeCasesTest : ApiBaseTest() {
    @Test
    fun `Double slashes in path rejected`() {
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
    fun `Empty path segments rejected`() {
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
    fun `Empty braces in path rejected`() {
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
    fun `Trailing slash is normalized`() {
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
                object GetUsersRequest
                """.trimIndent(),
            )
        val kotlinCompilation = getCompilation(validApiSpec, request)
        val compilation = kotlinCompilation.compile()
        compilation.exitCode shouldBe OK
        val source = kotlinCompilation.getGeneratedSource("TestClient.kt")
        source shouldContain $$"\"$baseUrl/users\""
    }

    @Test
    fun `List query parameter generates forEach loop`() {
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

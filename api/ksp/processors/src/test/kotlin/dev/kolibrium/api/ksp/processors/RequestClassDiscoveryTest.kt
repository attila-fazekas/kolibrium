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

import com.tschuchort.compiletesting.KotlinCompilation.ExitCode.OK
import com.tschuchort.compiletesting.SourceFile.Companion.kotlin
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Test

class RequestClassDiscoveryTest : ApiBaseTest() {
    @Test
    fun `Convention scan package`() {
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
    fun `Subpackage of scan package is included`() {
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
    fun `Class outside scan package is ignored`() {
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
    fun `Class without @Serializable is ignored`() {
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
    fun `Class without HTTP method annotation is ignored`() {
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

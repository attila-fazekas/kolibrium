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
import com.tschuchort.compiletesting.SourceFile.Companion.kotlin
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Test

class FunctionNameCollisionDetectionTest : ApiBaseTest() {
    @Test
    fun `Two request classes that derive the same function name in SingleClient rejected`() {
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
                object GetUserRequest
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
                object GetUserRequest
                """.trimIndent(),
            )
        val compilation = getCompilation(validApiSpec, requestA, requestB).compile()
        compilation.exitCode shouldBe COMPILATION_ERROR
        compilation.messages shouldContain "conflicts with another function"
    }
}

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
import com.tschuchort.compiletesting.kspSourcesDir
import io.kotest.matchers.shouldBe
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Test

class ErrorIsolationTest : ApiBaseTest() {
    @Test
    fun `Any single validation error suppresses all code generation`() {
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

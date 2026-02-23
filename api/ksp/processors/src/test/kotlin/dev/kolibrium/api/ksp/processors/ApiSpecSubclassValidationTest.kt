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

class ApiSpecSubclassValidationTest : ApiBaseTest() {
    @Test
    fun `Abstract class rejected`() {
        val source =
            kotlin(
                "PetApiSpec.kt",
                """
                package dev.kolibrium.api.ksp.test
                import dev.kolibrium.api.core.ApiSpec
                import dev.kolibrium.api.ksp.annotations.GenerateApi
                @GenerateApi
                abstract class PetApiSpec : ApiSpec()
                """.trimIndent(),
            )
        val compilation = getCompilation(source).compile()
        compilation.exitCode shouldBe COMPILATION_ERROR
        compilation.messages shouldContain "must be an object declaration or a concrete class"
    }

    @Test
    fun `Named exactly ApiSpec rejected`() {
        val source =
            kotlin(
                "ApiSpec.kt",
                """
                package dev.kolibrium.api.ksp.test
                import dev.kolibrium.api.core.ApiSpec
                import dev.kolibrium.api.ksp.annotations.GenerateApi
                @GenerateApi
                object ApiSpec : ApiSpec()
                """.trimIndent(),
            )
        val compilation = getCompilation(source).compile()
        compilation.exitCode shouldBe COMPILATION_ERROR
        compilation.messages shouldContain "must not be named exactly 'ApiSpec'"
    }

    @Test
    fun `Invalid package name rejected`() {
        val source =
            kotlin(
                "PetApiSpec.kt",
                """
                package com.`123invalid`
                import dev.kolibrium.api.core.ApiSpec
                import dev.kolibrium.api.ksp.annotations.GenerateApi
                @GenerateApi
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
    fun `Duplicate apiName in same package rejected`() {
        val source1 =
            kotlin(
                "PetApiSpec.kt",
                """
                package dev.kolibrium.api.ksp.test
                import dev.kolibrium.api.core.ApiSpec
                import dev.kolibrium.api.ksp.annotations.GenerateApi
                @GenerateApi
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
                import dev.kolibrium.api.ksp.annotations.GenerateApi
                @GenerateApi
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
    fun `Same apiName in different packages is allowed`() {
        val spec1 =
            kotlin(
                "TestApiSpec1.kt",
                """
                package dev.kolibrium.api.ksp.test.one
                import dev.kolibrium.api.core.ApiSpec
                import dev.kolibrium.api.ksp.annotations.GenerateApi
                @GenerateApi
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
                import dev.kolibrium.api.ksp.annotations.GenerateApi
                @GenerateApi
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
                object GetUsersRequest
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
                object GetItemsRequest
                """.trimIndent(),
            )
        val kotlinCompilation = getCompilation(spec1, spec2, request1, request2)
        val compilation = kotlinCompilation.compile()
        compilation.exitCode shouldBe OK
        kotlinCompilation.getGeneratedSource("TestClient.kt")
        kotlinCompilation.getGeneratedSource("TestTestHarness.kt")
    }

    @Test
    fun `GenerateApi on class not extending ApiSpec rejected`() {
        val source =
            kotlin(
                "NotAnApiSpec.kt",
                """
                package dev.kolibrium.api.ksp.test
                import dev.kolibrium.api.ksp.annotations.GenerateApi
                @GenerateApi
                object NotAnApiSpec {
                    val baseUrl = "https://test.api"
                }
                """.trimIndent(),
            )
        val compilation = getCompilation(source).compile()
        compilation.exitCode shouldBe COMPILATION_ERROR
        compilation.messages shouldContain "annotated with @GenerateApi but does not extend"
    }
}

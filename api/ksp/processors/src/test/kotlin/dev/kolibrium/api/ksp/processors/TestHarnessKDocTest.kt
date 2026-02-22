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
import io.kotest.matchers.string.shouldNotContain
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Test

class TestHarnessKDocTest : ApiBaseTest() {
    @Test
    fun `Test harness functions get KDoc when generateKDoc is true`() {
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
        source shouldContain "Runs a test against the Test API client."
        source shouldContain "Runs a test against the Test API client with setUp and tearDown phases."
    }

    @Test
    fun `generateKDoc = false suppresses KDoc on test harness functions`() {
        val noKDocApiSpec =
            kotlin(
                "NoKDocApiSpec.kt",
                """
                package dev.kolibrium.api.ksp.test
                import dev.kolibrium.api.core.ApiSpec
                import dev.kolibrium.api.ksp.annotations.GenerateApi
                @GenerateApi(generateKDoc = false)
                object NoKDocApiSpec : ApiSpec() {
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
        val kotlinCompilation = getCompilation(noKDocApiSpec, request)
        val compilation = kotlinCompilation.compile()
        compilation.exitCode shouldBe OK
        val source = kotlinCompilation.getGeneratedSource("NoKDocTestHarness.kt")
        source shouldNotContain "Runs a test against"
    }
}

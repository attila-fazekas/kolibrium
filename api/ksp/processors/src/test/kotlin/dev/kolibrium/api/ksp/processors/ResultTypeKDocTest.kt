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

class ResultTypeKDocTest : ApiBaseTest() {
    @Test
    fun `Sealed result type and variants get KDoc`() {
        val request =
            kotlin(
                "Requests.kt",
                """
                package dev.kolibrium.api.ksp.test.models
                import dev.kolibrium.api.ksp.annotations.*
                import kotlinx.serialization.Serializable
                @Serializable
                data class LoginResponse(val token: String)
                @Serializable
                data class ErrorResponse(val message: String)
                @POST("/login")
                @Returns(success = LoginResponse::class, error = ErrorResponse::class)
                @Serializable
                object LoginRequest
                """.trimIndent(),
            )
        val kotlinCompilation = getCompilation(validApiSpec, request)
        val compilation = kotlinCompilation.compile()
        compilation.exitCode shouldBe OK
        val source = kotlinCompilation.getGeneratedSource("TestClient.kt")
        source shouldContain "Sealed result type for the Login endpoint."
        source shouldContain "Returns this as [Success] or throws [IllegalStateException] if this is [Error]."
        source shouldContain "Returns this as [Error] or throws [IllegalStateException] if this is [Success]."
        source shouldContain "Successful response with a [LoginResponse] body."
        source shouldContain "Error response with a [ErrorResponse] body."
    }
}

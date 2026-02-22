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
import com.tschuchort.compiletesting.kspSourcesDir
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Test

class NoOpBehaviourTest : ApiBaseTest() {
    @Test
    fun `No ApiSpec in source`() {
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
    fun `ApiSpec present but no request classes in scan package`() {
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

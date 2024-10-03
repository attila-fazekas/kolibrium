/*
 * Copyright 2023-2024 Attila Fazekas & contributors
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

package dev.kolibrium.ksp.processors

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.kspSourcesDir
import com.tschuchort.compiletesting.symbolProcessorProviders
import io.kotest.matchers.shouldBe
import org.intellij.lang.annotations.Language
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import java.io.File

open class ProcessorBaseTest {
    protected fun getCompilation(
        path: File,
        vararg sourceFiles: SourceFile,
    ) = KotlinCompilation().apply {
        workingDir = path.absoluteFile
        inheritClassPath = true
        sources = sourceFiles.asList()
        symbolProcessorProviders = listOf(PageProcessorProvider(), ResourceProcessorProvider())
        verbose = false
    }

    protected fun verifyExitCode(
        result: KotlinCompilation.Result,
        exitCode: KotlinCompilation.ExitCode,
    ) = result.exitCode shouldBe exitCode

    protected fun assertSourceEquals(
        @Language("kotlin") expected: String,
        actualFileName: String = "KolibriumTestPage.kt",
        compilation: KotlinCompilation,
    ) = compilation.getGeneratedSource(actualFileName).trimIndent() shouldBe expected.trimIndent()

    private fun KotlinCompilation.getGeneratedSource(fileName: String) =
        kspSourcesDir.walkTopDown().first {
            it.name == fileName
        }.readText()
}

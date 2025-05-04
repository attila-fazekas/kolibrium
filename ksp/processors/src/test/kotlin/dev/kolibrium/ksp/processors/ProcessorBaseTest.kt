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

package dev.kolibrium.ksp.processors

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.SourceFile.Companion.kotlin
import com.tschuchort.compiletesting.kspProcessorOptions
import com.tschuchort.compiletesting.kspSourcesDir
import com.tschuchort.compiletesting.symbolProcessorProviders
import com.tschuchort.compiletesting.useKsp2
import io.kotest.matchers.shouldBe
import org.intellij.lang.annotations.Language
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi

open class ProcessorBaseTest {
    private val pageAnnotation =
        kotlin(
            "PageDsl.kt",
            """
            package dev.kolibrium.ksp.annotations

            @Retention(AnnotationRetention.SOURCE)
            @Target(AnnotationTarget.CLASS)
            public annotation class PageDsl(
                val value: String = "",
            )
            """.trimIndent(),
        )

    protected fun getCompilation(
        vararg sourceFiles: SourceFile,
        useDsl: Boolean = false,
    ) = KotlinCompilation().apply {
        sources = listOf(pageAnnotation, *sourceFiles)
        useKsp2()
        symbolProcessorProviders = mutableListOf(PageProcessorProvider())
        inheritClassPath = true
        verbose = true
        kspProcessorOptions = mutableMapOf("kolibriumKsp.useDsl" to useDsl.toString())
    }

    protected fun assertSourceEquals(
        @Language("kotlin") expected: String,
        actualFileName: String,
        compilation: KotlinCompilation,
    ) = compilation.getGeneratedSource(actualFileName).trimIndent() shouldBe expected.trimIndent()

    private fun KotlinCompilation.getGeneratedSource(fileName: String) =
        kspSourcesDir
            .walkTopDown()
            .firstOrNull { it.name == fileName }
            ?.readText()
            ?: throw IllegalArgumentException(
                "Unable to find $fileName",
            )
}

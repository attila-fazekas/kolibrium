/*
 * Copyright 2023-2026 Attila Fazekas & contributors
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

package dev.kolibrium.selenium.ksp.processors

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.SourceFile.Companion.kotlin
import com.tschuchort.compiletesting.kspSourcesDir
import com.tschuchort.compiletesting.symbolProcessorProviders
import com.tschuchort.compiletesting.useKsp2

open class BaseTest {
    protected val validSite =
        kotlin(
            "TestSite.kt",
            """
            package dev.kolibrium.selenium.ksp.test
            import dev.kolibrium.selenium.core.SeleniumSite
            import dev.kolibrium.selenium.ksp.annotations.GenerateTestHarness
            @GenerateTestHarness
            object TestSite : SeleniumSite(baseUrl = "https://example.com")
            """.trimIndent(),
        )

    protected fun getCompilation(
        vararg sourceFiles: SourceFile,
    ) = KotlinCompilation().apply {
        sources = listOf(*sourceFiles)
        useKsp2()
        symbolProcessorProviders = mutableListOf(TestHarnessProcessorProvider())
        inheritClassPath = true
        verbose = true
        kotlincArguments = listOf("-Xcontext-parameters")
    }

    protected fun KotlinCompilation.getGeneratedSource(fileName: String) =
        kspSourcesDir
            .walkTopDown()
            .firstOrNull { it.name == fileName }
            ?.readText()
            ?: throw IllegalArgumentException(
                "Unable to find $fileName",
            )
}

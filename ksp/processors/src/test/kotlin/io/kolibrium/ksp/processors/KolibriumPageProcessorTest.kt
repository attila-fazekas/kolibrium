/*
 * Copyright 2023 Attila Fazekas
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

@file:Suppress("LongMethod")

package io.kolibrium.ksp.processors

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.kspSourcesDir
import com.tschuchort.compiletesting.symbolProcessorProviders
import io.kotest.matchers.shouldBe
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class KolibriumPageProcessorTest {

    @Test
    fun `enum class annotated with KolibriumPage and enum entries annotated with locators`(@TempDir path: File) {
        val sourceFile = SourceFile.kotlin(
            "KolibriumTestPage.kt",
            """
            package io.kolibrium.ksp.processors.test  

            import io.kolibrium.ksp.annotations.*

            @KolibriumPage
            enum class KolibriumTestPage {
                @ClassName("className")
                entry1,

                @Css("css")
                entry2,

                @Id("id")
                entry3,

                @LinkText("linkText")
                entry4,

                @Name("name")
                entry5,

                @PartialLinkText("partialLinkText")
                entry6,

                @TagName("tagName")
                entry7,

                @Xpath("xpath")
                entry8
            }
            """.trimIndent()
        )

        val compilation = getCompilation(path, sourceFile)
        verifyExitCode(compilation.compile(), KotlinCompilation.ExitCode.OK)

        assertSourceEquals(
            """
                package io.kolibrium.ksp.processors.test.generated

                import arrow.core.Either
                import io.kolibrium.core.Error
                import io.kolibrium.core.className
                import io.kolibrium.core.css
                import io.kolibrium.core.getValueOrFail
                import io.kolibrium.core.id
                import io.kolibrium.core.linkText
                import io.kolibrium.core.name
                import io.kolibrium.core.partialLinkText
                import io.kolibrium.core.tagName
                import io.kolibrium.core.xpath
                import org.openqa.selenium.WebDriver
                import org.openqa.selenium.WebElement
                
                context(WebDriver)
                public class KolibriumTestPage {
                  private val _entry1: Either<Error, WebElement> by className<WebElement>("className")

                  public val entry1: WebElement
                    get() = getValueOrFail(_entry1)
                
                  private val _entry2: Either<Error, WebElement> by css<WebElement>("css")

                  public val entry2: WebElement
                    get() = getValueOrFail(_entry2)
                
                  private val _entry3: Either<Error, WebElement> by id<WebElement>("id")

                  public val entry3: WebElement
                    get() = getValueOrFail(_entry3)
                
                  private val _entry4: Either<Error, WebElement> by linkText<WebElement>("linkText")

                  public val entry4: WebElement
                    get() = getValueOrFail(_entry4)
                
                  private val _entry5: Either<Error, WebElement> by name<WebElement>("name")

                  public val entry5: WebElement
                    get() = getValueOrFail(_entry5)
                
                  private val _entry6: Either<Error, WebElement> by partialLinkText<WebElement>("partialLinkText")

                  public val entry6: WebElement
                    get() = getValueOrFail(_entry6)
                
                  private val _entry7: Either<Error, WebElement> by tagName<WebElement>("tagName")

                  public val entry7: WebElement
                    get() = getValueOrFail(_entry7)
                
                  private val _entry8: Either<Error, WebElement> by xpath<WebElement>("xpath")

                  public val entry8: WebElement
                    get() = getValueOrFail(_entry8)
                }
            """.trimIndent(),
            compilation = compilation
        )
    }
}

private fun getCompilation(path: File, vararg sourceFiles: SourceFile) = KotlinCompilation().apply {
    workingDir = path.absoluteFile
    inheritClassPath = true
    sources = sourceFiles.asList()
    symbolProcessorProviders = listOf(KolibriumPageProcessorProvider())
    verbose = false
}

private fun verifyExitCode(result: KotlinCompilation.Result, exitCode: KotlinCompilation.ExitCode) =
    result.exitCode shouldBe exitCode

private fun assertSourceEquals(
    @Language("kotlin") expected: String,
    actual: String = "KolibriumTestPage.kt",
    compilation: KotlinCompilation
) = compilation.getGeneratedSource(actual).trimIndent() shouldBe expected.trimIndent()

private fun KotlinCompilation.getGeneratedSource(fileName: String) = kspSourcesDir.walkTopDown().first {
    it.name == fileName
}.readText()

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

package dev.kolibrium.dsl.selenium.actions

import com.titusfortner.logging.SeleniumLogger
import dev.kolibrium.dsl.selenium.creation.Arguments.Chrome.disable_search_engine_choice_screen
import dev.kolibrium.dsl.selenium.creation.chromeDriver
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.openqa.selenium.By

class ScrollTest {
    private val driver =
        chromeDriver {
            options {
                arguments {
                    +disable_search_engine_choice_screen
                    windowSize {
                        width = 300
                    }
                }
            }
        }

    companion object {
        @JvmStatic
        @BeforeAll
        fun enableLogging() {
            SeleniumLogger.enable()
        }
    }

    @BeforeEach
    fun openUrl() {
        driver["https://www.selenium.dev/selenium/web/scrolling_tests/frame_with_nested_scrolling_frame_out_of_view.html"]
    }

    @AfterEach
    fun quitDriver() {
        driver.quit()
    }

    @Test
    fun scrollTest(): Unit =
        with(driver) {
            val iframe = findElement(By.tagName("iframe"))

            actions {
                scrollTo(iframe)
            }

            switchTo().frame(iframe)

            val h1 = findElement(By.tagName("h1"))

            h1.text shouldBe "This is a scrolling frame test"
        }
}

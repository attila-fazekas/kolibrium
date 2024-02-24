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

package dev.kolibrium.selenium.external

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions

class ButtonsTest {
    private val url = "https://eviltester.github.io/synchole/buttons.html"

    private lateinit var driver: WebDriver

    companion object {
        @JvmStatic
        @BeforeAll
        fun printExecutionPath() {
//            SeleniumLogger.enable("RemoteWebDriver")
        }
    }

    @BeforeEach
    fun setUp() {
        driver =
            ChromeDriver(
                ChromeOptions().addArguments("--headless"),
            )
        driver[url]
    }

    @AfterEach
    fun tearDown() {
        driver.quit()
    }

    @Test
    fun testEasyButtons() {
        with(driver) {
            with(ButtonsPage()) {
                easyButton1.click()
                easyButton2.click()
                easyButton3.click()
                easyButton4.click()

                easyMessage.text shouldBe "All Buttons Clicked"
            }
        }
    }

    @Test
    fun testHardButtons() {
        with(driver) {
            with(ButtonsPage()) {
                hardButton1.click()
                hardButton2.click()
                hardButton3.click()
                hardButton4.click()

                hardMessage.text shouldBe "All Buttons Clicked"
            }
        }
    }
}

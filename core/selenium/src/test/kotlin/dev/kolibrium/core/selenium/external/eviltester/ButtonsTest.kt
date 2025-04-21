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

package dev.kolibrium.core.selenium.external.eviltester

import dev.kolibrium.core.selenium.cssSelector
import dev.kolibrium.core.selenium.id
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
        fun enableLogging() {
//            SeleniumLogger.enable("RemoteWebDriver")
        }
    }

    @BeforeEach
    fun setUp() {
        driver =
            ChromeDriver(
                ChromeOptions().addArguments("--headless=new", "--disable-search-engine-choice-screen"),
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
            val easyButton1 by cssSelector("#easy00")
            easyButton1.click()

            val easyButton2 by cssSelector("#easy01")
            easyButton2.click()

            val easyButton3 by cssSelector("#easy02")
            easyButton3.click()

            val easyButton4 by cssSelector("#easy03")
            easyButton4.click()

            val easyMessage by id("easybuttonmessage")

            easyMessage.text shouldBe "All Buttons Clicked"
        }
    }

    @Test
    fun testEasyButtons_pageObject() {
        with(ButtonsPage(driver)) {
            easyButton1.click()
            easyButton2.click()
            easyButton3.click()
            easyButton4.click()

            easyMessage.text shouldBe "All Buttons Clicked"
        }
    }

    @Test
    fun testHardButtons() {
        with(driver) {
            val hardButton1 by cssSelector("#button00") {
                isEnabled
            }
            hardButton1.click()

            val hardButton2 by cssSelector("#button01") {
                isEnabled
            }
            hardButton2.click()

            val hardButton3 by cssSelector("#button02") {
                isEnabled
            }
            hardButton3.click()

            val hardButton4 by cssSelector("#button03") {
                isEnabled
            }
            hardButton4.click()

            val hardMessage by id("buttonmessage")

            hardMessage.text shouldBe "All Buttons Clicked"
        }
    }

    @Test
    fun testHardButtons_pageObject() {
        with(ButtonsPage(driver)) {
            hardButton1.click()
            hardButton2.click()
            hardButton3.click()
            hardButton4.click()

            hardMessage.text shouldBe "All Buttons Clicked"
        }
    }
}

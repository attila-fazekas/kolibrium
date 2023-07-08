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

package io.kolibrium.core

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.support.ui.Select
import java.nio.file.Paths

class KolibriumLocatorDelegateTest {

    private lateinit var driver: WebDriver

    private val homePage =
        Paths.get("").toAbsolutePath().parent.resolve("pages/home.html").toUri().toString()

    @BeforeEach
    fun setup() {
        driver = ChromeDriver(
            ChromeOptions().addArguments("--headless=new")
        )
    }

    @AfterEach
    fun teardown() {
        driver.quit()
    }

    @Test
    fun `className - WebElement`() {
        driver.get(homePage)
        with(driver) {
            with(HomePage()) {
                header.text shouldBe "Kolibrium"
            }
        }
    }

    @Test
    fun `css - WebElement`() {
        driver.get(homePage)
        with(driver) {
            with(HomePage()) {
                name.getAttribute("value") shouldBe "Enter your name"
            }
        }
    }

    @Test
    fun `id - WebElement`() {
        driver.get(homePage)
        with(driver) {
            with(HomePage()) {
                phone.getAttribute("value") shouldBe "Enter your phone number"
            }
        }
    }

    @Test
    fun `idOrName - WebElement`() {
        driver.get(homePage)
        with(driver) {
            with(HomePage()) {
                email.getAttribute("value") shouldBe "Enter your email"
            }
        }
    }

    @Test
    fun `linkText - WebElement`() {
        driver.get(homePage)
        with(driver) {
            with(HomePage()) {
                fbLink.text shouldBe "Facebook"
            }
        }
    }

    @Test
    fun `name - WebElement`() {
        driver.get(homePage)
        with(driver) {
            with(HomePage()) {
                phoneName.getAttribute("value") shouldBe "Enter your phone number"
            }
        }
    }

    @Test
    fun `partialLinkText - WebElement`() {
        driver.get(homePage)
        with(driver) {
            with(HomePage()) {
                clickHereLink.text shouldBe "Click here"
            }
        }
    }

    @Test
    fun `tagName - WebElement`() {
        driver.get(homePage)
        with(driver) {
            with(HomePage()) {
                a1TagName.text shouldBe "Kolibrium"
            }
        }
    }

    @Test
    fun `xpath - WebElement`() {
        driver.get(homePage)
        with(driver) {
            with(HomePage()) {
                nameXpath.getAttribute("value") shouldBe "Enter your name"
            }
        }
    }

    // WebElements

    @Test
    fun `className - WebElements`() {
        driver.get(homePage)
        with(driver) {
            with(HomePage()) {
                links.size shouldBe 6
            }
        }
    }

    @Test
    fun `css - WebElements`() {
        driver.get(homePage)
        with(driver) {
            with(HomePage()) {
                linksCss.size shouldBe 6
            }
        }
    }

    @Test
    fun `linkText - WebElements`() {
        driver.get(homePage)
        with(driver) {
            with(HomePage()) {
                fbLinks.size shouldBe 2
            }
        }
    }

    @Test
    fun `name - WebElements`() {
        driver.get(homePage)
        with(driver) {
            with(HomePage()) {
                selects.forEach {
                    Select(it).options.size shouldBe 3
                }
            }
        }
    }

    @Test
    fun `partialLinkText - WebElements`() {
        driver.get(homePage)
        with(driver) {
            with(HomePage()) {
                fbPartialLinks.size shouldBe 3
            }
        }
    }

    @Test
    fun `tagName - WebElements`() {
        driver.get(homePage)
        with(driver) {
            with(HomePage()) {
                linksTagname.size shouldBe 6
            }
        }
    }

    @Test
    fun `xpath - WebElements`() {
        driver.get(homePage)
        with(driver) {
            with(HomePage()) {
                linksXpath.size shouldBe 6
            }
        }
    }
}

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

    private fun homePage(block: HomePage.() -> Unit) {
        driver.get(homePage)
        with(driver) {
            with(HomePage()) {
                block()
            }
        }
    }

    @BeforeEach
    fun setUp() {
        driver = ChromeDriver(
            ChromeOptions().addArguments("--headless=new")
        )
    }

    @AfterEach
    fun tearDown() {
        driver.quit()
    }

    @Test
    fun `className - WebElement`() = homePage {
        header.text shouldBe "Kolibrium"
    }

    @Test
    fun `css - WebElement`() = homePage {
        name.getAttribute("value") shouldBe "Enter your name"
    }

    @Test
    fun `id - WebElement`() = homePage {
        phone.getAttribute("value") shouldBe "Enter your phone number"
    }

    @Test
    fun `idOrName - WebElement`() = homePage {
        email.getAttribute("value") shouldBe "Enter your email"
    }

    @Test
    fun `linkText - WebElement`() = homePage {
        fbLink.text shouldBe "Facebook"
    }

    @Test
    fun `name - WebElement`() = homePage {
        phoneName.getAttribute("value") shouldBe "Enter your phone number"
    }

    @Test
    fun `partialLinkText - WebElement`() = homePage {
        clickHereLink.text shouldBe "Click here"
    }

    @Test
    fun `tagName - WebElement`() = homePage {
        a1TagName.text shouldBe "Kolibrium"
    }

    @Test
    fun `xpath - WebElement`() = homePage {
        nameXpath.getAttribute("value") shouldBe "Enter your name"
    }

    // WebElements

    @Test
    fun `className - WebElements`() = homePage {
        links.size shouldBe 6
    }

    @Test
    fun `css - WebElements`() = homePage {
        linksCss.size shouldBe 6
    }

    @Test
    fun `linkText - WebElements`() = homePage {
        fbLinks.size shouldBe 2
    }

    @Test
    fun `name - WebElements`() = homePage {
        selects.forEach {
            Select(it).options.size shouldBe 3
        }
    }

    @Test
    fun `partialLinkText - WebElements`() = homePage {
        fbPartialLinks.size shouldBe 3
    }

    @Test
    fun `tagName - WebElements`() = homePage {
        linksTagname.size shouldBe 6
    }

    @Test
    fun `xpath - WebElements`() = homePage {
        linksXpath.size shouldBe 6
    }
}

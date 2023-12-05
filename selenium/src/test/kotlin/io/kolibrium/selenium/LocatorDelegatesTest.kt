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

package io.kolibrium.selenium

import io.kolibrium.selenium.pages.ButtonPage
import io.kolibrium.selenium.pages.HomePage
import io.kolibrium.selenium.pages.ImagesPage
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.support.ui.Select
import java.nio.file.Paths

private fun getPage(pageName: String) = Paths.get("").toAbsolutePath()
    .parent.resolve("pages/$pageName.html").toUri().toString()

private val buttonPage = getPage("button")
private val homePage = getPage("home")
private val imagesPage = getPage("images")

class LocatorDelegatesTest {

    private lateinit var driver: WebDriver

    private fun buttonPage(block: ButtonPage.() -> Unit) {
        driver.get(buttonPage)
        with(driver) {
            with(ButtonPage()) {
                block()
            }
        }
    }

    private fun homePage(block: HomePage.() -> Unit) {
        driver.get(homePage)
        with(driver) {
            with(HomePage()) {
                block()
            }
        }
    }

    private fun imagesPage(block: ImagesPage.() -> Unit) {
        driver.get(imagesPage)
        with(driver) {
            with(ImagesPage()) {
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

    @Test
    fun `id - button with firework`() = buttonPage {
        button.click()
        firework.size.width shouldBe 10
        firework.size.height shouldBe 10
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
        linksTagName.size shouldBe 6
    }

    @Test
    fun `xpath - WebElements`() = homePage {
        linksXpath.size shouldBe 6
    }

// WebElements with size

    @Test
    fun `name - WebElements with number`() = imagesPage {
        images.forEachIndexed { index, element ->
            element.getAttribute("alt") shouldBe "kodee$index"
        }
    }
}

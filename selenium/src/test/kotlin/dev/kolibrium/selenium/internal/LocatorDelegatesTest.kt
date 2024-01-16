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

package dev.kolibrium.selenium.internal

import dev.kolibrium.selenium.internal.pages.ButtonDelayedPage
import dev.kolibrium.selenium.internal.pages.ButtonElementClickInterceptedExceptionPage
import dev.kolibrium.selenium.internal.pages.ButtonStaleElementReferenceExceptionPage
import dev.kolibrium.selenium.internal.pages.ButtonsPage
import dev.kolibrium.selenium.internal.pages.ElementNotInteractableExceptionPage
import dev.kolibrium.selenium.internal.pages.HomePage
import dev.kolibrium.selenium.internal.pages.ImagesPage
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.support.ui.Select
import java.nio.file.Paths
import java.util.concurrent.TimeUnit.MILLISECONDS

private fun getPage(pageName: String) =
    Paths.get("").toAbsolutePath()
        .parent.resolve("pages/$pageName.html").toUri().toString()

private val buttonPage1 = getPage("button_delayed")
private val buttonPage2 = getPage("button_exception1")
private val buttonPage3 = getPage("button_exception2")
private val buttonPage4 = getPage("input_exception")
private val buttonsPage = getPage("buttons")
private val homePage = getPage("home")
private val imagesPage = getPage("images")

class LocatorDelegatesTest {
    private lateinit var driver: WebDriver

    private fun buttonDelayedPage(block: ButtonDelayedPage.() -> Unit) {
        driver.get(buttonPage1)
        with(driver) {
            with(ButtonDelayedPage()) {
                block()
            }
        }
    }

    private fun buttonStaleElementReferenceExceptionPage(block: ButtonStaleElementReferenceExceptionPage.() -> Unit) {
        driver.get(buttonPage2)
        with(driver) {
            with(ButtonStaleElementReferenceExceptionPage()) {
                block()
            }
        }
    }

    private fun buttonElementClickInterceptedExceptionPage(block: ButtonElementClickInterceptedExceptionPage.() -> Unit) {
        driver.get(buttonPage3)
        with(driver) {
            with(ButtonElementClickInterceptedExceptionPage()) {
                block()
            }
        }
    }

    private fun inputElementNotInteractableExceptionPage(block: ElementNotInteractableExceptionPage.() -> Unit) {
        driver.get(buttonPage4)
        with(driver) {
            with(ElementNotInteractableExceptionPage()) {
                block()
            }
        }
    }

    private fun buttonsPage(block: ButtonsPage.() -> Unit) {
        driver.get(buttonsPage)
        with(driver) {
            with(ButtonsPage()) {
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
                ChromeOptions().addArguments("--headless=new"),
            )
    }

    @AfterEach
    fun tearDown() {
        driver.quit()
    }

    @Test
    fun `className - WebElement`() =
        homePage {
            header.text shouldBe "Kolibrium"
        }

    @Test
    fun `css - WebElement`() =
        homePage {
            name.getAttribute("value") shouldBe "Enter your name"
        }

    @Test
    fun `id - WebElement`() =
        homePage {
            phone.getAttribute("value") shouldBe "Enter your phone number"
        }

    @Test
    fun `idOrName - WebElement`() =
        homePage {
            email.getAttribute("value") shouldBe "Enter your email"
        }

    @Test
    fun `linkText - WebElement`() =
        homePage {
            fbLink.text shouldBe "Facebook"
        }

    @Test
    fun `name - WebElement`() =
        homePage {
            phoneName.getAttribute("value") shouldBe "Enter your phone number"
        }

    @Test
    fun `partialLinkText - WebElement`() =
        homePage {
            clickHereLink.text shouldBe "Click here"
        }

    @Test
    fun `tagName - WebElement`() =
        homePage {
            a1TagName.text shouldBe "Kolibrium"
        }

    @Test
    fun `xpath - WebElement`() =
        homePage {
            nameXpath.getAttribute("value") shouldBe "Enter your name"
        }

    @Test
    fun `button delayed`() =
        buttonDelayedPage {
            button.click()
            message.text shouldBe "Button clicked!"
        }

    @Test
    fun `buttons delayed`() =
        buttonsPage {
            with(driver) {
                with(ButtonsPage()) {
                    button1.click()
                    button2.click()
                    button3.click()
                    button4.click()

                    result.text shouldBe "All buttons clicked!"
                }
            }
        }

    @Test
    @Disabled
    fun `throws StaleElementReferenceException`() =
        buttonStaleElementReferenceExceptionPage {
            button.click()
            firework.size.width shouldBe 10
            firework.size.height shouldBe 10
        }

    @Test
    @Disabled
    fun `throws ElementClickInterceptedException`() =
        buttonElementClickInterceptedExceptionPage {
            button.click()
            MILLISECONDS.sleep(250)
            button.click()
            button.click()
            button.text shouldBe "Clicked: 3"
        }

    @Test
    @Disabled
    fun `throws ElementNotInteractableException`() =
        inputElementNotInteractableExceptionPage {
            input.sendKeys("test")
            input.getAttribute("value") shouldBe "test"
        }

// WebElements

    @Test
    fun `className - WebElements`() =
        homePage {
            links.size shouldBe 6
        }

    @Test
    fun `css - WebElements`() =
        homePage {
            linksCss.size shouldBe 6
        }

    @Test
    fun `linkText - WebElements`() =
        homePage {
            fbLinks.size shouldBe 2
        }

    @Test
    fun `name - WebElements`() =
        homePage {
            selects.forEach {
                Select(it).options.size shouldBe 3
            }
        }

    @Test
    fun `partialLinkText - WebElements`() =
        homePage {
            fbPartialLinks.size shouldBe 3
        }

    @Test
    fun `tagName - WebElements`() =
        homePage {
            linksTagName.size shouldBe 6
        }

    @Test
    fun `xpath - WebElements`() =
        homePage {
            linksXpath.size shouldBe 6
        }

// WebElements with size

    @Test
    fun `name - WebElements with number`() =
        imagesPage {
            images.forEachIndexed { index, element ->
                element.getAttribute("alt") shouldBe "kodee$index"
            }
        }
}

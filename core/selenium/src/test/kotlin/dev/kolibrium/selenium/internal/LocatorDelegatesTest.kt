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

package dev.kolibrium.selenium.internal

import dev.kolibrium.selenium.className
import dev.kolibrium.selenium.internal.pages.ButtonsPage
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.openqa.selenium.support.ui.Select
import java.util.concurrent.TimeUnit.MILLISECONDS

class LocatorDelegatesTest : BaseTest() {
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
    fun `xPath - WebElement`() =
        homePage {
            nameXPath.getAttribute("value") shouldBe "Enter your name"
        }

    @Test
    fun `button delayed`() =
        buttonDelayedPage {
            button.click()
            message.text shouldBe "Button clicked!"
        }

    @Test
    fun `custom complex condition`() =
        searchInputPage {
            makeVisibleButton.click()

            Thread.sleep(1000)

            addPlaceholderButton.click()

            Thread.sleep(1000)

            enableInputButton.click()

            Thread.sleep(1000)

            searchInput.sendKeys("Kolibrium")

            searchInput.getAttribute("value") shouldBe "Kolibrium"
        }

    @Test
    fun `state-dependent conditions`() =
        dynamicElementPage {
            makeVisibleButton.click()

            Thread.sleep(1000)

            removeLoadingButton.click()

            Thread.sleep(1000)

            updateTextButton.click()

            Thread.sleep(1000)

            dynamicElement.text shouldBe "Content is Ready"
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
    fun `dataTest should work with quotes`() =
        dataTestPage {
            element1.text shouldBe "Element 1 (no quotes)"
            element2.text shouldBe "Element 2 (single quote)"
            element3.text shouldBe "Element 3 (double quote)"
            element4.text shouldBe "Element 4 (both quotes)"
        }

    @Test
    fun `throws StaleElementReferenceException - single element`() =
        staleElementReferenceExceptionSingleElementPage {
            button.click()
            firework.size.width shouldBe 10
            firework.size.height shouldBe 10
        }

    @Test
    fun `throws StaleElementReferenceException - multiple elements`() =
        staleElementReferenceExceptionMultipleElementsPage {
            buttons.forEach {
                it.click()
            }
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
    fun `xPath - WebElements`() =
        homePage {
            linksXPath.size shouldBe 6
        }

    @Test
    fun `using locator delegate functions with WebElement`() =
        tutorial {
            val byClassName by singleLocators.className("by-class-name")

            byClassName.text shouldBe "Locate by Class Name"
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

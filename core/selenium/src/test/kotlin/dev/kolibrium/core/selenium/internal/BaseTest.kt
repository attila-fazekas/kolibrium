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

package dev.kolibrium.core.selenium.internal

import dev.kolibrium.core.selenium.internal.pages.ButtonsPage
import dev.kolibrium.core.selenium.internal.pages.DataTestPage
import dev.kolibrium.core.selenium.internal.pages.DynamicElementPage
import dev.kolibrium.core.selenium.internal.pages.ElementNotInteractableExceptionPage
import dev.kolibrium.core.selenium.internal.pages.HomePage
import dev.kolibrium.core.selenium.internal.pages.ImagesPage
import dev.kolibrium.core.selenium.internal.pages.SearchInputPage
import dev.kolibrium.core.selenium.internal.pages.StaleElementReferenceExceptionSingleElementPage
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import java.nio.file.Paths

private fun getPage(pageName: String) =
    Paths
        .get("")
        .toAbsolutePath()
        .parent
        .parent
        .resolve("pages/$pageName.html")
        .toUri()
        .toString()

private val buttonPage1 = getPage("button_delayed")
private val buttonPage3 = getPage("button_exception2")
private val buttonPage4 = getPage("input_exception")
private val buttonsPage = getPage("buttons")
private val homePage = getPage("home")
private val imagesPage = getPage("images")
private val search_input = getPage("search_input")
private val dataTest = getPage("dataTest")
private val dynamic_element = getPage("dynamic_element")
private val staleElementReferenceException_singleElement = getPage("StaleElementReferenceException_SingleElement")
private val staleElementReferenceException_multipleElements = getPage("StaleElementReferenceException_MultipleElements")
private val tutorial = getPage("tutorial")

open class BaseTest {
    protected lateinit var driver: WebDriver

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
    }

    @AfterEach
    fun tearDown() {
        driver.quit()
    }

    protected fun buttonDelayedPage(block: dev.kolibrium.core.selenium.internal.pages.ButtonDelayedPage.() -> Unit) {
        with(driver) {
            get(buttonPage1)
            with(
                dev.kolibrium.core.selenium.internal.pages
                    .ButtonDelayedPage(),
            ) {
                block()
            }
        }
    }

    protected fun staleElementReferenceExceptionSingleElementPage(block: StaleElementReferenceExceptionSingleElementPage.() -> Unit) {
        with(driver) {
            get(staleElementReferenceException_singleElement)
            with(StaleElementReferenceExceptionSingleElementPage()) {
                block()
            }
        }
    }

    protected fun staleElementReferenceExceptionMultipleElementsPage(block: dev.kolibrium.core.selenium.internal.pages.StaleElementReferenceExceptionMultipleElementsPage.() -> Unit) {
        with(driver) {
            get(staleElementReferenceException_multipleElements)
            with(
                dev.kolibrium.core.selenium.internal.pages
                    .StaleElementReferenceExceptionMultipleElementsPage(),
            ) {
                block()
            }
        }
    }

    protected fun buttonElementClickInterceptedExceptionPage(block: dev.kolibrium.core.selenium.internal.pages.ButtonElementClickInterceptedExceptionPage.() -> Unit) {
        with(driver) {
            get(buttonPage3)
            with(
                dev.kolibrium.core.selenium.internal.pages
                    .ButtonElementClickInterceptedExceptionPage(),
            ) {
                block()
            }
        }
    }

    protected fun inputElementNotInteractableExceptionPage(block: ElementNotInteractableExceptionPage.() -> Unit) {
        with(driver) {
            get(buttonPage4)
            with(ElementNotInteractableExceptionPage()) {
                block()
            }
        }
    }

    protected fun searchInputPage(block: SearchInputPage.() -> Unit) {
        with(driver) {
            get(search_input)
            with(SearchInputPage()) {
                block()
            }
        }
    }

    protected fun dynamicElementPage(block: DynamicElementPage.() -> Unit) {
        with(driver) {
            get(dynamic_element)
            with(DynamicElementPage()) {
                block()
            }
        }
    }

    protected fun dataTestPage(block: DataTestPage.() -> Unit) {
        with(driver) {
            get(dataTest)
            with(DataTestPage()) {
                block()
            }
        }
    }

    protected fun buttonsPage(block: ButtonsPage.() -> Unit) {
        with(driver) {
            get(buttonsPage)
            with(ButtonsPage()) {
                block()
            }
        }
    }

    protected fun homePage(block: HomePage.() -> Unit) {
        with(driver) {
            get(homePage)
            with(HomePage()) {
                block()
            }
        }
    }

    protected fun imagesPage(block: ImagesPage.() -> Unit) {
        with(driver) {
            get(imagesPage)
            with(ImagesPage()) {
                block()
            }
        }
    }

    protected fun tutorial(block: dev.kolibrium.core.selenium.internal.pages.TutorialPage.() -> Unit) {
        with(driver) {
            get(tutorial)
            with(
                dev.kolibrium.core.selenium.internal.pages
                    .TutorialPage(),
            ) {
                block()
            }
        }
    }
}

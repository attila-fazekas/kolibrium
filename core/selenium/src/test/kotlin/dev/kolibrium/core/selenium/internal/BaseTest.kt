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

import dev.kolibrium.core.selenium.internal.pages.ButtonDelayedPage
import dev.kolibrium.core.selenium.internal.pages.ButtonElementClickInterceptedExceptionPage
import dev.kolibrium.core.selenium.internal.pages.ButtonsPage
import dev.kolibrium.core.selenium.internal.pages.DataTestPage
import dev.kolibrium.core.selenium.internal.pages.DynamicElementPage
import dev.kolibrium.core.selenium.internal.pages.ElementNotInteractableExceptionPage
import dev.kolibrium.core.selenium.internal.pages.HomePage
import dev.kolibrium.core.selenium.internal.pages.ImagesPage
import dev.kolibrium.core.selenium.internal.pages.SearchInputPage
import dev.kolibrium.core.selenium.internal.pages.StaleElementReferenceExceptionMultipleElementsPage
import dev.kolibrium.core.selenium.internal.pages.StaleElementReferenceExceptionSingleElementPage
import dev.kolibrium.core.selenium.internal.pages.TutorialPage
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions

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
                ChromeOptions().addArguments(
                    "--headless=new",
                    "--disable-search-engine-choice-screen",
                ),
            )
    }

    @AfterEach
    fun tearDown() {
        driver.quit()
    }

    protected fun buttonDelayedPage(block: ButtonDelayedPage.() -> Unit) {
        with(driver) {
            with(
                ButtonDelayedPage(this),
            ) {
                block()
            }
        }
    }

    protected fun staleElementReferenceExceptionSingleElementPage(block: StaleElementReferenceExceptionSingleElementPage.() -> Unit) {
        with(driver) {
            with(StaleElementReferenceExceptionSingleElementPage(this)) {
                block()
            }
        }
    }

    protected fun staleElementReferenceExceptionMultipleElementsPage(block: StaleElementReferenceExceptionMultipleElementsPage.() -> Unit) {
        with(driver) {
            with(
                StaleElementReferenceExceptionMultipleElementsPage(this),
            ) {
                block()
            }
        }
    }

    protected fun buttonElementClickInterceptedExceptionPage(block: ButtonElementClickInterceptedExceptionPage.() -> Unit) {
        with(driver) {
            with(
                ButtonElementClickInterceptedExceptionPage(this),
            ) {
                block()
            }
        }
    }

    protected fun inputElementNotInteractableExceptionPage(block: ElementNotInteractableExceptionPage.() -> Unit) {
        with(driver) {
            with(ElementNotInteractableExceptionPage(this)) {
                block()
            }
        }
    }

    protected fun searchInputPage(block: SearchInputPage.() -> Unit) {
        with(driver) {
            with(SearchInputPage(this)) {
                block()
            }
        }
    }

    protected fun dynamicElementPage(block: DynamicElementPage.() -> Unit) {
        with(driver) {
            with(DynamicElementPage(this)) {
                block()
            }
        }
    }

    protected fun dataTestPage(block: DataTestPage.() -> Unit) {
        with(driver) {
            with(DataTestPage(this)) {
                block()
            }
        }
    }

    protected fun buttonsPage(block: ButtonsPage.() -> Unit) {
        with(driver) {
            with(ButtonsPage(this)) {
                block()
            }
        }
    }

    protected fun homePage(block: HomePage.() -> Unit) {
        with(driver) {
            with(HomePage(this)) {
                block()
            }
        }
    }

    protected fun imagesPage(block: ImagesPage.() -> Unit) {
        with(driver) {
            with(ImagesPage(this)) {
                block()
            }
        }
    }

    protected fun tutorial(block: TutorialPage.() -> Unit) {
        with(driver) {
            with(
                TutorialPage(this),
            ) {
                block()
            }
        }
    }
}

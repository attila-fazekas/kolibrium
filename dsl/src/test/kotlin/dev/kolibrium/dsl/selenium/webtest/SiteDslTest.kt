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

@file:Suppress("ktlint")

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

package dev.kolibrium.dsl.selenium.webtest

import dev.kolibrium.core.selenium.DriverProfile
import dev.kolibrium.dsl.PageEntry
import dev.kolibrium.dsl.selenium.creation.Arguments.Chrome.disable_search_engine_choice_screen
import dev.kolibrium.dsl.selenium.creation.Arguments.Chrome.incognito
import dev.kolibrium.dsl.selenium.creation.Preferences.Chromium.credentials_enable_service
import dev.kolibrium.dsl.selenium.creation.Preferences.Chromium.password_manager_enabled
import dev.kolibrium.dsl.selenium.creation.Preferences.Chromium.password_manager_leak_detection
import dev.kolibrium.dsl.selenium.creation.chromeDriver
import dev.kolibrium.dsl.selenium.webtest.pages.LoginPage
import dev.kolibrium.dsl.webTest
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver

class SiteDslTest() {
    companion object {
        @JvmStatic
        @BeforeAll
        fun enableLogging() {
//            SeleniumLogger.enable("RemoteWebDriver")
        }
    }

    @Test
    fun `no use of webTest`() {
        with(ChromeDriver()) {
            this.get("https://www.saucedemo.com")
            LoginPage().login()
            quit()
        }
    }

    @Test
    fun `webTest used with page creation through constructor`() = webTest(
        site = SauceDemo,
        keepBrowserOpen = false,
    ) {
        LoginPage(driver).login()
    }

    @Test
    fun `webTest used with PageEntry's open() function`() = webTest(
        site = SauceDemo,
        keepBrowserOpen = false,
    ) {
        open(::LoginPage) {
            login()
        }
    }

    @Test
    fun `sauceDemoTest used with PageEntry's open() function`() = sauceDemoTest(
        keepBrowserOpen = false,
    ) {
        open(::LoginPage) {
            login()
        }
    }

    @Test
    fun `sauceDemoTest used with PageEntry's open() function and chained() `() = sauceDemoTest(
        keepBrowserOpen = false,
    ) {
        open(::LoginPage) {
            login()
        }.on {
            goToCart()
        }.on {
            checkout()
        }
    }

    private fun sauceDemoTest(
        driverProfile: () -> WebDriver = {
            chromeDriver {
                options {
                    arguments {
                        +disable_search_engine_choice_screen
                        +incognito
                    }
                    experimentalOptions {
                        preferences {
                            pref(credentials_enable_service, false)
                            pref(password_manager_enabled, false)
                            pref(password_manager_leak_detection, false)
                        }
                    }
                }
            }
        },
        keepBrowserOpen: Boolean = false,
        block: context(SauceDemo) PageEntry<SauceDemo>.() -> Unit,
    ) = webTest(
        site = SauceDemo,
        driverProfile = driverProfile,
        keepBrowserOpen = keepBrowserOpen,
    ) {
        block()
    }
}

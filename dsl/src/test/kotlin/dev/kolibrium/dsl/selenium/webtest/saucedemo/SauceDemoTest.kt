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

package dev.kolibrium.dsl.selenium.webtest.saucedemo

import com.titusfortner.logging.SeleniumLogger
import dev.kolibrium.dsl.selenium.DriverFactory
import dev.kolibrium.dsl.selenium.PageEntry
import dev.kolibrium.dsl.selenium.chrome
import dev.kolibrium.dsl.selenium.creation.Arguments.Chrome.disable_search_engine_choice_screen
import dev.kolibrium.dsl.selenium.creation.Arguments.Chrome.incognito
import dev.kolibrium.dsl.selenium.creation.Preferences.Chromium.credentials_enable_service
import dev.kolibrium.dsl.selenium.creation.Preferences.Chromium.password_manager_enabled
import dev.kolibrium.dsl.selenium.creation.Preferences.Chromium.password_manager_leak_detection
import dev.kolibrium.dsl.selenium.creation.chromeDriver
import dev.kolibrium.dsl.selenium.webTest
import dev.kolibrium.dsl.selenium.webtest.saucedemo.Product.Backpack
import dev.kolibrium.dsl.selenium.webtest.saucedemo.Product.BikeLight
import dev.kolibrium.dsl.selenium.webtest.saucedemo.pages.InventoryPage
import dev.kolibrium.dsl.selenium.webtest.saucedemo.pages.LoginPage
import dev.kolibrium.dsl.selenium.webtest.saucedemo.pages.twitter.TwitterHomePage
import dev.kolibrium.dsl.selenium.webtest.saucedemo.pages.visitTwitter
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.openqa.selenium.Cookie
import org.openqa.selenium.chrome.ChromeDriver

class SauceDemoTest() {
    companion object {
        @JvmStatic
        @BeforeAll
        fun enableLogging() {
//            SeleniumLogger.enable("RemoteWebDriver")
        }
    }

    @Test
    fun `webTest used with PageEntry's open() function`() = webTest(
        site = SauceDemo,
        keepBrowserOpen = false,
        driverFactory = chrome(),
        prepare = { },
    ) { _: Unit ->
        open(::LoginPage) {
            login()
        }
    }

    @Test
    fun `sauceDemoTest used with PageEntry's open() function`() = sauceDemoTest(
        keepBrowserOpen = false,
    ) {
        val products = listOf(Backpack, BikeLight)

        open(::LoginPage) {
            login()
        }.on {
            verify { titleText() == "Swag Labs" }

            products.addToCart()

            goToCart()
        }.verify {
            val items = getItemsOnShoppingCart()
            items.size shouldBe products.size
            items.zip(products).forEach { (item, product) ->
                item.quantity() shouldBe 1
                item.name() shouldBe product.productName
                item.price() shouldBe product.price
            }
        }.on {
            checkout()
        }
    }

    @Test
    fun `authenticatedSauceDemoTest used with PageEntry's open() function and chained() `() = authenticatedSauceDemoTest(
        keepBrowserOpen = false,
    ) {
        val products = listOf(Backpack, BikeLight)

        open(::InventoryPage) {
            verify { titleText() == "Swag Labs" }

            products.addToCart()

            goToCart()
        }.verify {
            val items = getItemsOnShoppingCart()
            items.size shouldBe products.size
            items.zip(products).forEach { (item, product) ->
                item.quantity() shouldBe 1
                item.name() shouldBe product.productName
                item.price() shouldBe product.price
            }
        }.on {
            checkout()
        }
    }

    @Test
    fun `crossing domains`() = authenticatedSauceDemoTest(
        user = User.Visual,
        keepBrowserOpen = false,
    ) {
        open(::InventoryPage) {
            visitTwitter()
        }.switchTo<Twitter>(navigateToBase = false) {
            on(::TwitterHomePage) {
                login()
            }.then {
                likeKolibrium()
            }
        }.switchBack {
            goToCart()
        }
    }

    private fun sauceDemoTest(
        driverFactory: DriverFactory = sauceDemoDriver,
        keepBrowserOpen: Boolean = false,
        block: PageEntry<SauceDemo>.() -> Unit,
    ) = webTest(
        site = SauceDemo,
        keepBrowserOpen = keepBrowserOpen,
        driverFactory = driverFactory,
        prepare = { },
    ) { _: Unit ->
        block()
    }

    private fun authenticatedSauceDemoTest(
        user: User = User.Standard,
        keepBrowserOpen: Boolean = false,
        driverFactory: DriverFactory = sauceDemoDriver,
        block: PageEntry<SauceDemo>.() -> Unit,
    ) = webTest(
        site = SauceDemo,
        keepBrowserOpen = keepBrowserOpen,
        driverFactory = driverFactory,
        prepare = { user.acquireCredentials() },
        startup = { username -> loginAs(username) },
        block = {
            block()
        },
    )

    //Imitating backend call to acquire credentials
    private fun User.acquireCredentials(): String {
        return username
    }

    private fun PageEntry<SauceDemo>.loginAs(username: String) {
        addCookie(Cookie("session-username", username))
    }

    private val sauceDemoDriver = {
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
    }
}

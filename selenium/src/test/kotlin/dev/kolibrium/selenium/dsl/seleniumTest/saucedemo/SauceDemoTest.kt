/*
 * Copyright 2023-2026 Attila Fazekas & contributors
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

package dev.kolibrium.selenium.dsl.seleniumTest.saucedemo

import dev.kolibrium.selenium.DriverFactory
import dev.kolibrium.selenium.dsl.SiteScope
import dev.kolibrium.selenium.dsl.creation.Arguments.Chrome.disable_search_engine_choice_screen
import dev.kolibrium.selenium.dsl.creation.Arguments.Chrome.incognito
import dev.kolibrium.selenium.dsl.creation.Preferences.Chromium.credentials_enable_service
import dev.kolibrium.selenium.dsl.creation.Preferences.Chromium.password_manager_enabled
import dev.kolibrium.selenium.dsl.creation.Preferences.Chromium.password_manager_leak_detection
import dev.kolibrium.selenium.dsl.creation.chromeDriver
import dev.kolibrium.selenium.dsl.seleniumTest
import dev.kolibrium.selenium.dsl.seleniumTest.saucedemo.Product.Backpack
import dev.kolibrium.selenium.dsl.seleniumTest.saucedemo.Product.BikeLight
import dev.kolibrium.selenium.dsl.seleniumTest.saucedemo.pages.CartPage
import dev.kolibrium.selenium.dsl.seleniumTest.saucedemo.pages.InventoryPage
import dev.kolibrium.selenium.dsl.seleniumTest.saucedemo.pages.LoginPage
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class SauceDemoTest {
    companion object {
        @JvmStatic
        @BeforeAll
        fun enableLogging() {
//            SeleniumLogger.enable("RemoteWebDriver")
        }
    }

    @Test
    fun `login should succeed with default credentials`() =
        seleniumTest(
            site = SauceDemo,
            keepBrowserOpen = false,
        ) {
            on(::LoginPage) {
                login()
            }
        }

    @Test
    fun `login should fail with locked out credentials`() =
        seleniumTest(
            site = SauceDemo,
            keepBrowserOpen = false,
        ) {
            on(::LoginPage) {
                loginAsLockedOutUser()

                errorText() shouldBe "Epic sadface: Sorry, this user has been locked out."
            }
        }

    @Test
    fun `user should be able to add products to cart after bypassing login`() =
        seleniumTest(
            site = SauceDemo,
            keepBrowserOpen = false,
        ) {
            loginAs(User.Standard)

            on(::InventoryPage) {
                val products = listOf(Backpack, BikeLight)
                products.addToCart()
            }
        }

    @Test
    fun `shopping cart should contain all added products with correct details`() =
        sauceDemoTest(
            keepBrowserOpen = false,
        ) {
            val products = listOf(Backpack, BikeLight)

            on(::LoginPage) {
                login()
            }.on(::InventoryPage) {
                titleText() shouldBe "Products"

                products.addToCart()

                goToCart()
            }.on(::CartPage) {
                val items = getItemsOnShoppingCart()
                items.size shouldBe products.size
                items.zip(products).forEach { (item, product) ->
                    item.quantity() shouldBe 1
                    item.name() shouldBe product.productName
                    item.price() shouldBe product.price
                }
                checkout()
            }
        }

    @Test
    fun `authenticated user should be able to checkout multiple products`() =
        authenticatedSauceDemoTest(
            keepBrowserOpen = false,
        ) {
            val products = listOf(Backpack, BikeLight)

            on(::InventoryPage) {
                titleText() shouldBe "Products"

                products.addToCart()

                goToCart()
            }.on(::CartPage) {
                val items = getItemsOnShoppingCart()
                items.size shouldBe products.size
                items.zip(products).forEach { (item, product) ->
                    item.quantity() shouldBe 1
                    item.name() shouldBe product.productName
                    item.price() shouldBe product.price
                }
                checkout()
            }
        }

    private fun sauceDemoTest(
        driverFactory: DriverFactory = sauceDemoDriver,
        keepBrowserOpen: Boolean = false,
        block: SiteScope<SauceDemo>.() -> Unit,
    ) = seleniumTest(
        site = SauceDemo,
        keepBrowserOpen = keepBrowserOpen,
        driverFactory = driverFactory,
        setUp = { },
    ) {
        block()
    }

    private fun authenticatedSauceDemoTest(
        user: User = User.Standard,
        driverFactory: DriverFactory = sauceDemoDriver,
        keepBrowserOpen: Boolean = false,
        block: SiteScope<SauceDemo>.() -> Unit,
    ) = seleniumTest(
        site = SauceDemo,
        keepBrowserOpen = keepBrowserOpen,
        driverFactory = driverFactory,
        block = {
            loginAs(user)
            block()
        },
    )

    private fun SiteScope<SauceDemo>.loginAs(user: User) {
        cookies {
            addCookie("session-username", user.username)
        }
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

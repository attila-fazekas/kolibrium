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

package dev.kolibrium.examples.saucedemo

import com.titusfortner.logging.SeleniumLogger
import dev.kolibrium.dsl.selenium.DriverFactory
import dev.kolibrium.dsl.selenium.PageEntry
import dev.kolibrium.dsl.selenium.alsoLogDuration
import dev.kolibrium.dsl.selenium.chrome
import dev.kolibrium.dsl.selenium.discard
import dev.kolibrium.dsl.selenium.webTest
import dev.kolibrium.dsl.selenium.webTestResult
import dev.kolibrium.examples.saucedemo.Product.Backpack
import dev.kolibrium.examples.saucedemo.Product.BikeLight
import dev.kolibrium.examples.saucedemo.pages.InventoryPage
import dev.kolibrium.examples.saucedemo.pages.LoginPage
import dev.kolibrium.examples.saucedemo.pages.twitter.TwitterHomePage
import dev.kolibrium.examples.saucedemo.pages.visitTwitter
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.openqa.selenium.Cookie

class SauceDemoTest {
    companion object {
        @JvmStatic
        @BeforeAll
        fun enableLogging() {
            SeleniumLogger.enable("RemoteWebDriver")
        }
    }

    private val products = listOf(Backpack, BikeLight)

    @Test
    fun `webTest - only open used`() =
        webTest(
            site = SauceDemo,
            driverFactory = chrome(),
            keepBrowserOpen = false,
            prepare = { User.Standard.acquireCredentials() },
            startup = { username ->
                addCookie(Cookie("session-username", username))
            },
        ) {
            open(::InventoryPage) {
                products.addToCart()
            }.verify {
                numberOfProductsOnShoppingCart() shouldBe 2
            }
        }

    @Test
    fun `sauceDemoTest - only open used`() =
        sauceDemoTest(
            keepBrowserOpen = false,
            prepare = { User.Standard.acquireCredentials() },
            startup = { username -> loginAs(username) },
        ) {
            open(::InventoryPage) {
                products.addToCart()
            }.verify {
                numberOfProductsOnShoppingCart() shouldBe 2
            }
        }

    @Test
    fun `authenticatedSauceDemoTest - open used with verify`() =
        authenticatedSauceDemoTest(
            keepBrowserOpen = false,
        ) {
            open(::InventoryPage) {
                products.addToCart()
            }.verify {
                numberOfProductsOnShoppingCart() shouldBe 2
            }
        }

    @Test
    fun `sauceDemoTest used with PageEntry's open() function`() =
        sauceDemoTest(
            keepBrowserOpen = false,
            prepare = {},
        ) {
            open(::LoginPage) {
                login()
            }.on {
                verify { titleText() shouldBe "Products" }
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
    fun `crossing domains`() =
        authenticatedSauceDemoTest(
            user = User.Visual,
            keepBrowserOpen = false,
        ) {
            open(::InventoryPage) {
                visitTwitter()
            }.switchTo<Twitter>(navigateToBase = false) {
                on(::TwitterHomePage) {
                    login()
                }
            }.switchBack {
                goToCart()
            }
        }

    @Test
    fun `webTestResult - open used with verify`() =
        webTestResult(
            site = SauceDemo,
            driverFactory = chrome(),
            keepBrowserOpen = false,
            prepare = { User.Standard.acquireCredentials() },
            startup = { username -> loginAs(username) },
        ) {
            open(::InventoryPage) {
                products.addToCart()
            }.verify {
                numberOfProductsOnShoppingCart() shouldBe 2
            }
        }.alsoLogDuration()
            .also { println("status: ${it.status}") }
            .discard()

    private fun <T> sauceDemoTest(
        driverFactory: DriverFactory = chrome(),
        keepBrowserOpen: Boolean = false,
        prepare: () -> T,
        startup: PageEntry<SauceDemo>.(T) -> Unit = {},
        block: PageEntry<SauceDemo>.() -> Unit,
    ) = webTest(
        site = SauceDemo,
        keepBrowserOpen = keepBrowserOpen,
        driverFactory = driverFactory,
        prepare = prepare,
        startup = startup,
    ) {
        block()
    }

    private fun authenticatedSauceDemoTest(
        user: User = User.Standard,
        driverFactory: DriverFactory = chrome(),
        keepBrowserOpen: Boolean = false,
        block: PageEntry<SauceDemo>.() -> Unit,
    ) = webTest(
        site = SauceDemo,
        driverFactory = driverFactory,
        keepBrowserOpen = keepBrowserOpen,
        prepare = { user.acquireCredentials() },
        startup = { username -> loginAs(username) },
    ) {
        block()
    }

    // Imitating backend call to acquire credentials
    private fun User.acquireCredentials(): String = username

    private fun PageEntry<SauceDemo>.loginAs(username: String) {
        addCookie(Cookie("session-username", username))
    }
}

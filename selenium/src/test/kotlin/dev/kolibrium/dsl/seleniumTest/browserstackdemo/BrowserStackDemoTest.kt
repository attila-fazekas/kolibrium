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

package dev.kolibrium.dsl.seleniumTest.browserstackdemo

import com.titusfortner.logging.SeleniumLogger
import dev.kolibrium.dsl.DriverFactory
import dev.kolibrium.dsl.SiteEntry
import dev.kolibrium.dsl.creation.Arguments.Chrome.disable_search_engine_choice_screen
import dev.kolibrium.dsl.creation.Arguments.Chrome.incognito
import dev.kolibrium.dsl.creation.chromeDriver
import dev.kolibrium.dsl.seleniumTest
import dev.kolibrium.dsl.seleniumTest.browserstackdemo.Product.IPHONE_12
import dev.kolibrium.dsl.seleniumTest.browserstackdemo.Product.IPHONE_12_MINI
import dev.kolibrium.dsl.seleniumTest.browserstackdemo.backend.getProducts
import dev.kolibrium.dsl.seleniumTest.browserstackdemo.pages.ProductsPage
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.openqa.selenium.chrome.ChromeDriver

class BrowserStackDemoTest {
    private val products = listOf(IPHONE_12, IPHONE_12_MINI)

    companion object {
        @JvmStatic
        @BeforeAll
        fun enableLogging() {
            SeleniumLogger.enable("RemoteWebDriver")
        }
    }

//    @Test
//    fun test() =
//        seleniumTest(
//            site = BrowserstackDemo,
//            keepBrowserOpen = false,
//            setUp = {
//                val displayNames = products.map { it.displayName }
//                val productIds: List<Int> =
//                    getProducts()
//                        .filter { product -> product.title in displayNames }
//                        .map { it.id }
//                productIds
//            },
//        ) { productIds ->
//            ProductsPage().apply {
//                productIds.forEach(::addToCart)
//
//                verifyShoppingCartBadgeIs(products.size)
//            }
//        }

    @Test
    fun test2() =
        browserstackDemoTest(
            keepBrowserOpen = false,
            setUp = {
                val displayNames = products.map { it.displayName }
                val productIds: List<Int> =
                    getProducts()
                        .filter { product -> product.title in displayNames }
                        .map { it.id }
                productIds
            },
        ) { productIds ->
            open(::ProductsPage) {
                apply {
                    productIds.forEach(::addToCart)
                    verifyShoppingCartBadgeIs(products.size)
                }
            }
        }

    // TODO this test fails, fix it
    @Test
    fun simple_navigation() =
        browserstackDemoTest(
            keepBrowserOpen = false,
        ) {
            open(::ProductsPage) {
                apply {
                    verifyShoppingCartBadgeIs(0)
                }
            }
        }

    private fun browserstackDemoTest(
        driverFactory: DriverFactory = { ChromeDriver() },
        keepBrowserOpen: Boolean = false,
        block: SiteEntry<BrowserStackDemo>.(Unit) -> Unit,
    ) = seleniumTest(
        site = BrowserStackDemo,
        keepBrowserOpen = keepBrowserOpen,
        driverFactory = driverFactory,
        block = block,
    )

    private fun <T> browserstackDemoTest(
        driverFactory: DriverFactory = browserstackDemoDriver,
        keepBrowserOpen: Boolean = false,
        setUp: () -> T,
        block: SiteEntry<BrowserStackDemo>.(T) -> Unit,
    ) = seleniumTest(
        site = BrowserStackDemo,
        keepBrowserOpen = keepBrowserOpen,
        driverFactory = driverFactory,
        setUp = setUp,
        block = block,
    )

    private val browserstackDemoDriver = {
        chromeDriver {
            options {
                arguments {
                    +disable_search_engine_choice_screen
                    +incognito
                }
            }
        }
    }
}

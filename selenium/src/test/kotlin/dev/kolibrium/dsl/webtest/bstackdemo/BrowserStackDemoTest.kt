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

package dev.kolibrium.dsl.webtest.bstackdemo

import com.titusfortner.logging.SeleniumLogger
import dev.kolibrium.dsl.DriverFactory
import dev.kolibrium.dsl.SiteEntry
import dev.kolibrium.dsl.creation.Arguments.Chrome.disable_search_engine_choice_screen
import dev.kolibrium.dsl.creation.Arguments.Chrome.incognito
import dev.kolibrium.dsl.creation.chromeDriver
import dev.kolibrium.dsl.webTest
import dev.kolibrium.dsl.webtest.bstackdemo.Product.IPHONE_12
import dev.kolibrium.dsl.webtest.bstackdemo.Product.IPHONE_12_MINI
import dev.kolibrium.dsl.webtest.bstackdemo.backend.getProducts
import dev.kolibrium.dsl.webtest.bstackdemo.pages.ProductsPage
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.openqa.selenium.chrome.ChromeDriver

class BrowserstackDemoTest {
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
//        webTest(
//            site = BrowserstackDemo,
//            keepBrowserOpen = false,
//            prepare = {
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
            prepare = {
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
        startup: SiteEntry<BrowserstackDemo>.(Unit) -> Unit = { _ -> },
        block: SiteEntry<BrowserstackDemo>.(Unit) -> Unit,
    ) = webTest(
        site = BrowserstackDemo,
        keepBrowserOpen = keepBrowserOpen,
        driverFactory = driverFactory,
        startup = startup,
        block = block,
    )

    private fun <T> browserstackDemoTest(
        driverFactory: DriverFactory = browserstackDemoDriver,
        keepBrowserOpen: Boolean = false,
        prepare: () -> T,
        startup: SiteEntry<BrowserstackDemo>.(T) -> Unit = { },
        block: SiteEntry<BrowserstackDemo>.(T) -> Unit,
    ) = webTest(
        site = BrowserstackDemo,
        keepBrowserOpen = keepBrowserOpen,
        driverFactory = driverFactory,
        prepare = prepare,
        startup = startup,
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

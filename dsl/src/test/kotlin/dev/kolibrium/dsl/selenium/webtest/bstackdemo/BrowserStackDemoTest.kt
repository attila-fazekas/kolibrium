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

package dev.kolibrium.dsl.selenium.webtest.bstackdemo

import dev.kolibrium.dsl.selenium.DriverFactory
import dev.kolibrium.dsl.selenium.PageEntry
import dev.kolibrium.dsl.selenium.creation.Arguments.Chrome.disable_search_engine_choice_screen
import dev.kolibrium.dsl.selenium.creation.Arguments.Chrome.incognito
import dev.kolibrium.dsl.selenium.creation.chromeDriver
import dev.kolibrium.dsl.selenium.verify
import dev.kolibrium.dsl.selenium.webTest
import dev.kolibrium.dsl.selenium.webtest.bstackdemo.Product.IPHONE_12
import dev.kolibrium.dsl.selenium.webtest.bstackdemo.Product.IPHONE_12_MINI
import dev.kolibrium.dsl.selenium.webtest.bstackdemo.backend.getProducts
import dev.kolibrium.dsl.selenium.webtest.bstackdemo.pages.ProductsPage
import org.junit.jupiter.api.Test
import org.openqa.selenium.WebDriver

class BrowserStackDemoTest {
    private val products = listOf(IPHONE_12, IPHONE_12_MINI)

    @Test
    fun test() =
        webTest(
            site = BrowserStackDemo,
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
            ProductsPage(driver).apply {
                productIds.forEach { productId ->
                    addToCart(productId)
                }

                verifyShoppingCartBadgeIs(products.size)
            }
        }

    @Test
    fun test2() =
        browserStackDemoTest(
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
                productIds.forEach(::addToCart)

                verify { verifyShoppingCartBadgeIs(products.size) }
            }
        }

    private fun <T> browserStackDemoTest(
        driverFactory: DriverFactory = {
            chromeDriver {
                options {
                    arguments {
                        +disable_search_engine_choice_screen
                        +incognito
                    }
                }
            }
        },
        keepBrowserOpen: Boolean = false,
        prepare: context(BrowserStackDemo) () -> T,
        block: context(BrowserStackDemo) PageEntry<BrowserStackDemo>.(T) -> Unit,
    ) = webTest(
        site = BrowserStackDemo,
        driver = driverFactory,
        keepBrowserOpen = keepBrowserOpen,
        prepare = prepare,
        block = block,
    )
}

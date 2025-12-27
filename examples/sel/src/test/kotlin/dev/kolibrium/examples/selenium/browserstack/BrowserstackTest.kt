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

package dev.kolibrium.examples.selenium.browserstack

import com.titusfortner.logging.SeleniumLogger
import dev.kolibrium.examples.api.browserstack.browserstackApiPrepare
import dev.kolibrium.examples.selenium.browserstack.pages.ProductsPage
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class BrowserstackTest {
    private val products = listOf(Product.IPHONE_12, Product.IPHONE_12_MINI)

    companion object {
        @JvmStatic
        @BeforeAll
        fun enableLogging() {
            SeleniumLogger.enable("RemoteWebDriver")
        }
    }

    @Test
    fun `add products to shopping cart`() = browserstackDemoTest(
        keepBrowserOpen = false,
        prepare = browserstackApiPrepare {
            val displayNames = products.map { it.displayName }

            val productIds: List<Int> =
                getProducts()
                    .body
                    .products
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
}

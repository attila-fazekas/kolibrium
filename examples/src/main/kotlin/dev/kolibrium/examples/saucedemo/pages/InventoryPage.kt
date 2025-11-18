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

package dev.kolibrium.examples.saucedemo.pages

import dev.kolibrium.core.selenium.Page
import dev.kolibrium.core.selenium.className
import dev.kolibrium.core.selenium.dataTest
import dev.kolibrium.core.selenium.dataTests
import dev.kolibrium.examples.saucedemo.Products
import dev.kolibrium.examples.saucedemo.SauceDemo

class InventoryPage :
    Page<SauceDemo>(),
    HasFooter {
    override val path = "inventory.html"

    private val title by className("title")
    private val cartButton by className("shopping_cart_link")
    private val products by dataTests("inventory-item")
    private val shoppingCartBadge by dataTest("shopping-cart-badge")

    fun titleText(): String = title.text

    fun Products.addToCart(): InventoryPage {
        forEach { product ->
            products
                .asSequence()
                .map { webElement -> Item(webElement, product) }
                .firstOrNull { item -> item.name.text.contains(product.productName) }
                ?.addToCartButton
                ?.click()
        }
        return this@InventoryPage
    }

    fun goToCart(): CartPage {
        cartButton.click()
        return CartPage()
    }

    fun numberOfProductsOnShoppingCart() = shoppingCartBadge.text.toInt()
}

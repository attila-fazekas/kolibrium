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

package dev.kolibrium.dsl.webtest.saucedemo.pages

import dev.kolibrium.core.Page
import dev.kolibrium.core.className
import dev.kolibrium.core.dataTests
import dev.kolibrium.dsl.webtest.saucedemo.Products
import dev.kolibrium.dsl.webtest.saucedemo.SauceDemo

class InventoryPage : Page<SauceDemo>() {
    override val path = "inventory.html"

    private val title by className("title")
    private val cartButton by className("shopping_cart_link")
    private val products by dataTests("inventory-item")

    fun titleText(): String = title.text

    fun Products.addToCart() {
        forEach { product ->
            products
                .asSequence()
                .map { webElement -> Item(webElement, product) }
                .firstOrNull { item -> item.name.text.contains(product.productName) }
                ?.addToCartButton
                ?.click()
        }
    }

    fun goToCart(): CartPage {
        cartButton.click()
        return CartPage()
    }
}

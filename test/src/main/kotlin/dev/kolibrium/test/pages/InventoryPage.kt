/*
 * Copyright 2023-2024 Attila Fazekas & contributors
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

package dev.kolibrium.test.pages

import dev.kolibrium.ksp.annotations.Page
import dev.kolibrium.selenium.className
import dev.kolibrium.selenium.id
import dev.kolibrium.test.Product
import dev.kolibrium.test.dataTest
import dev.kolibrium.test.dataTests
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement

context(WebDriver)
@Page("inventory.html")
class InventoryPage {
    private val shoppingCart by className("shopping_cart_link")
    private val shoppingCartBadge by dataTests("shopping-cart-badge", cacheLookup = false)
    private val sortMenu by dataTest("product-sort-container")
    private val products by dataTests("inventory-item")

    init {
        check(sortMenu.isDisplayed) {
            "This is not the Inventory Page, current page is: " + this@WebDriver.currentUrl
        }
    }

    fun Product.addToCart() {
        for (product in products) {
            val item = Item(product, this)
            if (item.name.text.contains(this.productName)) {
                item.addToCartButton.click()
                break
            }
        }
    }

    fun Product.removeFromCart() {
        for (product in products) {
            val item = Item(product, this)
            if (item.name.text.contains(this.productName)) {
                item.removeFromCartButton.click()
                break
            }
        }
    }

    fun verifyShoppingCartCountIs(count: Int) {
        shoppingCartBadge.first().text shouldBe count.toString()
    }

    fun verifyShoppingCartIsEmpty() {
        shoppingCartBadge.shouldBeEmpty()
    }
}

class Item(root: WebElement, product: Product) {
    val image by root.className("inventory_item_img")
    val name by root.className("inventory_item_name")
    val description by root.className("inventory_item_desc")
    val price by root.className("inventory_item_price")
    val addToCartButton by root.id("add-to-cart-sauce-labs-${product.locatorName}")
    val removeFromCartButton by root.id("remove-sauce-labs-${product.locatorName}")
}

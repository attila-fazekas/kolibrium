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

import dev.kolibrium.core.Page
import dev.kolibrium.core.dataTest
import dev.kolibrium.core.dataTests
import dev.kolibrium.core.idOrName
import dev.kolibrium.examples.saucedemo.SauceDemo
import org.openqa.selenium.WebElement

class CartPage : Page<SauceDemo>() {
    override val path = "/cart.html"

    private val title = dataTest("title")

//    override val ready = title.toReadinessDescriptor()

    private val inventoryItems by dataTests("inventory-item")
    private val continueShoppingButton by idOrName("continue-shopping")
    private val checkoutButton by idOrName("checkout")

    fun checkout(): CheckoutPage {
        checkoutButton.click()
        return CheckoutPage()
    }

    fun continueShopping(): InventoryPage {
        continueShoppingButton.click()
        return InventoryPage()
    }

    fun getItemsOnShoppingCart(): List<CartItem> {
        val productsOnCart =
            inventoryItems.map {
                CartItem(it)
            }

        return productsOnCart
    }
}

class CartItem(
    root: WebElement,
) {
    private val quantity by root.dataTest("item-quantity")
    private val name by root.dataTest("inventory-item-name")
    private val price by root.dataTest("inventory-item-price")
    private val removeButton by root.idOrName("x")

    fun quantity() = quantity.text.toInt()

    fun name() = name.text

    fun price() = price.text

    fun removeButton() = removeButton.text
}

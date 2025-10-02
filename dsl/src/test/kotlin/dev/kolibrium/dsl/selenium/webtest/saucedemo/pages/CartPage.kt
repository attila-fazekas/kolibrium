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

package dev.kolibrium.dsl.selenium.webtest.saucedemo.pages

import dev.kolibrium.core.selenium.Page
import dev.kolibrium.core.selenium.dataTest
import dev.kolibrium.core.selenium.dataTests
import dev.kolibrium.core.selenium.idOrName
import dev.kolibrium.dsl.selenium.webtest.saucedemo.SauceDemo
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement

class CartPage(
    driver: WebDriver,
) : Page<SauceDemo>(driver) {
    override val path = "/cart.html"

    override val readyBy = By.className("title")

    private val inventoryItems by dataTests("inventory-item")
    private val continueShoppingButton by idOrName("continue-shopping")
    private val checkoutButton by idOrName("checkout")

    fun checkout(): CheckoutPage {
        checkoutButton.click()
        return CheckoutPage(driver)
    }

    fun continueShopping(): InventoryPage {
        continueShoppingButton.click()
        return InventoryPage(driver)
    }

    fun getItemsOnShoppingCart(): List<CartItem> {
        val productsOnCart =
            inventoryItems.map {
                CartItem(it)
            }

        return productsOnCart
    }

    fun getItemCount(): Int = 0
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

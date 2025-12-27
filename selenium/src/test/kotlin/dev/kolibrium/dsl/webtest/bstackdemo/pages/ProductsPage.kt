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

package dev.kolibrium.dsl.webtest.bstackdemo.pages

import dev.kolibrium.core.Page
import dev.kolibrium.core.cssSelector
import dev.kolibrium.core.cssSelectors
import dev.kolibrium.dsl.webtest.bstackdemo.BrowserstackDemo
import dev.kolibrium.dsl.webtest.bstackdemo.Click
import dev.kolibrium.dsl.webtest.bstackdemo.on
import io.kotest.matchers.shouldBe
import org.openqa.selenium.WebElement

class ProductsPage : Page<BrowserstackDemo>() {
    private val shoppingCartBadge by cssSelectors("span[class='bag__quantity']")

    fun addToCart(id: Int) {
        val root by cssSelector("div[id='$id']")
        val item = Item(root, id)
        Click on item.addToCartButton
    }

    fun verifyShoppingCartBadgeIs(count: Int) {
        shoppingCartBadge.first().text shouldBe count.toString()
    }
}

private class Item(
    root: WebElement,
    indexOfProduct: Int,
) {
    val addToCartButton by root.cssSelector(
        value = "div[id='$indexOfProduct'] div[class='shelf-item__buy-btn']",
    )
}

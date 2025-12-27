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

package dev.kolibrium.examples.selenium.saucedemo.pages

import dev.kolibrium.core.className
import dev.kolibrium.core.id
import dev.kolibrium.examples.selenium.saucedemo.Product
import org.openqa.selenium.WebElement

class Item(
    root: WebElement,
    product: Product,
) {
    val image by root.className("inventory_item_img")
    val name by root.className("inventory_item_name")
    val description by root.className("inventory_item_desc")
    val price by root.className("inventory_item_price")
    val addToCartButton by root.id("add-to-cart-sauce-labs-${product.locatorName}")
    val removeFromCartButton by root.id("remove-sauce-labs-${product.locatorName}")
}

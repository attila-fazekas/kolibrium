/*
 * Copyright 2023-2026 Attila Fazekas & contributors
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

package dev.kolibrium.playwright.pages

import dev.kolibrium.playwright.PlaywrightPage
import dev.kolibrium.playwright.SauceDemo

class InventoryPage : PlaywrightPage<SauceDemo>() {
    fun titleText(): String = page.locator(".title").textContent()

    fun addToCart(item: String) {
        page.click("[data-test='add-to-cart-$item']")
    }

    fun openCart() {
        page.click(".shopping_cart_link")
    }

    fun cartBadgeCount(): String = page.locator(".shopping_cart_badge").textContent()
}

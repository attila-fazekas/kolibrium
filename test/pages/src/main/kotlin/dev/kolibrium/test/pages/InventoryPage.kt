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

package dev.kolibrium.test.pages

import dev.kolibrium.core.selenium.Page
import dev.kolibrium.core.selenium.className
import org.openqa.selenium.Cookie
import org.openqa.selenium.WebDriver

class InventoryPage(driver: WebDriver) : Page(driver) {
    private val title by className("title")

    override fun url() = "inventory.html"

    override fun cookies() = setOf(Cookie("session-username", "standard_user"))

    init {
        check(title.text == "Products") {
            "This is not the Inventory Page, current page is: $currentUrl"
        }
    }

    fun titleText(): String = title.text

    companion object {
        context(driver: WebDriver)
        operator fun invoke(openPage: Boolean = false): InventoryPage = InventoryPage(driver)
    }
}

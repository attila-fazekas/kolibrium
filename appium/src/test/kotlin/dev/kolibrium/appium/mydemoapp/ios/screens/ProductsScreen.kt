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

package dev.kolibrium.appium.mydemoapp.ios.screens

import dev.kolibrium.appium.IosScreen
import dev.kolibrium.appium.accessibilityId
import dev.kolibrium.appium.ios.XCUIElementType
import dev.kolibrium.appium.ios.iOSNSPredicate
import dev.kolibrium.appium.ios.iOSNSPredicates
import dev.kolibrium.appium.ios.nsPredicate
import dev.kolibrium.appium.mydemoapp.ios.Product
import org.openqa.selenium.WebElement

class ProductsScreen : IosScreen() {
    private companion object {
        private val PRODUCT_NAME_PREDICATE =
            nsPredicate {
                type equalTo XCUIElementType.STATIC_TEXT
                name equalTo "Product Name"
            }
    }

    private val title by accessibilityId("title")
    private val productElements by iOSNSPredicates(nsPredicate { type equalTo XCUIElementType.CELL })

    fun titleText(): String = title.text

    fun Product.openProductDetails() {
        val screen = driver.manage().window().size
        productElements
            .filter { element ->
                val rect = element.rect
                rect.y + rect.height <= screen.height
            }.firstOrNull { ProductComponent(it).getName() == productName }
            ?.click()
            ?: error("Product '$productName' not found")
    }

    class ProductComponent(
        root: WebElement,
    ) {
        private val productName by root.iOSNSPredicate(PRODUCT_NAME_PREDICATE)

        fun getName(): String = productName.text
    }
}

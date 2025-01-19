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

package dev.kolibrium.test.swaglabs

import com.titusfortner.logging.SeleniumLogger
import dev.kolibrium.junit.Kolibrium
import dev.kolibrium.test.Product.BACKPACK
import dev.kolibrium.test.Product.BIKE_LIGHT
import dev.kolibrium.test.pages.generated.inventoryPage
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.openqa.selenium.WebDriver

context(WebDriver)
@Kolibrium
class ShoppingCartTest {
    companion object {
        @JvmStatic
        @BeforeAll
        fun enableLogging() {
            SeleniumLogger.enable("RemoteWebDriver")
        }
    }

    @Test
    fun `shopping cart badge is updated when product added or removed`() = inventoryPage {
        val products = listOf(BACKPACK, BIKE_LIGHT)

        products.forEach { product ->
            product.addToCart()
        }

        verifyShoppingCartCountIs(products.size)

        products.forEach { product ->
            product.removeFromCart()
        }

        verifyShoppingCartIsEmpty()
    }
}

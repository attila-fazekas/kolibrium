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

package dev.kolibrium.appium.screens

import dev.kolibrium.appium.Product
import dev.kolibrium.appium.SauceDemoAndroidApp
import dev.kolibrium.appium.SauceDemoIosApp
import dev.kolibrium.appium.Screen
import dev.kolibrium.appium.accessibilityId
import dev.kolibrium.appium.iOSClassChain
import dev.kolibrium.appium.iOSNsPredicates
import dev.kolibrium.selenium.core.xpaths

sealed interface ProductsScreen {
    fun titleText(): String

    fun Product.openProductDetails(): ProductDetailsScreen

    class Android :
        Screen<SauceDemoAndroidApp>(),
        ProductsScreen {
        private val title by accessibilityId("title")
        private val products by xpaths(
            """//*[@resource-id="${SauceDemoAndroidApp.appPackage}:id/productRV"]/android.view.ViewGroup""",
        )

        override fun titleText(): String = title.text

        override fun Product.openProductDetails(): ProductDetailsScreen {
            products
                .asSequence()
                .map { webElement -> Item(webElement) }
                .firstOrNull { item -> item.title.text == productName }
                ?.image
                ?.click()
                ?: error("Product '$productName' not found")

            return ProductDetailsScreen()
        }
    }

    class Ios :
        Screen<SauceDemoIosApp>(),
        ProductsScreen {
        private val title by iOSClassChain("XCUIElementTypeStaticText[`name == 'title'`]")
        private val products by iOSNsPredicates("type == 'XCUIElementTypeCell'")

        override fun titleText(): String = title.text

        override fun Product.openProductDetails(): ProductDetailsScreen {
            TODO("Not yet implemented")
        }
    }
}

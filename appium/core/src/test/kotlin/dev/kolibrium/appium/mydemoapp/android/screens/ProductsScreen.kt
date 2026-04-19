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

package dev.kolibrium.appium.mydemoapp.android.screens

import dev.kolibrium.appium.Screen
import dev.kolibrium.appium.accessibilityId
import dev.kolibrium.appium.mydemoapp.android.MyDemoAndroidApp
import dev.kolibrium.appium.mydemoapp.android.Product
import dev.kolibrium.appium.resourceId
import dev.kolibrium.appium.xpaths
import org.openqa.selenium.WebElement

class ProductsScreen : Screen<MyDemoAndroidApp>() {
    private val title by accessibilityId("title")
    private val products by xpaths(
        """//*[@resource-id="${MyDemoAndroidApp.appPackage}:id/productRV"]/android.view.ViewGroup""",
    )

    fun titleText(): String = title.text

    fun Product.openProductDetails() {
        products
            .asSequence()
            .map { webElement -> ProductComponent(webElement) }
            .firstOrNull { item -> item.title.text == productName }
            ?.image
            ?.click()
            ?: error("Product '$productName' not found")
    }

    class ProductComponent(
        root: WebElement,
    ) {
        val image by root.resourceId("productIV")
        val title by root.resourceId("titleTV")
        val price by root.resourceId("priceTV")
    }
}

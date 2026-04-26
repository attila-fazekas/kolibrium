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

package dev.kolibrium.appium.mydemoapp.android

import dev.kolibrium.appium.AndroidDriverFactory
import dev.kolibrium.appium.AppScope
import dev.kolibrium.appium.androidTest
import dev.kolibrium.appium.mydemoapp.android.screens.ProductDetailsScreen
import dev.kolibrium.appium.mydemoapp.android.screens.ProductsScreen
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class MyDemoAppAndroidTest {
    @Test
    fun checkout() =
        myDemoAndroidAppTest {
            on(::ProductsScreen) {
                titleText() shouldBe "Products"

                Product.BackpackBlack.openProductDetails()
            }.on(::ProductDetailsScreen) {
                addToCart()
            }
        }
}

fun myDemoAndroidAppTest(
    app: MyDemoAndroidApp = MyDemoAndroidApp,
    driverFactory: AndroidDriverFactory = app.driverFactory,
    block: AppScope<MyDemoAndroidApp>.(Unit) -> Unit,
) {
    androidTest(app = app, driverFactory = driverFactory, block = block)
}

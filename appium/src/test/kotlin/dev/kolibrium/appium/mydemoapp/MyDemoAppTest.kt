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

package dev.kolibrium.appium.mydemoapp

import dev.kolibrium.appium.AndroidDriverFactory
import dev.kolibrium.appium.AppEntry
import dev.kolibrium.appium.IosDriverFactory
import dev.kolibrium.appium.androidTest
import dev.kolibrium.appium.iosTest
import dev.kolibrium.appium.mydemoapp.screens.ProductDetailsScreen
import dev.kolibrium.appium.mydemoapp.screens.ProductsScreen
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class MyDemoAppTest {
    @Test
    fun `android checkout`() =
        myDemoAndroidAppTest {
            open(ProductsScreen::Android) {
                titleText() shouldBe "Products"

                Product.Backpack.openProductDetails()
            }.on(::ProductDetailsScreen) {
                addToCart()
            }
        }

    @Test
    fun `iOS checkout`() =
        myDemoIosAppTest {
//            open(::ProductsScreen) {
//                titleText() shouldBe "Products"
//
//                Backpack.openProductDetails()
//            }.on(::ProductDetailsScreen) {
//                addToCart()
//            }
        }
}

// TODO Generate this with KSP
fun myDemoAndroidAppTest(
    app: MyDemoAndroidApp = MyDemoAndroidApp,
    driverFactory: AndroidDriverFactory = app.driverFactory,
    block: AppEntry<MyDemoAndroidApp>.(Unit) -> Unit,
) {
    androidTest(app = app, driverFactory = driverFactory, block = block)
}

// TODO Generate this with KSP
fun myDemoIosAppTest(
    app: MyDemoIosApp = MyDemoIosApp,
    driverFactory: IosDriverFactory = app.driverFactory,
    block: AppEntry<MyDemoIosApp>.(Unit) -> Unit,
) {
    iosTest(app = app, driverFactory = driverFactory, block = block)
}

enum class Product(
    val productName: String,
    val price: String,
    val locatorName: String,
) {
    Backpack("Sauce Labs Backpack", "$29.99", "backpack"),
    BikeLight("Sauce Labs Bike Light", "$9.99", "bike-light"),
    BoltTShirt("Sauce Labs Bolt T-Shirt", "$15.99", "bolt-t-shirt"),
    FleeceJacket("Sauce Labs Fleece Jacket", "$49.99", "fleece-jacket"),
    RedTShirt("Test.allTheThings() T-Shirt (Red)", "$7.99", "t-shirt-red"),
    Onesie("Sauce Labs Onesie", "$15.99", "onesie"),
}

typealias Products = List<Product>

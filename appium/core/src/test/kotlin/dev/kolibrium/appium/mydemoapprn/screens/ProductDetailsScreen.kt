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

package dev.kolibrium.appium.mydemoapprn.screens

import dev.kolibrium.appium.Screen
import dev.kolibrium.appium.android.androidUIAutomator
import dev.kolibrium.appium.android.uiSelector
import dev.kolibrium.appium.mydemoapprn.MyDemoAppRnAndroidApp

class ProductDetailsScreen : Screen<MyDemoAppRnAndroidApp>() {
    val addToCartButton by androidUIAutomator(uiSelector { text("Add To Cart") })

    fun addToCart() {
        addToCartButton.click()
    }
}

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

import dev.kolibrium.appium.Screen
import dev.kolibrium.appium.accessibilityId
import dev.kolibrium.appium.mydemoapp.ios.MyDemoIosApp

class Footer : Screen<MyDemoIosApp>() {
    val catalogItem by accessibilityId("Catalog-tab-item")
    val cartItem by accessibilityId("Cart-tab-item")
    val more by accessibilityId("More-tab-item")

    fun openCatalog() {
        catalogItem.click()
    }

    fun openCart() {
        cartItem.click()
    }

    fun openMore() {
        more.click()
    }
}

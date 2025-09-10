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

package dev.kolibrium.dsl.selenium.webtest

enum class Product(
    val productName: String,
) {
    BACKPACK("Sauce Labs Backpack"),
    BIKE_LIGHT("Sauce Labs Bike Light"),
    BOLT_T_SHIRT("Sauce Labs Bolt T-Shirt"),
    FLEECE_JACKET("Sauce Labs Fleece Jacket"),
    T_SHIRT_RED("Test.allTheThings() T-Shirt (Red)"),
    ONESIE("Sauce Labs Onesie"),
    ;

    val locatorName: String
        get() = name.lowercase().replace("_", "-")
}

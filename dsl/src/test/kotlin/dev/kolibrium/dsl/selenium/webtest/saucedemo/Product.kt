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

package dev.kolibrium.dsl.selenium.webtest.saucedemo

enum class Product(
    val productName: String,
    val price: String,
) {
    BACKPACK("Sauce Labs Backpack", "$29.99"),
    BIKE_LIGHT("Sauce Labs Bike Light", "$9.99"),
    BOLT_T_SHIRT("Sauce Labs Bolt T-Shirt", "$15.99"),
    FLEECE_JACKET("Sauce Labs Fleece Jacket", "$49.99"),
    T_SHIRT_RED("Test.allTheThings() T-Shirt (Red)", "$7.99"),
    ONESIE("Sauce Labs Onesie", "$15.99"),
    ;

    val locatorName: String
        get() = name.lowercase().replace("_", "-")
}

typealias Products = List<Product>

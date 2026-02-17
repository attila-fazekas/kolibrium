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

package dev.kolibrium.examples.api.browserstack.models

import dev.kolibrium.api.ksp.annotations.GET
import dev.kolibrium.api.ksp.annotations.Returns
import kotlinx.serialization.Serializable

public typealias Products = List<Product>

@GET("/api/products")
@Returns(success = ProductList::class)
@Serializable
public class GetProductsRequest

@Serializable
public data class ProductList(
    val products: Products,
)

@Serializable
public data class Product(
    val id: Int,
    val title: String,
    val availableSizes: List<String>,
    val currencyFormat: String,
    val currencyId: String,
    val description: String,
    val installments: Int,
    val altText: String,
    val isFav: Boolean,
    val price: Int,
    val sku: String,
)

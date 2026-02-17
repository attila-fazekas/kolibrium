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

package dev.kolibrium.examples.api.vinylstore.models

import dev.kolibrium.api.core.AuthType
import dev.kolibrium.api.ksp.annotations.Auth
import dev.kolibrium.api.ksp.annotations.DELETE
import dev.kolibrium.api.ksp.annotations.GET
import dev.kolibrium.api.ksp.annotations.POST
import dev.kolibrium.api.ksp.annotations.PUT
import dev.kolibrium.api.ksp.annotations.Path
import dev.kolibrium.api.ksp.annotations.Query
import dev.kolibrium.api.ksp.annotations.Returns
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@POST("/auth/login")
@Returns(success = LoginResponse::class)
@Serializable
data class LoginRequest(
    var email: String? = null,
    var password: String? = null,
)

@Serializable
data class LoginResponse(
    val token: String,
    val user: UserResponse,
)

@Serializable
data class UserResponse(
    val id: Int,
    val email: String,
    val role: String,
    val isActive: Boolean,
)

@POST("/vinyls")
@Returns(success = Vinyl::class)
@Serializable
data class CreateVinylRequest(
    var artist: String? = null,
    var album: String? = null,
    var year: Int? = null,
    var genre: String? = null,
    var price: Double? = null,
    var stock: Int? = null,
)

@GET("/vinyls/{id}")
@Returns(success = Vinyl::class)
@Serializable
data class GetVinylRequest(
    @Path @Transient val id: Int = 0,
)

@GET("/vinyls")
@Auth(type = AuthType.BEARER)
@Returns(success = VinylList::class)
@Serializable
data class ListVinylsRequest(
    @Query @Transient val genre: String? = null,
    @Query @Transient val artist: String? = null,
)

@PUT("/vinyls/{id}")
@Returns(success = Vinyl::class)
@Serializable
data class UpdateVinylRequest(
    @Path @Transient val id: Int = 0,
    var artist: String? = null,
    var album: String? = null,
    var year: Int? = null,
    var genre: String? = null,
    var price: Double? = null,
    var stock: Int? = null,
)

@DELETE("/vinyls/{id}")
@Returns(success = Unit::class)
@Serializable
data class DeleteVinylRequest(
    @Path @Transient val id: Int = 0,
)

// Response models
@Serializable
data class Vinyl(
    val id: Int,
    val artist: String,
    val album: String,
    val year: Int,
    val genre: String,
    val price: Double,
    val stock: Int,
)

@Serializable
data class VinylList(
    val vinyls: List<Vinyl>,
    val total: Int,
)

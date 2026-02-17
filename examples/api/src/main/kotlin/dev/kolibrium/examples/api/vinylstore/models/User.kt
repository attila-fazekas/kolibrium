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

import dev.kolibrium.api.ksp.annotations.POST
import dev.kolibrium.api.ksp.annotations.PUT
import dev.kolibrium.api.ksp.annotations.Path
import dev.kolibrium.api.ksp.annotations.Returns
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@POST("/users")
@Returns(success = User::class)
@Serializable
data class CreateUserRequest(
    var username: String? = null,
    var email: String? = null,
    var firstName: String? = null,
    var lastName: String? = null,
)

@PUT("/users/{id}")
@Returns(success = User::class)
@Serializable
data class UpdateUserRequest(
    @Path @Transient val id: Int = 0,
    var username: String? = null,
    var email: String? = null,
    var firstName: String? = null,
    var lastName: String? = null,
    var active: Boolean? = null,
)

@Serializable
data class User(
    val id: Int,
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val active: Boolean,
)

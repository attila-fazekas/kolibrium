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

package dev.kolibrium.examples.api.vinylstore

import dev.kolibrium.api.core.ApiSpec
import dev.kolibrium.api.ksp.annotations.ClientGrouping
import dev.kolibrium.api.ksp.annotations.GenerateApi

@GenerateApi(
    grouping = ClientGrouping.ByPrefix,
)
object VinylStoreApiSpec : ApiSpec(
    baseUrl = "http://localhost:8080/api",
)

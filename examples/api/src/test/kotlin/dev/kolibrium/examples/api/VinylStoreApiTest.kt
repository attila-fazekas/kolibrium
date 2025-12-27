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

package dev.kolibrium.examples.api

import dev.kolibrium.examples.api.vinylstore.generated.vinylStoreApiTest
import io.kotest.matchers.shouldBe
import io.ktor.http.HttpStatusCode
import org.junit.jupiter.api.Test

class VinylStoreApiTest {
    @Test
    fun `create vinyl returns 201`() =
        vinylStoreApiTest {
            val response =
                vinyls.createVinyl {
                    artist = "Sasha"
                    album = "LUZoSCURA"
                    year = 2021
                    genre = "Electronic"
                    price = 15.00
                    stock = 20
                }

            response.status shouldBe HttpStatusCode.Created
        }

    @Test
    fun `list vinyls returns 200`() =
        vinylStoreApiTest {
            val response =
                vinyls.listVinyls(
                    genre = "Rock",
                )

            response.status shouldBe HttpStatusCode.OK
            response.body.vinyls.size shouldBe 2
        }

    @Test
    fun `update vinyls returns 200`() =
        vinylStoreApiTest {
            val vinyl =
                vinyls.listVinyls().body.vinyls.first { vinyl ->
                    vinyl.album == "Nevermind"
                }

            val response =
                vinyls.updateVinyl(vinyl.id) {
                    price = 18.00
                }

            response.status shouldBe HttpStatusCode.OK
            response.body.price shouldBe 18.00
        }

    @Test
    fun `test with setUp and tearDown`() =
        vinylStoreApiTest(
            setUp = {
                // Setup: create a vinyl
                val response =
                    vinyls.createVinyl {
                        artist = "Bicep"
                        album = "Isles"
                        year = 2021
                        genre = "Electronic"
                        price = 18.00
                        stock = 15
                    }
                response.body.id // Return the ID for use in test and tearDown
            },
            tearDown = { vinylId ->
                // Cleanup: delete the vinyl
                vinyls.deleteVinyl(vinylId)
            },
        ) { vinylId ->
            // Test: verify the vinyl exists
            val response = vinyls.getVinyl(vinylId)
            response.status shouldBe HttpStatusCode.OK
        }
}

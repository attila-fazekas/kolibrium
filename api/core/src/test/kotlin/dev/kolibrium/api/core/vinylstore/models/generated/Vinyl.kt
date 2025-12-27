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

package dev.kolibrium.api.core.vinylstore.models.generated

import dev.kolibrium.api.core.ApiResponse
import dev.kolibrium.api.core.EmptyResponse
import dev.kolibrium.api.core.defaultHttpClient
import dev.kolibrium.api.core.vinylstore.models.CreateVinylRequest
import dev.kolibrium.api.core.vinylstore.models.UpdateVinylRequest
import dev.kolibrium.api.core.vinylstore.models.Vinyl
import dev.kolibrium.api.core.vinylstore.models.VinylList
import dev.kolibrium.api.core.vinylstore.models.VinylStoreApiSpec
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.runBlocking

public class VinylStoreClient(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    public suspend fun createVinyl(block: CreateVinylRequest.() -> Unit): ApiResponse<Vinyl> {
        val request = CreateVinylRequest().apply(block)

        val httpResponse =
            client.post("$baseUrl/api/vinyls") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }

        return ApiResponse(
            status = httpResponse.status,
            headers = httpResponse.headers,
            contentType = httpResponse.contentType(),
            body = httpResponse.body(),
        )
    }

    public suspend fun getVinyl(id: Int): ApiResponse<Vinyl> {
        val httpResponse = client.get("$baseUrl/api/vinyls/$id")

        return ApiResponse(
            status = httpResponse.status,
            headers = httpResponse.headers,
            contentType = httpResponse.contentType(),
            body = httpResponse.body(),
        )
    }

    public suspend fun listVinyls(
        genre: String? = null,
        artist: String? = null,
    ): ApiResponse<VinylList> {
        val httpResponse =
            client.get("$baseUrl/api/vinyls") {
                genre?.let { parameter("genre", it) }
                artist?.let { parameter("artist", it) }
            }

        return ApiResponse(
            status = httpResponse.status,
            headers = httpResponse.headers,
            contentType = httpResponse.contentType(),
            body = httpResponse.body(),
        )
    }

    public suspend fun updateVinyl(
        id: Int,
        block: UpdateVinylRequest.() -> Unit,
    ): ApiResponse<Vinyl> {
        val request = UpdateVinylRequest(id = id).apply(block)

        val httpResponse =
            client.put("$baseUrl/api/vinyls/$id") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }

        return ApiResponse(
            status = httpResponse.status,
            headers = httpResponse.headers,
            contentType = httpResponse.contentType(),
            body = httpResponse.body(),
        )
    }

    public suspend fun deleteVinyl(id: Int): EmptyResponse {
        val httpResponse = client.delete("$baseUrl/api/vinyls/$id")

        return ApiResponse(
            status = httpResponse.status,
            headers = httpResponse.headers,
            contentType = httpResponse.contentType(),
            body = Unit,
        )
    }
}

public fun vinylStoreApiTest(
    baseUrl: String = VinylStoreApiSpec.baseUrl,
    client: HttpClient = defaultHttpClient,
    block: suspend VinylStoreClient.() -> Unit,
) {
    runBlocking {
        val api = VinylStoreClient(client, baseUrl)
        api.block()
    }
}

public fun <T> vinylStoreApiTest(
    baseUrl: String = VinylStoreApiSpec.baseUrl,
    client: HttpClient = defaultHttpClient,
    setUp: suspend VinylStoreClient.() -> T,
    tearDown: suspend VinylStoreClient.(T) -> Unit = {},
    block: suspend VinylStoreClient.(T) -> Unit,
) {
    runBlocking {
        val api = VinylStoreClient(client, baseUrl)
        val context = api.setUp()
        try {
            api.block(context)
        } finally {
            try {
                api.tearDown(context)
            } catch (_: Throwable) {
            }
        }
    }
}

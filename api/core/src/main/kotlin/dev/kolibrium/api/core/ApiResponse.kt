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

package dev.kolibrium.api.core

import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpStatusCode

/**
 * Represents an HTTP API response with typed body content.
 *
 * This data class encapsulates all relevant information from an HTTP response,
 * including status code, headers, content type, and the deserialized response body.
 *
 * @param T The type of the response body
 * @property status The HTTP status code of the response
 * @property headers The HTTP headers from the response
 * @property contentType The content type of the response, or null if not specified
 * @property body The deserialized response body
 */
public class ApiResponse<T>(
    public val status: HttpStatusCode,
    public val headers: Headers,
    public val contentType: ContentType?,
    public val body: T,
) {
    /**
     * Returns `true` if the response status code indicates success (2xx range).
     */
    public val isSuccess: Boolean
        get() = status.value in 200..299

    /**
     * Returns `true` if the response status code indicates a client error (4xx range).
     */
    public val isClientError: Boolean
        get() = status.value in 400..499

    /**
     * Returns `true` if the response status code indicates a server error (5xx range).
     */
    public val isServerError: Boolean
        get() = status.value in 500..599

    /**
     * Retrieves the value of the specified header.
     *
     * @param name The name of the header to retrieve
     * @return The header value, or null if the header is not present
     */
    public fun header(name: String): String? = headers[name]

    /**
     * Validates that the response was successful and returns this response.
     *
     * @return This response if successful
     * @throws ApiException if the response status code is not in the 2xx range
     */
    public fun requireSuccess(): ApiResponse<T> =
        apply {
            if (!isSuccess) throw ApiException(this)
        }
}

/**
 * Type alias for an API response with no body content.
 *
 * Use this when an API endpoint returns no meaningful response body (e.g., DELETE),
 * but you still need to access status code, headers, and other response metadata.
 */
public typealias EmptyResponse = ApiResponse<Unit>

/**
 * Exception thrown when an API call fails.
 *
 * This exception is thrown by [ApiResponse.requireSuccess] when the response
 * status code is not in the 2xx success range. The exception message includes
 * both the status code and the response body for debugging purposes.
 *
 * @param response The failed API response that triggered this exception
 */
public class ApiException(
    response: ApiResponse<*>,
) : Exception("API call failed: ${response.status} - ${response.body}")

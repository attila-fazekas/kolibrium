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

import io.ktor.client.HttpClient

/**
 * Base specification class for API testing.
 *
 * This abstract class serves as a foundation for defining API specifications.
 * Extend this class to create specific API clients or test specifications.
 * It owns runtime configuration (`baseUrl`, `httpClient`). For codegen
 * configuration (`scanPackages`, `grouping`, `generateTestHarness`), use the
 * [@GenerateApi][dev.kolibrium.api.ksp.annotations.GenerateApi] annotation.
 *
 * Example usage:
 * ```
 * @GenerateApi(grouping = ClientGrouping.ByPrefix)
 * object MyApiSpec : ApiSpec() {
 *     override val baseUrl = "https://api.example.com"
 *
 *     // Optional: customize HTTP client
 *     override val httpClient = HttpClient(CIO) {
 *         install(ContentNegotiation) {
 *             json(Json { ignoreUnknownKeys = true })
 *         }
 *     }
 * }
 * ```
 */
public abstract class ApiSpec {
    /**
     * The base URL for the API endpoint (e.g., "https://api.example.com").
     *
     * This property is required and must be overridden by implementations.
     */
    public abstract val baseUrl: String

    /**
     * The HTTP client instance used for API requests.
     *
     * Defaults to [defaultHttpClient], which is pre-configured with JSON content
     * negotiation, logging, and timeouts. Override this property to customize
     * the client configuration (e.g., different engine, plugins, or settings).
     */
    public open val httpClient: HttpClient by lazy { defaultHttpClient }
}

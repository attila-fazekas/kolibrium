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

import dev.kolibrium.api.ksp.annotations.ClientGrouping
import io.ktor.client.HttpClient

/**
 * Base specification class for API testing.
 *
 * This abstract class serves as a foundation for defining API specifications.
 * Extend this class to create specific API clients or test specifications.
 *
 * Example usage:
 * ```
 * object MyApiSpec : ApiSpec() {
 *     override val baseUrl = "https://api.example.com"
 *
 *     // Optional: customize scan packages
 *     override val scanPackages = setOf("com.example.api.models")
 *
 *     // Optional: customize grouping
 *     override val grouping = ClientGrouping.ByPrefix
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
     * Defines how generated client classes are organized.
     *
     * Defaults to [ClientGrouping.SingleClient], which generates a single client class
     * containing all API methods. Override with [ClientGrouping.ByPrefix] to group
     * methods by their API path prefix into separate client classes.
     */
    public open val grouping: ClientGrouping = ClientGrouping.SingleClient

    /**
     * Set of packages to scan for request classes.
     *
     * Defaults to a set containing `<api-package>.models`, where `<api-package>` is
     * the package containing the ApiSpec implementation. Override this property to
     * scan different or multiple packages.
     */
    public open val scanPackages: Set<String>
        get() = setOf("${this::class.java.packageName}.models")

    /**
     * The HTTP client instance used for API requests.
     *
     * Defaults to [defaultHttpClient], which is pre-configured with JSON content
     * negotiation, logging, and timeouts. Override this property to customize
     * the client configuration (e.g., different engine, plugins, or settings).
     */
    public open val httpClient: HttpClient by lazy { defaultHttpClient }
}

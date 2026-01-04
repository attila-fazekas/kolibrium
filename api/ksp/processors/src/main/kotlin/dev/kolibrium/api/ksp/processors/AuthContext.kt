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

package dev.kolibrium.api.ksp.processors

import io.ktor.client.request.HttpRequestBuilder

/**
 * Represents the runtime authentication context for API requests.
 *
 * This sealed interface defines the different authentication mechanisms that can be used
 * when making HTTP requests. Each implementation provides the necessary credentials
 * and configuration for its respective authentication type.
 */
public sealed interface AuthContext {
    /**
     * No authentication context. Used when API requests don't require authentication.
     */
    public data object None : AuthContext

    /**
     * Bearer token authentication context (e.g., OAuth 2.0 access tokens).
     *
     * @property token The bearer token to be sent in the Authorization header.
     */
    public class Bearer(
        public val token: String,
    ) : AuthContext

    /**
     * Basic HTTP authentication context using username and password.
     *
     * @property username The username for basic authentication.
     * @property password The password for basic authentication.
     */
    public class Basic(
        public val username: String,
        public val password: String,
    ) : AuthContext

    /**
     * API key authentication context for custom header-based authentication.
     *
     * @property key The API key to be sent in the configured header.
     */
    public class ApiKey(
        public val key: String,
    ) : AuthContext

    /**
     * Custom authentication context for application-specific authentication mechanisms.
     *
     * @property configure A lambda that configures the [HttpRequestBuilder] to add
     *   custom authentication headers, parameters, or other request modifications.
     */
    public class Custom(
        public val configure: HttpRequestBuilder.() -> Unit,
    ) : AuthContext
}

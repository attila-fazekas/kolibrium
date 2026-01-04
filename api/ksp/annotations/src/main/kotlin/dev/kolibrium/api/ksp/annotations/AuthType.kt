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

package dev.kolibrium.api.ksp.annotations

/**
 * Defines the available authentication types for API requests.
 *
 * This enum is used with the [Auth] annotation to specify how authentication
 * credentials should be handled when making API calls.
 */
public enum class AuthType {
    /**
     * No authentication is required for the API request.
     */
    NONE,

    /**
     * Bearer token authentication (e.g., OAuth 2.0 access tokens).
     * The token is sent in the Authorization header as "Bearer {token}".
     */
    BEARER,

    /**
     * Basic HTTP authentication using username and password.
     * Credentials are Base64-encoded and sent in the Authorization header.
     */
    BASIC,

    /**
     * API key authentication, typically sent as a custom header or query parameter.
     * The header name can be configured using [Auth.headerName].
     */
    API_KEY,

    /**
     * Custom authentication mechanism that allows for application-specific implementations.
     */
    CUSTOM,
}

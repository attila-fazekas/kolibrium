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
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Default pre-configured HTTP client for API testing.
 *
 * This client is configured with:
 * - **Content Negotiation**: JSON serialization with pretty printing and support for unknown keys
 * - **Logging**: Full request/response logging at ALL level
 * - **Timeouts**: 5 second request timeout and 10 second connection timeout
 * - **Error Handling**: Does not throw exceptions on non-2xx responses (expectSuccess = false)
 *
 * The client uses the CIO (Coroutine-based I/O) engine for asynchronous operations.
 */
public val defaultHttpClient: HttpClient =
    HttpClient(CIO) {
        install(ContentNegotiation) {
            json(
                Json {
                    prettyPrint = true
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                },
            )
        }
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.ALL
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 5_000
            connectTimeoutMillis = 10_000
        }
        expectSuccess = false
    }

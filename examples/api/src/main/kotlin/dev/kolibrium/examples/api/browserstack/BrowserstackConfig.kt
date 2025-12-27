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

package dev.kolibrium.examples.api.browserstack

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addResourceSource

public object BrowserstackApiConfig {
    private val config: BrowserstackConfiguration by lazy {
        try {
            ConfigLoaderBuilder
                .default()
                .addResourceSource("/browserstack-api.yml")
                .build()
                .loadConfigOrThrow<BrowserstackConfiguration>()
        } catch (_: Exception) {
            // Fall back to environment variable
            BrowserstackConfiguration(
                api =
                    BrowserstackConfiguration.ApiConfiguration(
                        baseUrl =
                            System.getenv("API_BASE_URL")
                                ?: error("API baseUrl not configured. Set API_BASE_URL env var or create browserstack-api.yml"),
                    ),
            )
        }
    }

    public val baseUrl: String
        get() = System.getenv("API_BASE_URL") ?: config.api.baseUrl
}

public data class BrowserstackConfiguration(
    val api: ApiConfiguration,
) {
    public data class ApiConfiguration(
        val baseUrl: String,
    )
}

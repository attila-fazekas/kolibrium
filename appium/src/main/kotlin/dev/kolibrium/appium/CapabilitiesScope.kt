/*
 * Copyright 2023-2026 Attila Fazekas & contributors
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

package dev.kolibrium.appium

import dev.kolibrium.appium.android.SettingsScope
import dev.kolibrium.webdriver.InternalKolibriumApi
import dev.kolibrium.webdriver.KolibriumDsl

/**
 * DSL scope for assembling server-side capabilities for Appium.
 *
 * Entries added here are forwarded to [io.appium.java_client.service.local.AppiumServiceBuilder.withCapabilities]
 * and influence server behavior, not per-session capabilities.
 */
@KolibriumDsl
public class CapabilitiesScope
    @InternalKolibriumApi
    constructor() {
        internal val capabilities: MutableMap<String, Any> = mutableMapOf()

        /**
         * Adds a capability key-value pair to the scope.
         *
         * Example:
         *
         * ```kotlin
         * capabilities {
         *     "allowCors" to true
         * }
         * ```
         */
        public infix fun String.to(value: Any) {
            capabilities[this] = value
        }

        /**
         * DSL for configuring Appium settings passed to the server at startup via the
         * `appium:settings` capability key.
         *
         * Settings added here are forwarded as part of the server-side capabilities and
         * influence server behavior. They are **not** the same as per-session settings
         * adjustable at runtime via [AppScope.settings] or [ScreenScope.settings].
         *
         * Example:
         * ```kotlin
         * capabilities {
         *     settings {
         *         ignoreUnimportantViews = true
         *     }
         * }
         * ```
         */
        public fun settings(block: SettingsScope.() -> Unit) {
            val settingsMap = SettingsScope().apply(block).toMap()
            if (settingsMap.isNotEmpty()) {
                "appium:settings" to settingsMap
            }
        }

        /**
         * Returns a string representation of the [CapabilitiesScope], primarily for debugging purposes.
         */
        override fun toString(): String = "CapabilitiesScope(capabilities=$capabilities)"
    }

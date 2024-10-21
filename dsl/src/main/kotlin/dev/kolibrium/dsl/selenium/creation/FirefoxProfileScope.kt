/*
 * Copyright 2023-2024 Attila Fazekas & contributors
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

package dev.kolibrium.dsl.selenium.creation

/**
 * Scope class for configuring Firefox profile preferences.
 */
@KolibriumDsl
public class FirefoxProfileScope {
    internal val preferences = mutableMapOf<String, Any>()

    /**
     * Adds a preference key-value pair to the Firefox profile.
     */
    @KolibriumDsl
    public fun preference(
        key: FirefoxPreference,
        value: Any,
    ) {
        preferences[key.value] = value
    }

    /**
     * Returns a string representation of the [FirefoxProfileScope], primarily for debugging purposes.
     */
    override fun toString(): String = "FirefoxProfileScope(preferences=$preferences)"
}

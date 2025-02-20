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

package dev.kolibrium.dsl.selenium.creation

import dev.kolibrium.dsl.selenium.KolibriumDsl

internal sealed interface PreferencesScope<T : Preference> {
    fun pref(
        key: T,
        value: Any,
    )

    fun pref(
        key: String,
        value: Any,
    )
}

/**
 * Scope class for configuring Chromium browser preferences.
 */
@KolibriumDsl
public class ChromiumPreferencesScope : PreferencesScope<ChromiumPreference> {
    internal val preferences = mutableMapOf<String, Any>()

    /**
     * Adds a preference key-value pair to the Chrome options using a [ChromiumPreference] key.
     *
     * @param key The preference key represented by a [ChromiumPreference] instance.
     * @param value The preference value to be associated with the key.
     */
    @KolibriumDsl
    override fun pref(
        key: ChromiumPreference,
        value: Any,
    ) {
        preferences[key.value] = value
    }

    /**
     * Adds a preference key-value pair to the Chrome options using a string key.
     *
     * @param key The preference key as a string.
     * @param value The preference value to be associated with the key.
     */
    @KolibriumDsl
    override fun pref(
        key: String,
        value: Any,
    ) {
        preferences[key] = value
    }

    /**
     * Returns a string representation of the [ChromiumPreferencesScope], primarily for debugging purposes.
     */
    override fun toString(): String = "PreferencesScope(preferences=$preferences)"
}

/**
 * Scope class for configuring Firefox preferences.
 */
@KolibriumDsl
public class FirefoxPreferencesScope : PreferencesScope<FirefoxPreference> {
    internal val preferences = mutableMapOf<String, Any>()

    /**
     * Adds a preference key-value pair to the Firefox options using a [FirefoxPreference] key.
     *
     * @param key The preference key represented by a [FirefoxPreference] instance.
     * @param value The preference value to be associated with the key.
     */
    @KolibriumDsl
    override fun pref(
        key: FirefoxPreference,
        value: Any,
    ) {
        preferences[key.value] = value
    }

    /**
     * Adds a preference key-value pair to the Firefox options using a string key.
     *
     * @param key The preference key as a string.
     * @param value The preference value to be associated with the key.
     */
    @KolibriumDsl
    override fun pref(
        key: String,
        value: Any,
    ) {
        preferences[key] = value
    }

    /**
     * Returns a string representation of the [FirefoxPreferencesScope], primarily for debugging purposes.
     */
    override fun toString(): String = "PreferencesScope(preferences=$preferences)"
}

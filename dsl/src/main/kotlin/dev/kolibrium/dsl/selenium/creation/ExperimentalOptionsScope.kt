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
 * Scope class for configuring experimental browser options.
 */
@KolibriumDsl
public class ExperimentalOptionsScope {
    internal val excludeSwitchesScope: ExcludeSwitchesScope by lazy { ExcludeSwitchesScope() }
    internal val localStateScope: LocalStateScope by lazy { LocalStateScope() }
    internal val preferencesScope: ChromiumPreferencesScope by lazy { ChromiumPreferencesScope() }

    /**
     * Configures switches to be excluded from the browser launch.
     *
     * @param block The configuration block for excluded switches.
     * @return The configured [ExcludeSwitchesScope].
     */
    @KolibriumDsl
    public fun excludeSwitches(block: ExcludeSwitchesScope.() -> Unit): ExcludeSwitchesScope = excludeSwitchesScope.apply(block)

    /**
     * Configures browser local state preferences.
     *
     * @param block The configuration block for local state settings.
     * @return The configured [LocalStateScope].
     */
    @KolibriumDsl
    public fun localState(block: LocalStateScope.() -> Unit): LocalStateScope = localStateScope.apply(block)

    /**
     * Configures Chromium-specific preferences.
     *
     * @param block The configuration block for browser preferences.
     * @return The configured [ChromiumPreferencesScope].
     */
    @KolibriumDsl
    public fun preferences(block: ChromiumPreferencesScope.() -> Unit): ChromiumPreferencesScope = preferencesScope.apply(block)

    /**
     * Returns a string representation of the [ExperimentalOptionsScope], primarily for debugging purposes.
     */
    override fun toString(): String =
        "ExperimentalOptionsScope(excludeSwitchesScope=$excludeSwitchesScope, " +
            "localStateScope=$localStateScope, preferencesScope=$preferencesScope)"
}

/*
 * Copyright 2023 Attila Fazekas
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

@KolibriumDsl
public class ExperimentalOptionsScope<T : Chromium> {
    public val excludeSwitchesScope: ExcludeSwitchesScope by lazy { ExcludeSwitchesScope() }
    public val localStateScope: LocalStateScope by lazy { LocalStateScope() }
    public val preferencesScope: PreferencesScope<T> by lazy { PreferencesScope() }

    @KolibriumDsl
    public fun excludeSwitches(block: ExcludeSwitchesScope.() -> Unit): ExcludeSwitchesScope =
        excludeSwitchesScope.apply(block)

    @KolibriumDsl
    public fun localState(block: LocalStateScope.() -> Unit): LocalStateScope = localStateScope.apply(block)

    @KolibriumDsl
    public fun preferences(block: PreferencesScope<T>.() -> Unit): PreferencesScope<T> = preferencesScope.apply(block)

    override fun toString(): String {
        return "ExperimentalOptionsScope(excludeSwitchesScope=$excludeSwitchesScope, " +
            "localStateScope=$localStateScope, preferencesScope=$preferencesScope)"
    }
}

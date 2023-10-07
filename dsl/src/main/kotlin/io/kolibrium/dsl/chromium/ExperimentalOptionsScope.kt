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

package io.kolibrium.dsl.chromium

import io.kolibrium.dsl.Browser
import io.kolibrium.dsl.Chrome
import io.kolibrium.dsl.Chromium
import io.kolibrium.dsl.DriverScope
import io.kolibrium.dsl.Edge
import io.kolibrium.dsl.KolibriumDsl
import io.kolibrium.dsl.OptionsScope
import io.kolibrium.dsl.PreferencesScope
import org.openqa.selenium.chromium.ChromiumOptions
import org.openqa.selenium.remote.AbstractDriverOptions

public class ExperimentalOptionsScope<T : Browser> {
    public val preferencesScope: PreferencesScope<Chromium> by lazy { PreferencesScope() }
    public val switchesScope: SwitchesScope by lazy { SwitchesScope() }
    public val localStateScope: LocalStateScope by lazy { LocalStateScope() }

    @KolibriumDsl
    public fun preferences(block: PreferencesScope<Chromium>.() -> Unit): PreferencesScope<Chromium> =
        preferencesScope.apply(block)

    @KolibriumDsl
    public fun excludeSwitches(block: SwitchesScope.() -> Unit): SwitchesScope = switchesScope.apply(block)

    @KolibriumDsl
    public fun localState(block: LocalStateScope.() -> Unit): LocalStateScope = localStateScope.apply(block)
}

context(OptionsScope<Chrome>)
@KolibriumDsl
@JvmName("experimentalOptionsChrome")
public fun experimentalOptions(block: ExperimentalOptionsScope<Chromium>.() -> Unit): Unit =
    experimentalOptions(options, block)

context(OptionsScope<Edge>)
@KolibriumDsl
@JvmName("experimentalOptionsEdge")
public fun experimentalOptions(block: ExperimentalOptionsScope<Chromium>.() -> Unit): Unit =
    experimentalOptions(options, block)

context(DriverScope<Chrome>.OptionsScope)
@KolibriumDsl
@JvmName("experimentalOptionsChrome")
public fun experimentalOptions(block: ExperimentalOptionsScope<Chromium>.() -> Unit): Unit =
    experimentalOptions(options, block)

context(DriverScope<Edge>.OptionsScope)
@KolibriumDsl
@JvmName("experimentalOptionsEdge")
public fun experimentalOptions(block: ExperimentalOptionsScope<Chromium>.() -> Unit): Unit =
    experimentalOptions(options, block)

@SuppressWarnings("NestedBlockDepth")
private inline fun <reified T : Chromium> experimentalOptions(
    options: AbstractDriverOptions<*>,
    block: ExperimentalOptionsScope<T>.() -> Unit
) {
    val expOptionsScope = ExperimentalOptionsScope<T>().apply(block)
    with(expOptionsScope) {
        when (T::class) {
            Chromium::class -> {
                with((options as ChromiumOptions<*>)) {
                    if (preferencesScope.preferences.isNotEmpty()) {
                        setExperimentalOption("prefs", preferencesScope.preferences)
                    }
                    if (switchesScope.switches.isNotEmpty()) {
                        setExperimentalOption("excludeSwitches", switchesScope.switches)
                    }
                    if (localStateScope.localStatePrefs.isNotEmpty()) {
                        setExperimentalOption("localState", localStateScope.localStatePrefs)
                    }
                }
            }
        }
    }
}

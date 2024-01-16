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

import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.chromium.ChromiumOptions
import org.openqa.selenium.edge.EdgeOptions

@KolibriumDsl
public abstract class ChromiumOptionsScope(override val options: ChromiumOptions<*>) : OptionsScope() {
    protected val expOptionsScope: ExperimentalOptionsScope<Chromium> by lazy { ExperimentalOptionsScope() }
    protected val extensionsScope: ExtensionsScope by lazy { ExtensionsScope() }

    @KolibriumPropertyDsl
    public var binary: String? = null

    override fun configure() {
        super.configure()
        options.apply {
            binary?.let { setBinary(it) }
        }
    }

    @KolibriumDsl
    public fun experimentalOptions(block: ExperimentalOptionsScope<Chromium>.() -> Unit) {
        expOptionsScope.apply(block)
        with(expOptionsScope) {
            with(this@ChromiumOptionsScope.options) {
                if (preferencesScope.preferences.isNotEmpty()) {
                    setExperimentalOption("prefs", preferencesScope.preferences)
                }
                if (excludeSwitchesScope.switches.isNotEmpty()) {
                    setExperimentalOption("excludeSwitches", excludeSwitchesScope.switches)
                }
                if (localStateScope.experiments.experimentalFlags.isNotEmpty()) {
//                    val x = localStatePrefs["browser.enabled_labs_experiments"] = experiments.experimentalFlags

                    setExperimentalOption(
                        "localState",
                        mapOf("browser.enabled_labs_experiments" to localStateScope.experiments.experimentalFlags),
                    )
                }
            }
        }
    }

    @KolibriumDsl
    public fun extensions(block: ExtensionsScope.() -> Unit) {
        extensionsScope.apply(block)
        when (options) {
            is ChromeOptions -> options.addExtensions(extensionsScope.extensions.toList())

            is EdgeOptions -> options.addExtensions(extensionsScope.extensions.toList())
        }
    }
}

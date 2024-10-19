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

import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.chromium.ChromiumOptions
import org.openqa.selenium.edge.EdgeOptions

/**
 * Base scope class for configuring Chromium browser options.
 * Provides configuration options common to Chrome and Edge browsers.
 *
 * @property options The underlying [ChromiumOptions] instance being configured.
 */
@KolibriumDsl
public abstract class ChromiumOptionsScope(
    override val options: ChromiumOptions<*>,
) : OptionsScope() {
    protected val expOptionsScope: ExperimentalOptionsScope by lazy { ExperimentalOptionsScope() }
    protected val extensionsScope: ExtensionsScope by lazy { ExtensionsScope() }

    /**
     * Sets the path to the browser binary.
     */
    @KolibriumPropertyDsl
    public var binary: String? = null

    override fun configure() {
        super.configure()
        options.apply {
            binary?.let { setBinary(it) }
        }
    }

    /**
     * Configures experimental browser options.
     *
     * @param block The configuration block for experimental options.
     */
    @KolibriumDsl
    public fun experimentalOptions(block: ExperimentalOptionsScope.() -> Unit) {
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
                    setExperimentalOption(
                        "localState",
                        mapOf("browser.enabled_labs_experiments" to localStateScope.experiments.experimentalFlags),
                    )
                }
            }
        }
    }

    /**
     * Configures browser extensions.
     *
     * @param block The configuration block for extension settings.
     */
    @KolibriumDsl
    public fun extensions(block: ExtensionsScope.() -> Unit) {
        extensionsScope.apply(block)
        when (options) {
            is ChromeOptions -> options.addExtensions(extensionsScope.extensions.toList())

            is EdgeOptions -> options.addExtensions(extensionsScope.extensions.toList())
        }
    }
}

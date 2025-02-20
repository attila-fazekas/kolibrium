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
import org.openqa.selenium.chrome.ChromeOptions

/**
 * Scope class for configuring Chrome-specific options.
 *
 * This class extends [ChromiumOptionsScope] to provide Edge-specific configurations
 * while inheriting common Chromium browser options.
 *
 * @property options The underlying [ChromeOptions] instance being configured.
 */
@KolibriumDsl
public class ChromeOptionsScope(
    override val options: ChromeOptions,
) : ChromiumOptionsScope(options) {
    private val argsScope by lazy { ChromeArgumentsScope() }

    /**
     * Configures command-line arguments for Chrome browser.
     *
     * @param block The configuration block for Chrome-specific arguments.
     */
    @KolibriumDsl
    public fun arguments(block: ChromeArgumentsScope.() -> Unit) {
        argsScope.apply(block)
        options.addArguments(argsScope.args.map { it.value })
    }

    /**
     * Returns a string representation of the [ChromeOptionsScope], primarily for debugging purposes.
     */
    override fun toString(): String =
        "ChromeOptionsScope(acceptInsecureCerts=$acceptInsecureCerts, argumentsScope=$argsScope, " +
            "binary=$binary, browserVersion=$browserVersion, experimentalOptionsScope=$expOptionsScope, " +
            "extensionsScope=$extensionsScope, pageLoadStrategy=$pageLoadStrategy, platform=$platform, " +
            "proxyScope=$proxyScope, strictFileInteractability=$strictFileInteractability, " +
            "timeoutsScope=$timeoutsScope, unhandledPromptBehaviour=$unhandledPromptBehaviour)"
}

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

import org.openqa.selenium.edge.EdgeOptions

/**
 * Scope class for configuring Edge browser-specific options.
 *
 * This class extends [ChromiumOptionsScope] to provide Edge-specific configurations
 * while inheriting common Chromium browser options.
 *
 * @property options The underlying [EdgeOptions] instance being configured.
 */
@KolibriumDsl
public class EdgeOptionsScope(
    override val options: EdgeOptions,
) : ChromiumOptionsScope(options) {
    private val argsScope by lazy { EdgeArgumentsScope() }

    /**
     * Configures whether to change the browser name to 'webview2' to enable test automation
     * of WebView2 apps with Edge WebDriver.
     */
    @KolibriumPropertyDsl
    public var useWebView: Boolean? = null

    override fun configure() {
        super.configure()
        options.apply {
            useWebView?.let { useWebView(it) }
        }
    }

    /**
     * Configures command-line arguments for Edge browser.
     *
     * @param block The configuration block for Edge-specific arguments.
     */
    @KolibriumDsl
    public fun arguments(block: EdgeArgumentsScope.() -> Unit) {
        argsScope.apply(block)
        options.addArguments(argsScope.args.map { it.value })
    }

    /**
     * Returns a string representation of the [EdgeOptionsScope], primarily for debugging purposes.
     */
    override fun toString(): String =
        "EdgeOptionsScope(acceptInsecureCerts=$acceptInsecureCerts, argumentsScope=$argsScope, " +
            "binary=$binary, browserVersion=$browserVersion, experimentalOptionsScope=$expOptionsScope, " +
            "extensionsScope=$extensionsScope, pageLoadStrategy=$pageLoadStrategy, platform=$platform, " +
            "proxyScope=$proxyScope, strictFileInteractability=$strictFileInteractability, " +
            "timeoutsScope=$timeoutsScope, unhandledPromptBehaviour=$unhandledPromptBehaviour, " +
            "useWebView=$useWebView)"
}

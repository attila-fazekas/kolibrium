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

package dev.kolibrium.dsl.creation

import dev.kolibrium.dsl.KolibriumDsl
import org.openqa.selenium.firefox.FirefoxOptions
import org.openqa.selenium.firefox.FirefoxProfile
import java.io.File

/**
 * Scope class for configuring Firefox browser-specific options.
 *
 * This class provides Firefox-specific configurations while inheriting common browser options
 * from [OptionsScope].
 *
 * @property options The underlying [FirefoxOptions] instance being configured.
 */
@KolibriumDsl
public class FirefoxOptionsScope(
    override val options: FirefoxOptions,
) : OptionsScope() {
    private val argsScope by lazy { FirefoxArgumentsScope() }
    private val preferencesScope by lazy { FirefoxPreferencesScope() }
    private val ffProfileScope by lazy { FirefoxProfileScope() }

    /**
     * Path to the Firefox binary executable.
     *
     * When set, Firefox will be launched using this specific binary instead of the system default.
     */
    public var binary: String? = null

    /**
     * Path to a custom Firefox profile directory.
     *
     * When set, Firefox will use this profile instead of creating a new temporary profile.
     */
    public var profileDir: String? = null

    override fun configure() {
        super.configure()
        options.apply {
            this@FirefoxOptionsScope.binary?.let { setBinary(it) }
            profileDir?.let { profile = FirefoxProfile(File(it)) }
        }
    }

    /**
     * Configures command-line arguments for Firefox browser.
     *
     * @param block The configuration block for Firefox-specific arguments.
     */
    @KolibriumDsl
    public fun arguments(block: FirefoxArgumentsScope.() -> Unit) {
        argsScope.apply(block)
        options.addArguments(argsScope.args.map { it.value })
    }

    /**
     * Configures Firefox preferences through FirefoxOptions.
     *
     * These preferences are applied directly to the Firefox options and take precedence
     * over preferences set in the Firefox profile.
     *
     * @param block The configuration block for Firefox preferences.
     */
    @KolibriumDsl
    public fun preferences(block: FirefoxPreferencesScope.() -> Unit) {
        preferencesScope.apply(block)
        if (preferencesScope.preferences.isNotEmpty()) {
            preferencesScope.preferences.forEach(options::addPreference)
        }
    }

    /**
     * Configures a custom Firefox profile with specific preferences.
     *
     * Creates a new Firefox profile and applies the specified preferences to it.
     *
     * @param block The configuration block for Firefox profile preferences.
     */
    @KolibriumDsl
    public fun profile(block: FirefoxProfileScope.() -> Unit) {
        ffProfileScope.apply(block)
        if (ffProfileScope.preferences.isNotEmpty()) {
            val profile = FirefoxProfile()
            ffProfileScope.preferences.forEach(profile::setPreference)
            options.profile = profile
        }
    }

    /**
     * Returns a string representation of the [FirefoxOptionsScope], primarily for debugging purposes.
     */
    override fun toString(): String =
        "FirefoxOptionsScope(acceptInsecureCerts=$acceptInsecureCerts, argumentsScope=$argsScope, " +
            "binary=$binary, browserVersion=$browserVersion, firefoxProfileScope=$ffProfileScope, " +
            "pageLoadStrategy=$pageLoadStrategy, platform=$platform, preferencesScope=$preferencesScope, " +
            "profileDir=$profileDir, proxyScope=$proxyScope, strictFileInteractability=$strictFileInteractability, " +
            "timeoutsScope=$timeoutsScope, unhandledPromptBehaviour=$unhandledPromptBehaviour)"
}

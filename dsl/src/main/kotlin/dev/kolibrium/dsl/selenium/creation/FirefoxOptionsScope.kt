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

import org.openqa.selenium.firefox.FirefoxOptions
import org.openqa.selenium.firefox.FirefoxProfile
import java.io.File

@KolibriumDsl
public class FirefoxOptionsScope(override val options: FirefoxOptions) : OptionsScope() {
    private val argsScope by lazy { ArgumentsScope<Firefox>() }
    private val preferencesScope by lazy { PreferencesScope<Firefox>() }
    private val ffProfileScope by lazy { FirefoxProfileScope() }

    @KolibriumPropertyDsl
    public var binary: String? = null

    @KolibriumPropertyDsl
    public var profileDir: String? = null

    override fun configure() {
        super.configure()
        options.apply {
            this@FirefoxOptionsScope.binary?.let { setBinary(it) }
            profileDir?.let { profile = FirefoxProfile(File(it)) }
        }
    }

    @KolibriumDsl
    public fun arguments(block: ArgumentsScope<Firefox>.() -> Unit) {
        argsScope.apply(block)
        options.addArguments(argsScope.args.map { it.value })
    }

    @KolibriumDsl
    public fun preferences(block: PreferencesScope<Firefox>.() -> Unit) {
        preferencesScope.apply(block)
        if (preferencesScope.preferences.isNotEmpty()) {
            preferencesScope.preferences.forEach(options::addPreference)
        }
    }

    @KolibriumDsl
    public fun profile(block: FirefoxProfileScope.() -> Unit) {
        ffProfileScope.apply(block)
        if (ffProfileScope.preferences.isNotEmpty()) {
            val profile = FirefoxProfile()
            ffProfileScope.preferences.forEach(profile::setPreference)
            options.profile = profile
        }
    }

    override fun toString(): String {
        return "FirefoxOptionsScope(acceptInsecureCerts=$acceptInsecureCerts, argumentsScope=$argsScope, " +
            "binary=$binary, browserVersion=$browserVersion, firefoxProfileScope=$ffProfileScope, " +
            "pageLoadStrategy=$pageLoadStrategy, platform=$platform, preferencesScope=$preferencesScope, " +
            "profileDir=$profileDir, proxyScope=$proxyScope, strictFileInteractability=$strictFileInteractability, " +
            "timeoutsScope=$timeoutsScope, unhandledPromptBehaviour=$unhandledPromptBehaviour)"
    }
}

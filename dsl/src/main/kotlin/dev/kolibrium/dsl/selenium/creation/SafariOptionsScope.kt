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
import dev.kolibrium.dsl.selenium.KolibriumPropertyDsl
import org.openqa.selenium.safari.SafariOptions

/**
 * Scope class for configuring Safari-specific options.
 *
 * This class provides Safari-specific configurations while inheriting common browser options
 * from [OptionsScope].
 *
 * @property options The underlying [SafariOptions] instance being configured.
 */
@KolibriumDsl
public class SafariOptionsScope(
    override val options: SafariOptions,
) : OptionsScope() {
    /**
     * Enables automatic inspection of web pages.
     */
    @KolibriumPropertyDsl
    public var automaticInspection: Boolean? = null

    /**
     * Enables automatic profiling of web pages.
     */
    @KolibriumPropertyDsl
    public var automaticProfiling: Boolean? = null

    /**
     * Enables the use of Safari Technology Preview.
     */
    @KolibriumPropertyDsl
    public var useTechnologyPreview: Boolean? = null

    override fun configure() {
        super.configure()
        options.apply {
            this@SafariOptionsScope.automaticInspection?.let { automaticInspection = it }
            this@SafariOptionsScope.automaticProfiling?.let { automaticProfiling = it }
            this@SafariOptionsScope.useTechnologyPreview?.let { useTechnologyPreview = it }
        }
    }

    /**
     * Returns a string representation of the [SafariOptionsScope], primarily for debugging purposes.
     */
    override fun toString(): String =
        "SafariOptionsScope(acceptInsecureCerts=$acceptInsecureCerts, " +
            "automaticInspection=$automaticInspection, automaticProfiling=$automaticProfiling, " +
            "browserVersion=$browserVersion, pageLoadStrategy=$pageLoadStrategy, platform=$platform, " +
            "proxyScope=$proxyScope, strictFileInteractability=$strictFileInteractability, " +
            "timeoutsScope=$timeoutsScope, unhandledPromptBehaviour=$unhandledPromptBehaviour, " +
            "useTechnologyPreview=$useTechnologyPreview)"
}

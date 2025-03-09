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
import org.openqa.selenium.PageLoadStrategy
import org.openqa.selenium.Platform
import org.openqa.selenium.Proxy
import org.openqa.selenium.UnexpectedAlertBehaviour
import org.openqa.selenium.remote.AbstractDriverOptions
import kotlin.time.toJavaDuration

/**
 * Base scope class for configuring browser-specific WebDriver options.
 * Provides common configuration options supported across different browsers.
 */
@KolibriumDsl
public sealed class OptionsScope {
    internal abstract val options: AbstractDriverOptions<*>

    protected val proxyScope: ProxyScope by lazy { ProxyScope() }
    protected val timeoutsScope: TimeoutsScope by lazy { TimeoutsScope() }

    /**
     * Configures whether to accept insecure certificates.
     */
    @KolibriumPropertyDsl
    public var acceptInsecureCerts: Boolean? = null

    /**
     * Sets the desired browser version.
     */
    @KolibriumPropertyDsl
    public var browserVersion: String? = null

    /**
     * Sets the page load strategy for the WebDriver.
     * @see PageLoadStrategy
     */
    @KolibriumPropertyDsl
    public var pageLoadStrategy: PageLoadStrategy? = null

    /**
     * Sets the desired platform where the WebDriver should run.
     * @see Platform
     */
    @KolibriumPropertyDsl
    public var platform: Platform? = null

    /**
     * Configures whether to enable strict interactability checks to input type=file elements.
     */
    @KolibriumPropertyDsl
    public var strictFileInteractability: Boolean? = null

    /**
     * Sets the behavior for handling unexpected alerts.
     * @see UnexpectedAlertBehaviour
     */
    @KolibriumPropertyDsl
    public var unhandledPromptBehaviour: UnexpectedAlertBehaviour? = null

    internal open fun configure() {
        options.apply {
            acceptInsecureCerts?.let { setAcceptInsecureCerts(it) }
            this@OptionsScope.browserVersion?.let { setBrowserVersion(it) }
            pageLoadStrategy?.let { setPageLoadStrategy(it) }
            platform?.let { setPlatformName(it.name) }
            strictFileInteractability?.let { setStrictFileInteractability(it) }
            unhandledPromptBehaviour?.let { setUnhandledPromptBehaviour(it) }
        }
    }

    /**
     * Configures various timeout settings for the WebDriver instance.
     *
     * @param block The configuration block for timeout settings.
     */
    @KolibriumDsl
    public fun timeouts(block: TimeoutsScope.() -> Unit) {
        options.apply {
            timeoutsScope.apply {
                block()
                implicitWait?.let { setImplicitWaitTimeout(it.toJavaDuration()) }
                pageLoad?.let { setPageLoadTimeout(it.toJavaDuration()) }
                script?.let { setScriptTimeout(it.toJavaDuration()) }
            }
        }
    }

    /**
     * Configures proxy settings for the WebDriver instance.
     *
     * @param block The configuration block for proxy settings.
     */
    @KolibriumDsl
    public fun proxy(block: ProxyScope.() -> Unit) {
        proxyScope.apply {
            block()
            proxyMap.apply {
                proxyType?.let { proxyMap["proxyType"] = it.name }
                autodetect?.let { proxyMap["autodetect"] = it }
                ftpProxy?.let { proxyMap["ftpProxy"] = it }
                httpProxy?.let { proxyMap["httpProxy"] = it }
                noProxy?.let { proxyMap["noProxy"] = it }
                sslProxy?.let { proxyMap["sslProxy"] = it }
                proxyAutoconfigUrl?.let { proxyMap["proxyAutoconfigUrl"] = it }
            }
        }

        if (proxyScope.proxyMap.isNotEmpty()) {
            options.setProxy(Proxy(proxyScope.proxyMap))
        }
    }

    /**
     * Returns a string representation of the [OptionsScope], primarily for debugging purposes.
     */
    override fun toString(): String =
        "OptionsScope(acceptInsecureCerts=$acceptInsecureCerts, browserVersion=$browserVersion, " +
            "pageLoadStrategy=$pageLoadStrategy, platform=$platform, proxyScope=$proxyScope, " +
            "strictFileInteractability=$strictFileInteractability, timeoutsScope=$timeoutsScope, " +
            "unhandledPromptBehaviour=$unhandledPromptBehaviour)"
}

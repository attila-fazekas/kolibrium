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

import org.openqa.selenium.PageLoadStrategy
import org.openqa.selenium.Platform
import org.openqa.selenium.Proxy
import org.openqa.selenium.UnexpectedAlertBehaviour
import org.openqa.selenium.remote.AbstractDriverOptions
import kotlin.time.toJavaDuration

@KolibriumDsl
public sealed class OptionsScope {

    internal abstract val options: AbstractDriverOptions<*>

    protected val proxyScope: ProxyScope by lazy { ProxyScope() }
    protected val timeoutsScope: TimeoutsScope by lazy { TimeoutsScope() }

    public var acceptInsecureCerts: Boolean? = null
    public var browserVersion: String? = null
    public var pageLoadStrategy: PageLoadStrategy? = null
    public var platform: Platform? = null
    public var strictFileInteractability: Boolean? = null
    public var unhandledPromptBehaviour: UnexpectedAlertBehaviour? = null

    internal open fun configure() {
        with(options) {
            acceptInsecureCerts?.let { setAcceptInsecureCerts(it) }
            this@OptionsScope.browserVersion?.let { setBrowserVersion(it) }
            pageLoadStrategy?.let { setPageLoadStrategy(it) }
            platform?.let { setPlatformName(it.name) }
            strictFileInteractability?.let { setStrictFileInteractability(it) }
            unhandledPromptBehaviour?.let { setUnhandledPromptBehaviour(it) }
        }
    }

    @KolibriumDsl
    public fun timeouts(block: TimeoutsScope.() -> Unit) {
        timeoutsScope.apply(block)
        options.apply {
            with(timeoutsScope) {
                implicitWait?.let { setImplicitWaitTimeout(it.toJavaDuration()) }
                pageLoad?.let { setPageLoadTimeout(it.toJavaDuration()) }
                script?.let { setScriptTimeout(it.toJavaDuration()) }
            }
        }
    }

    @KolibriumDsl
    public fun proxy(block: ProxyScope.() -> Unit) {
        proxyScope.apply(block)
        proxyScope.proxyMap.apply {
            with(proxyScope) {
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

    override fun toString(): String {
        return "OptionsScope(acceptInsecureCerts=$acceptInsecureCerts, browserVersion=$browserVersion, " +
            "pageLoadStrategy=$pageLoadStrategy, platform=$platform, proxyScope=$proxyScope, " +
            "strictFileInteractability=$strictFileInteractability, timeoutsScope=$timeoutsScope, " +
            "unhandledPromptBehaviour=$unhandledPromptBehaviour)"
    }
}

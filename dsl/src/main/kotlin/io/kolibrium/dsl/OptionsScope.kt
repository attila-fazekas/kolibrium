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

package io.kolibrium.dsl

import dev.drewhamilton.poko.Poko
import org.openqa.selenium.MutableCapabilities
import org.openqa.selenium.PageLoadStrategy
import org.openqa.selenium.Platform
import org.openqa.selenium.Proxy
import org.openqa.selenium.UnexpectedAlertBehaviour
import org.openqa.selenium.remote.AbstractDriverOptions
import kotlin.time.toJavaDuration

@Poko
public sealed class BaseOptionsScope(internal val options: AbstractDriverOptions<*>) {

    @KolibriumDsl
    public var acceptInsecureCerts: Boolean? = null

    @KolibriumDsl
    public var browserVersion: String? = null

    @KolibriumDsl
    public var platform: Platform? = null

    @KolibriumDsl
    public var pageLoadStrategy: PageLoadStrategy? = null

    @KolibriumDsl
    public var strictFileInteractability: Boolean? = null

    @KolibriumDsl
    public var unhandledPromptBehaviour: UnexpectedAlertBehaviour? = null

    internal fun configure(): BaseOptionsScope {
        options.apply {
            with(this@BaseOptionsScope) {
                acceptInsecureCerts?.let { setAcceptInsecureCerts(it) }
                browserVersion?.let { setBrowserVersion(it) }
                platform?.let { setPlatformName(it.name) }
                pageLoadStrategy?.let { setPageLoadStrategy(it) }
                strictFileInteractability?.let { setStrictFileInteractability(it) }
                unhandledPromptBehaviour?.let { setUnhandledPromptBehaviour(it) }
            }
        }
        return this
    }

    @KolibriumDsl
    public fun timeouts(block: TimeoutsScope.() -> Unit) {
        val timeoutsScope = TimeoutsScope().apply(block)
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
        val proxyScope = ProxyScope().apply(block)
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
}

public class OptionsScope<T : MutableCapabilities>(options: AbstractDriverOptions<*>) : BaseOptionsScope(options)

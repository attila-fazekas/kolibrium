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
import org.openqa.selenium.Proxy

/**
 * Scope class for configuring proxy settings.
 */
@KolibriumDsl
public class ProxyScope {
    internal val proxyMap = mutableMapOf<String, Any>()

    private val socksScope by lazy { SocksScope() }

    /**
     * Sets the proxy type to be used.
     * @see Proxy.ProxyType
     */
    @KolibriumPropertyDsl
    public var proxyType: Proxy.ProxyType? = null

    /**
     * Enables or disables proxy autodetection.
     */
    @KolibriumPropertyDsl
    public var autodetect: Boolean? = null

    /**
     * Sets the FTP proxy server address.
     */
    @KolibriumPropertyDsl
    public var ftpProxy: String? = null

    /**
     * Sets the HTTP proxy server address.
     */
    @KolibriumPropertyDsl
    public var httpProxy: String? = null

    /**
     * Specifies hosts that should bypass the proxy.
     */
    @KolibriumPropertyDsl
    public var noProxy: String? = null

    /**
     * Sets the HTTPS proxy server address.
     */
    @KolibriumPropertyDsl
    public var sslProxy: String? = null

    /**
     * Sets the URL for proxy autoconfiguration.
     */
    @KolibriumPropertyDsl
    public var proxyAutoconfigUrl: String? = null

    /**
     * Configures SOCKS proxy settings.
     *
     * @param block The configuration block for SOCKS proxy settings.
     */
    @KolibriumDsl
    public fun socks(block: SocksScope.() -> Unit) {
        socksScope.apply {
            block()
            address?.let { this@ProxyScope.proxyMap["socksProxy"] = it }
            version?.let { this@ProxyScope.proxyMap["socksVersion"] = it }
            username?.let { this@ProxyScope.proxyMap["socksUsername"] = it }
            password?.let { this@ProxyScope.proxyMap["socksPassword"] = it }
        }
    }

    /**
     * Returns a string representation of the [ProxyScope], primarily for debugging purposes.
     */
    override fun toString(): String = "ProxyScope(proxyMap=$proxyMap)"
}

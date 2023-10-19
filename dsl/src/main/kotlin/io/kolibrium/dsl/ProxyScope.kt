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

import org.openqa.selenium.Proxy

@KolibriumDsl
public class ProxyScope {
    internal val proxyMap = mutableMapOf<String, Any>()

    private val socksScope by lazy { SocksScope() }

    public var proxyType: Proxy.ProxyType? = null
    public var autodetect: Boolean? = null
    public var ftpProxy: String? = null
    public var httpProxy: String? = null
    public var noProxy: String? = null
    public var sslProxy: String? = null
    public var proxyAutoconfigUrl: String? = null

    @KolibriumDsl
    public fun socks(block: SocksScope.() -> Unit) {
        socksScope.apply(block)
        with(socksScope) {
            address?.let { this@ProxyScope.proxyMap["socksProxy"] = it }
            version?.let { this@ProxyScope.proxyMap["socksVersion"] = it }
            username?.let { this@ProxyScope.proxyMap["socksUsername"] = it }
            password?.let { this@ProxyScope.proxyMap["socksPassword"] = it }
        }
    }

    override fun toString(): String {
        return "ProxyScope(proxyMap=$proxyMap)"
    }
}

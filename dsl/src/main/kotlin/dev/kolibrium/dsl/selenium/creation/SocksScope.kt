/*
 * Copyright 2023-2024 Attila Fazekas & contributors
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

/**
 * Scope  for configuring SOCKS proxy settings.
 */
@KolibriumDsl
public class SocksScope {
    /**
     * The SOCKS proxy server address.
     */
    @KolibriumPropertyDsl
    public var address: String? = null

    /**
     * The SOCKS protocol version.
     */
    @KolibriumPropertyDsl
    public var version: Int? = null

    /**
     * Username for SOCKS proxy authentication.
     */
    @KolibriumPropertyDsl
    public var username: String? = null

    /**
     * Password for SOCKS proxy authentication.
     */
    @KolibriumPropertyDsl
    public var password: String? = null

    /**
     * Returns a string representation of the [SocksScope], primarily for debugging purposes.
     */
    override fun toString(): String = "SocksScope(address=$address, version=$version, username=$username, password=$password)"
}

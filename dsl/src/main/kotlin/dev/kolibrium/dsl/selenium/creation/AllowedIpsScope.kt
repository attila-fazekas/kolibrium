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

/**
 * Scope class for configuring allowed IP addresses for incoming connections to Chromium-based driver services.
 *
 * This scope enables specification of IPv4 addresses that are allowed to connect to the driver service,
 * providing a security mechanism for controlling access to the browser automation interface.
 */
@KolibriumDsl
public class AllowedIpsScope {
    internal val allowedIps = mutableSetOf<String>()

    /**
     * Adds an allowed IP address.
     *
     * This operator function allows adding allowed IP addresses using the unary plus operator (+).
     */
    public operator fun String.unaryPlus() {
        allowedIps.add(this)
    }

    /**
     * Returns a string representation of the [AllowedIpsScope], primarily for debugging purposes.
     */
    override fun toString(): String = "AllowedIpsScope(allowedIps=$allowedIps)"
}

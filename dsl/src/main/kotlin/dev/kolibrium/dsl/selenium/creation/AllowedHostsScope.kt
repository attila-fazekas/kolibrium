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
 * DSL scope for specifying allowed hosts.
 *
 * Use this scope to define values of the Host header to allow for incoming requests.
 */
@KolibriumDsl
public class AllowedHostsScope {
    internal val allowedHosts = mutableSetOf<String>()

    /**
     * Adds a hostname to the set of allowed hosts.
     *
     * This operator function allows adding allowed hosts using the unary plus operator (+).
     */
    public operator fun String.unaryPlus() {
        allowedHosts.add(this)
    }

    /**
     * Returns a string representation of the [AllowedHostsScope], primarily for debugging purposes.
     */
    override fun toString(): String = "AllowedHostsScope(allowedHosts=$allowedHosts)"
}

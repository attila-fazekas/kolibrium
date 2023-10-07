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

import org.apache.commons.validator.routines.InetAddressValidator
import org.openqa.selenium.chrome.ChromeDriverService
import org.openqa.selenium.edge.EdgeDriverService
import org.openqa.selenium.firefox.GeckoDriverService
import org.openqa.selenium.remote.service.DriverService

public class AllowedIpsScope<T : Browser> : UnaryPlus<String> {
    internal val allowedIps = mutableSetOf<String>()

    override fun String.unaryPlus() {
        allowedIps.add(this)
    }
}

public typealias AllowedHostsScope = AllowedIpsScope<Browser>

@KolibriumDsl
@JvmName("allowedIpsChrome")
public fun DriverServiceScope<Chrome>.allowedIps(block: AllowedIpsScope<Chromium>.() -> Unit): Unit =
    allowedIps(builder, block)

@KolibriumDsl
@JvmName("allowedIpsChrome")
public fun DriverScope<Chrome>.DriverServiceScope.allowedIps(block: AllowedIpsScope<Chromium>.() -> Unit): Unit =
    allowedIps(builder, block)

@KolibriumDsl
@JvmName("allowedIpsEdge")
public fun DriverServiceScope<Edge>.allowedIps(block: AllowedIpsScope<Chromium>.() -> Unit): Unit =
    allowedIps(builder, block)

@KolibriumDsl
@JvmName("allowedIpsEdge")
public fun DriverScope<Edge>.DriverServiceScope.allowedIps(block: AllowedIpsScope<Chromium>.() -> Unit): Unit =
    allowedIps(builder, block)

@KolibriumDsl
public fun DriverServiceScope<Firefox>.allowedHosts(block: AllowedHostsScope.() -> Unit): Unit =
    allowedIps(builder, block)

@KolibriumDsl
public fun DriverScope<Firefox>.DriverServiceScope.allowedHosts(block: AllowedHostsScope.() -> Unit): Unit =
    allowedIps(builder, block)

internal fun <T : Browser> allowedIps(builder: DriverService.Builder<*, *>, block: AllowedIpsScope<T>.() -> Unit) {
    val allowedIpsScope = AllowedIpsScope<T>().apply(block)
    with(allowedIpsScope.allowedIps) {
        if (isNotEmpty()) {
            val invalidIPAddresses = filter { !InetAddressValidator.getInstance().isValid(it) }

            check(invalidIPAddresses.isEmpty()) { "Following IP addresses are invalid: $invalidIPAddresses" }

            when (builder) {
                is ChromeDriverService.Builder -> builder.withAllowedListIps(joinToString(separator = ", "))

                is GeckoDriverService.Builder -> builder.withAllowHosts(joinToString(separator = " "))

                is EdgeDriverService.Builder -> builder.withAllowedListIps(joinToString(separator = ", "))
            }
        }
    }
}

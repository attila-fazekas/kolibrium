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

public class AllowedIpsScope : UnaryPlus<String> {
    internal val allowedIps = mutableSetOf<String>()

    override fun String.unaryPlus() {
        allowedIps.add(this)
    }
}

internal fun allowedIps(builder: DriverService.Builder<*, *>, block: AllowedIpsScope.() -> Unit) {
    val allowedIpsScope = AllowedIpsScope().apply(block)
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

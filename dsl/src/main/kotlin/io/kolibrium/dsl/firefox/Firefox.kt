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

package io.kolibrium.dsl.firefox

import io.kolibrium.dsl.AllowedIpsScope
import io.kolibrium.dsl.DriverServiceScope
import io.kolibrium.dsl.KolibriumDsl
import io.kolibrium.dsl.allowedIps
import io.kolibrium.dsl.internal.threadLocalLazyDelegate
import org.openqa.selenium.firefox.FirefoxDriverLogLevel
import org.openqa.selenium.firefox.GeckoDriverService

@KolibriumDsl
public var DriverServiceScope<GeckoDriverService>.executable: String? by threadLocalLazyDelegate()

@KolibriumDsl
public var DriverServiceScope<GeckoDriverService>.logFile: String? by threadLocalLazyDelegate()

@KolibriumDsl
public var DriverServiceScope<GeckoDriverService>.logLevel: FirefoxDriverLogLevel? by threadLocalLazyDelegate()

@KolibriumDsl
public var DriverServiceScope<GeckoDriverService>.profileRoot: String? by threadLocalLazyDelegate()

@KolibriumDsl
public var DriverServiceScope<GeckoDriverService>.truncatedLogs: Boolean? by threadLocalLazyDelegate()

public typealias AllowedHostsScope = AllowedIpsScope

@KolibriumDsl
public fun DriverServiceScope<GeckoDriverService>.allowedHosts(block: AllowedHostsScope.() -> Unit): Unit =
    allowedIps(builder, block)

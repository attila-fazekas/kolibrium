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

import io.kolibrium.dsl.internal.threadLocalLazyDelegate
import org.openqa.selenium.chromium.ChromiumDriverLogLevel
import org.openqa.selenium.edge.EdgeDriver
import org.openqa.selenium.edge.EdgeDriverService
import org.openqa.selenium.edge.EdgeOptions

@KolibriumDsl
public var DriverServiceScope<EdgeDriverService>.appendLog: Boolean? by threadLocalLazyDelegate()

@KolibriumDsl
public var DriverScope<EdgeDriver>.DriverServiceScope.appendLog: Boolean? by threadLocalLazyDelegate()

@KolibriumDsl
public var DriverServiceScope<EdgeDriverService>.buildCheckDisabled: Boolean? by threadLocalLazyDelegate()

@KolibriumDsl
public var DriverScope<EdgeDriver>.DriverServiceScope.buildCheckDisabled: Boolean? by threadLocalLazyDelegate()

@KolibriumDsl
public var DriverServiceScope<EdgeDriverService>.executable: String? by threadLocalLazyDelegate()

@KolibriumDsl
public var DriverScope<EdgeDriver>.DriverServiceScope.executable: String? by threadLocalLazyDelegate()

@KolibriumDsl
public var DriverServiceScope<EdgeDriverService>.logFile: String? by threadLocalLazyDelegate()

@KolibriumDsl
public var DriverScope<EdgeDriver>.DriverServiceScope.logFile: String? by threadLocalLazyDelegate()

@KolibriumDsl
public var DriverServiceScope<EdgeDriverService>.logLevel: ChromiumDriverLogLevel? by threadLocalLazyDelegate()

@KolibriumDsl
public var DriverScope<EdgeDriver>.DriverServiceScope.logLevel: ChromiumDriverLogLevel? by threadLocalLazyDelegate()

@KolibriumDsl
public var DriverServiceScope<EdgeDriverService>.readableTimestamp: Boolean? by threadLocalLazyDelegate()

@KolibriumDsl
public var DriverScope<EdgeDriver>.DriverServiceScope.readableTimestamp: Boolean? by threadLocalLazyDelegate()

@KolibriumDsl
public var OptionsScope<EdgeOptions>.binary: String? by threadLocalLazyDelegate()

@KolibriumDsl
public var DriverScope<EdgeDriver>.OptionsScope.binary: String? by threadLocalLazyDelegate()

@KolibriumDsl
public var OptionsScope<EdgeOptions>.useWebView: Boolean? by threadLocalLazyDelegate()

@KolibriumDsl
public var DriverScope<EdgeDriver>.OptionsScope.useWebView: Boolean? by threadLocalLazyDelegate()

@KolibriumDsl
public fun DriverServiceScope<EdgeDriverService>.allowedIps(block: AllowedIpsScope.() -> Unit): Unit =
    allowedIps(builder, block)

@KolibriumDsl
public fun DriverScope<EdgeDriver>.DriverServiceScope.allowedIps(block: AllowedIpsScope.() -> Unit): Unit =
    allowedIps(builder, block)

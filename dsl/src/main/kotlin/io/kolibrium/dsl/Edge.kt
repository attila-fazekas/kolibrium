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

@KolibriumDsl
public var DriverServiceScope<Edge>.appendLog: Boolean? by threadLocalLazyDelegate()

@KolibriumDsl
public var DriverScope<Edge>.DriverServiceScope.appendLog: Boolean? by threadLocalLazyDelegate()

@KolibriumDsl
public var DriverServiceScope<Edge>.buildCheckDisabled: Boolean? by threadLocalLazyDelegate()

@KolibriumDsl
public var DriverScope<Edge>.DriverServiceScope.buildCheckDisabled: Boolean? by threadLocalLazyDelegate()

@KolibriumDsl
public var DriverServiceScope<Edge>.executable: String? by threadLocalLazyDelegate()

@KolibriumDsl
public var DriverScope<Edge>.DriverServiceScope.executable: String? by threadLocalLazyDelegate()

@KolibriumDsl
public var DriverServiceScope<Edge>.logFile: String? by threadLocalLazyDelegate()

@KolibriumDsl
public var DriverScope<Edge>.DriverServiceScope.logFile: String? by threadLocalLazyDelegate()

@KolibriumDsl
public var DriverServiceScope<Edge>.logLevel: ChromiumDriverLogLevel? by threadLocalLazyDelegate()

@KolibriumDsl
public var DriverScope<Edge>.DriverServiceScope.logLevel: ChromiumDriverLogLevel? by threadLocalLazyDelegate()

@KolibriumDsl
public var DriverServiceScope<Edge>.readableTimestamp: Boolean? by threadLocalLazyDelegate()

@KolibriumDsl
public var DriverScope<Edge>.DriverServiceScope.readableTimestamp: Boolean? by threadLocalLazyDelegate()

@KolibriumDsl
public var OptionsScope<Edge>.binary: String? by threadLocalLazyDelegate()

@KolibriumDsl
public var DriverScope<Edge>.OptionsScope.binary: String? by threadLocalLazyDelegate()

@KolibriumDsl
public var OptionsScope<Edge>.useWebView: Boolean? by threadLocalLazyDelegate()

@KolibriumDsl
public var DriverScope<Edge>.OptionsScope.useWebView: Boolean? by threadLocalLazyDelegate()

@KolibriumDsl
public fun DriverServiceScope<Edge>.allowedIps(block: AllowedIpsScope.() -> Unit): Unit =
    allowedIps(builder, block)

@KolibriumDsl
public fun DriverScope<Edge>.DriverServiceScope.allowedIps(block: AllowedIpsScope.() -> Unit): Unit =
    allowedIps(builder, block)

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
import io.kolibrium.dsl.Argument
import io.kolibrium.dsl.ArgumentsScope
import io.kolibrium.dsl.DriverScope
import io.kolibrium.dsl.DriverServiceScope
import io.kolibrium.dsl.KolibriumDsl
import io.kolibrium.dsl.OptionsScope
import io.kolibrium.dsl.PreferencesScope
import io.kolibrium.dsl.WindowSizeScope
import io.kolibrium.dsl.allowedIps
import io.kolibrium.dsl.internal.threadLocalLazyDelegate
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.firefox.FirefoxDriverLogLevel
import org.openqa.selenium.firefox.FirefoxOptions
import org.openqa.selenium.firefox.FirefoxProfile
import org.openqa.selenium.firefox.GeckoDriverService
import org.openqa.selenium.remote.AbstractDriverOptions

@KolibriumDsl
public var DriverServiceScope<GeckoDriverService>.executable: String? by threadLocalLazyDelegate()

@KolibriumDsl
public var DriverScope<FirefoxDriver>.DriverServiceScope.executable: String? by threadLocalLazyDelegate()

@KolibriumDsl
public var DriverServiceScope<GeckoDriverService>.logFile: String? by threadLocalLazyDelegate()

@KolibriumDsl
public var DriverScope<FirefoxDriver>.DriverServiceScope.logFile: String? by threadLocalLazyDelegate()

@KolibriumDsl
public var DriverServiceScope<GeckoDriverService>.logLevel: FirefoxDriverLogLevel? by threadLocalLazyDelegate()

@KolibriumDsl
public var DriverScope<FirefoxDriver>.DriverServiceScope.logLevel: FirefoxDriverLogLevel? by threadLocalLazyDelegate()

@KolibriumDsl
public var DriverServiceScope<GeckoDriverService>.profileRoot: String? by threadLocalLazyDelegate()

@KolibriumDsl
public var DriverScope<FirefoxDriver>.DriverServiceScope.profileRoot: String? by threadLocalLazyDelegate()

@KolibriumDsl
public var DriverServiceScope<GeckoDriverService>.truncatedLogs: Boolean? by threadLocalLazyDelegate()

@KolibriumDsl
public var DriverScope<FirefoxDriver>.DriverServiceScope.truncatedLogs: Boolean? by threadLocalLazyDelegate()

@KolibriumDsl
public var OptionsScope<FirefoxOptions>.binary: String? by threadLocalLazyDelegate()

@KolibriumDsl
public var DriverScope<FirefoxDriver>.OptionsScope.binary: String? by threadLocalLazyDelegate()

@KolibriumDsl
public var OptionsScope<FirefoxOptions>.profileDir: String? by threadLocalLazyDelegate()

@KolibriumDsl
public var DriverScope<FirefoxDriver>.OptionsScope.profileDir: String? by threadLocalLazyDelegate()

public typealias AllowedHostsScope = AllowedIpsScope

@KolibriumDsl
public fun DriverServiceScope<GeckoDriverService>.allowedHosts(block: AllowedHostsScope.() -> Unit): Unit =
    allowedIps(builder, block)

@KolibriumDsl
public fun OptionsScope<FirefoxOptions>.preferences(block: PreferencesScope.() -> Unit): Unit =
    preferences(options, block)

@KolibriumDsl
public fun DriverScope<FirefoxDriver>.OptionsScope.preferences(block: PreferencesScope.() -> Unit): Unit =
    preferences(options, block)

private fun preferences(options: AbstractDriverOptions<*>, block: PreferencesScope.() -> Unit) {
    val preferencesScope = PreferencesScope().apply(block)
    if (preferencesScope.preferences.isNotEmpty()) {
        preferencesScope.preferences.forEach((options as FirefoxOptions)::addPreference)
    }
}

@KolibriumDsl
public fun OptionsScope<FirefoxOptions>.profile(block: FirefoxProfileScope.() -> Unit): Unit = profile(options, block)

@KolibriumDsl
public fun DriverScope<FirefoxDriver>.OptionsScope.profile(block: FirefoxProfileScope.() -> Unit): Unit =
    profile(options, block)

private fun profile(options: AbstractDriverOptions<*>, block: FirefoxProfileScope.() -> Unit) {
    val ffProfileScope = FirefoxProfileScope().apply(block)
    if (ffProfileScope.preferences.isNotEmpty()) {
        val profile = FirefoxProfile()
        ffProfileScope.preferences.forEach(profile::setPreference)
        (options as FirefoxOptions).profile = profile
    }
}

context(OptionsScope<FirefoxOptions>, ArgumentsScope)
@KolibriumDsl
public fun windowSize(block: WindowSizeScope.() -> Unit): Unit = setWindowSize(block)

context(DriverScope<FirefoxDriver>, ArgumentsScope)
@KolibriumDsl
public fun windowSize(block: WindowSizeScope.() -> Unit): Unit = setWindowSize(block)

context(ArgumentsScope)
private fun setWindowSize(block: WindowSizeScope.() -> Unit) {
    val windowSizeScope = WindowSizeScope().apply(block)
    this@ArgumentsScope.args.add(Argument("--height=${windowSizeScope.width}"))
    this@ArgumentsScope.args.add(Argument("--width=${windowSizeScope.height}"))
}

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

@file:Suppress("UNCHECKED_CAST", "TooManyFunctions")

package io.kolibrium.dsl

import io.kolibrium.dsl.BrowserType.CHROME
import io.kolibrium.dsl.BrowserType.EDGE
import io.kolibrium.dsl.BrowserType.FIREFOX
import io.kolibrium.dsl.BrowserType.SAFARI
import io.kolibrium.dsl.chromium.chrome.appendLog
import io.kolibrium.dsl.chromium.chrome.binary
import io.kolibrium.dsl.chromium.chrome.buildCheckDisabled
import io.kolibrium.dsl.chromium.chrome.executable
import io.kolibrium.dsl.chromium.chrome.logFile
import io.kolibrium.dsl.chromium.chrome.logLevel
import io.kolibrium.dsl.chromium.chrome.readableTimestamp
import io.kolibrium.dsl.chromium.edge.appendLog
import io.kolibrium.dsl.chromium.edge.binary
import io.kolibrium.dsl.chromium.edge.buildCheckDisabled
import io.kolibrium.dsl.chromium.edge.executable
import io.kolibrium.dsl.chromium.edge.logFile
import io.kolibrium.dsl.chromium.edge.logLevel
import io.kolibrium.dsl.chromium.edge.readableTimestamp
import io.kolibrium.dsl.chromium.edge.useWebView
import io.kolibrium.dsl.firefox.binary
import io.kolibrium.dsl.firefox.executable
import io.kolibrium.dsl.firefox.logFile
import io.kolibrium.dsl.firefox.logLevel
import io.kolibrium.dsl.firefox.profileDir
import io.kolibrium.dsl.firefox.profileRoot
import io.kolibrium.dsl.firefox.truncatedLogs
import io.kolibrium.dsl.safari.automaticInspection
import io.kolibrium.dsl.safari.automaticProfiling
import io.kolibrium.dsl.safari.logging
import io.kolibrium.dsl.safari.useTechnologyPreview
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeDriverService
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.edge.EdgeDriver
import org.openqa.selenium.edge.EdgeDriverService
import org.openqa.selenium.edge.EdgeOptions
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.firefox.FirefoxOptions
import org.openqa.selenium.firefox.FirefoxProfile
import org.openqa.selenium.firefox.GeckoDriverService
import org.openqa.selenium.remote.AbstractDriverOptions
import org.openqa.selenium.remote.service.DriverService
import org.openqa.selenium.safari.SafariDriver
import org.openqa.selenium.safari.SafariDriverService
import org.openqa.selenium.safari.SafariOptions
import java.io.File
import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.FUNCTION
import kotlin.annotation.AnnotationTarget.PROPERTY
import kotlin.annotation.AnnotationTarget.TYPEALIAS

@DslMarker
@Target(FUNCTION, PROPERTY, CLASS, TYPEALIAS)
internal annotation class KolibriumDsl

@KolibriumDsl
public fun chromeDriver(block: DriverScope<Chrome>.() -> Unit): ChromeDriver = driver<Chrome>(block) as ChromeDriver

@KolibriumDsl
public fun firefoxDriver(block: DriverScope<Firefox>.() -> Unit): FirefoxDriver =
    driver<Firefox>(block) as FirefoxDriver

@KolibriumDsl
public fun safariDriver(block: DriverScope<Safari>.() -> Unit): SafariDriver = driver<Safari>(block) as SafariDriver

@KolibriumDsl
public fun edgeDriver(block: DriverScope<Edge>.() -> Unit): EdgeDriver = driver<Edge>(block) as EdgeDriver

@KolibriumDsl
public fun driver(browser: BrowserType, block: DriverScope<Browser>.() -> Unit): WebDriver = when (browser) {
    CHROME -> driver(block as (DriverScope<Chrome>.() -> Unit))
    FIREFOX -> driver(block as (DriverScope<Firefox>.() -> Unit))
    SAFARI -> driver(block as (DriverScope<Safari>.() -> Unit))
    EDGE -> driver(block as (DriverScope<Edge>.() -> Unit))
}

internal inline fun <reified T : Browser> driver(noinline block: DriverScope<T>.() -> Unit): WebDriver =
    when (T::class) {
        Chrome::class -> {
            val driverScope =
                driverScope(ChromeDriverService.Builder(), ChromeOptions(), block) as DriverScope<Chrome>
            configureChromeDriver(driverScope)
        }

        Firefox::class -> {
            val driverScope =
                driverScope(GeckoDriverService.Builder(), FirefoxOptions(), block) as DriverScope<Firefox>
            configureFirefoxDriver(driverScope)
        }

        Safari::class -> {
            val driverScope =
                driverScope(SafariDriverService.Builder(), SafariOptions(), block) as DriverScope<Safari>
            configureSafariDriver(driverScope)
        }

        Edge::class -> {
            val driverScope =
                driverScope(EdgeDriverService.Builder(), EdgeOptions(), block) as DriverScope<Edge>
            configureEdgeDriver(driverScope)
        }

        else -> throw UnsupportedOperationException()
    }

internal fun <T : Browser> driverScope(
    builder: DriverService.Builder<*, *>,
    options: AbstractDriverOptions<*>,
    block: DriverScope<T>.() -> Unit
): DriverScope<T> = DriverScope<T>(builder, options).apply(block).validate()

@SuppressWarnings("NestedBlockDepth")
internal fun configureChromeDriver(driverScope: DriverScope<Chrome>): ChromeDriver {
    with(driverScope) {
        val driverService = (driverServiceScope.builder as ChromeDriverService.Builder).apply {
            with(driverServiceScope) {
                appendLog?.let { withAppendLog(it) }
                buildCheckDisabled?.let { withBuildCheckDisabled(it) }
                executable?.let {
                    ifExists(it).run {
                        usingDriverExecutable(File(it))
                    }
                }
                logFile?.let { withLogFile(File(it)) }
                logLevel?.let { withLogLevel(it) }
                readableTimestamp?.let { withReadableTimestamp(it) }
            }
        }.build()

        val options = (optionsScope.options as ChromeOptions).apply {
            optionsScope.binary?.let { setBinary(it) }
        }

        return ChromeDriver(driverService, options)
    }
}

@SuppressWarnings("NestedBlockDepth")
internal fun configureFirefoxDriver(driverScope: DriverScope<Firefox>): FirefoxDriver {
    with(driverScope) {
        val driverService = (driverServiceScope.builder as GeckoDriverService.Builder).apply {
            with(driverServiceScope) {
                executable?.let {
                    ifExists(it).run {
                        usingDriverExecutable(File(it))
                    }
                }
                logFile?.let { withLogFile(File(it)) }
                logLevel?.let { withLogLevel(it) }
                profileRoot?.let { withProfileRoot(File(it)) }
                truncatedLogs?.let { withTruncatedLogs(it) }
            }
        }.build()

        val options = (optionsScope.options as FirefoxOptions).apply {
            with(optionsScope) {
                binary?.let { setBinary(it) }
                profileDir?.let { profile = FirefoxProfile(File(it)) }
            }
        }

        return FirefoxDriver(driverService, options)
    }
}

@SuppressWarnings("NestedBlockDepth")
internal fun configureSafariDriver(driverScope: DriverScope<Safari>): SafariDriver {
    with(driverScope) {
        val driverService = (driverServiceScope.builder as SafariDriverService.Builder).apply {
            driverServiceScope.logging?.let { withLogging(it) }
        }.build()

        val options = (optionsScope.options as SafariOptions).apply {
            with(optionsScope) {
                automaticInspection?.let { setAutomaticInspection(it) }
                automaticProfiling?.let { setAutomaticProfiling(it) }
                useTechnologyPreview?.let { setUseTechnologyPreview(it) }
            }
        }

        return SafariDriver(driverService, options)
    }
}

@SuppressWarnings("NestedBlockDepth")
internal fun configureEdgeDriver(driverScope: DriverScope<Edge>): EdgeDriver {
    with(driverScope) {
        val driverService = (driverServiceScope.builder as EdgeDriverService.Builder).apply {
            with(driverServiceScope) {
                appendLog?.let { withAppendLog(it) }
                buildCheckDisabled?.let { withBuildCheckDisabled(it) }
                executable?.let {
                    ifExists(it).run {
                        usingDriverExecutable(File(it))
                    }
                }
                logFile?.let { withLogFile(File(it)) }
                logLevel?.let { withLoglevel(it) }
                readableTimestamp?.let { withReadableTimestamp(it) }
            }
        }.build()

        val options = (optionsScope.options as EdgeOptions).apply {
            optionsScope.binary?.let { setBinary(it) }
            optionsScope.useWebView?.let { useWebView(it) }
        }

        return EdgeDriver(driverService, options)
    }
}

@KolibriumDsl
public fun chromeDriverService(block: DriverServiceScope<Chrome>.() -> Unit): ChromeDriverService =
    driverService<Chrome>(block) as ChromeDriverService

@KolibriumDsl
public fun geckoDriverService(block: DriverServiceScope<Firefox>.() -> Unit): GeckoDriverService =
    driverService<Firefox>(block) as GeckoDriverService

@KolibriumDsl
public fun safariDriverService(block: DriverServiceScope<Safari>.() -> Unit): SafariDriverService =
    driverService<Safari>(block) as SafariDriverService

@KolibriumDsl
public fun edgeDriverService(block: DriverServiceScope<Edge>.() -> Unit): EdgeDriverService =
    driverService<Edge>(block) as EdgeDriverService

@KolibriumDsl
public fun driverService(browser: BrowserType, block: DriverServiceScope<Browser>.() -> Unit): DriverService =
    when (browser) {
        CHROME -> driverService(block as (DriverServiceScope<Chrome>.() -> Unit))
        FIREFOX -> driverService(block as (DriverServiceScope<Firefox>.() -> Unit))
        SAFARI -> driverService(block as (DriverServiceScope<Safari>.() -> Unit))
        EDGE -> driverService(block as (DriverServiceScope<Edge>.() -> Unit))
    }

internal inline fun <reified T : Browser> driverService(
    noinline block: DriverServiceScope<T>.() -> Unit
): DriverService {
    return when (T::class) {
        Chrome::class -> {
            val driverServiceScope =
                driverServiceScope(ChromeDriverService.Builder(), block) as DriverServiceScope<Chrome>
            configureChromeDriverService(driverServiceScope)
        }

        Firefox::class -> {
            val driverServiceScope =
                driverServiceScope(GeckoDriverService.Builder(), block) as DriverServiceScope<Firefox>
            configureGeckoDriverService(driverServiceScope)
        }

        Safari::class -> {
            val driverServiceScope =
                driverServiceScope(SafariDriverService.Builder(), block) as DriverServiceScope<Safari>
            configureSafariDriverService(driverServiceScope)
        }

        Edge::class -> {
            val driverServiceScope =
                driverServiceScope(EdgeDriverService.Builder(), block) as DriverServiceScope<Edge>
            configureEdgeDriverService(driverServiceScope)
        }

        else -> throw UnsupportedOperationException()
    }.build()
}

internal fun <T : Browser> driverServiceScope(
    builder: DriverService.Builder<*, *>,
    block: DriverServiceScope<T>.() -> Unit
): BaseDriverServiceScope = DriverServiceScope<T>(builder).apply(block)
    .checkPort()
    .configure()

@SuppressWarnings("NestedBlockDepth")
internal fun configureChromeDriverService(driverServiceScope: DriverServiceScope<Chrome>):
    ChromeDriverService.Builder = (driverServiceScope.builder as ChromeDriverService.Builder).apply {
    with(driverServiceScope) {
        appendLog?.let { withAppendLog(it) }
        buildCheckDisabled?.let { withBuildCheckDisabled(it) }
        executable?.let {
            ifExists(it).run {
                usingDriverExecutable(File(it))
            }
        }
        logFile?.let { withLogFile(File(it)) }
        logLevel?.let { withLogLevel(it) }
        readableTimestamp?.let { withReadableTimestamp(it) }
    }
}

@SuppressWarnings("NestedBlockDepth")
internal fun configureGeckoDriverService(driverServiceScope: DriverServiceScope<Firefox>):
    GeckoDriverService.Builder = (driverServiceScope.builder as GeckoDriverService.Builder).apply {
    with(driverServiceScope) {
        executable?.let {
            ifExists(it).run {
                usingDriverExecutable(File(it))
            }
        }
        logFile?.let { withLogFile(File(it)) }
        logLevel?.let { withLogLevel(it) }
        profileRoot?.let { withProfileRoot(File(it)) }
        truncatedLogs?.let { withTruncatedLogs(it) }
    }
}

internal fun configureSafariDriverService(driverServiceScope: DriverServiceScope<Safari>):
    SafariDriverService.Builder = (driverServiceScope.builder as SafariDriverService.Builder).apply {
    driverServiceScope.logging?.let { withLogging(it) }
}

@SuppressWarnings("NestedBlockDepth")
internal fun configureEdgeDriverService(driverServiceScope: DriverServiceScope<Edge>):
    EdgeDriverService.Builder = (driverServiceScope.builder as EdgeDriverService.Builder).apply {
    with(driverServiceScope) {
        appendLog?.let { withAppendLog(it) }
        buildCheckDisabled?.let { withBuildCheckDisabled(it) }
        executable?.let {
            ifExists(it).run {
                usingDriverExecutable(File(it))
            }
        }
        logFile?.let { withLogFile(File(it)) }
        logLevel?.let { withLoglevel(it) }
        readableTimestamp?.let { withReadableTimestamp(it) }
    }
}

@KolibriumDsl
public fun chromeOptions(block: OptionsScope<Chrome>.() -> Unit): ChromeOptions =
    options<Chrome>(block) as ChromeOptions

@KolibriumDsl
public fun firefoxOptions(block: OptionsScope<Firefox>.() -> Unit): FirefoxOptions =
    options<Firefox>(block) as FirefoxOptions

@KolibriumDsl
public fun safariOptions(block: OptionsScope<Safari>.() -> Unit): SafariOptions =
    options<Safari>(block) as SafariOptions

@KolibriumDsl
public fun edgeOptions(block: OptionsScope<Edge>.() -> Unit): EdgeOptions = options<Edge>(block) as EdgeOptions

@KolibriumDsl
public fun options(browser: BrowserType, block: OptionsScope<Browser>.() -> Unit): AbstractDriverOptions<*> =
    when (browser) {
        CHROME -> options(block as (OptionsScope<Chrome>.() -> Unit))
        FIREFOX -> options(block as (OptionsScope<Firefox>.() -> Unit))
        SAFARI -> options(block as (OptionsScope<Safari>.() -> Unit))
        EDGE -> options(block as (OptionsScope<Edge>.() -> Unit))
    }

internal inline fun <reified T : Browser> options(
    noinline block: OptionsScope<T>.() -> Unit
): AbstractDriverOptions<*> {
    return when (T::class) {
        Chrome::class -> {
            val optionsScope =
                optionsScope(ChromeOptions(), block) as OptionsScope<Chrome>
            configureChromeOptions(optionsScope)
        }

        Firefox::class -> {
            val optionsScope =
                optionsScope(FirefoxOptions(), block) as OptionsScope<Firefox>
            configureFirefoxOptions(optionsScope)
        }

        Safari::class -> {
            val optionsScope =
                optionsScope(SafariOptions(), block) as OptionsScope<Safari>
            configureSafariOptions(optionsScope)
        }

        Edge::class -> {
            val optionsScope =
                optionsScope(EdgeOptions(), block) as OptionsScope<Edge>
            configureEdgeOptions(optionsScope)
        }

        else -> throw UnsupportedOperationException()
    }
}

internal fun <T : Browser> optionsScope(
    options: AbstractDriverOptions<*>,
    block: OptionsScope<T>.() -> Unit
): BaseOptionsScope = OptionsScope<T>(options).apply(block)
    .configure()

internal fun configureChromeOptions(optionsScope: OptionsScope<Chrome>): ChromeOptions =
    (optionsScope.options as ChromeOptions).apply {
        optionsScope.binary?.let { setBinary(it) }
    }

internal fun configureFirefoxOptions(optionsScope: OptionsScope<Firefox>): FirefoxOptions =
    (optionsScope.options as FirefoxOptions).apply {
        with(optionsScope) {
            binary?.let { setBinary(it) }
            profileDir?.let { profile = FirefoxProfile(File(it)) }
        }
    }

internal fun configureSafariOptions(optionsScope: OptionsScope<Safari>): SafariOptions =
    (optionsScope.options as SafariOptions).apply {
        with(optionsScope) {
            automaticInspection?.let { setAutomaticInspection(it) }
            automaticProfiling?.let { setAutomaticProfiling(it) }
            useTechnologyPreview?.let { setUseTechnologyPreview(it) }
        }
    }

internal fun configureEdgeOptions(optionsScope: OptionsScope<Edge>): EdgeOptions =
    (optionsScope.options as EdgeOptions).apply {
        optionsScope.binary?.let { setBinary(it) }
        optionsScope.useWebView?.let { useWebView(it) }
    }

private fun ifExists(file: String?): Boolean {
    file?.let {
        require(File(it).exists()) {
            """
                |DriverService is not set up properly:
                |The following file does not exist at the specified path: $file
            """.trimMargin()
        }
    }
    return true
}

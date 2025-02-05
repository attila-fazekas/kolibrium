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

import dev.kolibrium.common.Browser
import dev.kolibrium.common.Browser.CHROME
import dev.kolibrium.common.Browser.EDGE
import dev.kolibrium.common.Browser.FIREFOX
import dev.kolibrium.common.Browser.SAFARI
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeDriverService
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.edge.EdgeDriver
import org.openqa.selenium.edge.EdgeDriverService
import org.openqa.selenium.edge.EdgeOptions
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.firefox.FirefoxOptions
import org.openqa.selenium.firefox.GeckoDriverService
import org.openqa.selenium.remote.AbstractDriverOptions
import org.openqa.selenium.remote.service.DriverService
import org.openqa.selenium.safari.SafariDriver
import org.openqa.selenium.safari.SafariDriverService
import org.openqa.selenium.safari.SafariOptions
import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.FUNCTION
import kotlin.annotation.AnnotationTarget.PROPERTY

/**
 * Marker annotation for Kolibrium DSL functions and classes.
 */
@DslMarker
@Target(FUNCTION, CLASS)
internal annotation class KolibriumDsl

/**
 * Marker annotation for Kolibrium DSL properties.
 *
 * Use this annotation to extend the Kolibrium DSL with your own properties, such as command-line arguments,
 * experimental flags, browser preferences, or browser feature switches.
 */
@DslMarker
@Target(PROPERTY)
public annotation class KolibriumPropertyDsl

/**
 * Creates a [WebDriver] instance for the specified browser type with custom configuration.
 *
 * It delegates to the appropriate browser-specific driver creation function based on
 * the provided [browser] parameter.
 *
 * @param browser The browser type to create a driver for (CHROME, SAFARI, FIREFOX, or EDGE).
 * @param block The configuration block to customize the driver settings.
 * @return A configured WebDriver instance for the specified browser.
 */
@KolibriumDsl
public fun driver(
    browser: Browser,
    block: DriverScope<*, *>.() -> Unit,
): WebDriver =
    when (browser) {
        CHROME -> chromeDriver(block as (ChromeDriverScope.() -> Unit))
        SAFARI -> safariDriver(block as (SafariDriverScope.() -> Unit))
        FIREFOX -> firefoxDriver(block as (FirefoxDriverScope.() -> Unit))
        EDGE -> edgeDriver(block as (EdgeDriverScope.() -> Unit))
    }

/**
 * Creates a ChromeDriver instance with custom configuration.
 *
 * This function provides a DSL for configuring [ChromeDriverService] and [ChromeOptions] settings.
 *
 * @param block The configuration block to customize Chrome-specific driver settings.
 * @return A configured ChromeDriver instance.
 */
@KolibriumDsl
public fun chromeDriver(block: ChromeDriverScope.() -> Unit): ChromeDriver {
    val driverScope = ChromeDriverScope().apply(block)
    return ChromeDriver(
        driverScope.driverServiceScope.builder.build(),
        driverScope.optionsScope.options,
    )
}

/**
 * Creates a SafariDriver instance with custom configuration.
 *
 * This function provides a DSL for configuring [SafariDriverService] and [SafariOptions] settings.
 *
 * @param block The configuration block to customize Safari-specific driver settings.
 * @return A configured SafariDriver instance.
 */
@KolibriumDsl
public fun safariDriver(block: SafariDriverScope.() -> Unit): SafariDriver {
    val driverScope = SafariDriverScope().apply(block)
    return SafariDriver(
        driverScope.driverServiceScope.builder.build(),
        driverScope.optionsScope.options,
    )
}

/**
 * Creates a FirefoxDriver instance with custom configuration.
 *
 * This function provides a DSL for configuring [GeckoDriverService] and [FirefoxOptions] settings.
 *
 * @param block The configuration block to customize Firefox-specific driver settings.
 * @return A configured FirefoxDriver instance.
 */
@KolibriumDsl
public fun firefoxDriver(block: FirefoxDriverScope.() -> Unit): FirefoxDriver {
    val driverScope = FirefoxDriverScope().apply(block)
    return FirefoxDriver(
        driverScope.driverServiceScope.builder.build(),
        driverScope.optionsScope.options,
    )
}

/**
 * Creates an EdgeDriver instance with custom configuration.
 *
 * This function provides a DSL for configuring [EdgeDriverService] and [EdgeOptions] settings.
 *
 * @param block The configuration block to customize Edge-specific driver settings.
 * @return A configured EdgeDriver instance.
 */
@KolibriumDsl
public fun edgeDriver(block: EdgeDriverScope.() -> Unit): EdgeDriver {
    val driverScope = EdgeDriverScope().apply(block)
    return EdgeDriver(
        driverScope.driverServiceScope.builder.build(),
        driverScope.optionsScope.options,
    )
}

/**
 * Creates a [DriverService] instance for the specified browser type with custom configuration.
 *
 * It delegates to the appropriate browser-specific driver service creation function based on
 * the provided [browser] parameter.
 *
 * @param browser The browser type for which to create a driver service.
 * @param block The configuration block that defines driver service settings within the appropriate scope.
 * @return A configured [DriverService] instance for the specified browser.
 */
@KolibriumDsl
public fun driverService(
    browser: Browser,
    block: DriverServiceScope.() -> Unit,
): DriverService =
    when (browser) {
        CHROME -> chromeDriverService(block as (ChromeDriverServiceScope.() -> Unit))
        SAFARI -> safariDriverService(block as (SafariDriverServiceScope.() -> Unit))
        FIREFOX -> geckoDriverService(block as (GeckoDriverServiceScope.() -> Unit))
        EDGE -> edgeDriverService(block as (EdgeDriverServiceScope.() -> Unit))
    }

/**
 * Creates a ChromeDriver service with custom configurations.
 *
 * This function provides a DSL for configuring ChromeDriver service settings, such as log level, allowed IPs,
 * executable path, and other Chrome-specific options.
 *
 * @param block The configuration block that defines ChromeDriver service settings within [ChromeDriverServiceScope].
 * @return A configured [ChromeDriverService] instance.
 */
@KolibriumDsl
public fun chromeDriverService(block: ChromeDriverServiceScope.() -> Unit): ChromeDriverService {
    val driverServiceScopeScope =
        ChromeDriverServiceScope(
            ChromeDriverService.Builder(),
        ).apply {
            block()
            configure()
        }
    return driverServiceScopeScope.builder.build()
}

/**
 * Creates a SafariDriver service with custom configurations.
 *
 * This function provides a DSL for configuring SafariDriver service settings, such as logging, port configuration, and
 * timeout.
 *
 * @param block The configuration block that defines SafariDriver service settings within [SafariDriverServiceScope].
 * @return A configured [SafariDriverService] instance.
 */
@KolibriumDsl
public fun safariDriverService(block: SafariDriverServiceScope.() -> Unit): SafariDriverService {
    val driverServiceScopeScope =
        SafariDriverServiceScope(SafariDriverService.Builder()).apply {
            block()
            configure()
        }
    return driverServiceScopeScope.builder.build()
}

/**
 * Creates a GeckoDriver service with custom configurations.
 *
 * This function provides a DSL for configuring GeckoDriver service settings, such as logging, allowed hosts,
 * executable path, profile root, and other Firefox-specific options.
 *
 * @param block The configuration block that defines GeckoDriver service settings within [GeckoDriverServiceScope].
 * @return A configured [GeckoDriverService] instance.
 */
@KolibriumDsl
public fun geckoDriverService(block: GeckoDriverServiceScope.() -> Unit): GeckoDriverService {
    val driverServiceScopeScope =
        GeckoDriverServiceScope(GeckoDriverService.Builder()).apply {
            block()
            configure()
        }
    return driverServiceScopeScope.builder.build()
}

/**
 * Creates an EdgeDriver service with custom configurations.
 *
 * This function provides a DSL for configuring EdgeDriver service settings, such as log level, allowed IPs,
 * executable path, and other Edge-specific options.
 *
 * @param block The configuration block that defines EdgeDriver service settings within [EdgeDriverServiceScope].
 * @return A configured [EdgeDriverService] instance.
 */
@KolibriumDsl
public fun edgeDriverService(block: EdgeDriverServiceScope.() -> Unit): EdgeDriverService {
    val driverServiceScopeScope =
        EdgeDriverServiceScope(EdgeDriverService.Builder()).apply {
            block()
            configure()
        }
    return driverServiceScopeScope.builder.build()
}

/**
 * Creates an options instance for the specified browser type with custom configuration.
 *
 * It delegates to the appropriate browser-specific options creation function based on
 * the provided [browser] parameter.
 *
 * @param browser The browser type for which to create options.
 * @param block The configuration block to customize the options.
 * @return The configured driver options for the specified browser.
 */
@KolibriumDsl
public fun options(
    browser: Browser,
    block: OptionsScope.() -> Unit,
): AbstractDriverOptions<*> =
    when (browser) {
        CHROME -> chromeOptions(block as (ChromeOptionsScope.() -> Unit))
        SAFARI -> safariOptions(block as (SafariOptionsScope.() -> Unit))
        FIREFOX -> firefoxOptions(block as (FirefoxOptionsScope.() -> Unit))
        EDGE -> edgeOptions(block as (EdgeOptionsScope.() -> Unit))
    }

/**
 * Creates an ChromeOptions with custom configurations.
 *
 * This function provides a DSL for configuring ChromeOptions settings, such as experimental options,
 * extensions, and other Chrome-specific options.
 *
 * @param block The configuration block to customize Chrome-specific options.
 * @return The configured [ChromeOptions] instance.
 */
@KolibriumDsl
public fun chromeOptions(block: ChromeOptionsScope.() -> Unit): ChromeOptions =
    ChromeOptionsScope(ChromeOptions())
        .apply {
            block()
            configure()
        }.options

/**
 * Creates an SafariOptions with custom configurations.
 *
 * This function provides a DSL for configuring SafariOptions settings, such as automatic inspection,
 * profiling of web pages, and enabling Safari Technology Preview
 *
 * @param block The configuration block to customize Safari-specific options.
 * @return The configured [SafariOptions] instance.
 */
@KolibriumDsl
public fun safariOptions(block: SafariOptionsScope.() -> Unit): SafariOptions =
    SafariOptionsScope(SafariOptions())
        .apply {
            block()
            configure()
        }.options

/**
 * Creates an FirefoxOptions with custom configurations.
 *
 * This function provides a DSL for configuring FirefoxOptions settings, such as the path to the binary
 * executable, profile preferences, directory, and other Firefox-specific options.
 *
 * @param block The configuration block to customize Firefox-specific options.
 * @return The configured [FirefoxOptions] instance.
 */
@KolibriumDsl
public fun firefoxOptions(block: FirefoxOptionsScope.() -> Unit): FirefoxOptions =
    FirefoxOptionsScope(FirefoxOptions())
        .apply {
            block()
            configure()
        }.options

/**
 * Creates an EdgeOptions with custom configurations.
 *
 * This function provides a DSL for configuring EdgeOptions settings, such as experimental options,
 * extensions, and other Edge-specific options.
 *
 * @param block The configuration block to customize Edge-specific options.
 * @return The configured [EdgeOptions] instance.
 */
@KolibriumDsl
public fun edgeOptions(block: EdgeOptionsScope.() -> Unit): EdgeOptions =
    EdgeOptionsScope(EdgeOptions())
        .apply {
            block()
            configure()
        }.options

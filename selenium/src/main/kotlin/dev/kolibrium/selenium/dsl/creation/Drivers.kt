/*
 * Copyright 2023-2026 Attila Fazekas & contributors
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

package dev.kolibrium.selenium.dsl.creation

import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeDriverService
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.edge.EdgeDriver
import org.openqa.selenium.edge.EdgeDriverService
import org.openqa.selenium.edge.EdgeOptions
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.firefox.FirefoxOptions
import org.openqa.selenium.firefox.GeckoDriverService
import org.openqa.selenium.safari.SafariDriver
import org.openqa.selenium.safari.SafariDriverService
import org.openqa.selenium.safari.SafariOptions

/**
 * Creates a ChromeDriver instance with custom configuration.
 *
 * This function provides a DSL for configuring [ChromeDriverService] and [ChromeOptions] settings.
 *
 * @param block The configuration block to customize Chrome-specific driver settings.
 * @return A configured ChromeDriver instance.
 */
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
public fun edgeDriver(block: EdgeDriverScope.() -> Unit): EdgeDriver {
    val driverScope = EdgeDriverScope().apply(block)
    return EdgeDriver(
        driverScope.driverServiceScope.builder.build(),
        driverScope.optionsScope.options,
    )
}

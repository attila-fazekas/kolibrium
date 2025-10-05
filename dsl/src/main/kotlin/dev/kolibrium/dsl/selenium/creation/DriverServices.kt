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
import dev.kolibrium.common.Browser.Chrome
import dev.kolibrium.common.Browser.Edge
import dev.kolibrium.common.Browser.Firefox
import dev.kolibrium.common.Browser.Safari
import dev.kolibrium.dsl.selenium.KolibriumDsl
import org.openqa.selenium.chrome.ChromeDriverService
import org.openqa.selenium.edge.EdgeDriverService
import org.openqa.selenium.firefox.GeckoDriverService
import org.openqa.selenium.remote.service.DriverService
import org.openqa.selenium.safari.SafariDriverService

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
        Chrome -> chromeDriverService(block as (ChromeDriverServiceScope.() -> Unit))
        Safari -> safariDriverService(block as (SafariDriverServiceScope.() -> Unit))
        Firefox -> geckoDriverService(block as (GeckoDriverServiceScope.() -> Unit))
        Edge -> edgeDriverService(block as (EdgeDriverServiceScope.() -> Unit))
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

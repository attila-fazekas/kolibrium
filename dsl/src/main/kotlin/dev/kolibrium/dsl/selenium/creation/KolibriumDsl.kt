/*
 * Copyright 2023-2024 Attila Fazekas & contributors
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

import dev.kolibrium.core.Browser
import dev.kolibrium.core.Browser.CHROME
import dev.kolibrium.core.Browser.EDGE
import dev.kolibrium.core.Browser.FIREFOX
import dev.kolibrium.core.Browser.SAFARI
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

@DslMarker
@Target(FUNCTION, CLASS)
internal annotation class KolibriumDsl

@DslMarker
@Target(PROPERTY)
public annotation class KolibriumPropertyDsl

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

@KolibriumDsl
public fun chromeDriver(block: ChromeDriverScope.() -> Unit): ChromeDriver {
    val driverScope = ChromeDriverScope().apply(block)
    return ChromeDriver(
        driverScope.driverServiceScope.builder.build(),
        driverScope.optionsScope.options,
    )
}

@KolibriumDsl
public fun safariDriver(block: SafariDriverScope.() -> Unit): SafariDriver {
    val driverScope = SafariDriverScope().apply(block)
    return SafariDriver(
        driverScope.driverServiceScope.builder.build(),
        driverScope.optionsScope.options,
    )
}

@KolibriumDsl
public fun edgeDriver(block: EdgeDriverScope.() -> Unit): EdgeDriver {
    val driverScope = EdgeDriverScope().apply(block)
    return EdgeDriver(
        driverScope.driverServiceScope.builder.build(),
        driverScope.optionsScope.options,
    )
}

@KolibriumDsl
public fun firefoxDriver(block: FirefoxDriverScope.() -> Unit): FirefoxDriver {
    val driverScope = FirefoxDriverScope().apply(block)
    return FirefoxDriver(
        driverScope.driverServiceScope.builder.build(),
        driverScope.optionsScope.options,
    )
}

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

@KolibriumDsl
public fun safariDriverService(block: SafariDriverServiceScope.() -> Unit): SafariDriverService {
    val driverServiceScopeScope =
        SafariDriverServiceScope(SafariDriverService.Builder()).apply {
            block()
            configure()
        }
    return driverServiceScopeScope.builder.build()
}

@KolibriumDsl
public fun edgeDriverService(block: EdgeDriverServiceScope.() -> Unit): EdgeDriverService {
    val driverServiceScopeScope =
        EdgeDriverServiceScope(EdgeDriverService.Builder()).apply {
            block()
            configure()
        }
    return driverServiceScopeScope.builder.build()
}

@KolibriumDsl
public fun geckoDriverService(block: GeckoDriverServiceScope.() -> Unit): GeckoDriverService {
    val driverServiceScopeScope =
        GeckoDriverServiceScope(GeckoDriverService.Builder()).apply {
            block()
            configure()
        }
    return driverServiceScopeScope.builder.build()
}

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

@KolibriumDsl
public fun chromeOptions(block: ChromeOptionsScope.() -> Unit): ChromeOptions {
    val optionsScope =
        ChromeOptionsScope(ChromeOptions()).apply {
            block()
            configure()
        }
    return optionsScope.options
}

@KolibriumDsl
public fun safariOptions(block: SafariOptionsScope.() -> Unit): SafariOptions {
    val optionsScope =
        SafariOptionsScope(SafariOptions()).apply {
            block()
            configure()
        }
    return optionsScope.options
}

@KolibriumDsl
public fun edgeOptions(block: EdgeOptionsScope.() -> Unit): EdgeOptions {
    val optionsScope =
        EdgeOptionsScope(EdgeOptions()).apply {
            block()
            configure()
        }
    return optionsScope.options
}

@KolibriumDsl
public fun firefoxOptions(block: FirefoxOptionsScope.() -> Unit): FirefoxOptions {
    val optionsScope =
        FirefoxOptionsScope(FirefoxOptions()).apply {
            block()
            configure()
        }
    return optionsScope.options
}

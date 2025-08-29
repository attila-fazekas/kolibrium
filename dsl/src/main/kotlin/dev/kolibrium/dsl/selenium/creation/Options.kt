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
import dev.kolibrium.dsl.selenium.KolibriumDsl
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.edge.EdgeOptions
import org.openqa.selenium.firefox.FirefoxOptions
import org.openqa.selenium.remote.AbstractDriverOptions
import org.openqa.selenium.safari.SafariOptions

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

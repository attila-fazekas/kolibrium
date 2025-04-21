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

package dev.kolibrium.core.selenium

import dev.kolibrium.common.Browser
import dev.kolibrium.common.Browser.CHROME
import dev.kolibrium.common.Browser.EDGE
import dev.kolibrium.common.Browser.FIREFOX
import dev.kolibrium.common.Browser.SAFARI
import dev.kolibrium.common.InternalKolibriumApi
import dev.kolibrium.core.selenium.configuration.DefaultSeleniumProjectConfiguration
import dev.kolibrium.core.selenium.configuration.SeleniumProjectConfiguration
import org.openqa.selenium.WebDriver

@OptIn(InternalKolibriumApi::class)
private val actualConfig = SeleniumProjectConfiguration.actualConfig
private val defaultConfig = DefaultSeleniumProjectConfiguration

/**
 * Executes a browser test with the specified parameters and provides access to a WebDriver instance
 * within the lambda [block] via the context receiver.
 *
 * This function handles the lifecycle of the WebDriver, including:
 * - Creating a driver instance for the specified [browser]
 * - Navigating to the specified [baseUrl]
 * - Executing the provided test [block]
 * - Closing the browser when testing is complete (unless [keepBrowserOpen] is set to true)
 *
 * Example usage:
 * ```
 * browserTest {
 *     // 'this' is the WebDriver instance
 * }
 * ```
 *
 * @param browser The browser to use for this test. Defaults to the configured default browser.
 * @param baseUrl The URL to navigate to before executing the test block. Defaults to the configured base URL.
 * @param keepBrowserOpen Whether to keep the browser open after the test completes. Defaults to the configured value.
 * @param block The test code to execute with access to the WebDriver instance.
 */
public fun browserTest(
    browser: Browser = actualConfig.defaultBrowser ?: defaultConfig.defaultBrowser,
    baseUrl: String = actualConfig.baseUrl ?: defaultConfig.baseUrl,
    keepBrowserOpen: Boolean = actualConfig.keepBrowserOpen ?: defaultConfig.keepBrowserOpen,
    block: context(WebDriver) () -> Unit,
) {
    val driver = createDriver(browser)

    try {
        driver.apply {
            get(baseUrl)
            block()
        }
    } finally {
        if (!keepBrowserOpen) {
            driver.quit()
        }
    }
}

private fun createDriver(browser: Browser = CHROME): WebDriver =
    when (browser) {
        CHROME -> actualConfig.chromeDriver ?: defaultConfig.chromeDriver
        SAFARI -> actualConfig.safariDriver ?: defaultConfig.safariDriver
        EDGE -> actualConfig.edgeDriver ?: defaultConfig.edgeDriver
        FIREFOX -> actualConfig.firefoxDriver ?: defaultConfig.firefoxDriver
    }()

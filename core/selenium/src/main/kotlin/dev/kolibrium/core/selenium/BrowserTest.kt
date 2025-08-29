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
import dev.kolibrium.common.config.ProjectConfigurationException
import dev.kolibrium.core.selenium.configuration.ABOUT_BLANK
import dev.kolibrium.core.selenium.configuration.DefaultSeleniumProjectConfiguration
import dev.kolibrium.core.selenium.configuration.SeleniumProjectConfiguration.actualConfig
import org.openqa.selenium.WebDriver
import java.net.URI

/**
 * Executes a browser test with the specified parameters and provides access to a WebDriver instance
 * within the lambda [block] via the context receiver.
 *
 * This function handles the lifecycle of the WebDriver, including:
 * - Creating a driver instance for the specified [browser]
 * - Navigating to the specified [url]
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
 * @param url The URL to navigate to before executing the test block. Defaults to the configured base URL.
 * @param keepBrowserOpen Whether to keep the browser open after the test completes. Defaults to the configured value.
 * @param block The test code to execute with access to the WebDriver instance.
 */
@OptIn(InternalKolibriumApi::class)
public fun browserTest(
    browser: Browser = actualConfig.defaultBrowser ?: DefaultSeleniumProjectConfiguration.defaultBrowser,
    url: String = actualConfig.baseUrl ?: DefaultSeleniumProjectConfiguration.baseUrl,
    keepBrowserOpen: Boolean = actualConfig.keepBrowserOpen ?: DefaultSeleniumProjectConfiguration.keepBrowserOpen,
    block: WebDriver.() -> Unit,
) {
    if (url == ABOUT_BLANK) {
        throw ProjectConfigurationException(
            "\"baseUrl\" was neither configured through project-level settings nor provided as a parameter!",
        )
    }

    val normalizedUrl =
        url.getNormalizedUrl() ?: throw ProjectConfigurationException(
            "Provided $url URL is invalid!",
        )

    val driver = createDriver(browser)

    try {
        driver.apply {
            get(normalizedUrl)
            block()
        }
    } finally {
        if (!keepBrowserOpen) {
            driver.quit()
        }
    }
}

private const val FILE_URL = "file://"

private fun String.getNormalizedUrl(): String? {
    // Check if it's a file URL and handle it separately
    if (this.startsWith(FILE_URL)) {
        return if (this.length > FILE_URL.length) this else null // Ensure the path follows the scheme
    }

    // Prepend scheme if missing
    val urlWithScheme =
        if (!this.startsWith("http://") && !this.startsWith("https://")) {
            "http://$this"
        } else {
            this
        }

    return try {
        val uri = URI(urlWithScheme)

        // Validate scheme (allow http, https, and file)
        if (uri.scheme !in listOf("http", "https", "file")) return null

        val host = uri.host ?: return null

        // Validate host (localhost, IP, or domain with dot)
        when {
            host == "localhost" -> uri.toString() // valid
            host.matches(Regex("""\d{1,3}(\.\d{1,3}){3}""")) -> uri.toString() // IP address valid
            host.contains(".") -> uri.toString() // valid domain
            else -> null // invalid (no dot)
        }
    } catch (_: Exception) {
        null // Invalid URL
    }
}

private fun createDriver(browser: Browser = CHROME): WebDriver =
    when (browser) {
        CHROME -> actualConfig.chromeDriver ?: DefaultSeleniumProjectConfiguration.chromeDriver
        SAFARI -> actualConfig.safariDriver ?: DefaultSeleniumProjectConfiguration.safariDriver
        EDGE -> actualConfig.edgeDriver ?: DefaultSeleniumProjectConfiguration.edgeDriver
        FIREFOX -> actualConfig.firefoxDriver ?: DefaultSeleniumProjectConfiguration.firefoxDriver
    }()

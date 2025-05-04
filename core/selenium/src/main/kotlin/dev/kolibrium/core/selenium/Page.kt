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

import dev.kolibrium.common.Cookies
import dev.kolibrium.common.InternalKolibriumApi
import dev.kolibrium.common.config.ProjectConfigurationException
import dev.kolibrium.core.selenium.configuration.SeleniumProjectConfiguration.actualConfig
import org.openqa.selenium.WebDriver
import java.net.URI

/**
 * Base class for Page Object Model implementations.
 *
 * This class serves as a foundation for creating page-specific classes that represent web pages
 * in your application. It delegates to the provided WebDriver instance to implement the
 * SearchContext interface, which makes all locator functions and extensions automatically
 * available on Page instances without requiring explicit forwarding functions.
 *
 * Example usage:
 * ```
 * class LoginPage(driver: WebDriver) : Page(driver) {
 *     private val username by name("user-name")
 *     private val password by idOrName("password")
 *     private val button by name("login-button")
 *
 *     fun login(username: String, password: String) {
 *         this.username.sendKeys(username)
 *         this.password.sendKeys(password)
 *         button.click()
 *     }
 * }
 * ```
 *
 * @property driver The WebDriver instance used to interact with the browser.
 */
public abstract class Page(
    protected val driver: WebDriver,
) : WebDriver by driver {
    /**
     * Returns the URL that the browser should navigate to when this page is instantiated.
     *
     * By default, this function returns the project configuration's [dev.kolibrium.core.selenium.configuration.DefaultSeleniumProjectConfiguration.baseUrl].
     * Override this function to provide a custom URL for this specific page.
     *
     * If the URL is a relative path (does not include scheme like "http://"), it will be resolved
     * against the [dev.kolibrium.core.selenium.configuration.DefaultSeleniumProjectConfiguration.baseUrl] from the project configuration.
     *
     * @return The URL to navigate to.
     * @see dev.kolibrium.core.selenium.configuration.AbstractSeleniumProjectConfiguration.baseUrl
     */
    public open fun url(): String? = actualConfig.baseUrl

    /**
     * Returns the cookies that should be added to the browser session for this page.
     *
     * By default, this function returns the project configuration's [dev.kolibrium.core.selenium.configuration.DefaultSeleniumProjectConfiguration.cookies]. Override this function
     * to provide custom cookies for this specific page.
     *
     * Cookies returned by this function will be added to the browser session before navigating to
     * the page URL.
     *
     * @return Set of cookies to be added to the browser session.
     * @see dev.kolibrium.common.Cookies
     * @see dev.kolibrium.core.selenium.configuration.AbstractSeleniumProjectConfiguration.cookies
     */
    public open fun cookies(): Cookies? = actualConfig.cookies

    init {
        cookies().let { cookies ->
            cookies?.forEach { cookie ->
                manage().addCookie(cookie)
            }
        }
        url()?.let { url ->
            // If it's an absolute URL from the page class, it would match the pattern
            // but not match the baseUrl (which we're returning by default in url())
            if (url.isAbsoluteUrl() && url != actualConfig.baseUrl) {
                println(
                    "Warning: Page class uses absolute URL '$url' which overrides baseUrl '${actualConfig.baseUrl}'. " +
                        "This causes an unnecessary navigation to baseUrl first.",
                )
            }

            val resolvedUrl = resolveUrl(url)
            driver.get(resolvedUrl)
        }
    }

    @OptIn(InternalKolibriumApi::class)
    private fun resolveUrl(url: String): String {
        // If URL is already absolute, return it as is
        if (url.isAbsoluteUrl()) {
            return url
        }

        // Handle case when baseUrl is null
        val baseUrl =
            actualConfig.baseUrl ?: throw ProjectConfigurationException(
                "Provided \"$url\" is a relative URL but \"baseUrl\" was not configured. Consider overriding \"baseUrl\" in your configuration file, or specify an absolute URL.",
            )

        // Handle case when URL is already the complete base URL
        if (url == baseUrl) {
            return url
        }

        // Properly join paths to avoid double slashes
        val fullUrl =
            buildString {
                append(baseUrl.removeSuffix("/"))
                append("/")
                append(url.removePrefix("/"))
            }

        return URI(fullUrl).normalize().toString()
    }

    private fun String.isAbsoluteUrl() = matches(Regex("^[a-zA-Z]+://.*"))
}

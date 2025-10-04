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
import org.openqa.selenium.By
import org.openqa.selenium.Cookie
import org.openqa.selenium.SearchContext
import org.openqa.selenium.WebDriver

/**
 * Base class for typed page objects bound to a specific [Site].
 *
 * Exposes common browser actions and cookie helpers while delegating [SearchContext] to the underlying [WebDriver].
 * Concrete pages typically declare locators and behavior and may override [path] to indicate their route.
 *
 * @param S The [Site] type this page belongs to. Keeps navigation and configuration consistent at compile time.
 * @property driver The active [WebDriver] for the session.
 */
public abstract class Page<S : Site>(
    protected val driver: WebDriver,
) : SearchContext by driver {
    /**
     * Relative path of this page, appended to [Site.baseUrl] by the DSL when opening.
     * Defaults to the root path.
     */
    public open val path: String = "/"

    /** The current URL as reported by the driver. */
    protected val currentUrl: String?
        get() = driver.currentUrl

    /** The current page title as reported by the driver. */
    protected val pageTitle: String?
        get() = driver.title

    /**
     * Optional canonical element descriptor the DSL can wait for after navigation.
     * If null, no built-in locator wait is performed.
     *
     * When provided, the DSL will use the descriptor's waitConfig and readyWhen if set;
     * otherwise it will fall back to the Site defaults.
     */
    public open val ready: WebElementDescriptor? = null

    /** Optional custom wait implementation (no-op by default). */
    public open fun awaitReady(driver: WebDriver): Unit = Unit

    /** Optional identity/readiness guard (no-op by default). */
    public open fun assertReady(): Unit = Unit

    /** Reloads the current page. */
    protected fun refresh() {
        driver.navigate().refresh()
    }

    /** Navigates back in the browser history. */
    protected fun back() {
        driver.navigate().back()
    }

    /** Navigates forward in the browser history. */
    protected fun forward() {
        driver.navigate().forward()
    }

    /** Adds a single cookie to the browser session. */
    protected fun addCookie(cookie: Cookie) {
        driver.manage().addCookie(cookie)
    }

    /** Adds multiple cookies to the browser session. */
    protected fun addCookies(cookies: Cookies) {
        val options = driver.manage()
        cookies.forEach { cookie ->
            options.addCookie(cookie)
        }
    }

    /**
     * Returns the first cookie with the given [name], or `null` if it does not exist.
     */
    protected fun cookie(name: String): Cookie? = driver.manage().cookies.firstOrNull { it.name == name }

    /** Deletes a cookie by [name]. */
    protected fun deleteCookie(name: String) {
        driver.manage().deleteCookieNamed(name)
    }

    /** Deletes all cookies in the current browser session. */
    protected fun clearCookies() {
        driver.manage().deleteAllCookies()
    }
}

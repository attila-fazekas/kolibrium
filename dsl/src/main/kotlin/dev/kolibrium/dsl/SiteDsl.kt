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

@file:OptIn(InternalKolibriumApi::class)

package dev.kolibrium.dsl

import dev.kolibrium.common.Cookies
import dev.kolibrium.common.InternalKolibriumApi
import dev.kolibrium.core.selenium.DefaultChromeDriverProfile
import dev.kolibrium.core.selenium.DriverProfile
import dev.kolibrium.core.selenium.Page
import dev.kolibrium.core.selenium.Site
import dev.kolibrium.core.selenium.SiteContext
import org.openqa.selenium.WebDriver

/**
 * Marker annotation for the Kolibrium page DSL.
 *
 * Prevents accidental scope leaks between page operations by restricting where DSL receivers can be used.
 */
@DslMarker
public annotation class PageDsl

/**
 * Scoped wrapper around a [Page] that ensures interactions happen only after navigation.
 *
 * Use [on] to perform actions that either return another [Page] (continuing the flow) or a terminal value.
 * The DSL marker prevents mixing operations from different pages in a single scope.
 *
 * @param P The page type wrapped by this scope.
 * @property page The underlying page instance.
 */
@PageDsl
public class PageScope<P : Page<*>>(
    public val page: P,
) {
    /**
     * Execute an action that transitions to the next page and returns a new [PageScope] for it.
     *
     * @param Next the next page type returned by [action]
     * @param action A function executed on the current page that returns the next page.
     * @return A scope for the returned page.
     */
    public fun <Next : Page<*>> on(action: P.() -> Next): PageScope<Next> = PageScope(page.action())

    /**
     * Execute a terminal action that produces a value and does not change the current page.
     *
     * @param T the return type produced by [action]
     * @param action A function executed on the current page.
     * @return The result of [action].
     */
    public fun <T> on(action: P.() -> T): T = page.action()

    /** Expose the underlying page instance when needed (e.g., for advanced use cases). */
    public fun unwrap(): P = page
}

/**
 * Entry point for opening and interacting with pages bound to a specific [Site].
 *
 * The DSL guarantees that a page is navigated to before interaction. Use [open] to instantiate a page,
 * navigate to its [Page.path] (or an overridden path), and continue the flow within a [PageScope].
 *
 * @param S The [Site] type driving navigation and configuration.
 * @property driver The active [WebDriver] of the session.
 */
@PageDsl
public class PageEntry<S : Site>(
    public val driver: WebDriver,
) {
    context(site: S)
    /**
     * Instantiate and open a page, then execute [action] on it.
     *
     * @param P the concrete page type created by [pageFactory]
     * @param R the page type returned by [action] to continue the flow
     * @param pageFactory Factory that receives the current [WebDriver] and returns a page instance.
     * @param path Optional override for [Page.path]; if `null`, the page’s own [Page.path] is used.
     * @param action Action to perform after navigation; may return another [Page] to continue the flow.
     * @return A [PageScope] for the page returned by [action].
     */
    public fun <P : Page<S>, R : Page<*>> open(
        pageFactory: (WebDriver) -> P,
        path: String? = null,
        action: P.() -> R,
    ): PageScope<R> {
        val page = pageFactory(driver)
        val targetPath = path ?: page.path
        driver.get("${site.baseUrl}$targetPath")
        return PageScope(page.action())
    }

    context(_: S)
    /**
     * Switch the current context to a different [Site] while reusing the same browser session.
     *
     * Applies optional [cookies], calls [Site.configureDriver], and optionally navigates to the new site’s base URL.
     *
     * @param S2 the target site type to switch to
     * @param site The new site context.
     * @param navigateToBase Whether to navigate to [Site.baseUrl] after switching. Default is `true`.
     * @param cookies Optional cookies to apply right after switching.
     * @return A new [PageEntry] bound to [site].
     */
    public fun <S2 : Site> switchTo(
        site: S2,
        navigateToBase: Boolean = true,
        cookies: Cookies? = null,
    ): PageEntry<S2> {
        SiteContext.set(site)
        site.configureDriver(driver)
        if (cookies != null && cookies.isNotEmpty()) {
            val options = driver.manage()
            cookies.forEach(options::addCookie)
        }
        if (navigateToBase) driver.get(site.baseUrl)
        return PageEntry(driver)
    }

    context(_: S)
    /**
     * Navigate to an absolute URL, bypassing the current [Site.baseUrl].
     *
     * @param url Absolute URL to navigate to.
     */
    public fun navigateAbsolute(
        url: String,
    ) {
        driver.get(url)
    }
}

/**
 * Run a browser test within a managed driver session and a given [Site] context.
 *
 * The driver is created from [driverProfile], configured via [Site.configureDriver], and navigated to [Site.baseUrl].
 * If site‑level cookies are present, they are applied and the base URL is reloaded to take effect.
 * The driver is quit automatically unless [keepBrowserOpen] is `true`.
 *
 * Example:
 * ```kotlin
 * webTest(ShopSite()) {
 *     open(::LoginPage) {
 *         login()
 *     }.on {
 *         goToCart()
 *     }.on {
 *         checkout()
 *     }
 * }
 * ```
 * @param S the site type used for the test
 * @param site The site configuration to use throughout the test.
 * @param driverProfile Strategy for creating the [WebDriver]. Defaults to [DefaultChromeDriverProfile].
 * @param keepBrowserOpen If `true`, leaves the browser open after the block for debugging.
 * @param block Test body executed with a [PageEntry] and the [site] as a context receiver.
 */
public inline fun <S : Site> webTest(
    site: S,
    driverProfile: DriverProfile = DefaultChromeDriverProfile,
    keepBrowserOpen: Boolean = false,
    crossinline block: context(S) PageEntry<S>.() -> Unit,
) {
    val driver = driverProfile.getDriver()
    SiteContext.withSite(site) {
        site.configureDriver(driver)
        driver.get(site.baseUrl)
        if (site.cookies.isNotEmpty()) {
            val options = driver.manage()
            site.cookies.forEach(options::addCookie)
            driver.get(site.baseUrl)
        }

        try {
            val pageEntry = PageEntry<S>(driver)
            context(site) {
                pageEntry.block()
            }
        } finally {
            if (!keepBrowserOpen) {
                driver.quit()
            }
        }
    }
}

/**
 * Convenience overload of [webTest] that accepts a plain [WebDriver] factory.
 *
 * Wraps the provided [driverProfile] function into a [DriverProfile] and delegates to the primary overload.
 *
 * @param S the site type used for the test
 * @param site The site configuration to use throughout the test.
 * @param driverProfile A factory function that returns a configured [WebDriver] instance.
 * @param keepBrowserOpen If `true`, leaves the browser open after the block for debugging.
 * @param block Test body executed with a [PageEntry] and the [site] as a context receiver.
 * @see webTest
 */
public inline fun <S : Site> webTest(
    site: S,
    crossinline driverProfile: () -> WebDriver,
    keepBrowserOpen: Boolean = false,
    crossinline block: context(S) PageEntry<S>.() -> Unit,
): Unit = webTest(site, DriverProfile { driverProfile() }, keepBrowserOpen, block)

context(current: S)
/**
 * Run a nested block with a different [Site] while reusing the current session.
 *
 * Useful for tests that span multiple domains (e.g., SSO, payment providers). Internally delegates to [switchTo].
 *
 * @param S the current site type of this [PageEntry]
 * @param S2 the site type to switch to for the nested block
 * @param site The site to switch to for the duration of [block].
 * @param navigateToBase Whether to navigate to [Site.baseUrl] for [site] before running [block].
 * @param block The test body executed with the new [Site] context and corresponding [PageEntry].
 */
public inline fun <S : Site, S2 : Site> PageEntry<S>.withSite(
    site: S2,
    navigateToBase: Boolean = true,
    crossinline block: context(S2) PageEntry<S2>.() -> Unit,
) {
    SiteContext.withSite(site) {
        val entry = switchTo(site, navigateToBase)
        context(site) { entry.block() }
    }
}

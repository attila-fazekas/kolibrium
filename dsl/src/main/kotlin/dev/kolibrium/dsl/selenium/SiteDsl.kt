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

package dev.kolibrium.dsl.selenium

import dev.kolibrium.common.Cookies
import dev.kolibrium.core.selenium.Page
import dev.kolibrium.core.selenium.Site
import dev.kolibrium.core.selenium.SiteContext
import dev.kolibrium.dsl.selenium.interactions.CookiesScope
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver

/**
 * Factory function that creates a new WebDriver instance for use by Kolibrium DSL helpers such as [webTest].
 *
 * Prefer the predefined factories in dev.kolibrium.dsl.selenium.DriverFactories for common setups
 * (e.g., headlessChrome, incognitoFirefox).
 */
public typealias DriverFactory = () -> WebDriver

/**
 * Marker annotation for the Kolibrium page DSL.
 *
 * Prevents accidental scope leaks between page operations by restricting where DSL receivers can be used.
 */
@DslMarker
public annotation class PageDsl

/**
 * Scoped wrapper around a [dev.kolibrium.core.selenium.Page] that ensures interactions happen only after navigation.
 *
 * Use [on] to perform actions that either return another [dev.kolibrium.core.selenium.Page] (continuing the flow) or a terminal value.
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

    /**
     * Run assertions against the current page and return this scope for fluent chaining.
     *
     * Useful when you want to keep the test flow readable while verifying conditions.
     *
     * @return this [PageScope]
     */
    public fun <T> verify(assertions: P.() -> T): PageScope<P> {
        page.assertions()
        return this
    }

    /** Expose the underlying page instance when needed (e.g., for advanced use cases). */
    public fun unwrap(): P = page
}

/**
 * Entry point for opening and interacting with pages bound to a specific [dev.kolibrium.core.selenium.Site].
 *
 * The DSL guarantees that a page is navigated to before interaction. Use [open] to instantiate a page,
 * navigate to its [Page.path] (or an overridden path), and continue the flow within a [PageScope].
 *
 * @param S The [dev.kolibrium.core.selenium.Site] type driving navigation and configuration.
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

    context(site: S)
    /**
     * Navigate to the given target.
     *
     * If [target] is an absolute URL (starts with http:// or https://), the driver navigates to it as-is.
     * Otherwise, the value is treated as a path relative to the current site's [Site.baseUrl]. A leading slash
     * is added when missing to avoid accidental path concatenation issues.
     *
     * Examples:
     * - navigateTo("/products/42")
     * - navigateTo("products/42")
     * - navigateTo("https://example.com/health")
     */
    public fun PageEntry<S>.navigateTo(
        target: String,
    ) {
        val trimmed = target.trim()
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            driver.get(trimmed)
        } else {
            val normalized = if (trimmed.startsWith('/')) trimmed else "/$trimmed"
            driver.get("${site.baseUrl}$normalized")
        }
    }

    /**
     * Add or manipulate cookies in the current browser session.
     *
     * The provided [builder] runs inside a [CookiesScope] and can add, remove or clear cookies via the
     * standard Selenium cookie management APIs. If [refreshPage] is true, the current page is refreshed
     * after the changes so that cookie effects take place immediately.
     *
     * @param refreshPage Whether to refresh the page after applying cookie changes. Default is false.
     * @param builder The cookie modification DSL.
     * @return This [PageEntry] for fluent chaining.
     */
    public fun PageEntry<S>.withCookies(
        refreshPage: Boolean = false,
        builder: CookiesScope.() -> Unit,
    ): PageEntry<S> {
        val options = driver.manage()
        CookiesScope(options).apply(builder)
        if (refreshPage) driver.navigate().refresh()
        return this
    }
}

/**
 * Run a browser test within a managed driver session and a given [Site] context.
 *
 * The driver is created from [driver], configured via [Site.configureDriver], and navigated to [Site.baseUrl].
 * If site‑level cookies are present, they are applied and the base URL is reloaded to take effect.
 * The driver is quit automatically unless [keepBrowserOpen] is `true`.
 *
 * This overload supports a prepare step that runs before the browser session is created. The value produced by
 * [prepare] is then passed into [startup] and [block], which run inside the managed browser session.
 *
 * @param S the site type used for the test
 * @param T the data type produced by [prepare] and passed to [startup] and [block]
 * @param site The site configuration to use throughout the test.
 * @param driver Factory that returns a configured [WebDriver]. Defaults to a new [org.openqa.selenium.chrome.ChromeDriver].
 * @param keepBrowserOpen If `true`, leaves the browser open after the block for debugging.
 * @param prepare A function executed with the [site] as context before the driver is created; its result is passed to [startup] and [block].
 * @param startup A hook that runs after driver creation and initial navigation but before [block].
 * @param block Test body executed with a [PageEntry] and the [site] as a context receiver; receives the value from [prepare].
 */
public inline fun <S : Site, T> webTest(
    site: S,
    crossinline driver: DriverFactory = { ChromeDriver() },
    keepBrowserOpen: Boolean = false,
    crossinline prepare: context(S) () -> T,
    crossinline startup: context(S) PageEntry<S>.(T) -> Unit = { _ -> },
    crossinline block: context(S) PageEntry<S>.(T) -> Unit,
) {
    SiteContext.withSite(site) {
        val prepared: T = context(site) { prepare() }

        val driver = driver()
        try {
            site.configureDriver(driver)
            driver.get(site.baseUrl)
            if (site.cookies.isNotEmpty()) {
                val options = driver.manage()
                site.cookies.forEach(options::addCookie)
                driver.get(site.baseUrl)
            }

            val pageEntry = PageEntry<S>(driver)
            context(site) { pageEntry.startup(prepared) }
            context(site) { pageEntry.block(prepared) }
        } finally {
            if (!keepBrowserOpen) driver.quit()
        }
    }
}

/**
 * Convenience overload of [webTest] for cases where no prepare data is needed.
 * Uses [Unit] as the prepared value and runs the optional [startup] before [block].
 */
public inline fun <S : Site> webTest(
    site: S,
    crossinline driver: DriverFactory = { ChromeDriver() },
    keepBrowserOpen: Boolean = false,
    crossinline startup: context(S) PageEntry<S>.(Unit) -> Unit = { _ -> },
    crossinline block: context(S) PageEntry<S>.(Unit) -> Unit,
) {
    webTest(
        site = site,
        driver = driver,
        keepBrowserOpen = keepBrowserOpen,
        prepare = { },
        startup = startup,
        block = block,
    )
}

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

/**
 * Run assertions against this page instance and return it for fluent chaining.
 */
public fun <P : Page<*>, T> P.verify(assertions: P.() -> T): P = apply { assertions() }

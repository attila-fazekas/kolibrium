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
import dev.kolibrium.core.selenium.configureWith
import dev.kolibrium.dsl.selenium.interactions.CookiesScope
import dev.kolibrium.dsl.selenium.internal.normalizePath
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.support.ui.FluentWait
import java.net.URI

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
 * Scoped wrapper around a [dev.kolibrium.core.selenium.Page] that keeps interactions within the
 * navigated page flow.
 *
 * Use [on] when an action transitions to another page and you want to continue chaining.
 * Use [then] for Unit-returning interactions that stay on the same page, and [verify] for assertions.
 * You can temporarily [switchTo] another [dev.kolibrium.core.selenium.Site] and return via
 * [SwitchBackScope.switchBack]. The [PageDsl] marker prevents mixing operations from different pages
 * in a single scope.
 *
 * @param P the page type wrapped by this scope
 * @property page the underlying page instance
 * @property entry the page entry backing this scope; holds session context and driver
 */
@PageDsl
public class PageScope<P : Page<*>>(
    public val page: P,
    @PublishedApi internal val entry: PageEntry<out Site>,
) {
    /**
     * Execute an action that transitions to the next page and returns a new [PageScope] for it.
     *
     * @param Next the next page type returned by [action]
     * @param action A function executed on the current page that returns the next page.
     * @return A scope for the returned page.
     */
    public fun <Next : Page<*>> on(action: P.() -> Next): PageScope<Next> {
        val next = page.action()
        // Ensure the returned page is ready as well
        entry.ensureReady(next, entry.driver, SiteContext.get()!!)
        return PageScope(next, entry)
    }

    /**
     * Execute a Unit-returning action and keep the current page scope for further fluent chaining.
     * Use when the lambda performs interactions but does not transition to a new Page.
     */
    public fun then(action: P.() -> Unit): PageScope<P> {
        page.action()
        return this
    }

    /**
     * Run assertions against the current page and return this scope for fluent chaining.
     *
     * Useful when you want to keep the test flow readable while verifying conditions.
     *
     * @return this [PageScope]
     */
    public fun verify(assertions: P.() -> Unit): PageScope<P> {
        // Lightweight identity guard; avoid re-waiting on every assertion
        page.assertReady()
        page.assertions()
        return this
    }

    /**
     * Member version of switchTo so callers can write `.switchTo<Twitter> { … }` without specifying the page type.
     * Captures the receiver's page type [P] for the returned [SwitchBackScope].
     */
    public inline fun <reified S2 : Site> switchTo(
        navigateToBase: Boolean = true,
        cookies: Cookies? = null,
        crossinline block: context(S2) PageEntry<S2>.() -> Unit,
    ): SwitchBackScope<P> {
        val driver = entry.driver
        val originalEntry = entry

        // Remember the original environment
        val originalWindow = driver.windowHandle
        val originalSite: Site = SiteContext.get()!!

        // Window selection heuristic: when a new tab likely opened, pick the last handle.
        val handles = driver.windowHandles.toList()
        if (handles.size > 1 && originalWindow != handles.last()) {
            driver.switchTo().window(handles.last())
        }

        // Switch site context manually
        val targetSite: S2 = siteOf()
        SiteContext.set(targetSite)
        targetSite.configureDriver(driver)
        if (cookies != null && cookies.isNotEmpty()) {
            val options = driver.manage()
            cookies.forEach(options::addCookie)
        }
        if (navigateToBase) driver.get(targetSite.baseUrl)
        val targetEntry: PageEntry<S2> = PageEntry(driver)

        // Execute user block in target site context
        context(targetSite) { targetEntry.block() }

        // Return a scope that can restore the original page as receiver
        return SwitchBackScope(
            original = originalEntry,
            originalSite = originalSite,
            originalWindow = originalWindow,
            originalPage = this.page,
        )
    }
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
    // --- Readiness utilities ---
    @PublishedApi
    internal fun ensureReady(
        page: Page<*>,
        driver: WebDriver,
        site: Site,
    ) {
        val descriptor = page.ready
        if (descriptor != null) {
            val waitCfg = descriptor.waitConfig ?: site.waitConfig
            val elementReady = descriptor.readyWhen ?: site.elementReadyCondition
            val wait = FluentWait(driver).configureWith(waitCfg)
            val by = descriptor.by
            wait.until {
                driver.findElements(by).firstOrNull()?.let(elementReady) == true
            }
        }
        page.awaitReady(driver)
        page.assertReady()
    }

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
        val normalizedPath = normalizePath(targetPath)
        driver.get("${site.baseUrl}$normalizedPath")

        // Readiness pipeline
        ensureReady(page, driver, site)

        return PageScope(page.action(), this)
    }

    context(site: S)
    /**
     * Instantiate a page without navigation and execute [action] on it.
     *
     * This is useful when the target page is already open (e.g., after a tab switch)
     * and you only want to bind a page object to the current tab.
     * A guard ensures the current tab's origin matches the current [Site]'s origin.
     */
    public fun <P : Page<S>, R : Page<*>> on(
        pageFactory: (WebDriver) -> P,
        action: P.() -> R,
    ): PageScope<R> {
        val page = pageFactory(driver)

        // Origin check: ensure we are on the same origin as the current site (allowing www. difference)
        val currentUri = kotlin.runCatching { URI(driver.currentUrl) }.getOrNull()
        val siteUri = kotlin.runCatching { URI(site.baseUrl) }.getOrNull()

        fun normalizeHost(uri: URI?): String? = uri?.host?.lowercase()?.removePrefix("www.")
        val currentHost = normalizeHost(currentUri)
        val siteHost = normalizeHost(siteUri)
        require(currentHost != null && siteHost != null && currentHost == siteHost) {
            "Current tab origin (${currentUri?.authority ?: currentUri?.host}) does not match site origin (${siteUri?.authority ?: siteUri?.host}). Use switchTo(...) first or navigate explicitly."
        }

        // Readiness pipeline for non-navigation binding
        ensureReady(page, driver, site)

        return PageScope(page.action(), this)
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
            val normalized = normalizePath(trimmed)
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
     * Notes:
     * - Selenium only allows adding cookies for the current origin. Ensure the driver is already on the target site's origin.
     * - This function does not auto-refresh unless [refreshPage] is true.
     *
     * @param refreshPage Whether to refresh the page after applying cookie changes. Default is false.
     * @param builder The cookie modification DSL.
     * @return This [PageEntry] for fluent chaining.
     */
    public fun cookies(
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
 * Add or manipulate cookies while staying on the same page scope.
 */
public fun <P : Page<*>> PageScope<P>.cookies(
    refreshPage: Boolean = false,
    builder: CookiesScope.() -> Unit,
): PageScope<P> {
    entry.cookies(refreshPage, builder)
    return this
}

/**
 * Run a browser test within a managed driver session and a given [Site] context.
 *
 * The driver is created from [driverFactory], configured via [Site.configureDriver], and navigated to [Site.baseUrl].
 * If site‑level cookies are present, they are applied and the base URL is reloaded to take effect.
 * The driver is quit automatically unless [keepBrowserOpen] is `true`.
 *
 * This overload supports a prepare step that runs before the browser session is created. The value produced by
 * [prepare] is then passed into [startup] and [block], which run inside the managed browser session.
 *
 * @param S the site type used for the test
 * @param T the data type produced by [prepare] and passed to [startup] and [block]
 * @param site The site configuration to use throughout the test.
 * @param keepBrowserOpen If `true`, leaves the browser open after the block for debugging.
 * @param driverFactory Factory that returns a configured [WebDriver]. Defaults to a new [org.openqa.selenium.chrome.ChromeDriver].
 * @param prepare A function executed with the [site] as context before the driver is created; its result is passed to [startup] and [block].
 * @param startup A hook that runs after driver creation and initial navigation but before [block].
 * @param block Test body executed with a [PageEntry] and the [site] as a context receiver; receives the value from [prepare].
 */
public inline fun <S : Site, T> webTest(
    site: S,
    keepBrowserOpen: Boolean = false,
    crossinline driverFactory: DriverFactory = { ChromeDriver() },
    crossinline prepare: context(S) () -> T,
    crossinline startup: context(S) PageEntry<S>.(T) -> Unit = { _ -> },
    crossinline block: context(S) PageEntry<S>.(T) -> Unit,
) {
    SiteContext.withSite(site) {
        val prepared: T = context(site) { prepare() }

        val driver = driverFactory()
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
    keepBrowserOpen: Boolean = false,
    crossinline driverFactory: DriverFactory = { ChromeDriver() },
    crossinline startup: context(S) PageEntry<S>.(Unit) -> Unit = { _ -> },
    crossinline block: context(S) PageEntry<S>.(Unit) -> Unit,
) {
    webTest(
        site = site,
        keepBrowserOpen = keepBrowserOpen,
        driverFactory = driverFactory,
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
public fun <P : Page<*>> P.verify(assertions: P.() -> Unit): P = apply { assertions() }

/**
 * Resolve a [Site] singleton instance by its type parameter.
 *
 * The target [S] must be declared as a Kotlin `object`.
 */
public inline fun <reified S : Site> siteOf(): S =
    S::class.objectInstance
        ?: error("${S::class.simpleName} must be declared as a Kotlin object to use siteOf()")

/**
 * Wrapper representing a switched site/window context, allowing the caller to switch back
 * to the original site and window via [switchBack].
 */
public class SwitchBack<S1 : Site>(
    private val original: PageEntry<S1>,
    private val originalSite: S1,
    private val originalWindow: String,
) {
    /**
     * Switch back to the original browser window and site context, then run [block].
     */
    public fun switchBack(block: context(S1) PageEntry<S1>.() -> Unit): PageEntry<S1> {
        val driver = original.driver
        // Return to the original window
        driver.switchTo().window(originalWindow)
        // Restore site context and configuration
        SiteContext.set(originalSite)
        originalSite.configureDriver(driver)
        // Execute caller's code in the restored site context
        context(originalSite) { original.block() }
        return original
    }
}

/** A variant of [SwitchBack] that restores the original page as the receiver. */
public class SwitchBackScope<P : Page<*>>(
    private val original: PageEntry<out Site>,
    private val originalSite: Site,
    private val originalWindow: String,
    private val originalPage: P,
) {
    /**
     * Switch back to the original window and site, then execute [block] on the original page instance.
     */
    public fun switchBack(block: P.() -> Unit): PageScope<P> {
        val driver = original.driver
        driver.switchTo().window(originalWindow)
        SiteContext.set(originalSite)
        originalSite.configureDriver(driver)
        // Ensure the original page is ready before executing the block
        original.ensureReady(originalPage, driver, originalSite)
        originalPage.block()
        return PageScope(originalPage, original)
    }
}

context(_: S1)
/**
 * Switch to another [Site] identified by type parameter [S2], run [block] in that site's context,
 * and return a [SwitchBack] handle.
 *
 * Behavior:
 * - Resolves the target site via [siteOf] so [S2] must be declared as a Kotlin `object`.
 * - Calls [Site.configureDriver], applies optional [cookies] before navigation, and optionally navigates to
 *   the target site's base URL when [navigateToBase] is true.
 * - Window policy: if multiple windows exist and the current window is not the last handle, it switches to
 *   the last handle (common when an external link opened a new tab).
 *
 * Receiver: a [PageEntry] of the current site (provided via a context receiver of type `S1`).
 *
 * @param S2 the target site type to switch to.
 * @param S1 the current site type of this [PageEntry] (context receiver).
 * @param navigateToBase Whether to navigate to the target site's base URL after switching (default true).
 * @param cookies Optional cookies to apply right after switching.
 * @param block Code to run in the target site context using a [PageEntry] bound to it.
 * @return a [SwitchBack] handle that can restore the original site and window.
 */
public inline fun <reified S2 : Site, S1 : Site> PageEntry<S1>.switchTo(
    navigateToBase: Boolean = true,
    cookies: Cookies? = null,
    crossinline block: context(S2) PageEntry<S2>.() -> Unit,
): SwitchBack<S1> {
    val driver = this.driver

    // Remember the original environment
    val originalWindow = driver.windowHandle

    @Suppress("UNCHECKED_CAST")
    val originalSite = SiteContext.get() as S1

    // Window selection heuristic: when a new tab likely opened, pick the last handle.
    val handles = driver.windowHandles.toList()
    if (handles.size > 1 && originalWindow != handles.last()) {
        driver.switchTo().window(handles.last())
    }

    // Switch site context
    val targetSite: S2 = siteOf()
    val targetEntry: PageEntry<S2> = this.switchTo(targetSite, navigateToBase, cookies)

    // Run the block within the target site's context
    context(targetSite) { targetEntry.block() }

    // Provide a handle to switch back later
    return SwitchBack(original = this, originalSite = originalSite, originalWindow = originalWindow)
}

/**
 * Overload of [switchTo] that starts from a [PageScope] so you can fluently write
 * open(::InventoryPage) { visitTwitter() }.switchTo<Twitter> { ... }.switchBack { goToCart() }
 */
public inline fun <reified S2 : Site, P : Page<*>> PageScope<P>.switchTo(
    navigateToBase: Boolean = true,
    cookies: Cookies? = null,
    crossinline block: context(S2) PageEntry<S2>.() -> Unit,
): SwitchBackScope<P> {
    val driver = this.entry.driver
    val originalEntry = this.entry

    // Remember the original environment
    val originalWindow = driver.windowHandle
    val originalSite: Site = SiteContext.get()!!

    // Window selection heuristic: when a new tab likely opened, pick the last handle.
    val handles = driver.windowHandles.toList()
    if (handles.size > 1 && originalWindow != handles.last()) {
        driver.switchTo().window(handles.last())
    }

    // Switch site context manually (avoid requiring a context receiver here)
    val targetSite: S2 = siteOf()
    SiteContext.set(targetSite)
    targetSite.configureDriver(driver)
    if (cookies != null && cookies.isNotEmpty()) {
        val options = driver.manage()
        cookies.forEach(options::addCookie)
    }
    if (navigateToBase) driver.get(targetSite.baseUrl)
    val targetEntry: PageEntry<S2> = PageEntry(driver)

    // Execute user block in target site context
    context(targetSite) { targetEntry.block() }

    // Return a scope that can restore the original page as receiver
    return SwitchBackScope(
        original = originalEntry,
        originalSite = originalSite,
        originalWindow = originalWindow,
        originalPage = this.page,
    )
}

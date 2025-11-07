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

import dev.kolibrium.core.selenium.Cookies
import dev.kolibrium.core.selenium.Page
import dev.kolibrium.core.selenium.Site
import dev.kolibrium.core.selenium.SiteContext
import dev.kolibrium.core.selenium.withDriver
import org.openqa.selenium.Cookie
import org.openqa.selenium.WebDriver
import java.net.URI

/**
 * Scope used as the fluent receiver when working with a [Page] in the Selenium DSL.
 *
 * It ties a concrete [page] instance to the live browser session behind the scenes and
 * provides helpers to continue the flow, assert state, or temporarily switch to another [Site].
 *
 * @param P the type of the current page
 * @property page the current page instance bound to the active WebDriver session
 * @property entry internal wiring to the underlying browser session; not intended for direct use
 */
@KolibriumDsl
public class PageScope<P : Page<*>>(
    public val page: P,
    @PublishedApi internal val entry: PageEntry<out Site>,
) {
    /**
     * Execute [action] on the current [page], producing the next page in the flow.
     *
     * The current page is ensured to be ready before the action runs. The returned scope
     * is bound to the newly produced page.
     *
     * @param Next the type of the next page produced by [action]
     * @param action operation to perform on the current page that returns the next page
     */
    @KolibriumDsl
    public fun <Next : Page<*>> on(action: P.() -> Next): PageScope<Next> =
        withDriver(entry.driver) {
            page.assertReady()
            val next = page.action()
            entry.scope(next)
        }

    /**
     * Run [assertions] against the current page, keeping the scope unchanged.
     *
     * The page is ensured to be ready before assertions are executed.
     */
    @KolibriumDsl
    public fun verify(assertions: P.() -> Unit): PageScope<P> =
        apply {
            withDriver(entry.driver) {
                page.assertReady()
                page.assertions()
            }
        }

    /**
     * Execute a side-effecting [action] on the current page, keeping the scope unchanged.
     *
     * The page is ensured to be ready before the action runs.
     */
    @KolibriumDsl
    public fun then(action: P.() -> Unit): PageScope<P> =
        apply {
            withDriver(entry.driver) {
                page.assertReady()
                page.action()
            }
        }

    /**
     * Temporarily switch to another [Site] within the same browser session, run [block],
     * and return a [SwitchBackScope] that can restore the original site/window/page.
     *
     * If a new tab/window was likely opened before calling this function, the newest handle will be selected.
     *
     * @param S2 the target site type to switch to
     * @param navigateToBase whether to navigate to the target site's base URL before running [block]
     * @param cookies optional cookies to apply in the target site context
     * @param block operations to perform in the target site context
     * @return a scope that allows switching back to the original context
     */
    public inline fun <reified S2 : Site> switchTo(
        navigateToBase: Boolean = true,
        cookies: Cookies? = null,
        crossinline block: PageEntry<S2>.() -> Unit,
    ): SwitchBackScope<P> {
        val originalEntry = entry

        // Remember the original environment
        val originalWindow = originalEntry.currentWindowHandle()
        val originalSite: Site = SiteContext.get()!!

        // Window selection heuristic: when a new tab likely opened, pick the last handle.
        originalEntry.switchToNewestWindowIfOpenedSince(originalWindow)

        // Switch site context using helper (no WebDriver leakage)
        val (targetSite, targetEntry) =
            performSiteSwitch(
                originalEntry,
                navigateToBase,
                cookies,
            ) { siteOf<S2>() }

        // Execute user block in target site context
        context(targetSite) { targetEntry.block() }

        // Return a scope that can restore the original page as receiver
        return SwitchBackScope(
            originalEntry = originalEntry,
            originalSite = originalSite,
            originalWindow = originalWindow,
            originalPage = this.page,
        )
    }

    /** Inline gateway to the underlying entry without exposing it as a property. */
    public inline fun <R> withEntry(block: (PageEntry<out Site>) -> R): R = block(entry)
}

/**
 * Lightweight entry point bound to a live [WebDriver] session for a specific [dev.kolibrium.core.selenium.Site].
 *
 * Instances are created by the test harness (see webTest) and passed into user code as the receiver
 * of startup and test blocks.
 */
@KolibriumDsl
public class PageEntry<S : Site>
    @PublishedApi
    internal constructor(
        internal val driver: WebDriver,
    ) {
        /** Navigate the current tab to the given absolute URL. */
        internal fun navigateTo(url: String) {
            driver.get(url)
        }

        /** All known window handles in this session. */
        internal fun windowHandles(): Set<String> = driver.windowHandles

        /** The handle of the currently active window or tab. */
        @PublishedApi
        internal fun currentWindowHandle(): String = driver.windowHandle

        /** Switch to a different window or tab identified by its handle. */
        internal fun switchToWindow(handle: String) {
            driver.switchTo().window(handle)
        }

        /** Apply a prebuilt collection of cookies to the current session. */
        internal fun applyCookies(cookies: Cookies) {
            if (cookies.isEmpty()) return
            val manager = driver.manage().cookies
            cookies.forEach(manager::add)
        }

        /** Add a cookie to the current browser session. */
        public fun addCookie(cookie: Cookie) {
            driver.manage().addCookie(cookie)
        }

        /** Delete a cookie by name in the current browser session. */
        public fun deleteCookie(name: String) {
            driver.manage().deleteCookieNamed(name)
        }

        /** Delete all cookies in the current browser session. */
        public fun deleteAllCookies() {
            driver.manage().deleteAllCookies()
        }

        /** Configure the given site against this entry's live session. */
        internal fun configureSite(site: Site) {
            site.configure(driver)
        }

        /**
         * Open a page created by [factory], navigate to its route, wait for readiness, and run [action]
         * that returns the next page to continue the flow.
         *
         * The returned page will have the active browser session attached and will be synchronized via
         * await/assert before being returned.
         */
        public fun <P : Page<S>, R : Page<*>> open(
            factory: () -> P,
            path: String? = null,
            action: P.() -> R,
        ): PageScope<R> {
            val page = factory()

            val site =
                SiteContext.get()
                    ?: error("No active Site in SiteContext; open() must be called within webTest/site context.")

            val effectivePath = path ?: page.path
            val url = joinUrls(site.baseUrl, effectivePath)
            driver.get(url)

            return withDriver(driver) {
                ensureReady(page)
                val next = page.action()
                this@PageEntry.scope(next)
            }
        }

        /**
         * Instantiate a page without navigation and execute [action] on it.
         *
         * This is useful when the target page is already open (e.g., after a tab switch)
         * and you only want to bind a page object to the current tab.
         * A guard ensures the current tab's origin matches the current [dev.kolibrium.core.selenium.Site]'s origin.
         */
        public fun <P : Page<S>, R : Page<*>> on(
            factory: () -> P,
            action: P.() -> R,
        ): PageScope<R> {
            val page = factory()

            val site =
                SiteContext.get()
                    ?: error("No active Site in SiteContext; on() must be called within webTest/site context.")

            // Ensure the current tab belongs to the active site origin
            val currentUri = runCatching { URI(driver.currentUrl) }.getOrNull()
            val siteUri = runCatching { URI(site.baseUrl) }.getOrNull()

            fun normalizeHost(uri: URI?): String? = uri?.host?.lowercase()?.removePrefix("www.")

            val currentHost = normalizeHost(currentUri)
            val siteHost = normalizeHost(siteUri)
            require(currentHost != null && siteHost != null && currentHost == siteHost) {
                "Current tab origin does not match site origin"
            }

            return withDriver(driver) {
                ensureReady(page)
                val next = page.action()
                this@PageEntry.scope(next)
            }
        }

        internal fun <R : Page<*>> scope(next: R): PageScope<R> {
            ensureReady(next)
            return PageScope(next, this)
        }

        /**
         * Run assertions on the current page instance and return it for fluent chaining.
         *
         * Usage:
         * open(::SomePage) { /* interactions */ }.verify { /* assertions with receiver = SomePage */ }
         */
        public fun <P : Page<S>> P.verify(assertions: P.() -> Unit): P {
            this.assertReady()
            this.assertions()
            return this
        }

        private fun ensureReady(page: Page<*>) {
            page.awaitReady()
            page.assertReady()
        }

        @PublishedApi
        internal fun switchToNewestWindowIfOpenedSince(originalWindow: String) {
            val handles = driver.windowHandles.toList()
            if (handles.size > 1 && originalWindow != handles.last()) {
                driver.switchTo().window(handles.last())
            }
        }
    }

/**
 * Handle returned from [PageScope.switchTo] that can restore the original site/window/page context.
 *
 * Use [switchBack] to return to the original context and continue the flow on the original page.
 */
@KolibriumDsl
public class SwitchBackScope<P : Page<*>>(
    private val originalEntry: PageEntry<out Site>,
    private val originalSite: Site,
    private val originalWindow: String,
    private val originalPage: P,
) {
    /**
     * Restore the original site and window context and run [block] on the original page.
     *
     * @return a [PageScope] bound to the original page to continue the flow
     */
    @KolibriumDsl
    public fun switchBack(block: P.() -> Unit): PageScope<P> {
        // Restore original window
        originalEntry.switchToWindow(originalWindow)

        // Restore site context and run its configurator on the same session
        SiteContext.set(originalSite)
        originalEntry.configureSite(originalSite)

        // Execute the caller's code back on the original page within the original driver context
        return withDriver(originalEntry.driver) {
            originalPage.block()
            PageScope(originalPage, originalEntry)
        }
    }
}

@PublishedApi
internal fun <S2 : Site> performSiteSwitch(
    originalEntry: PageEntry<out Site>,
    navigateToBase: Boolean,
    cookies: Cookies?,
    siteProvider: () -> S2,
): Pair<S2, PageEntry<S2>> {
    val targetSite: S2 = siteProvider()

    // Bind target site in the thread‐local context
    SiteContext.set(targetSite)

    @Suppress("UNCHECKED_CAST")
    val typedEntry = originalEntry as PageEntry<S2>

    // Apply cookies (if any)
    if (cookies != null && cookies.isNotEmpty()) {
        typedEntry.applyCookies(cookies)
    }

    // Optionally navigate to base URL
    if (navigateToBase) {
        typedEntry.navigateTo(targetSite.baseUrl)
    }

    // Let the site finalize its session configuration
    typedEntry.configureSite(targetSite)

    return targetSite to typedEntry
}

// --- helpers ---

@PublishedApi
internal inline fun <reified S : Site> siteOf(): S =
    S::class.objectInstance
        ?: error("${S::class.simpleName} must be declared as a Kotlin object to use siteOf()")

private fun joinUrls(
    base: String,
    path: String,
): String {
    if (path.isBlank()) return base
    if (path.startsWith("http://") || path.startsWith("https://")) return path
    val baseNormalized = if (base.endsWith('/')) base.dropLast(1) else base
    val pathNormalized = if (path.startsWith('/')) path.drop(1) else path
    return "$baseNormalized/$pathNormalized"
}

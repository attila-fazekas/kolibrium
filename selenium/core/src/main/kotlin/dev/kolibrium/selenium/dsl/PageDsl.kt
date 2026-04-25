/*
 * Copyright 2023-2026 Attila Fazekas & contributors
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

package dev.kolibrium.selenium.dsl

import dev.kolibrium.annotations.InternalKolibriumApi
import dev.kolibrium.annotations.KolibriumDsl
import dev.kolibrium.selenium.core.SeleniumPage
import dev.kolibrium.selenium.core.SeleniumSite
import dev.kolibrium.selenium.core.Session
import dev.kolibrium.selenium.core.SessionContext
import dev.kolibrium.selenium.core.withDriver
import org.openqa.selenium.Cookie
import org.openqa.selenium.WebDriver
import java.net.URI
import kotlin.reflect.KClass

private typealias Cookies = Set<Cookie>

/**
 * Scope used as the fluent receiver when working with a [SeleniumPage] in the Selenium DSL.
 *
 * It ties a concrete [page] instance to the live browser session behind the scenes and
 * provides helpers to continue the flow, assert state, or temporarily switch to another [SeleniumSite].
 *
 * @param P the type of the current page
 * @property page the current page instance bound to the active WebDriver session
 * @property entry internal wiring to the underlying browser session; not intended for direct use
 */
@KolibriumDsl
public class PageScope<P : SeleniumPage<*>> internal constructor(
    public val page: P,
    internal val entry: PageEntry<out SeleniumSite>,
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
    public fun <Next : SeleniumPage<*>> on(action: P.() -> Next): PageScope<Next> =
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
    public fun then(action: P.() -> Unit): PageScope<P> =
        apply {
            withDriver(entry.driver) {
                page.assertReady()
                page.action()
            }
        }
}

/**
 * Lightweight entry point bound to a live [WebDriver] session for a specific [SeleniumSite].
 *
 * Instances are created by the test harness (see seleniumTest) and passed into user code as the receiver
 * of startup and test blocks.
 */
@KolibriumDsl
internal class PageEntry<S : SeleniumSite>
    internal constructor(
        internal val driver: WebDriver,
    ) : SiteEntry<S> {
        /** Navigate the current tab to the given absolute URL. */
        internal fun navigateTo(url: String) {
            requireSessionChecked("PageEntry.navigateTo")
            driver.get(url)
        }

        /** Add a cookie to the current browser session. */
        override fun addCookie(cookie: Cookie) {
            requireSessionChecked("SiteEntry.addCookie")
            driver.manage().addCookie(cookie)
        }

        /** Delete a cookie by name in the current browser session. */
        override fun deleteCookie(name: String) {
            requireSessionChecked("SiteEntry.deleteCookie")
            driver.manage().deleteCookieNamed(name)
        }

        /** Delete all cookies in the current browser session. */
        override fun deleteAllCookies() {
            requireSessionChecked("SiteEntry.deleteAllCookies")
            driver.manage().deleteAllCookies()
        }

        /** Configure the given site against this entry's live session. */
        internal fun configureSite(seleniumSite: SeleniumSite) {
            seleniumSite.configureSite()
            seleniumSite.onSessionReady(driver)
        }

        /**
         * Open a page created by [factory], navigate to its route, wait for readiness, and run [action]
         * that returns the next page to continue the flow.
         *
         * The returned page will have the active browser session attached and will be synchronized via
         * await/assert before being returned.
         */
        override fun <P : SeleniumPage<S>, R : SeleniumPage<S>> open(
            factory: () -> P,
            path: String?,
            action: P.() -> R,
        ): PageScope<R> {
            requireSessionChecked("SiteEntry.open")
            val page = factory()

            val site =
                SessionContext.get()?.seleniumSite
                    ?: error("No active Session in SessionContext; open() must be called within seleniumTest/site context.")

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
         * A guard ensures the current tab's origin matches the current [SeleniumSite]'s origin.
         */
        override fun <P : SeleniumPage<S>, R : SeleniumPage<S>> on(
            factory: () -> P,
            action: P.() -> R,
        ): PageScope<R> {
            requireSessionChecked("SiteEntry.on")
            val page = factory()

            val site =
                SessionContext.get()?.seleniumSite
                    ?: error("No active Session in SessionContext; on() must be called within seleniumTest/site context.")

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

        internal fun <R : SeleniumPage<*>> scope(next: R): PageScope<R> {
            ensureReady(next)
            return PageScope(next, this)
        }

        /**
         * Run assertions on the current page instance and return it for fluent chaining.
         *
         * Usage:
         * open(::SomePage) { /* interactions */ }.verify { /* assertions with receiver = SomePage */ }
         */
        fun <P : SeleniumPage<S>> P.verify(assertions: P.() -> Unit): P {
            this.assertReady()
            this.assertions()
            return this
        }

        private fun ensureReady(seleniumPage: SeleniumPage<*>) {
            seleniumPage.awaitReady()
            seleniumPage.assertReady()
        }

        private fun requireSessionChecked(op: String): Session =
            SessionContext.get()?.also { it.assertThreadOrFail(op) }
                ?: error("No active Session in SessionContext; $op requires an active session.")
    }

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

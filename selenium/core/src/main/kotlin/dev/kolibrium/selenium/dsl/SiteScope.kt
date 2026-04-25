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

import dev.kolibrium.annotations.KolibriumDsl
import dev.kolibrium.selenium.core.SeleniumPage
import dev.kolibrium.selenium.core.SeleniumSite
import dev.kolibrium.selenium.core.Session
import dev.kolibrium.selenium.core.SessionContext
import dev.kolibrium.selenium.dsl.interactions.CookiesScope
import dev.kolibrium.selenium.dsl.interactions.cookies
import org.openqa.selenium.Cookie
import org.openqa.selenium.WebDriver
import java.net.URI

/**
 * Site-scoped DSL receiver available inside `seleniumTest { … }` blocks.
 *
 * It represents the entry surface for flows on the active [SeleniumSite], exposing operations like [open], [on],
 * and cookie helpers.
 *
 */
@KolibriumDsl
public class SiteScope<S : SeleniumSite> internal constructor(
    internal val driver: WebDriver,
) {
    public fun cookies(
        refreshPage: Boolean = false,
        block: CookiesScope.() -> Unit,
    ) {
        driver.cookies(refreshPage, block)
    }

    /**
     * Open a page created by [factory], navigate to its route, wait for readiness, and run [action].
     *
     * The returned page will have the active browser session attached and will be synchronized via
     * await/assert before being returned.
     *
     * @param P the type of the page to open
     * @param factory factory function to create the page instance
     * @param path optional path override; if null, uses the page's default path
     * @param action operation to perform on the page
     * @return a [PageScope] bound to the opened page for further chaining
     */
    public fun <P : SeleniumPage<S>> open(
        factory: () -> P,
        path: String? = null,
        action: P.() -> Unit,
    ): PageScope<P> {
        requireDriver("SiteScope.open")
        val page = factory()

        val site =
            SessionContext.get()?.seleniumSite
                ?: error("No active Session in SessionContext; open() must be called within seleniumTest/site context.")

        val effectivePath = path ?: page.path
        val url = joinUrls(site.baseUrl, effectivePath)
        driver.get(url)

        ensureReady(page)
        page.action()
        return PageScope(page, driver)
    }

    /**
     * Instantiate a page without navigation and execute [action] on it.
     *
     * This is useful when the target page is already open (e.g., after a tab switch)
     * and you only want to bind a page object to the current tab.
     * A guard ensures the current tab's origin matches the current [SeleniumSite]'s origin.
     *
     * @param P the type of the page to bind
     * @param factory factory function to create the page instance
     * @param action operation to perform on the page
     * @return a [PageScope] bound to the bound page for further chaining
     */
    public fun <P : SeleniumPage<S>> on(
        factory: () -> P,
        action: P.() -> Unit,
    ): PageScope<P> {
        requireDriver("SiteScope.on")
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

        ensureReady(page)
        page.action()
        return PageScope(page, driver)
    }

    internal fun <R : SeleniumPage<*>> scope(next: R): PageScope<R> {
        ensureReady(next)
        return PageScope(next, driver)
    }

    private fun requireDriver(op: String): Session =
        SessionContext.get()?.also { it.assertThreadOrFail(op) }
            ?: error("No active Session in SessionContext; $op requires an active session.")
}

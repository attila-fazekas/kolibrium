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

package dev.kolibrium.playwright

import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.Cookie
import dev.kolibrium.annotations.KolibriumDsl

/**
 * Top-level DSL scope for a Playwright test session bound to a [PlaywrightSite].
 *
 * Provides page navigation via [on] and runtime cookie management for the active
 * [BrowserContext]. All operations are guarded by thread confinement checks when a
 * [PlaywrightSession] is active.
 *
 * Instances are created internally by the harness ([playwrightTestImpl]) and supplied
 * as the receiver of the user's test block.
 *
 * Example:
 * ```kotlin
 * sauceDemoTest {
 *     on(::LoginPage) {
 *         login()
 *     }.on(::InventoryPage) {
 *         titleText() shouldBe "Products"
 *     }
 * }
 * ```
 *
 * @param S The concrete [PlaywrightSite] type this scope operates on.
 * @param page The Playwright [Page] for the current session.
 */
@KolibriumDsl
public class SiteScope<S : PlaywrightSite> internal constructor(
    private val page: Page,
) {
    private val context: BrowserContext get() = page.context()

    /**
     * Creates a page object via [factory], runs readiness checks, executes [action] on it,
     * and returns a [PageScope] for chaining further page transitions.
     *
     * @param P The page object type.
     * @param factory Constructor reference for the page object (e.g., `::LoginPage`).
     * @param action The block to execute with the page object as receiver.
     * @return A [PageScope] bound to the created page object, enabling chained [PageScope.on] or [PageScope.then] calls.
     */
    public fun <P : PlaywrightPage<S>> on(
        factory: () -> P,
        action: P.() -> Unit,
    ): PageScope<S, P> {
        val pageObject = factory()
        ensureReady(pageObject)
        pageObject.action()
        return PageScope(pageObject, page)
    }

    /**
     * Adds a single cookie to the current browser context.
     *
     * @param cookie The Playwright [Cookie] to add.
     */
    public fun addCookie(cookie: Cookie) {
        assertThread("addCookie")
        context.addCookies(listOf(cookie))
    }

    /**
     * Adds multiple cookies to the current browser context.
     *
     * @param cookies The list of Playwright [Cookie] instances to add.
     */
    public fun addCookies(cookies: List<Cookie>) {
        assertThread("addCookies")
        context.addCookies(cookies)
    }

    /**
     * Removes cookies matching the given [name] from the current browser context.
     *
     * @param name The name of the cookie(s) to remove.
     */
    public fun clearCookie(name: String) {
        assertThread("clearCookie")
        context.clearCookies(BrowserContext.ClearCookiesOptions().setName(name))
    }

    /**
     * Removes all cookies from the current browser context.
     */
    public fun clearCookies() {
        assertThread("clearCookies")
        context.clearCookies()
    }

    /**
     * Returns all cookies visible to the current browser context.
     *
     * @return A list of all [Cookie] instances in the context.
     */
    public fun cookies(): List<Cookie> {
        assertThread("cookies")
        return context.cookies()
    }

    /**
     * Returns all cookies for the given [urls].
     *
     * @param urls The URLs to filter cookies by.
     * @return A list of [Cookie] instances matching the specified URLs.
     */
    public fun cookies(vararg urls: String): List<Cookie> {
        assertThread("cookies")
        return context.cookies(urls.toList())
    }

    private fun assertThread(op: String) {
        PlaywrightSessionContext.get()?.assertThreadOrFail("SiteScope.$op")
    }
}

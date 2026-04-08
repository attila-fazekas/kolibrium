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

import com.microsoft.playwright.Page

/**
 * Base type for Playwright page objects bound to a [PlaywrightSite].
 *
 * Subclasses model a single page (or screen) of the application under test and expose
 * domain-specific actions and queries. The underlying Playwright [Page] is available
 * via the [page] property, which is `protected` to encourage encapsulation —
 * tests interact through page object methods, not raw locators.
 *
 * Page instances rely on a contextual Playwright [Page] installed by the Kolibrium DSL
 * ([playwrightTest] → [SiteScope.on]). Constructing or using a page object
 * outside that context will fail with a runtime error.
 *
 * @param S The concrete [PlaywrightSite] this page belongs to.
 */
public abstract class PlaywrightPage<S : PlaywrightSite> {
    /**
     * Hook invoked by the harness after the page object is created and before actions run.
     *
     * Override to wait for a readiness signal (e.g., a specific element becoming visible).
     * Default is a no-op.
     */
    public open fun awaitReady() {}

    /**
     * Hook invoked by the harness immediately after [awaitReady].
     *
     * Override to assert page invariants (e.g., expected URL or title).
     * Default is a no-op.
     */
    public open fun assertReady() {}

    /**
     * The Playwright [Page] for the current session.
     *
     * Access is guarded by a thread confinement check when a [Session] is active.
     *
     * @throws IllegalStateException if no Playwright page context is available on the current thread.
     */
    protected val page: Page
        get() {
            SessionContext.get()?.assertThreadOrFail("PlaywrightPage.page")
            return PlaywrightPageContextHolder.get() ?: error(
                "Kolibrium runtime error: PlaywrightPage '${this::class.simpleName ?: "<unknown>"}' " +
                    "has no active Playwright Page context.\n" +
                    "Run page interactions inside Kolibrium DSL (e.g., playwrightTest → on).",
            )
        }
}

/**
 * Runs the readiness lifecycle for a [PlaywrightPage]: thread confinement check,
 * then [PlaywrightPage.awaitReady] followed by [PlaywrightPage.assertReady].
 *
 * Called by [SiteScope.on] and [PageScope.on]/[PageScope.then] before user actions execute.
 *
 * @param page The page object to check for readiness.
 */
internal fun ensureReady(page: PlaywrightPage<*>) {
    SessionContext.get()?.assertThreadOrFail("ensureReady")
    page.awaitReady()
    page.assertReady()
}

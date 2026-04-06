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
import dev.kolibrium.annotations.KolibriumDsl

/**
 * DSL scope representing the result of a page transition, enabling fluent chaining.
 *
 * Returned by [SiteScope.on] and [PageScope.on], this scope lets you chain further page
 * transitions with [on] or stay on the current page with [then].
 *
 * @param S The concrete [PlaywrightSite] type.
 * @param P The current page object type.
 * @property pageObject The current page object.
 * @property page The underlying Playwright [Page] for the current session.
 * @property context The underlying Playwright [BrowserContext] for the current session.
 */
@KolibriumDsl
public class PageScope<S : PlaywrightSite, P : PlaywrightPage<S>> internal constructor(
    internal val pageObject: P,
    internal val page: Page,
    internal val context: BrowserContext,
) {
    /**
     * Transitions to a new page object, runs readiness checks, and executes [action] on it.
     *
     * @param Next The target page object type.
     * @param factory Constructor reference for the next page object (e.g., `::InventoryPage`).
     * @param action The block to execute with the new page object as receiver.
     * @return A new [PageScope] bound to the target page object.
     */
    public fun <Next : PlaywrightPage<S>> on(
        factory: () -> Next,
        action: Next.() -> Unit,
    ): PageScope<S, Next> {
        val next = factory()
        ensureReady(next)
        next.action()
        return PageScope(next, page, context)
    }

    /**
     * Stays on the current page object, re-runs readiness checks, and executes [action].
     *
     * Useful for performing additional verifications or actions on the same page after
     * a previous transition without navigating away.
     *
     * @param action The block to execute with the current page object as receiver.
     * @return This [PageScope], allowing further chaining.
     */
    public fun then(action: P.() -> Unit): PageScope<S, P> =
        apply {
            ensureReady(pageObject)
            pageObject.action()
        }
}

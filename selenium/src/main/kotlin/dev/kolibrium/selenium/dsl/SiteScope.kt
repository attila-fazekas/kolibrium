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
import dev.kolibrium.selenium.core.Page
import dev.kolibrium.selenium.core.Site
import dev.kolibrium.selenium.core.SiteContextHolder
import dev.kolibrium.selenium.dsl.interactions.CookiesScope
import dev.kolibrium.selenium.dsl.interactions.cookies
import org.openqa.selenium.WebDriver

/**
 * Site-scoped DSL receiver available inside `seleniumTest { … }` blocks.
 *
 * It represents the entry surface for flows on the active [Site], exposing [on] for page navigation
 * and cookie helpers.
 */
@KolibriumDsl
public class SiteScope<S : Site> internal constructor(
    internal val driver: WebDriver,
) {
    /**
     * Inspect or manage cookies for the current session.
     *
     * Provides a [CookiesScope] to add, retrieve, or delete cookies. If [refreshPage] is `true`,
     * the browser is refreshed after the operations in [block] are completed.
     *
     * @param refreshPage whether to refresh the page after cookie operations; defaults to `false`
     * @param block configuration block for cookie operations
     */
    public fun cookies(
        refreshPage: Boolean = false,
        block: CookiesScope.() -> Unit,
    ) {
        driver.cookies(refreshPage, block)
    }

    /**
     * Navigate to a page created by [factory], wait for readiness, and execute [action].
     *
     * Navigation behavior is determined by the effective path:
     * - If [path] parameter is provided, it overrides the page's declared path (useful for deep links).
     * - If the effective path is non-empty, navigates to `baseUrl + effectivePath`.
     * - If the effective path is empty, no navigation occurs (page is already loaded or reached via interaction).
     *
     * @param P the type of the page
     * @param factory factory function to create the page instance
     * @param path optional path override; if null, uses the page's declared [Page.path]
     * @param action operation to perform on the page
     * @return a [PageScope] bound to the page for further chaining
     */
    public fun <P : Page<S>> on(
        factory: () -> P,
        path: String? = null,
        action: P.() -> Unit,
    ): PageScope<P> {
        val page = factory()
        val site =
            SiteContextHolder.get()
                ?: error("No active site context; on() must be called within seleniumTest.")
        val effectivePath = path ?: page.path

        if (effectivePath.isNotEmpty()) {
            val url = joinUrls(site.baseUrl, effectivePath)
            driver.get(url)
        }

        ensureReady(page)
        page.action()
        return PageScope(page, driver)
    }
}

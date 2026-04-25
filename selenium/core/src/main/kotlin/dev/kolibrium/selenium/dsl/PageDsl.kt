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
import org.openqa.selenium.WebDriver

/**
 * Scope used as the fluent receiver when working with a [SeleniumPage] in the Selenium DSL.
 *
 * It ties a concrete [page] instance to the active WebDriver session behind the scenes and
 * provides helpers to continue the flow, verify page state, or perform side-effecting actions.
 *
 * @param P the type of the current page
 * @property page the current page instance bound to the active WebDriver session
 * @property driver the underlying [WebDriver] session
 */
@KolibriumDsl
public class PageScope<P : SeleniumPage<*>> internal constructor(
    public val page: P,
    internal val driver: WebDriver,
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
    public fun <Next : SeleniumPage<*>> on(action: P.() -> Next): PageScope<Next> {
        page.assertReady()
        val next = page.action()
        ensureReady(next)
        return PageScope(next, driver)
    }

    /**
     * Run [assertions] against the current page, keeping the scope unchanged.
     *
     * The page is ensured to be ready before assertions are executed.
     */
    public fun verify(assertions: P.() -> Unit): PageScope<P> =
        apply {
            page.assertReady()
            page.assertions()
        }

    /**
     * Execute a side-effecting [action] on the current page, keeping the scope unchanged.
     *
     * The page is ensured to be ready before the action runs.
     */
    public fun then(action: P.() -> Unit): PageScope<P> =
        apply {
            page.assertReady()
            page.action()
        }
}

internal fun ensureReady(seleniumPage: SeleniumPage<*>) {
    seleniumPage.awaitReady()
    seleniumPage.assertReady()
}

internal fun joinUrls(
    base: String,
    path: String,
): String {
    if (path.isBlank()) return base
    if (path.startsWith("http://") || path.startsWith("https://")) return path
    val baseNormalized = if (base.endsWith('/')) base.dropLast(1) else base
    val pathNormalized = if (path.startsWith('/')) path.drop(1) else path
    return "$baseNormalized/$pathNormalized"
}

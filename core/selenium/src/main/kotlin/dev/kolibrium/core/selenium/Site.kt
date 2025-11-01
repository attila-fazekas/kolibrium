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

package dev.kolibrium.core.selenium

import dev.kolibrium.core.selenium.decorators.AbstractDecorator
import org.openqa.selenium.Cookie
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement

/**
 * Base configuration for an application under test.
 *
 * A [Site] centralizes cross-cutting defaults and policies for your tests:
 * - Where your app lives ([baseUrl])
 * - Which cookies should always be present for a new WebDriver session
 * - Default waiting behavior used by pages and element interactions
 * - An optional, session-aware configuration hook
 *
 * Declarative vs. imperative configuration
 * - Prefer declarative properties on the site ([cookies], [waitConfig]) for stable, site-wide policy that
 *   the framework can apply deterministically before the first real navigation.
 * - Use [configureBrowser] only for exceptional, dynamic, or environment-specific logic that truly
 *   requires a live session.
 *
 * Lifecycle and ordering
 * - The test DSL binds the active site to the current thread, creates a [WebDriver] session, navigates to
 *   [baseUrl] to establish origin, applies declarative [cookies] (if any), then re-navigates to [baseUrl]
 *   so cookies take effect immediately. Finally, it calls [configure] with the driver, which runs
 *   [configure] (no-arg) first and then [configureBrowser].
 * - Never navigate in [configureBrowser]; keep it fast and idempotent.
 *
 * @property baseUrl Base URL used by pages and the test DSL to build absolute routes.
 */
public abstract class Site(
    public val baseUrl: String,
) {
    /**
     * Declarative cookies applied to every new [BrowserSession] for this site.
     *
     * - Applied by the framework at the correct time (typically before the first real navigation)
     *   so the server sees them on the initial request.
     * - Prefer this over mutating cookies from [configureBrowser] whenever values are static/stable.
     *
     * Example:
     * ```kotlin
     * override val cookies = setOf(
     *     cookie(name = "ab", value = "test"),
     *     cookie(name = "locale", value = "en-US"),
     * )
     * ```
     */
    public open val cookies: Set<Cookie> = emptySet()

    /**
     * List of decorators to be applied to SearchContext objects (WebDriver or WebElement).
     * Decorators can add behavior like highlighting or slow motion to Selenium operations.
     *
     * Merging rules with test-level decorators:
     * - Site-level decorators are merged with test-level decorators registered via [dev.kolibrium.core.selenium.decorators.DecoratorManager].
     * - De-duplication is by decorator class; when both are present, the test-level instance wins.
     * - Order is deterministic: site-level first, then test-level (after de-duplication).
     * - If any decorator is [dev.kolibrium.core.selenium.decorators.InteractionAware], their WebDriver listeners are
     *   multiplexed behind a single Selenium [org.openqa.selenium.support.events.EventFiringDecorator] proxy so only one
     *   WebDriver wrapper is used per session.
     */
    public open val decorators: List<AbstractDecorator> = emptyList()

    /**
     * A predicate that determines when the found element is considered ready for use.
     */
    public open val elementReadyCondition: WebElement.() -> Boolean = { isDisplayed }

    /**
     * A predicate that determines when the found elements are considered ready for use.
     */
    public open val elementsReadyCondition: WebElements.() -> Boolean = { isDisplayed }

    /**
     * Default wait configuration used by pages and element interactions when no more specific
     * configuration is provided.
     */
    public open val waitConfig: WaitConfig = WaitConfig.Default

    /**
     * No-arg hook for per-site tweaks that do not require an active [WebDriver] session.
     *
     * Timing and order:
     * - Invoked by [configure] just before [configureBrowser].
     * - The DSL calls [configure] with the current driver after it has navigated to [baseUrl],
     *   applied declarative [cookies] (if any), and re-navigated to [baseUrl].
     *
     * Recommended uses:
     * - Read environment variables or feature flags to decide site-level policy (decorators, [waitConfig], etc.).
     * - Compute declarative [cookies] when values are static for the test run.
     *
     * Do not:
     * - Perform navigation or call session APIs here; no session is provided, and navigation is owned by the DSL.
     */
    public open fun configure() { /* no-op by default */ }

    /**
     * Imperative, session-aware hook invoked after a [WebDriver] session is created and whenever
     * the active test switches to this [Site].
     *
     * Timing and order:
     * - Called by [configure] (the overload that accepts [WebDriver]) after [configure()].
     * - The DSL performs the initial navigation to [baseUrl], applies declarative [cookies], then re-navigates
     *   to [baseUrl] before invoking this hook.
     *
     * Guidelines:
     * - Do not perform navigation here; the DSL owns predictable navigation.
     * - Keep it fast and idempotent; this may be called multiple times across a test run.
     * - Prefer the declarative [cookies] property for stable defaults; use this hook for dynamic/session-specific work.
     *
     * Example:
     * ```kotlin
     * override fun configureBrowser(driver: WebDriver) {
     *     // Example: add a run-specific token fetched at runtime
     *     val token = System.getenv("RUN_TOKEN") ?: return
     *     driver.manage().addCookie(Cookie("auth", token))
     * }
     * ```
     */
    public open fun configureBrowser(driver: WebDriver) { /* no-op by default */ }

    /**
     * Aggregates site configuration for the current [WebDriver] session.
     *
     * This convenience method is invoked by the DSL after it has:
     * - navigated to [baseUrl] to establish origin,
     * - applied declarative [cookies] (if any), and
     * - re-navigated to [baseUrl] so cookies take effect immediately.
     *
     * It then runs [configure] (no‑arg) first and [configureBrowser] second.
     *
     * Notes:
     * - This function is not intended to be overridden. Override [configure] or [configureBrowser]
     *   to customize behavior.
     * - Do not perform navigation in [configureBrowser]; the DSL owns initial, predictable navigation.
     */
    public fun configure(driver: WebDriver) {
        configure()
        // Then allow session-specific customization if desired
        configureBrowser(driver)
    }
}

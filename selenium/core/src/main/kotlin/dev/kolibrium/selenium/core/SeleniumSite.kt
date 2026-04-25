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

package dev.kolibrium.selenium.core

import dev.kolibrium.selenium.core.decorators.AbstractDecorator
import dev.kolibrium.webdriver.WaitConfig
import dev.kolibrium.webdriver.WebElements
import dev.kolibrium.webdriver.isNotEmptyAndDisplayed
import org.openqa.selenium.Cookie
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.remote.service.DriverService

/**
 * Base configuration for an application under test.
 *
 * A [SeleniumSite] centralizes cross-cutting defaults and policies for your tests:
 * - Where your app lives ([baseUrl])
 * - Which cookies should always be present for a new [WebDriver] session
 * - Default waiting behavior used by pages and element interactions
 * - An optional, session-aware configuration hook
 *
 * Declarative vs. imperative configuration
 * - Prefer declarative properties on the site ([cookies], [waitConfig]) for stable, site-wide policy that
 *   the framework can apply deterministically before the first real navigation.
 * - Use [onSessionReady] only for exceptional, dynamic, or environment-specific logic that truly
 *   requires a live session.
 *
 * Lifecycle and ordering
 * - The test DSL binds the active site to the current thread, creates a [WebDriver] session, navigates to
 *   [baseUrl] to establish origin, applies declarative [cookies] (if any), then re-navigates to [baseUrl]
 *   so cookies take effect immediately. Finally, the DSL calls [onSessionReady] with the driver.
 * - Never navigate in [onSessionReady]; keep it fast and idempotent.
 *
 * @property baseUrl Base URL used by pages and the test DSL to build absolute routes.
 *           Must be provided as a constructor argument by subclasses.
 */
public abstract class SeleniumSite(
    public val baseUrl: String,
) {
    /**
     * Optional local driver service managed by Kolibrium for this site.
     *
     * When non-null, the test harness starts the service before creating the
     * WebDriver session and stops it during teardown.
     * If null (default), tests assume the driver binary is on PATH
     * (Selenium Manager handles it) or an external service is running.
     */
    public open val service: DriverService? = null

    /**
     * Declarative cookies applied to every new [WebDriver] session for this site.
     *
     * - Applied by the framework at the correct time (after an initial origin‑establishing navigation to baseUrl,
     *   before the first real navigation in your flow) so the server sees them on the next request.
     * - Prefer this over mutating cookies from [onSessionReady] whenever values are static/stable.
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
     * List of decorators to be applied to SearchContext objects ([WebDriver] or [WebElement]).
     * Decorators can add behavior like highlighting or slow motion to Selenium operations.
     *
     * Merging rules with test-level decorators:
     * - Site-level decorators are merged with test-level decorators registered via [dev.kolibrium.selenium.core.decorators.DecoratorManager].
     * - De-duplication is by decorator class; when both are present, the test-level instance wins.
     * - Order is deterministic: site-level first, then test-level (after de-duplication).
     * - If any decorator is [dev.kolibrium.selenium.core.decorators.InteractionAware], their [WebDriver] listeners are
     *   multiplexed behind a single Selenium [org.openqa.selenium.support.events.EventFiringDecorator] proxy so only one
     *   [WebDriver] wrapper is used per session.
     */
    public open val decorators: List<AbstractDecorator> = emptyList()

    /**
     * A predicate that determines when the found element is considered ready for use.
     */
    public open val elementReadyCondition: WebElement.() -> Boolean = { isDisplayed }

    /**
     * A predicate that determines when the found elements are considered ready for use.
     */
    public open val elementsReadyCondition: WebElements.() -> Boolean = { isNotEmptyAndDisplayed }

    /**
     * Default wait configuration used by pages and element interactions when no more specific
     * configuration is provided.
     */
    public open val waitConfig: WaitConfig = WaitConfig.Default

    /**
     * Imperative, session-aware hook invoked after a [WebDriver] session is created and whenever
     * the active test switches to this [SeleniumSite].
     *
     * Timing and order:
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
     * override fun onSessionReady(driver: WebDriver) {
     *     // Example: add a run-specific token fetched at runtime
     *     val token = System.getenv("RUN_TOKEN") ?: return
     *     driver.manage().addCookie(Cookie("auth", token))
     * }
     * ```
     */
    public open fun onSessionReady(driver: WebDriver) { // no-op by default
    }
}

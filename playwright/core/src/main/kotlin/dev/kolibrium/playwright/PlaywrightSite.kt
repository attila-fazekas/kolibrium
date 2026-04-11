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
import com.microsoft.playwright.options.Cookie

/**
 * Base configuration for a Playwright-driven application under test.
 *
 * A [PlaywrightSite] centralizes cross-cutting defaults for your Playwright tests:
 * - Where your app lives ([baseUrl])
 * - Which cookies should always be present for a new session ([cookies])
 * - An optional session-aware configuration hook ([onSessionReady])
 *
 * Declarative vs. imperative configuration:
 * - Prefer [cookies] for stable, site-wide cookie policy the framework applies before the first
 *   real navigation.
 * - Use [onSessionReady] only for exceptional, dynamic, or environment-specific logic that truly
 *   requires a live session.
 *
 * @property baseUrl Base URL used by the harness to navigate before the test body runs.
 */
public abstract class PlaywrightSite(
    public val baseUrl: String,
) {
    /**
     * Whether to record a Playwright trace for tests running against this site.
     *
     * When `true`, traces are always started; on test failure the trace is saved to the directory
     * configured in [KolibriumConfig.traceDir], otherwise it is discarded.
     *
     * Can be overridden per-test via [KolibriumConfig.recordTrace].
     *
     * Example:
     * ```kotlin
     * object StagingSite : PlaywrightSite(baseUrl = "https://staging.example.com") {
     *     override val recordTrace = true
     * }
     * ```
     */
    public open val recordTrace: Boolean = false

    /**
     * Declarative cookies applied to every new browser context for this site.
     *
     * Applied by the framework to the [BrowserContext][com.microsoft.playwright.BrowserContext]
     * before the first page is created or navigated, so the server sees them on the
     * very first request to [baseUrl].
     *
     * Each [Cookie] must have its `url` or `domain`+`path` set as required by Playwright.
     *
     * Example:
     * ```kotlin
     * override val cookies: List<Cookie> = listOf(
     *     Cookie("locale", "en-US").setUrl("https://example.com"),
     *     Cookie("ab_test", "variant_b").setUrl("https://example.com"),
     * )
     * ```
     */
    public open val cookies: List<Cookie> = emptyList()

    /**
     * Imperative, session-aware hook invoked after a Playwright page is created and navigated
     * to [baseUrl].
     *
     * Guidelines:
     * - Keep it fast and idempotent.
     * - Prefer the declarative [cookies] property for stable defaults; use this hook for
     *   dynamic/session-specific work.
     *
     * @param page The Playwright [Page] for the current session.
     */
    public open fun onSessionReady(page: Page) {}
}

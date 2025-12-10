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

package dev.kolibrium.dsl

import dev.kolibrium.core.Session
import dev.kolibrium.core.SessionContext
import dev.kolibrium.core.Site
import dev.kolibrium.core.withDriver
import dev.kolibrium.dsl.DriverFactory
import dev.kolibrium.dsl.PageEntry
import org.openqa.selenium.WebDriver
import kotlin.time.Duration
import kotlin.time.TimeSource

/**
 * Unified test harness that creates a WebDriver session for the given [Site] and runs the test flow.
 *
 * Flow:
 * - Binds a per-driver Session to [site] for the duration of the test thread.
 * - Executes [prepare] in the site's context to compute the input value [T].
 * - Creates a WebDriver via [driverFactory], navigates to [Site.baseUrl] to establish origin,
 *   applies [Site.cookies] (if any), and re-navigates to [Site.baseUrl] so cookies take effect immediately.
 *   Then calls [Site.configureSite] (which runs [Site.configureSite] no-arg followed by [Site.onSessionReady]).
 * - Invokes [startup] and then [block] with a [dev.kolibrium.dsl.PageEntry] receiver in the site's context.
 *
 * Cleanup:
 * - Unless [keepBrowserOpen] is true, the session is closed in a finally block for robustness.
 *
 * Notes:
 * - Do not navigate inside [Site.onSessionReady]; this function performs the initial, predictable navigation.
 *
 * @param S The concrete site type bound to this test.
 * @param T The type of the prepared input value passed to [startup] and [block].
 * @param site The site under test; establishes the base URL and defaults.
 * @param keepBrowserOpen When true, keeps the browser open after [block] (useful for debugging).
 * @param driverFactory Factory creating a WebDriver instance (e.g., `chrome`, `firefox`, or predefined factories like `headlessChrome`).
 * @param prepare Computes the input [T] before the browser session is created; runs in the site's context.
 * @param startup Optional step executed before [block], receiving the prepared value.
 * @param block Main test body executed with a [dev.kolibrium.dsl.PageEntry] receiver and the prepared value.
 */
@KolibriumDsl
public fun <S : Site, T> webTest(
    site: S,
    keepBrowserOpen: Boolean = false,
    driverFactory: DriverFactory,
    prepare: () -> T,
    startup: SiteEntry<S>.(T) -> Unit = { _ -> },
    block: SiteEntry<S>.(T) -> Unit,
) {
    webTestImpl(site, keepBrowserOpen, driverFactory, prepare, startup, block)
}

/**
 * Convenience overload of [webTest] when no prepare data is needed.
 *
 * Uses [Unit] as the prepared value and runs the optional [startup] before [block].
 * See [webTest] for full details about navigation, cookie application, and cleanup behavior.
 *
 * @param S The concrete site type bound to this test.
 * @param site The site under test.
 * @param keepBrowserOpen When true, keeps the browser open after [block] (useful for debugging).
 * @param driverFactory Factory creating a WebDriver instance.
 * @param startup Optional step executed before [block].
 * @param block Main test body executed with a [dev.kolibrium.dsl.PageEntry] receiver.
 */
@KolibriumDsl
public fun <S : Site> webTest(
    site: S,
    keepBrowserOpen: Boolean = false,
    driverFactory: DriverFactory,
    startup: SiteEntry<S>.(Unit) -> Unit = { _ -> },
    block: SiteEntry<S>.(Unit) -> Unit,
) {
    webTest(
        site = site,
        keepBrowserOpen = keepBrowserOpen,
        driverFactory = driverFactory,
        prepare = { },
        startup = startup,
        block = block,
    )
}

@PublishedApi
internal fun <S : Site, T> webTestImpl(
    site: S,
    keepBrowserOpen: Boolean,
    driverFactory: DriverFactory,
    prepare: () -> T,
    startup: SiteEntry<S>.(T) -> Unit,
    block: SiteEntry<S>.(T) -> Unit,
) {
    val prepared: T = context(site) { prepare() }
    val driver: WebDriver = driverFactory()
    try {
        // Establish origin, apply declarative cookies, and re-establish origin
        driver.get(site.baseUrl)
        if (site.cookies.isNotEmpty()) {
            val options = driver.manage()
            site.cookies.forEach(options::addCookie)
            driver.get(site.baseUrl)
        }

        // Bind per-driver Session and driver context while running startup/block
        val session = Session(driver = driver, site = site)
        SessionContext.withSession(session) {
            // Allow site to finalize session-specific bits; keep navigation out of this hook
            site.configureSite()
            site.onSessionReady(driver)

            val entry: SiteEntry<S> = PageEntry(driver)
            withDriver(driver) {
                context(site) { entry.startup(prepared) }
                context(site) { entry.block(prepared) }
            }
        }
    } finally {
        if (!keepBrowserOpen) runCatching { driver.quit() }
    }
}

// ----- Result & metrics -----

/**
 * Outcome of a web test run.
 *
 * @property testDuration Total duration of the test run, measured from the start of execution
 * to the end of the harness (after cleanup is triggered).
 * @property status Overall result of the run: [TestStatus.Success] when no exception escaped the test body,
 * [TestStatus.Failure] otherwise.
 * @property error The thrown exception when [status] is [TestStatus.Failure]; null on success.
 */
public class WebTestResult(
    public val testDuration: Duration,
    public val status: TestStatus,
    public val error: Throwable? = null,
) {
    /** High-level outcome of a test run. */
    public enum class TestStatus {
        /** The test completed without an uncaught exception escaping the test body. */
        Success,

        /** An uncaught exception escaped the test body; the run is considered failed. */
        Failure,
    }
}

/**
 * Result-returning test harness that creates a WebDriver session for the given [Site]
 * and runs the test flow.
 *
 * Always returns a [WebTestResult] capturing the total duration and outcome:
 * - On success: `status = Success`, `error = null`.
 * - On failure (any exception thrown from [prepare], [startup] or [block]): `status = Failure`, `error = <cause>`.
 *
 * Flow:
 * - Binds a per-driver Session to [site] for the duration of the test thread.
 * - Executes [prepare] in the site's context to compute the input value [T].
 * - Creates a WebDriver via [driverFactory], navigates to [Site.baseUrl] to establish origin,
 *   applies [Site.cookies] (if any), and re-navigates to [Site.baseUrl] so cookies take effect immediately.
 *   Then calls [Site.configureSite] (which runs [Site.configureSite] no-arg followed by [Site.onSessionReady]).
 * - Invokes [startup] and then [block] with a [PageEntry] receiver in the site's context.
 *
 * Cleanup:
 * - Unless [keepBrowserOpen] is true, the session is closed in a finally block for robustness.
 *
 * Notes:
 * - Do not navigate inside [Site.onSessionReady]; this function performs the initial, predictable navigation.
 *
 * Usage examples:
 *
 * Basic result handling and logging:
 * ```kotlin
 * val result = webTestResult(
 *     site = MySite,
 *     driverFactory = ::chrome,
 *     prepare = { buildTestData() },
 * ) { data ->
 *     // test body
 *     login(data.user)
 *     assertHomeIsVisible()
 * }.alsoLogDuration() // prints: "This test took: <duration>"
 *
 * if (result.status == WebTestResult.TestStatus.Failure) {
 *     // decide how to handle the failure in your environment
 *     println("Test failed: ${result.error}")
 * }
 * ```
 *
 * Make JUnit/TestNG fail by rethrowing explicitly (use this API when you still want a result object):
 * ```kotlin
 * val result = webTestResult(
 *     site = MySite,
 *     driverFactory = ::chrome,
 *     prepare = { },
 * ) { _ ->
 *     // test body
 * }
 *
 * if (result.status == WebTestResult.TestStatus.Failure) {
 *     throw checkNotNull(result.error)
 * }
 * ```
 *
 * Using the convenience overload (no prepare value):
 * ```kotlin
 * val result = webTestResult(
 *     site = MySite,
 *     driverFactory = ::headlessChrome,
 * ) {
 *     // test body
 * }
 * ```
 *
 * Custom duration formatting and logger:
 * ```kotlin
 * webTestResult(site = MySite, driverFactory = ::chrome, prepare = { Unit }) { }
 *     .alsoLogDuration(
 *         formatter = { d -> "UI test took $d" },
 *         logger = logger::info,
 *     )
 * ```
 *
 * If you prefer a throwing harness that lets exceptions escape to the test runner, use [webTest].
 *
 * @param S The concrete site type bound to this test.
 * @param T The type of the prepared input value passed to [startup] and [block].
 * @param site The site under test; establishes the base URL and defaults.
 * @param keepBrowserOpen When true, keeps the browser open after [block] (useful for debugging).
 * @param driverFactory Factory creating a WebDriver instance (e.g., `chrome`, `firefox`, or predefined factories like `headlessChrome`).
 * @param prepare Computes the input [T] before the browser session is created; runs in the site's context.
 * @param startup Optional step executed before [block], receiving the prepared value.
 * @param block Main test body executed with a [PageEntry] receiver and the prepared value.
 * @return A [WebTestResult] indicating success or failure. Never throws.
 */
@KolibriumDsl
public fun <S : Site, T> webTestResult(
    site: S,
    keepBrowserOpen: Boolean = false,
    driverFactory: DriverFactory,
    prepare: () -> T,
    startup: SiteEntry<S>.(T) -> Unit = { _ -> },
    block: SiteEntry<S>.(T) -> Unit,
): WebTestResult {
    val mark = TimeSource.Monotonic.markNow()
    return try {
        webTestResultImpl(
            site = site,
            keepBrowserOpen = keepBrowserOpen,
            driverFactory = driverFactory,
            prepare = prepare,
            startup = startup,
            block = block,
        )
        WebTestResult(
            testDuration = mark.elapsedNow(),
            status = WebTestResult.TestStatus.Success,
            error = null,
        )
    } catch (t: Throwable) {
        WebTestResult(
            testDuration = mark.elapsedNow(),
            status = WebTestResult.TestStatus.Failure,
            error = t,
        )
    }
}

/**
 * Convenience overload of [webTestResult] when no prepare data is needed.
 *
 * Uses [Unit] as the prepared value and runs the optional [startup] before [block].
 * See [webTestResult] for full details about navigation, cookie application, and cleanup behavior.
 *
 * @param S The concrete site type bound to this test.
 * @param site The site under test.
 * @param keepBrowserOpen When true, keeps the browser open after [block] (useful for debugging).
 * @param driverFactory Factory creating a WebDriver instance.
 * @param startup Optional step executed before [block].
 * @param block Main test body executed with a [PageEntry] receiver.
 */
@KolibriumDsl
public fun <S : Site> webTestResult(
    site: S,
    keepBrowserOpen: Boolean = false,
    driverFactory: DriverFactory,
    startup: SiteEntry<S>.(Unit) -> Unit = { _ -> },
    block: SiteEntry<S>.(Unit) -> Unit,
): WebTestResult =
    webTestResult(
        site = site,
        keepBrowserOpen = keepBrowserOpen,
        driverFactory = driverFactory,
        prepare = { },
        startup = startup,
        block = block,
    )

@PublishedApi
internal fun <S : Site, T> webTestResultImpl(
    site: S,
    keepBrowserOpen: Boolean,
    driverFactory: DriverFactory,
    prepare: () -> T,
    startup: SiteEntry<S>.(T) -> Unit,
    block: SiteEntry<S>.(T) -> Unit,
) {
    val prepared: T = context(site) { prepare() }
    val driver: WebDriver = driverFactory()
    try {
        driver.get(site.baseUrl)
        if (site.cookies.isNotEmpty()) {
            val options = driver.manage()
            site.cookies.forEach(options::addCookie)
            driver.get(site.baseUrl)
        }
        val session = Session(driver = driver, site = site)
        SessionContext.withSession(session) {
            site.configureSite()
            site.onSessionReady(driver)
            val entry: SiteEntry<S> = PageEntry<S>(driver)
            withDriver(driver) {
                context(site) { entry.startup(prepared) }
                context(site) { entry.block(prepared) }
            }
        }
    } finally {
        if (!keepBrowserOpen) runCatching { driver.quit() }
    }
}

/**
 * Convenience helper that logs the test duration using the provided [logger] and returns this [WebTestResult].
 *
 * @param logger Function used to output the formatted message. Defaults to [println].
 * @return The same [WebTestResult] instance for fluent chaining.
 */
public fun WebTestResult.alsoLogDuration(logger: (String) -> Unit = ::println): WebTestResult {
    logger("This test took: $testDuration")
    return this
}

/**
 * Logs the test duration using a custom [formatter] and [logger], then returns this [WebTestResult].
 *
 * @param formatter Function that converts the [Duration] into a message string.
 * @param logger Function used to output the formatted message. Defaults to [println].
 * @return The same [WebTestResult] instance for fluent chaining.
 */
public fun WebTestResult.alsoLogDuration(
    formatter: (Duration) -> String,
    logger: (String) -> Unit = ::println,
): WebTestResult {
    logger(formatter(testDuration))
    return this
}

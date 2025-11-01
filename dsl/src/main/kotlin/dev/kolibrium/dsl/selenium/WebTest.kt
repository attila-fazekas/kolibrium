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

package dev.kolibrium.dsl.selenium

import dev.kolibrium.core.selenium.Site
import dev.kolibrium.core.selenium.SiteContext
import org.openqa.selenium.WebDriver
import kotlin.time.Duration
import kotlin.time.TimeSource

/**
 * Unified test harness that creates a WebDriver session for the given [dev.kolibrium.core.selenium.Site] and runs the test flow.
 *
 * Flow:
 * - Binds [dev.kolibrium.core.selenium.SiteContext] to [site] for the duration of the test.
 * - Executes [prepare] in the site's context to compute the input value [T].
 * - Creates a WebDriver via [driverFactory], navigates to [dev.kolibrium.core.selenium.Site.baseUrl] to establish origin,
 *   applies [dev.kolibrium.core.selenium.Site.cookies] (if any), and re-navigates to [dev.kolibrium.core.selenium.Site.baseUrl] so cookies take effect immediately.
 *   Then calls [dev.kolibrium.core.selenium.Site.configure] (which runs [dev.kolibrium.core.selenium.Site.configure] no-arg followed by [dev.kolibrium.core.selenium.Site.configureBrowser]).
 * - Invokes [startup] and then [block] with a [PageEntry] receiver in the site's context.
 *
 * Cleanup:
 * - Unless [keepBrowserOpen] is true, the session is closed in a finally block for robustness.
 *
 * Notes:
 * - Do not navigate inside [dev.kolibrium.core.selenium.Site.configureBrowser]; this function performs the initial, predictable navigation.
 * - [driverFactory] is marked `noinline` to avoid non-local return constraints; it's a zero-arg factory.
 *
 * @param S The concrete site type bound to this test.
 * @param T The type of the prepared input value passed to [startup] and [block].
 * @param site The site under test; establishes the base URL and defaults.
 * @param keepBrowserOpen When true, keeps the browser open after [block] (useful for debugging).
 * @param driverFactory Factory creating a WebDriver instance (e.g., `chrome`, `firefox`, or predefined factories like `headlessChrome`).
 * @param prepare Computes the input [T] before the browser session is created; runs in the site's context.
 * @param startup Optional step executed before [block], receiving the prepared value.
 * @param block Main test body executed with a [PageEntry] receiver and the prepared value.
 */
public inline fun <S : Site, T> webTest(
    site: S,
    keepBrowserOpen: Boolean = false,
    noinline driverFactory: DriverFactory,
    crossinline prepare: () -> T,
    crossinline startup: PageEntry<S>.(T) -> Unit = { _ -> },
    crossinline block: PageEntry<S>.(T) -> Unit,
) {
    SiteContext.withSite(site) {
        val prepared: T = context(site) { prepare() }
        val driver: WebDriver = driverFactory()
        try {
            driver.get(site.baseUrl)
            if (site.cookies.isNotEmpty()) {
                val options = driver.manage()
                site.cookies.forEach(options::addCookie)
                driver.get(site.baseUrl)
            }
            site.configure(driver)
            val pageEntry = PageEntry<S>(driver)
            context(site) { pageEntry.startup(prepared) }
            context(site) { pageEntry.block(prepared) }
        } finally {
            if (!keepBrowserOpen) runCatching { driver.quit() }
        }
    }
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
 * @param block Main test body executed with a [PageEntry] receiver.
 */
public inline fun <S : Site> webTest(
    site: S,
    keepBrowserOpen: Boolean = false,
    noinline driverFactory: DriverFactory,
    crossinline startup: PageEntry<S>.(Unit) -> Unit = { _ -> },
    crossinline block: PageEntry<S>.(Unit) -> Unit,
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
 * Result-returning test harness that creates a WebDriver session for the given [dev.kolibrium.core.selenium.Site] and runs the test flow.
 *
 * Returns a [WebTestResult] containing the measured duration and status for successful runs. If an exception
 * is thrown during [startup] or [block], the exception is rethrown after the status is set to
 * [WebTestResult.TestStatus.Failure] and cleanup is attempted; in such cases no [WebTestResult] is returned.
 *
 * Flow:
 * - Binds [dev.kolibrium.core.selenium.SiteContext] to [site] for the duration of the test.
 * - Executes [prepare] in the site's context to compute the input value [T].
 * - Creates a WebDriver via [driverFactory], navigates to [dev.kolibrium.core.selenium.Site.baseUrl] to establish origin,
 *   applies [dev.kolibrium.core.selenium.Site.cookies] (if any), and re-navigates to [dev.kolibrium.core.selenium.Site.baseUrl] so cookies take effect immediately.
 *   Then calls [dev.kolibrium.core.selenium.Site.configure] (which runs [dev.kolibrium.core.selenium.Site.configure] no-arg followed by [dev.kolibrium.core.selenium.Site.configureBrowser]).
 * - Invokes [startup] and then [block] with a [PageEntry] receiver in the site's context.
 *
 * Cleanup:
 * - Unless [keepBrowserOpen] is true, the session is closed in a finally block for robustness.
 *
 * Notes:
 * - Do not navigate inside [dev.kolibrium.core.selenium.Site.configureBrowser]; this function performs the initial, predictable navigation.
 * - [driverFactory] is marked `noinline` to avoid non-local return constraints; it's a zero-arg factory.
 * - If [prepare] throws, it happens before the browser session is created and before failure status handling;
 *   the exception is propagated and no [WebTestResult] is produced.
 *
 * @param S The concrete site type bound to this test.
 * @param T The type of the prepared input value passed to [startup] and [block].
 * @param site The site under test; establishes the base URL and defaults.
 * @param keepBrowserOpen When true, keeps the browser open after [block] (useful for debugging).
 * @param driverFactory Factory creating a WebDriver instance (e.g., `chrome`, `firefox`, or predefined factories like `headlessChrome`).
 * @param prepare Computes the input [T] before the browser session is created; runs in the site's context.
 * @param startup Optional step executed before [block], receiving the prepared value.
 * @param block Main test body executed with a [PageEntry] receiver and the prepared value.
 * @return A [WebTestResult] for successful runs; on failure the exception is rethrown.
 */
public inline fun <S : Site, T> webTestResult(
    site: S,
    keepBrowserOpen: Boolean = false,
    noinline driverFactory: DriverFactory,
    crossinline prepare: () -> T,
    crossinline startup: PageEntry<S>.(T) -> Unit = { _ -> },
    crossinline block: PageEntry<S>.(T) -> Unit,
): WebTestResult {
    val mark = TimeSource.Monotonic.markNow()
    var status = WebTestResult.TestStatus.Success
    var error: Throwable? = null

    SiteContext.withSite(site) {
        val prepared: T = context(site) { prepare() }
        val driver: WebDriver = driverFactory()
        try {
            driver.get(site.baseUrl)
            if (site.cookies.isNotEmpty()) {
                val options = driver.manage()
                site.cookies.forEach(options::addCookie)
                driver.get(site.baseUrl)
            }
            site.configure(driver)
            val pageEntry = PageEntry<S>(driver)
            context(site) { pageEntry.startup(prepared) }
            context(site) { pageEntry.block(prepared) }
        } catch (t: Throwable) {
            status = WebTestResult.TestStatus.Failure
            error = t
            throw t
        } finally {
            if (!keepBrowserOpen) runCatching { driver.quit() }
        }
    }
    return WebTestResult(testDuration = mark.elapsedNow(), status = status, error = error)
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
public inline fun <S : Site> webTestResult(
    site: S,
    keepBrowserOpen: Boolean = false,
    noinline driverFactory: DriverFactory,
    crossinline startup: PageEntry<S>.(Unit) -> Unit = { _ -> },
    crossinline block: PageEntry<S>.(Unit) -> Unit,
): WebTestResult =
    webTestResult(
        site = site,
        keepBrowserOpen = keepBrowserOpen,
        driverFactory = driverFactory,
        prepare = { },
        startup = startup,
        block = block,
    )

/**
 * Convenience helper that logs the test duration using the provided [logger] and returns this [WebTestResult].
 *
 * @param logger Function used to output the formatted message. Defaults to [println].
 * @return The same [WebTestResult] instance for fluent chaining.
 */
public inline fun WebTestResult.alsoLogDuration(logger: (String) -> Unit = ::println): WebTestResult {
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
public inline fun WebTestResult.alsoLogDuration(
    formatter: (Duration) -> String,
    logger: (String) -> Unit = ::println,
): WebTestResult {
    logger(formatter(testDuration))
    return this
}

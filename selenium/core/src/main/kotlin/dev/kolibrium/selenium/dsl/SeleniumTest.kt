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

import dev.kolibrium.selenium.core.SeleniumSite
import dev.kolibrium.selenium.core.Session
import dev.kolibrium.selenium.core.SessionContext
import kotlinx.coroutines.runBlocking
import org.openqa.selenium.WebDriver

/**
 * Unified test harness that creates a WebDriver session for the given [SeleniumSite] and runs the test flow.
 *
 * This function provides a structured three-phase lifecycle for Selenium tests:
 * 1. **setUp**: Executes the [setUp] block **before** any WebDriver session exists.
 * 2. **block**: Creates a WebDriver session, navigates to the [site]'s base URL, and executes the main test body.
 * 3. **tearDown**: Executes the [tearDown] block after the test body, even if the test fails.
 *
 * Flow:
 * - Executes [setUp] before creating the WebDriver session to compute the input value [T].
 * - Binds a per-driver Session to [site] for the duration of the test thread.
 * - Creates a WebDriver via [driverFactory], navigates to [SeleniumSite.baseUrl] to establish origin,
 *   applies [SeleniumSite.cookies] (if any), and re-navigates to [SeleniumSite.baseUrl] so cookies take effect immediately.
 *   Then calls [SeleniumSite.onSessionReady] sequentially.
 * - Invokes [block] with a [SiteScope] receiver in the site's context.
 *
 * All three user-facing lambdas ([setUp], [tearDown], [block]) are `suspend`, so callers can invoke
 * suspend functions (e.g. Ktor-based API clients) directly without wrapping them in `runBlocking`.
 * The harness bridges the coroutine boundary internally via `runBlocking`.
 *
 * Cleanup:
 * - Unless [keepBrowserOpen] is true, the session is closed in a finally block for robustness.
 *
 * Notes:
 * - Do not navigate inside [SeleniumSite.onSessionReady]; this function performs the initial, predictable navigation.
 *
 * @param S The concrete site type bound to this test.
 * @param T The type of the prepared input value passed to [block].
 * @param site The site under test; establishes the base URL and defaults.
 * @param driverFactory Factory creating a WebDriver instance.
 * @param keepBrowserOpen When true, keeps the browser open after [block] (useful for debugging).
 * @param setUp Suspending block that computes the input [T] before the browser session is created.
 * @param tearDown Suspending block that cleans up the test context after the test body; runs even if the test fails.
 * @param block Suspending main test body executed with a [SiteScope] receiver and the prepared value.
 */
public fun <S : SeleniumSite, T> seleniumTest(
    site: S,
    driverFactory: DriverFactory,
    keepBrowserOpen: Boolean = false,
    setUp: suspend () -> T,
    tearDown: suspend (T) -> Unit = {},
    block: suspend SiteScope<S>.(T) -> Unit,
) {
    seleniumTestImpl(site, driverFactory, keepBrowserOpen, setUp, tearDown, block)
}

/**
 * Convenience overload of [seleniumTest] when no prepared data is needed.
 *
 * Uses [Unit] as the prepared value and runs [block].
 * See [seleniumTest] for full details about navigation, cookie application, and cleanup behavior.
 *
 * @param S The concrete site type bound to this test.
 * @param site The site under test.
 * @param driverFactory Factory creating a WebDriver instance.
 * @param keepBrowserOpen When true, keeps the browser open after [block] (useful for debugging).
 * @param block Suspending main test body executed with a [SiteScope] receiver.
 */
public fun <S : SeleniumSite> seleniumTest(
    site: S,
    driverFactory: DriverFactory,
    keepBrowserOpen: Boolean = false,
    block: suspend SiteScope<S>.(Unit) -> Unit,
) {
    seleniumTest(
        site = site,
        keepBrowserOpen = keepBrowserOpen,
        driverFactory = driverFactory,
        setUp = { },
        block = block,
    )
}

internal fun <S : SeleniumSite, T> seleniumTestImpl(
    site: S,
    driverFactory: DriverFactory,
    keepBrowserOpen: Boolean,
    setUp: suspend () -> T,
    tearDown: suspend (T) -> Unit = {},
    block: suspend SiteScope<S>.(T) -> Unit,
) {
    val service = site.service
    val shutdownHook =
        service?.let { svc ->
            Thread { runCatching { svc.stop() } }.also { hook ->
                Runtime.getRuntime().addShutdownHook(hook)
            }
        }
    service?.start()
    try {
        val prepared: T = runBlocking { setUp() }
        var testError: Throwable? = null
        var driver: WebDriver? = null

        try {
            driver = driverFactory()
            driver.get(site.baseUrl)
            if (site.cookies.isNotEmpty()) {
                val options = driver.manage()
                site.cookies.forEach(options::addCookie)
                driver.get(site.baseUrl)
            }

            val session = Session(driver = driver, seleniumSite = site)
            SessionContext.withSession(session) {
                site.onSessionReady(driver)

                val scope = SiteScope<S>(driver)
                context(site) {
                    runBlocking { scope.block(prepared) }
                }
            }
        } catch (e: Throwable) {
            testError = e
            throw e
        } finally {
            try {
                runBlocking { tearDown(prepared) }
            } catch (teardownError: Throwable) {
                if (testError != null) {
                    testError.addSuppressed(teardownError)
                } else {
                    throw teardownError
                }
            } finally {
                if (!keepBrowserOpen) {
                    runCatching { driver?.quit() }
                }
            }
        }
    } finally {
        service?.stop()
        shutdownHook?.let { thread ->
            runCatching { Runtime.getRuntime().removeShutdownHook(thread) }
        }
    }
}

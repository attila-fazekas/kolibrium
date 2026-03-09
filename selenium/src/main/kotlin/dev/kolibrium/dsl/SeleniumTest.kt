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

package dev.kolibrium.dsl

import dev.kolibrium.core.Session
import dev.kolibrium.core.SessionContext
import dev.kolibrium.core.Site
import dev.kolibrium.core.withDriver
import org.openqa.selenium.WebDriver
import kotlin.time.Duration
import kotlin.time.TimeSource

/**
 * Unified test harness that creates a WebDriver session for the given [Site] and runs the test flow.
 *
 * This function provides a structured three-phase lifecycle for Selenium tests:
 * 1. **setUp**: Executes the [setUp] block **before** any WebDriver session exists.
 * 2. **block**: Creates a WebDriver session, navigates to the [site]'s base URL, and executes the main test body.
 * 3. **tearDown**: Executes the [tearDown] block after the test body, even if the test fails.
 *
 * Flow:
 * - Executes [setUp] before creating the WebDriver session to compute the input value [T].
 * - Binds a per-driver Session to [site] for the duration of the test thread.
 * - Creates a WebDriver via [driverFactory], navigates to [Site.baseUrl] to establish origin,
 *   applies [Site.cookies] (if any), and re-navigates to [Site.baseUrl] so cookies take effect immediately.
 *   Then calls [Site.configureSite] (which runs [Site.configureSite] no-arg followed by [Site.onSessionReady]).
 * - Invokes [block] with a [dev.kolibrium.dsl.PageEntry] receiver in the site's context.
 *
 * Cleanup:
 * - Unless [keepBrowserOpen] is true, the session is closed in a finally block for robustness.
 *
 * Notes:
 * - Do not navigate inside [Site.onSessionReady]; this function performs the initial, predictable navigation.
 *
 * @param S The concrete site type bound to this test.
 * @param T The type of the prepared input value passed to [block].
 * @param site The site under test; establishes the base URL and defaults.
 * @param driverFactory Factory creating a WebDriver instance (e.g., `chrome`, `firefox`, or predefined factories like `headlessChrome`).
 * @param keepBrowserOpen When true, keeps the browser open after [block] (useful for debugging).
 * @param setUp Computes the input [T] before the browser session is created. It runs without
 * an active WebDriver or [SessionContext].
 * @param tearDown Cleans up the test context after the test body; runs even if the test fails.
 * @param block Main test body executed with a [dev.kolibrium.dsl.PageEntry] receiver and the prepared value.
 */
@KolibriumDsl
public fun <S : Site, T> seleniumTest(
    site: S,
    driverFactory: DriverFactory,
    keepBrowserOpen: Boolean = false,
    setUp: () -> T,
    tearDown: (T) -> Unit = {},
    block: SiteEntry<S>.(T) -> Unit,
) {
    seleniumTestImpl(site, driverFactory, keepBrowserOpen, setUp, tearDown, block)
}

/**
 * Convenience overload of [seleniumTest] when no prepare data is needed.
 *
 * Uses [Unit] as the prepared value and runs [block].
 * See [seleniumTest] for full details about navigation, cookie application, and cleanup behavior.
 *
 * @param S The concrete site type bound to this test.
 * @param site The site under test.
 * @param driverFactory Factory creating a WebDriver instance.
 * @param keepBrowserOpen When true, keeps the browser open after [block] (useful for debugging).
 * @param block Main test body executed with a [dev.kolibrium.dsl.PageEntry] receiver.
 */
@KolibriumDsl
public fun <S : Site> seleniumTest(
    site: S,
    driverFactory: DriverFactory,
    keepBrowserOpen: Boolean = false,
    block: SiteEntry<S>.(Unit) -> Unit,
) {
    seleniumTest(
        site = site,
        keepBrowserOpen = keepBrowserOpen,
        driverFactory = driverFactory,
        setUp = { },
        block = block,
    )
}

internal fun <S : Site, T> seleniumTestImpl(
    site: S,
    driverFactory: DriverFactory,
    keepBrowserOpen: Boolean,
    setUp: () -> T,
    tearDown: (T) -> Unit = {},
    block: SiteEntry<S>.(T) -> Unit,
) {
    val prepared: T = setUp()
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

        val session = Session(driver = driver, site = site)
        SessionContext.withSession(session) {
            site.configureSite()
            site.onSessionReady(driver)

            val entry: SiteEntry<S> = PageEntry(driver)
            withDriver(driver) {
                context(site) { entry.block(prepared) }
            }
        }
    } catch (e: Throwable) {
        testError = e
        throw e
    } finally {
        try {
            tearDown(prepared)
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
}

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

import dev.kolibrium.selenium.DriverFactory
import dev.kolibrium.selenium.Site
import dev.kolibrium.selenium.SiteContextHolder
import dev.kolibrium.selenium.WebDriverContextHolder
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver

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
 * - Binds the driver and site to context holders for the duration of the test thread.
 * - Creates a WebDriver via [driverFactory], navigates to [Site.baseUrl] to establish origin,
 *   applies [Site.cookies] (if any), and re-navigates to [Site.baseUrl] so cookies take effect immediately.
 *   Then calls [Site.onSessionReady].
 * - Invokes [block] with a [SiteScope] receiver in the site's context.
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
 * @param driverFactory Factory creating a WebDriver instance. Defaults to ChromeDriver.
 * @param keepBrowserOpen When true, keeps the browser open after [block] (useful for debugging).
 * @param setUp Block that computes the input [T] before the browser session is created.
 * @param tearDown Block that cleans up the test context after the test body; runs even if the test fails.
 * @param block Main test body executed with a [SiteScope] receiver and the prepared value.
 */
public fun <S : Site, T> seleniumTest(
    site: S,
    driverFactory: DriverFactory = ::ChromeDriver,
    keepBrowserOpen: Boolean = false,
    setUp: () -> T,
    tearDown: (T) -> Unit = {},
    block: SiteScope<S>.(T) -> Unit,
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
 * @param driverFactory Factory creating a WebDriver instance. Defaults to ChromeDriver.
 * @param keepBrowserOpen When true, keeps the browser open after [block] (useful for debugging).
 * @param block Main test body executed with a [SiteScope] receiver.
 */
public fun <S : Site> seleniumTest(
    site: S,
    driverFactory: DriverFactory = ::ChromeDriver,
    keepBrowserOpen: Boolean = false,
    block: SiteScope<S>.(Unit) -> Unit,
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
    block: SiteScope<S>.(T) -> Unit,
) {
    withService(site.service) {
        val prepared: T = setUp()
        runTestWithDriver(site, driverFactory, keepBrowserOpen, prepared, tearDown, block)
    }
}

private fun <S : Site, T> runTestWithDriver(
    site: S,
    driverFactory: DriverFactory,
    keepBrowserOpen: Boolean,
    prepared: T,
    tearDown: (T) -> Unit,
    block: SiteScope<S>.(T) -> Unit,
) {
    var testError: Throwable? = null
    var driver: WebDriver? = null

    try {
        driver = createAndInitializeDriver(driverFactory, site)
        executeTestBlock(site, driver, prepared, block)
    } catch (e: Throwable) {
        testError = e
        throw e
    } finally {
        cleanupAfterTest(driver, keepBrowserOpen, prepared, tearDown, testError)
    }
}

private fun <S : Site> createAndInitializeDriver(
    driverFactory: DriverFactory,
    site: S,
): WebDriver {
    val driver = driverFactory()
    driver.get(site.baseUrl)
    if (site.cookies.isNotEmpty()) {
        val options = driver.manage()
        site.cookies.forEach(options::addCookie)
        driver.get(site.baseUrl)
    }
    return driver
}

private fun <S : Site, T> executeTestBlock(
    site: S,
    driver: WebDriver,
    prepared: T,
    block: SiteScope<S>.(T) -> Unit,
) {
    WebDriverContextHolder.set(driver)
    SiteContextHolder.set(site)
    try {
        site.onSessionReady(driver)
        val scope = SiteScope<S>(driver)
        context(site) {
            scope.block(prepared)
        }
    } finally {
        SiteContextHolder.clear()
        WebDriverContextHolder.clear()
    }
}

private fun <T> cleanupAfterTest(
    driver: WebDriver?,
    keepBrowserOpen: Boolean,
    prepared: T,
    tearDown: (T) -> Unit,
    testError: Throwable?,
) {
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

private inline fun withService(
    service: org.openqa.selenium.remote.service.DriverService?,
    block: () -> Unit,
) {
    val shutdownHook =
        service?.let { svc ->
            Thread { runCatching { svc.stop() } }.also { hook ->
                Runtime.getRuntime().addShutdownHook(hook)
            }
        }
    service?.start()
    try {
        block()
    } finally {
        service?.stop()
        shutdownHook?.let { thread ->
            runCatching { Runtime.getRuntime().removeShutdownHook(thread) }
        }
    }
}

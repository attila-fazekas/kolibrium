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

package dev.kolibrium.appium

import io.appium.java_client.AppiumDriver
import io.appium.java_client.android.AndroidDriver
import io.appium.java_client.ios.IOSDriver
import kotlinx.coroutines.runBlocking

/**
 * Unified deep link–aware test harness.
 *
 * After the driver session is created, navigates to the given [deepLink] before
 * executing the test [block]. The deep link command is dispatched based on the
 * concrete [App] subtype:
 * - [AndroidApp] → `mobile: deepLink` with `package`
 * - [IosApp] → `mobile: deepLink` with `bundleId`
 * - [CrossPlatformApp] → resolved at runtime from the driver type
 *
 * The [block] lambda is `suspend`, so callers can invoke suspend functions
 * (e.g. Ktor-based API clients) directly without wrapping them in `runBlocking`.
 * The harness bridges the coroutine boundary internally.
 *
 * @param A The concrete app type bound to this test.
 * @param app The app under test.
 * @param driverFactory Factory creating an AppiumDriver instance.
 * @param deepLink The deep link URL to navigate to after session creation.
 * @param block Suspending main test body executed with an [AppScope] receiver.
 */
public fun <A : App> appiumTest(
    app: A,
    driverFactory: AppiumDriverFactory,
    deepLink: String,
    block: suspend AppScope<A>.(Unit) -> Unit,
) {
    appiumTest(
        app = app,
        driverFactory = driverFactory,
        setUp = { },
        block = {
            AppiumDriverContextHolder.get()?.let { driver ->
                driver.executeScript("mobile: deepLink", app.deepLinkParams(driver, deepLink))
            }
            block(Unit)
        },
    )
}

/**
 * Cross‑platform convenience harness.
 *
 * Requires an explicit [driverFactory] since there is no single sensible default. Use the
 * appropriate factory from [CrossPlatformApp.androidDriverFactory] or [CrossPlatformApp.iosDriverFactory].
 *
 * The [block] lambda is `suspend`, so callers can invoke suspend functions directly.
 * The harness bridges the coroutine boundary internally via `runBlocking`.
 *
 * @param A The concrete cross‑platform app type bound to this test.
 * @param app The app under test.
 * @param driverFactory The explicit driver factory to use for this run.
 * @param block Suspending test body executed with an [AppScope] receiver.
 */
public fun <A : CrossPlatformApp> appiumTest(
    app: A,
    driverFactory: AppiumDriverFactory,
    block: suspend AppScope<A>.(Unit) -> Unit,
) {
    appiumTest(
        app = app,
        driverFactory = driverFactory,
        setUp = { },
        block = block,
    )
}

/**
 * Unified test harness that creates an AppiumDriver session for the given [App] and runs the test flow.
 *
 * Three-phase lifecycle:
 * 1. **setUp**: Executes before any AppiumDriver session exists.
 * 2. **block**: Creates an AppiumDriver session and executes the main test body.
 * 3. **tearDown**: Executes after the test body, even if the test fails.
 *
 * All three user-facing lambdas ([setUp], [tearDown], [block]) are `suspend`, so callers can invoke
 * suspend functions (e.g. Ktor-based API clients) directly without wrapping them in `runBlocking`.
 * The harness bridges the coroutine boundary internally via `runBlocking`.
 *
 * @param A The concrete app type bound to this test.
 * @param T The type of the prepared input value passed to [block].
 * @param app The app under test.
 * @param driverFactory Factory creating an AppiumDriver instance.
 * @param setUp Suspending block that computes the input [T] before the driver session is created.
 * @param tearDown Suspending block that cleans up after the test body; runs even if the test fails.
 * @param block Suspending main test body executed with an [AppScope] receiver and the prepared value.
 */
public fun <A : App, T> appiumTest(
    app: A,
    driverFactory: AppiumDriverFactory,
    setUp: suspend () -> T,
    tearDown: suspend (T) -> Unit = {},
    block: suspend AppScope<A>.(T) -> Unit,
) {
    appiumTestImpl(app, driverFactory, setUp, tearDown, block)
}

private fun App.deepLinkParams(
    driver: AppiumDriver,
    deepLink: String,
): Map<String, String?> =
    when (this) {
        is AndroidApp -> {
            androidDeepLinkParams(deepLink)
        }

        is IosApp -> {
            iosDeepLinkParams(deepLink)
        }

        is CrossPlatformApp -> {
            when (driver) {
                is AndroidDriver -> androidDeepLinkParams(deepLink)
                is IOSDriver -> iosDeepLinkParams(deepLink)
                else -> error("Unsupported driver type: ${driver::class.simpleName}")
            }
        }
    }

private fun App.androidDeepLinkParams(deepLink: String): Map<String, String?> {
    val appPackage =
        when (this) {
            is AndroidApp -> appPackage
            is CrossPlatformApp -> appPackage
            else -> null
        }
    check(appPackage != null) {
        "Deep links require 'appPackage' to be set on the AndroidApp definition."
    }
    return mapOf("url" to deepLink, "package" to appPackage)
}

private fun App.iosDeepLinkParams(deepLink: String): Map<String, String?> {
    val bundleId =
        when (this) {
            is IosApp -> bundleId
            is CrossPlatformApp -> bundleId
            else -> null
        }
    check(bundleId != null) {
        "Deep links require 'bundleId' to be set on the IosApp definition."
    }
    return mapOf("url" to deepLink, "bundleId" to bundleId)
}

/**
 * Android‑focused convenience harness.
 *
 * Creates an Appium session using the app's default [AndroidDriverFactory] unless overridden,
 * and executes the provided test [block].
 *
 * The [block] lambda is `suspend`, so callers can invoke suspend functions directly.
 * The harness bridges the coroutine boundary internally via `runBlocking`.
 *
 * @param A The concrete Android app type bound to this test.
 * @param app The Android app under test.
 * @param driverFactory Optional override for the driver factory; defaults to [AndroidApp.driverFactory].
 * @param block Suspending test body executed with an [AppScope] receiver.
 */
public fun <A : AndroidApp> androidTest(
    app: A,
    driverFactory: AndroidDriverFactory = app.driverFactory,
    block: suspend AppScope<A>.(Unit) -> Unit,
) {
    appiumTest(
        app = app,
        driverFactory = driverFactory,
        setUp = { },
        block = block,
    )
}

/**
 * Android‑focused deep link convenience harness.
 *
 * Creates an Appium session using the app's default [AndroidDriverFactory] unless overridden,
 * navigates to the given [deepLink] after session creation, and then executes the test [block].
 *
 * Delegates to the unified [appiumTest] deep link overload, which dispatches the
 * `mobile: deepLink` command with the app's [AndroidApp.appPackage].
 *
 * The [block] lambda is `suspend`, so callers can invoke suspend functions directly.
 * The harness bridges the coroutine boundary internally via `runBlocking`.
 *
 * @param A The concrete Android app type bound to this test.
 * @param app The Android app under test.
 * @param driverFactory Optional override for the driver factory; defaults to [AndroidApp.driverFactory].
 * @param deepLink The deep link URL to navigate to before the test body runs.
 * @param block Suspending test body executed with an [AppScope] receiver.
 */
public fun <A : AndroidApp> androidTest(
    app: A,
    driverFactory: AndroidDriverFactory = app.driverFactory,
    deepLink: String,
    block: suspend AppScope<A>.(Unit) -> Unit,
) {
    appiumTest(
        app = app,
        driverFactory = driverFactory,
        deepLink = deepLink,
        block = block,
    )
}

/**
 * iOS‑focused convenience harness.
 *
 * Creates an Appium session using the app's default [IosDriverFactory] unless overridden,
 * and executes the provided test [block].
 *
 * The [block] lambda is `suspend`, so callers can invoke suspend functions directly.
 * The harness bridges the coroutine boundary internally via `runBlocking`.
 *
 * @param A The concrete iOS app type bound to this test.
 * @param app The iOS app under test.
 * @param driverFactory Optional override for the driver factory; defaults to [IosApp.driverFactory].
 * @param block Suspending test body executed with an [AppScope] receiver.
 */
public fun <A : IosApp> iosTest(
    app: A,
    driverFactory: IosDriverFactory = app.driverFactory,
    block: suspend AppScope<A>.(Unit) -> Unit,
) {
    appiumTest(
        app = app,
        driverFactory = driverFactory,
        setUp = { },
        block = block,
    )
}

/**
 * iOS‑focused deep link convenience harness.
 *
 * Creates an Appium session using the app's default [IosDriverFactory] unless overridden,
 * navigates to the given [deepLink] after session creation, and then executes the test [block].
 *
 * Delegates to the unified [appiumTest] deep link overload, which dispatches the
 * `mobile: deepLink` command with the app's [IosApp.bundleId].
 *
 * The [block] lambda is `suspend`, so callers can invoke suspend functions directly.
 * The harness bridges the coroutine boundary internally via `runBlocking`.
 *
 * @param A The concrete iOS app type bound to this test.
 * @param app The iOS app under test.
 * @param driverFactory Optional override for the driver factory; defaults to [IosApp.driverFactory].
 * @param deepLink The deep link URL to navigate to before the test body runs.
 * @param block Suspending test body executed with an [AppScope] receiver.
 */
public fun <A : IosApp> iosTest(
    app: A,
    driverFactory: IosDriverFactory = app.driverFactory,
    deepLink: String,
    block: suspend AppScope<A>.(Unit) -> Unit,
) {
    appiumTest(
        app = app,
        driverFactory = driverFactory,
        deepLink = deepLink,
        block = block,
    )
}

internal fun <A : App, T> appiumTestImpl(
    app: A,
    driverFactory: AppiumDriverFactory,
    setUp: suspend () -> T,
    tearDown: suspend (T) -> Unit = {},
    block: suspend AppScope<A>.(T) -> Unit,
) {
    val service = app.service
    val shutdownHook =
        service?.let { localService ->
            Thread {
                runCatching {
                    localService.stop()
                }
            }.also { hook ->
                Runtime.getRuntime().addShutdownHook(hook)
            }
        }
    service?.start()
    try {
        val prepared: T = runBlocking { setUp() }
        var testError: Throwable? = null
        var driver: AppiumDriver? = null

        try {
            driver = driverFactory()
            app.onSessionReady(driver)

            AppiumDriverContextHolder.set(driver)
            val entry = AppScope<A>(driver)
            runBlocking { entry.block(prepared) }
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
                AppiumDriverContextHolder.clear()
                runCatching { driver?.quit() }
            }
        }
    } finally {
        service?.stop()
        shutdownHook?.let { thread ->
            runCatching {
                Runtime.getRuntime().removeShutdownHook(thread)
            }
        }
    }
}

private fun <A : App> A.onSessionReady(driver: AppiumDriver) {
    when (this) {
        is AndroidApp -> {
            onSessionReady(driver as AndroidDriver)
        }

        is IosApp -> {
            onSessionReady(driver as IOSDriver)
        }

        is CrossPlatformApp -> {
            when (driver) {
                is AndroidDriver -> android.onSessionReady(driver)
                is IOSDriver -> ios.onSessionReady(driver)
            }
        }
    }
}

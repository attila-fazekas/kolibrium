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

package dev.kolibrium.appium

import dev.kolibrium.dsl.KolibriumDsl
import io.appium.java_client.AppiumDriver

/**
 * Unified test harness that creates an AppiumDriver session for the given [App] and runs the test flow.
 *
 * Three-phase lifecycle:
 * 1. **setUp**: Executes before any AppiumDriver session exists.
 * 2. **block**: Creates an AppiumDriver session and executes the main test body.
 * 3. **tearDown**: Executes after the test body, even if the test fails.
 *
 * @param A The concrete app type bound to this test.
 * @param T The type of the prepared input value passed to [block].
 * @param app The app under test.
 * @param driverFactory Factory creating an AppiumDriver instance.
 * @param setUp Computes the input [T] before the driver session is created.
 * @param tearDown Cleans up after the test body; runs even if the test fails.
 * @param block Main test body executed with an [AppEntry] receiver and the prepared value.
 */
@KolibriumDsl
public fun <A : App, T> appiumTest(
    app: A,
    driverFactory: AppiumDriverFactory,
    setUp: () -> T,
    tearDown: (T) -> Unit = {},
    block: AppEntry<A>.(T) -> Unit,
) {
    appiumTestImpl(app, driverFactory, setUp, tearDown, block)
}

/**
 * Android‑focused convenience harness.
 *
 * Creates an Appium session using the app's default [AndroidDriverFactory] unless overridden,
 * and executes the provided test [block].
 *
 * @param A The concrete Android app type bound to this test.
 * @param app The Android app under test.
 * @param driverFactory Optional override for the driver factory; defaults to [AndroidApp.driverFactory].
 * @param block The test body executed with an [AppEntry] receiver.
 */
@KolibriumDsl
public fun <A : AndroidApp> androidTest(
    app: A,
    driverFactory: AndroidDriverFactory = app.driverFactory,
    block: AppEntry<A>.(Unit) -> Unit,
) {
    appiumTest(
        app = app,
        driverFactory = driverFactory,
        setUp = { },
        block = block,
    )
}

/**
 * iOS‑focused convenience harness.
 *
 * Creates an Appium session using the app's default [IosDriverFactory] unless overridden,
 * and executes the provided test [block].
 *
 * @param A The concrete iOS app type bound to this test.
 * @param app The iOS app under test.
 * @param driverFactory Optional override for the driver factory; defaults to [IosApp.driverFactory].
 * @param block The test body executed with an [AppEntry] receiver.
 */
@KolibriumDsl
public fun <A : IosApp> iosTest(
    app: A,
    driverFactory: IosDriverFactory = app.driverFactory,
    block: AppEntry<A>.(Unit) -> Unit,
) {
    appiumTest(
        app = app,
        driverFactory = driverFactory,
        setUp = { },
        block = block,
    )
}

/**
 * Cross‑platform convenience harness.
 *
 * Requires an explicit [driverFactory] since there is no single sensible default. Use the
 * appropriate factory from [CrossPlatformApp.androidDriverFactory] or [CrossPlatformApp.iosDriverFactory].
 *
 * @param A The concrete cross‑platform app type bound to this test.
 * @param app The app under test.
 * @param driverFactory The explicit driver factory to use for this run.
 * @param block The test body executed with an [AppEntry] receiver.
 */
@KolibriumDsl
public fun <A : CrossPlatformApp> appiumTest(
    app: A,
    driverFactory: AppiumDriverFactory,
    block: AppEntry<A>.(Unit) -> Unit,
) {
    appiumTest(
        app = app,
        driverFactory = driverFactory,
        setUp = { },
        block = block,
    )
}

internal fun <A : App, T> appiumTestImpl(
    app: A,
    driverFactory: AppiumDriverFactory,
    setUp: () -> T,
    tearDown: (T) -> Unit = {},
    block: AppEntry<A>.(T) -> Unit,
) {
    val prepared: T = setUp()
    var testError: Throwable? = null
    var driver: AppiumDriver? = null

    try {
        driver = driverFactory()
        app.onSessionReady(driver)

        AppiumDriverContextHolder.set(driver)
        val entry: AppEntry<A> = ScreenEntry(driver)
        entry.block(prepared)
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
            AppiumDriverContextHolder.clear()
            runCatching { driver?.quit() }
        }
    }
}

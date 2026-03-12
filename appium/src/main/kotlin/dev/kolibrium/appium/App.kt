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

import dev.kolibrium.selenium.core.WaitConfig
import io.appium.java_client.AppiumDriver
import io.appium.java_client.service.local.AppiumDriverLocalService
import org.openqa.selenium.WebElement

/**
 * Marker interface for mobile applications under test.
 *
 * Concrete implementations model platform targets via the sealed subtypes:
 * - [AndroidApp] for Android‑only apps
 * - [IosApp] for iOS‑only apps
 * - [CrossPlatformApp] when both platforms are supported
 *
 * Implementations may override default element readiness and waiting behavior, and hook into
 * the driver lifecycle via [onSessionReady].
 */
public sealed interface App {
    /**
     * Optional local Appium server managed by Kolibrium for this app.
     *
     * When non-null, the test harness starts the service before creating the
     * [io.appium.java_client.AppiumDriver] session and stops it during teardown.
     * If null (default), tests assume an external Appium server is already running.
     */
    public val service: AppiumDriverLocalService?

    /**
     * Default readiness predicate applied by locator delegates for a single [WebElement].
     *
     * Override to change the global "ready" condition for this app (e.g., `isEnabled`).
     */
    public val elementReadyCondition: WebElement.() -> Boolean
        get() = { isDisplayed }

    /**
     * Default [dev.kolibrium.selenium.core.WaitConfig] used by locator delegates when none is specified at the call site.
     */
    public val waitConfig: WaitConfig
        get() = WaitConfig.Default

    /**
     * Optional hook invoked after the [AppiumDriver] session has been created and before any
     * screen interactions. Override to perform additional configuration (e.g., timeouts) or
     * pre‑navigation common to all tests.
     *
     * @param driver The newly created [AppiumDriver] backing this test run.
     */
    public fun onSessionReady(driver: AppiumDriver) {}
}

/**
 * Android‑only application definition.
 *
 * @property driverFactory The factory used to create an [io.appium.java_client.android.AndroidDriver]
 *                         session for this app.
 * @property service Optional [AppiumDriverLocalService] to be managed for this app.
 * If provided, it is started before the driver session is created and stopped during teardown.
 * If null, an external Appium server is assumed to be running.
 */
public abstract class AndroidApp(
    public val driverFactory: AndroidDriverFactory,
    override val service: AppiumDriverLocalService? = null,
) : App

/**
 * iOS‑only application definition.
 *
 * @property driverFactory The factory used to create an [io.appium.java_client.ios.IOSDriver]
 *                         session for this app.
 * @property service Optional [AppiumDriverLocalService] to be managed for this app.
 * If provided, it is started before the driver session is created and stopped during teardown.
 * If null, an external Appium server is assumed to be running.
 */
public abstract class IosApp(
    public val driverFactory: IosDriverFactory,
    override val service: AppiumDriverLocalService? = null,
) : App

/**
 * Cross‑platform application definition supporting both Android and iOS.
 *
 * @property androidDriverFactory The factory used to create Android driver sessions.
 * @property iosDriverFactory The factory used to create iOS driver sessions.
 * @property service Optional [AppiumDriverLocalService] to be managed for this app.
 * If provided, it is started before the driver session is created and stopped during teardown.
 * If null, an external Appium server is assumed to be running.
 */
public abstract class CrossPlatformApp(
    public val androidDriverFactory: AndroidDriverFactory,
    public val iosDriverFactory: IosDriverFactory,
    override val service: AppiumDriverLocalService? = null,
) : App

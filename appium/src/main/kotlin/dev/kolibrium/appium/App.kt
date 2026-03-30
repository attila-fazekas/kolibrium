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
import io.appium.java_client.service.local.AppiumDriverLocalService
import java.net.URL

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
     * [AppiumDriver] session and stops it during teardown.
     * If null (default), tests assume an external Appium server is already running.
     */
    public val service: AppiumDriverLocalService?
}

/**
 * Android‑only application definition.
 *
 * Supports three modes of construction — supply whichever combination applies:
 * - **by package**: [appPackage] + [appActivity] → factory derived via [androidDriverByPackage].
 * - **by app path**: [appPath] (+ optional [appPackage]) → factory derived via [androidDriverByApp].
 * - **custom factory**: pass [driverFactory] directly (+ optional [appPackage] for locators).
 *
 * @property appPackage The Android package name, available at runtime for locators.
 * @property appActivity The launcher activity. Required when launching by package.
 * @property appPath Filesystem path or URL to the APK/AAB. Required when installing from a binary.
 * @property appiumUrl URL of the Appium server to connect to. Defaults to the local server.
 * @property service Optional [AppiumDriverLocalService] to be managed for this app.
 * @param driverFactory The factory used to create an [AndroidDriver]
 *                      session. Derived automatically when not provided explicitly.
 */
public abstract class AndroidApp(
    public val appPackage: String? = null,
    public val appActivity: String? = null,
    public val appPath: String? = null,
    public val appiumUrl: URL = DEFAULT_APPIUM_URL,
    override val service: AppiumDriverLocalService? = null,
    driverFactory: AndroidDriverFactory? = null,
) : App {
    init {
        require(appPath == null || appActivity == null) {
            "Specify either 'appPath' (install mode) or 'appPackage' + 'appActivity' (launch mode), not both. " +
                "If you need 'appPackage' for locators with 'appPath', omit 'appActivity'."
        }
    }

    public open val driverFactory: AndroidDriverFactory = driverFactory ?: deriveDriverFactory()

    /**
     * Optional hook invoked after the [AndroidDriver] session has been created and before any
     * screen interactions. Override to perform additional configuration (e.g., setting orientation
     * or geolocation) or pre‑navigation common to all tests.
     *
     * @param driver The newly created [AndroidDriver] backing this test run.
     */
    public open fun onSessionReady(driver: AndroidDriver) {}

    private fun deriveDriverFactory(): AndroidDriverFactory =
        when {
            appPath != null -> androidDriverByApp(appPath, appiumUrl)
            appPackage != null && appActivity != null -> androidDriverByPackage(appPackage, appActivity, appiumUrl)
            else -> error("'AndroidApp' requires either 'appPath', both 'appPackage' and 'appActivity', or an explicit 'driverFactory'")
        }
}

/**
 * iOS‑only application definition.
 *
 * Supports three modes of construction:
 * - **by bundle ID**: [bundleId] → factory derived via [iosDriverByBundleId].
 * - **by app path**: [appPath] (+ optional [bundleId]) → factory derived via [iosDriverByApp].
 * - **custom factory**: pass [driverFactory] directly (+ optional [bundleId] for locators).
 *
 * @property bundleId The iOS bundle identifier, available at runtime for locators.
 * @property appPath Filesystem path or URL to the .app/IPA. Required when installing from a binary.
 * @property appiumUrl URL of the Appium server to connect to. Defaults to the local server.
 * @property service Optional [AppiumDriverLocalService] to be managed for this app.
 * @param driverFactory The factory used to create an [IOSDriver]
 *                      session. Derived automatically when not provided explicitly.
 */
public abstract class IosApp(
    public val bundleId: String? = null,
    public val appPath: String? = null,
    public val appiumUrl: URL = DEFAULT_APPIUM_URL,
    override val service: AppiumDriverLocalService? = null,
    driverFactory: IosDriverFactory? = null,
) : App {
    init {
        require(appPath == null || bundleId == null) {
            "Specify either 'appPath' (install mode) or 'bundleId' (launch mode), not both. " +
                "When installing from an app path, Appium derives the bundle ID from the app itself."
        }
    }

    public val driverFactory: IosDriverFactory = driverFactory ?: deriveDriverFactory()

    /**
     * Optional hook invoked after the [IOSDriver] session has been created and before any
     * screen interactions. Override to perform additional configuration (e.g., setting orientation
     * or geolocation) or pre‑navigation common to all tests.
     *
     * @param driver The newly created [IOSDriver] backing this test run.
     */
    public open fun onSessionReady(driver: IOSDriver) {}

    private fun deriveDriverFactory(): IosDriverFactory =
        when {
            appPath != null -> iosDriverByApp(appPath, appiumUrl)
            bundleId != null -> iosDriverByBundleId(bundleId, appiumUrl)
            else -> error("'IosApp' requires either 'appPath', a 'bundleId', or an explicit 'driverFactory'")
        }
}

/**
 * Cross‑platform application definition supporting both Android and iOS.
 *
 * Composes an [AndroidApp] and an [IosApp] — each defined with its own
 * platform-specific parameters — into a single app object.
 *
 * @property android The Android-side app definition.
 * @property ios The iOS-side app definition.
 */
public abstract class CrossPlatformApp(
    public val android: AndroidApp,
    public val ios: IosApp,
) : App {
    /**
     * Returns the service from the Android-side or iOS-side app, if either is configured.
     *
     * At most one platform app should define a service at a time — the harness manages
     * whichever one is present for the current test run.
     */
    override val service: AppiumDriverLocalService?
        get() = android.service ?: ios.service

    /**
     * The driver factory used to create Android driver sessions for this app.
     * Delegates to the [driverFactory] property of the Android-side app.
     */
    public val androidDriverFactory: AndroidDriverFactory
        get() = android.driverFactory

    /**
     * The driver factory used to create iOS driver sessions for this app.
     * Delegates to the [driverFactory] property of the iOS-side app.
     */
    public val iosDriverFactory: IosDriverFactory
        get() = ios.driverFactory

    /**
     * The Android package name for the app under test.
     * Delegates to the [appPackage] property of the Android-side app.
     */
    public val appPackage: String?
        get() = android.appPackage

    /**
     * The iOS bundle identifier for the app under test.
     * Delegates to the [bundleId] property of the iOS-side app.
     */
    public val bundleId: String?
        get() = ios.bundleId
}

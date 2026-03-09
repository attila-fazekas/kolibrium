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

import io.appium.java_client.android.AndroidDriver
import io.appium.java_client.android.options.UiAutomator2Options
import io.appium.java_client.ios.IOSDriver
import io.appium.java_client.ios.options.XCUITestOptions
import java.net.URI
import java.net.URL

private val DEFAULT_APPIUM_URL = URI("http://127.0.0.1:4723").toURL()

/**
 * Creates an [AndroidDriver] factory configured to launch an already installed application
 * by its Android package and entry activity using UiAutomator2.
 *
 * The returned factory is a zero‑argument function that builds a fresh driver instance when invoked.
 * Use this when the AUT is preinstalled on the device/emulator.
 *
 * @param appPackage The Android package name of the application under test (e.g. `com.example.app`).
 * @param appActivity The fully qualified or dot‑prefixed launcher activity (e.g. `.MainActivity`).
 * @param appiumUrl The Appium server URL to connect to; defaults to a local server at 127.0.0.1:4723.
 * @return An [AndroidDriverFactory] that creates a new [AndroidDriver] on each call.
 */
public fun androidDriverByPackage(
    appPackage: String,
    appActivity: String,
    appiumUrl: URL = DEFAULT_APPIUM_URL,
): AndroidDriverFactory =
    {
        val options =
            UiAutomator2Options().apply {
                setAppPackage(appPackage)
                setAppActivity(appActivity)
            }
        AndroidDriver(appiumUrl, options)
    }

/**
 * Creates an [AndroidDriver] factory configured to install and launch an Android application
 * from the given APK/app bundle path using UiAutomator2.
 *
 * The returned factory is a zero‑argument function that builds a fresh driver instance when invoked.
 *
 * @param appPath Filesystem path or URL to the application (APK/AAB) to install and run.
 * @param appiumUrl The Appium server URL to connect to; defaults to a local server at 127.0.0.1:4723.
 * @return An [AndroidDriverFactory] that creates a new [AndroidDriver] on each call.
 */
public fun androidDriverByApp(
    appPath: String,
    appiumUrl: URL = DEFAULT_APPIUM_URL,
): AndroidDriverFactory =
    {
        val options =
            UiAutomator2Options().apply {
                setApp(appPath)
            }
        AndroidDriver(appiumUrl, options)
    }

/**
 * Creates an [IOSDriver] factory configured to launch an already installed iOS application
 * by its bundle identifier using XCUITest.
 *
 * The returned factory is a zero‑argument function that builds a fresh driver instance when invoked.
 * Use this when the AUT is preinstalled on the device/simulator.
 *
 * @param bundleId The iOS bundle identifier of the application under test (e.g. `com.example.app`).
 * @param appiumUrl The Appium server URL to connect to; defaults to a local server at 127.0.0.1:4723.
 * @return An [IosDriverFactory] that creates a new [IOSDriver] on each call.
 */
public fun iosDriverByBundleId(
    bundleId: String,
    appiumUrl: URL = DEFAULT_APPIUM_URL,
): IosDriverFactory =
    {
        val options =
            XCUITestOptions().apply {
                setBundleId(bundleId)
            }
        IOSDriver(appiumUrl, options)
    }

/**
 * Creates an [IOSDriver] factory configured to install and launch an iOS application
 * from the given app bundle (.app) or IPA path using XCUITest.
 *
 * The returned factory is a zero‑argument function that builds a fresh driver instance when invoked.
 *
 * @param appPath Filesystem path or URL to the iOS application bundle/IPA to install and run.
 * @param appiumUrl The Appium server URL to connect to; defaults to a local server at 127.0.0.1:4723.
 * @return An [IosDriverFactory] that creates a new [IOSDriver] on each call.
 */
public fun iosDriverByApp(
    appPath: String,
    appiumUrl: URL = DEFAULT_APPIUM_URL,
): IosDriverFactory =
    {
        val options =
            XCUITestOptions().apply {
                setApp(appPath)
            }
        IOSDriver(appiumUrl, options)
    }

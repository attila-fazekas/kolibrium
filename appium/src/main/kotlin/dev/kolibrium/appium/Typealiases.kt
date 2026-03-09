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

/**
 * Factory function that creates a new [AppiumDriver] instance.
 *
 * Used by [appiumTest] for [CrossPlatformApp] tests and as the base type for the
 * full‑lifecycle overload accepting any [App] subtype.
 *
 * @see androidTest
 * @see iosTest
 */
public typealias AppiumDriverFactory = () -> AppiumDriver

/**
 * Factory function that creates a new [AndroidDriver] instance.
 *
 * Used by [androidTest] and as the driver configuration in [AndroidApp] and [CrossPlatformApp].
 *
 * @see AppiumDriverFactory
 */
public typealias AndroidDriverFactory = () -> AndroidDriver

/**
 * Factory function that creates a new [IOSDriver] instance.
 *
 * Used by [iosTest] and as the driver configuration in [IosApp] and [CrossPlatformApp].
 *
 * @see AppiumDriverFactory
 */
public typealias IosDriverFactory = () -> IOSDriver

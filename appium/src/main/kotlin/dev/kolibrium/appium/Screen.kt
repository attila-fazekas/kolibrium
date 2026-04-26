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

import dev.kolibrium.annotations.InternalKolibriumApi
import io.appium.java_client.AppiumDriver
import io.appium.java_client.android.AndroidDriver
import io.appium.java_client.ios.IOSDriver
import org.openqa.selenium.By
import org.openqa.selenium.SearchContext
import org.openqa.selenium.WebElement

/**
 * Base type for screen objects bound to an [App].
 *
 * Screen instances rely on a contextual [AppiumDriver] installed by Kolibrium's DSL
 * entry points ([androidTest], [iosTest], [appiumTest]). Constructing and using screens
 * outside those contexts will fail with a runtime error. Delegated `findElement(s)` calls
 * route through the contextual driver to satisfy [org.openqa.selenium.SearchContext].
 *
 * **For most use cases, extend [AndroidScreen] or [IosScreen] instead** to get typed
 * driver access without casting.
 */
@InternalKolibriumApi
public abstract class Screen<A : App> : SearchContext {
    /**
     * Wait until the screen is considered ready for interaction.
     * Default is a no-op; subclasses may override.
     */
    public open fun awaitReady() {}

    /**
     * Optional post-ready assertions to verify invariants.
     */
    public open fun assertReady() {}

    /**
     * Direct access to the underlying [AppiumDriver] for cases where the abstraction
     * needs to be bypassed (e.g., viewport calculations, advanced gestures).
     *
     * Use sparingly as an escape hatch when screen-level APIs are insufficient.
     */
    protected val driver: AppiumDriver
        get() = requireDriver()

    override fun findElement(by: By): WebElement = requireDriver().findElement(by)

    override fun findElements(by: By): List<WebElement> = requireDriver().findElements(by)

    private fun requireDriver(): AppiumDriver =
        AppiumDriverContextHolder.get() ?: error(
            "Kolibrium runtime error: Screen '${this::class.simpleName ?: "<unknown>"}' " +
                "has no active AppiumDriver context.\n" +
                "Run screen interactions inside Kolibrium DSL (e.g., androidTest/iosTest/appiumTest → on).",
        )
}

/**
 * Platform-specific base class for Android screen objects.
 *
 * Extends [Screen] with typed [AndroidDriver] access via the [androidDriver] property,
 * eliminating the need for casting when using Android-specific driver APIs.
 *
 * Example:
 * ```kotlin
 * class ProductsScreen : AndroidScreen() {
 *     fun doSomething() {
 *         androidDriver.pressKey(...)  // No cast needed
 *     }
 * }
 * ```
 *
 * For most Android apps, this should be your screen base class instead of [Screen].
 */
public abstract class AndroidScreen : Screen<AndroidApp>() {
    protected val androidDriver: AndroidDriver
        get() = super.driver as AndroidDriver
}

/**
 * Platform-specific base class for iOS screen objects.
 *
 * Extends [Screen] with typed [IOSDriver] access via the [iosDriver] property,
 * eliminating the need for casting when using iOS-specific driver APIs.
 *
 * Example:
 * ```kotlin
 * class ProductsScreen : IosScreen() {
 *     fun doSomething() {
 *         iosDriver.shake()  // No cast needed
 *     }
 * }
 * ```
 *
 * For most iOS apps, this should be your screen base class instead of [Screen].
 */
public abstract class IosScreen : Screen<IosApp>() {
    protected val iosDriver: IOSDriver
        get() = super.driver as IOSDriver
}

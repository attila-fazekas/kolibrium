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

import dev.kolibrium.appium.android.SettingsScope
import dev.kolibrium.webdriver.KolibriumDsl
import io.appium.java_client.AppiumDriver
import io.appium.java_client.HasSettings

/**
 * App‑scoped DSL receiver available inside `androidTest { … }`, `iosTest { … }` and
 * cross‑platform `appiumTest { … }` blocks.
 *
 * Provides the [on] function for constructing screen objects and chaining interactions
 * in a fluent style.
 */
@KolibriumDsl
public class AppScope<A : App> internal constructor(
    private val driver: AppiumDriver,
) {
    /**
     * Create a screen via [factory], ensure it is ready, execute [action] on it, and return
     * a [ScreenScope] bound to that screen for further chaining.
     *
     * Use this as the entry point for every screen interaction — whether the screen was reached
     * through normal navigation, a deep link, or is simply the first screen after app launch.
     *
     * Typical usage:
     * ```kotlin
     * androidTest(app = MyAndroidApp) {
     *     on(::ProductsScreen) {
     *         titleText() shouldBe "Products"
     *         selectProduct("Backpack")
     *     }.on(::ProductDetailsScreen) {
     *         addToCart()
     *     }
     * }
     * ```
     *
     * @param S The type of the screen to create and interact with.
     * @param factory No‑arg factory that constructs the screen instance (typically a constructor reference like `::MyScreen`).
     * @param action Action executed on the created screen with the screen as the receiver.
     * @return A [ScreenScope] bound to the screen, allowing further [ScreenScope.on] or [ScreenScope.then] calls.
     */
    public fun <S : Screen<A>> on(
        factory: () -> S,
        action: S.() -> Unit,
    ): ScreenScope<A, S> {
        val screen = factory()
        ensureReady(screen)
        screen.action()
        return ScreenScope(screen, driver)
    }

    /**
     * DSL for adjusting Appium settings at runtime.
     *
     * Unlike session capabilities, settings can be changed multiple times during a test.
     *
     * Example:
     * ```kotlin
     * androidTest(app = MyAndroidApp) {
     *     settings {
     *         ignoreUnimportantViews = true
     *     }
     * }
     * ```
     */
    public fun settings(block: SettingsScope.() -> Unit) {
        val settingsMap = SettingsScope().apply(block).toMap()
        if (settingsMap.isNotEmpty()) {
            (driver as HasSettings).settings = settingsMap
        }
    }
}

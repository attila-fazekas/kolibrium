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

import dev.kolibrium.annotations.KolibriumDsl
import dev.kolibrium.appium.android.SettingsScope
import io.appium.java_client.AppiumDriver
import io.appium.java_client.HasSettings

/**
 * Scope object returned from [AppScope.on] that carries the currently bound
 * screen and provides chaining helpers for further actions and assertions.
 *
 * @param A The app type, ensuring all chained screens belong to the same app.
 * @param S The type of the currently bound screen.
 * @property screen The bound screen instance.
 * @property driver The underlying [AppiumDriver] backing this scope.
 */
@KolibriumDsl
public class ScreenScope<A : App, S : Screen<A>> internal constructor(
    internal val screen: S,
    internal val driver: AppiumDriver,
) {
    /**
     * Bind a new screen created by [factory], ensure it is ready, execute [action] on it,
     * and return a new [ScreenScope] for further chaining.
     *
     * @param Next The type of the next screen to bind.
     * @param factory No‑arg factory that constructs the next screen instance.
     * @param action The action to execute on the next screen before returning the new scope.
     */
    public fun <Next : Screen<A>> on(
        factory: () -> Next,
        action: Next.() -> Unit,
    ): ScreenScope<A, Next> {
        val next = factory()
        ensureReady(next)
        next.action()
        return ScreenScope(next, driver)
    }

    /**
     * Perform actions or assertions on the currently bound [screen] while ensuring it is
     * in a ready state. Returns this [ScreenScope] for fluent chaining.
     *
     * @param action The action to run against the current screen.
     * @return This [ScreenScope] to allow further chaining.
     */
    public fun then(action: S.() -> Unit): ScreenScope<A, S> =
        apply {
            ensureReady(screen)
            screen.action()
        }

    /**
     * DSL for adjusting Appium settings at runtime.
     *
     * Example:
     * ```kotlin
     * androidTest(app = MyAndroidApp) {
     *     on(::ProductsScreen) {
     *         // ...
     *     }.settings {
     *         ignoreUnimportantViews = true
     *     }
     * }
     * ```
     */
    public fun settings(block: SettingsScope.() -> Unit): ScreenScope<A, S> =
        apply {
            val settingsMap = SettingsScope().apply(block).toMap()
            if (settingsMap.isNotEmpty()) {
                (driver as HasSettings).settings = settingsMap
            }
        }
}

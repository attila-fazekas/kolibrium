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

import dev.kolibrium.selenium.dsl.KolibriumDsl
import io.appium.java_client.AppiumDriver

/**
 * Scope object returned from [AppEntry.open] and [AppEntry.on] that carries the currently bound
 * screen and provides chaining helpers for further actions and assertions.
 *
 * @param S The type of the currently bound screen.
 * @property screen The bound screen instance.
 * @property driver The underlying [AppiumDriver] backing this scope.
 */
@KolibriumDsl
public class ScreenScope<S : Screen<*>> internal constructor(
    internal val screen: S,
    internal val driver: AppiumDriver,
) {
    /**
     * Bind a new screen created by [factory], execute [action] on it, and return a new [ScreenScope]
     * for further chaining.
     *
     * @param Next The type of the next screen to bind.
     * @param factory No‑arg factory that constructs the next screen instance.
     * @param action The action to execute on the next screen before returning the new scope.
     */
    @KolibriumDsl
    public fun <Next : Screen<*>> on(
        factory: () -> Next,
        action: Next.() -> Unit,
    ): ScreenScope<Next> {
        val next = factory()
        next.awaitReady()
        next.assertReady()
        next.action()
        return ScreenScope(next, driver)
    }

    /**
     * Perform assertions on the currently bound [screen] while ensuring it is in a ready state.
     *
     * @param assertions The assertions to run against the current screen.
     * @return This [ScreenScope] to allow fluent chaining.
     */
    @KolibriumDsl
    public fun verify(assertions: S.() -> Unit): ScreenScope<S> =
        apply {
            screen.assertReady()
            screen.assertions()
        }

    /**
     * Perform arbitrary actions on the currently bound [screen] while ensuring it is in a ready state.
     *
     * @param action The action to run against the current screen.
     * @return This [ScreenScope] to allow fluent chaining.
     */
    @KolibriumDsl
    public fun then(action: S.() -> Unit): ScreenScope<S> =
        apply {
            screen.assertReady()
            screen.action()
        }
}

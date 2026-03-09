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

/**
 * App‑scoped DSL receiver available inside `androidTest { … }`, `iosTest { … }` and
 * cross‑platform `appiumTest { … }` blocks.
 *
 * Exposes operations like [open] and [on] for constructing screen objects and chaining
 * interactions in a fluent style.
 */
@KolibriumDsl
public interface AppEntry<A : App> {
    /**
     * Open a screen created by [factory] and run [action] on it, returning a new [ScreenScope]
     * that is bound to the screen returned from [action].
     *
     * Typical usage:
     * ```kotlin
     * open(::ProductsScreen) {
     *     // this == ProductsScreen
     *     titleText()
     * }.on(::CartScreen) {
     *     // ...
     * }
     * ```
     *
     * @param S The type of the created screen.
     * @param R The type of the screen returned from [action] (can be the same as [S]).
     * @param factory No‑arg factory that constructs the initial screen instance.
     * @param action Action executed on the created screen which returns the next screen to bind.
     */
    public fun <S : Screen<A>, R : Screen<A>> open(
        factory: () -> S,
        action: S.() -> R,
    ): ScreenScope<R>

    /**
     * Bind a screen created by [factory] to the current context (no navigation implied) and run
     * [action] on it, returning a [ScreenScope] for the next screen.
     *
     * Use this when you want to attach to an already opened screen object.
     *
     * @param S The type of the created screen.
     * @param R The type of the screen returned from [action] (can be the same as [S]).
     * @param factory No‑arg factory that constructs the screen instance to bind.
     * @param action Action executed on the created screen which returns the next screen to bind.
     */
    public fun <S : Screen<A>, R : Screen<A>> on(
        factory: () -> S,
        action: S.() -> R,
    ): ScreenScope<R>
}

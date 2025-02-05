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

package dev.kolibrium.dsl.selenium.actions

import dev.kolibrium.dsl.selenium.creation.KolibriumDsl
import org.openqa.selenium.WebDriver
import org.openqa.selenium.interactions.Actions

/**
 * Creates a new Actions instance and executes the specified actions within the given [block].
 * This function provides a DSL for performing scrolling actions in a more readable and concise way.
 *
 * @receiver The [WebDriver] instance on which to perform Actions API operations.
 * @param batchActions When true (default), all scroll actions in the block are collected and executed
 *                     together at the end.
 *                     When false, each scroll action is executed immediately after it's called,
 *                     which can be useful for debugging.
 * @param block The actions to be performed within the DSL scope.
 */
@KolibriumDsl
public fun WebDriver.actions(
    batchActions: Boolean = true,
    block: ActionsScope.() -> Unit,
) {
    val actions = Actions(this)
    val actionsScope = ActionsScope(actions, batchActions).apply(block)
    if (batchActions) {
        actionsScope.actions.perform()
    }
}

/**
 * Scope class for defining scrolling actions using the Selenium Actions API.
 * This class provides a type-safe builder for various scrolling operations.
 */
@KolibriumDsl
public class ActionsScope(
    internal val actions: Actions,
    internal val batchActions: Boolean,
)

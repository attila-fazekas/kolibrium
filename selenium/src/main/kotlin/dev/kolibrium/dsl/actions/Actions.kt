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

package dev.kolibrium.dsl.actions

import dev.kolibrium.dsl.KolibriumDsl
import org.openqa.selenium.WebDriver
import org.openqa.selenium.interactions.Actions

/**
 * Creates a new [Actions] instance and executes the specified actions within [block].
 *
 * Provides a DSL around Selenium's Actions API. When [batchActions] is true (default), actions defined
 * in the block are queued and performed once after the block completes. When false, each action is
 * performed immediately (useful for debugging).
 *
 * @receiver The [WebDriver] instance on which to perform Actions API operations.
 * @param batchActions When true (default), actions are collected and executed together at the end; when false, each is performed immediately.
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

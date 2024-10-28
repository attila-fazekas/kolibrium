/*
 * Copyright 2023-2024 Attila Fazekas & contributors
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
import org.openqa.selenium.WebElement
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
 *
 * Example usage:
 * ```
 * // Execute all scrolls at once (more efficient)
 * driver.actions {
 *     scrollDown(100)
 *     scrollTo(element)
 * }
 *
 * // Execute each scroll immediately (useful for debugging)
 * driver.actions(batchActions = false) {
 *     scrollDown(100)
 *     scrollTo(element)
 * }
 * ```
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
    private val batchActions: Boolean,
) {
    /**
     * Scrolls the page to bring the specified element into view.
     * This is useful when you need to interact with elements that are not currently visible in the viewport.
     *
     * @param element The [WebElement] to scroll to.
     */
    @KolibriumDsl
    public fun scrollTo(element: WebElement) {
        actions.scrollToElement(element)
        if (!batchActions) {
            actions.perform()
        }
    }

    /**
     * Scrolls the page downward by the specified amount of pixels.
     *
     * @param amount The number of pixels to scroll down. Must be greater than zero.
     * @throws IllegalArgumentException if the amount is not greater than zero.
     */
    @KolibriumDsl
    public fun scrollDown(amount: Int) {
        validateInput(amount)
        actions.scrollByAmount(0, amount)
        if (!batchActions) {
            actions.perform()
        }
    }

    /**
     * Scrolls the page upward by the specified amount of pixels.
     *
     * @param amount The number of pixels to scroll up. Must be greater than zero.
     * @throws IllegalArgumentException if the amount is not greater than zero.
     */
    @KolibriumDsl
    public fun scrollUp(amount: Int) {
        validateInput(amount)
        actions.scrollByAmount(0, -amount)
        if (!batchActions) {
            actions.perform()
        }
    }

    /**
     * Scrolls the page to the right by the specified amount of pixels.
     *
     * @param amount The number of pixels to scroll right. Must be greater than zero.
     * @throws IllegalArgumentException if the amount is not greater than zero.
     */
    @KolibriumDsl
    public fun scrollRight(amount: Int) {
        validateInput(amount)
        actions.scrollByAmount(amount, 0)
        if (!batchActions) {
            actions.perform()
        }
    }

    /**
     * Scrolls the page to the left by the specified amount of pixels.
     *
     * @param amount The number of pixels to scroll left. Must be greater than zero.
     * @throws IllegalArgumentException if the amount is not greater than zero.
     */
    @KolibriumDsl
    public fun scrollLeft(amount: Int) {
        validateInput(amount)
        actions.scrollByAmount(-amount, 0)
        if (!batchActions) {
            actions.perform()
        }
    }

    private fun validateInput(amount: Int) = require(amount > 0) { "Scroll amount must be bigger than zero!" }
}

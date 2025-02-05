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
import org.openqa.selenium.WebElement

/**
 * Scrolls the page to bring the specified element into view.
 * This is useful when you need to interact with elements that are not currently visible in the viewport.
 *
 * @receiver The [ActionsScope] on which this scroll operation is performed.
 * @param element The [WebElement] to scroll to.
 */
@KolibriumDsl
public fun ActionsScope.scrollTo(element: WebElement) {
    actions.scrollToElement(element)
    if (batchActions) {
        actions.perform()
    }
}

/**
 * Scrolls the page downward by the specified amount of pixels.
 *
 * @receiver The [ActionsScope] on which this scroll operation is performed.
 * @param amount The number of pixels to scroll down. Must be greater than zero.
 * @throws IllegalArgumentException if the amount is not greater than zero.
 */
@KolibriumDsl
public fun ActionsScope.scrollDown(amount: Int) {
    validateInput(amount)
    actions.scrollByAmount(0, amount)
    if (!batchActions) {
        actions.perform()
    }
}

/**
 * Scrolls the page upward by the specified amount of pixels.
 *
 * @receiver The [ActionsScope] on which this scroll operation is performed.
 * @param amount The number of pixels to scroll up. Must be greater than zero.
 * @throws IllegalArgumentException if the amount is not greater than zero.
 */
@KolibriumDsl
public fun ActionsScope.scrollUp(amount: Int) {
    validateInput(amount)
    actions.scrollByAmount(0, -amount)
    if (!batchActions) {
        actions.perform()
    }
}

/**
 * Scrolls the page to the right by the specified amount of pixels.
 *
 * @receiver The [ActionsScope] on which this scroll operation is performed.
 * @param amount The number of pixels to scroll right. Must be greater than zero.
 * @throws IllegalArgumentException if the amount is not greater than zero.
 */
@KolibriumDsl
public fun ActionsScope.scrollRight(amount: Int) {
    validateInput(amount)
    actions.scrollByAmount(amount, 0)
    if (!batchActions) {
        actions.perform()
    }
}

/**
 * Scrolls the page to the left by the specified amount of pixels.
 *
 * @receiver The [ActionsScope] on which this scroll operation is performed.
 * @param amount The number of pixels to scroll left. Must be greater than zero.
 * @throws IllegalArgumentException if the amount is not greater than zero.
 */
@KolibriumDsl
public fun ActionsScope.scrollLeft(amount: Int) {
    validateInput(amount)
    actions.scrollByAmount(-amount, 0)
    if (!batchActions) {
        actions.perform()
    }
}

private fun validateInput(amount: Int) = require(amount > 0) { "Scroll amount must be bigger than zero!" }

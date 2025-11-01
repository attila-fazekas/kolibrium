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

package dev.kolibrium.dsl.selenium.interactions

import dev.kolibrium.dsl.selenium.KolibriumDsl
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement

/**
 * Executes actions within an iframe context and automatically restores the default content afterward.
 * The driver is switched into the iframe before executing [block] and switched back to the top-level document after [block] completes.
 *
 * This function provides a more readable and concise way to work with iframes compared to
 * manually calling `switchTo().frame()` and remembering to switch back.
 *
 * @receiver The [WebDriver] instance on which to perform the iframe switch.
 * @param iframe The iframe [WebElement] to switch into.
 * @param block The code block to execute within the iframe context.
 */
@KolibriumDsl
public fun WebDriver.iframe(
    iframe: WebElement,
    block: () -> Unit,
) {
    switchTo().frame(iframe)
    block()
    switchTo().defaultContent()
}

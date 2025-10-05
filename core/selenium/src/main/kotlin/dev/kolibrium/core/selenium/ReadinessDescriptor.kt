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

package dev.kolibrium.core.selenium

import org.openqa.selenium.By
import org.openqa.selenium.WebElement

/**
 * Describes how to decide a page is ready by locating a canonical element and checking a condition.
 *
 * A page is considered ready when the element located by [by] satisfies either the provided [custom]
 * predicate or, if [custom] is `null`, the selected [condition]. Optional [waitConfig] can be used to
 * control how long and how often the readiness check is retried.
 *
 * @property by Selenium [By] locator used to find the canonical element that represents readiness.
 * @property waitConfig Optional wait configuration to use while locating the element and evaluating readiness.
 * @property condition Built-in [ReadinessCondition] applied when no [custom] check is provided. Defaults to [ReadinessCondition.IsDisplayed].
 * @property custom Optional custom predicate that decides readiness from the located [WebElement]. When provided, it takes precedence over [condition].
 */
public class ReadinessDescriptor(
    public val by: By,
    public val waitConfig: WaitConfig? = null,
    public val condition: ReadinessCondition = ReadinessCondition.IsDisplayed,
    /** If provided, this predicate must evaluate to true for readiness; it takes precedence over [condition]. */
    public val custom: ElementReadyCheck? = null,
)

/** Built-in readiness conditions for a located element. */
public enum class ReadinessCondition {
    /** The element returns true for [WebElement.isDisplayed]. */
    IsDisplayed,

    /** The element returns true for [WebElement.isEnabled]. */
    IsEnabled,

    /** The element is considered clickable by WebDriver (typically displayed and enabled). */
    IsClickable,
}

/**
 * Functional interface for custom element readiness checks.
 *
 * Implementations should perform a fast, side-effect-free check against the provided [WebElement]
 * and return `true` if the page can be considered ready.
 */
public fun interface ElementReadyCheck {
    /**
     * Decide whether the page is ready based on the given [element].
     *
     * This method should avoid throwing; return `false` if the element state does not meet your criteria.
     */
    public fun isReady(element: WebElement): Boolean
}

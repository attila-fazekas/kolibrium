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

package dev.kolibrium.selenium.decorators

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement

/**
 * A decorator that caches positive state check results (`true` values) for WebElement state methods:
 * [WebElement.isDisplayed], [WebElement.isEnabled] and [WebElement.isSelected].
 *
 * This decorator optimizes performance by caching `true` results from state checks, eliminating
 * redundant WebDriver calls when an element is known to be in a positive state. Only `true` results
 * are cached - negative results (`false`) are always checked against the actual element state.
 *
 * State caching behavior:
 * - When a state check returns `true`, the result is cached.
 * - When a state check returns `false`, the result is not cached.
 * - Subsequent checks for a cached `true` state return immediately without WebDriver interaction.
 * - Each decorated element maintains its own independent state cache.
 *
 * Note: This decorator assumes that once an element becomes displayed, enabled or selected, it remains
 * in that state for the duration of the test. If your application can toggle these states dynamically,
 * consider not using this decorator or clearing the cache when state changes are possible.
 *
 * @param cacheDisplayed Whether to cache positive results from [WebElement.isDisplayed] calls.
 * @param cacheEnabled Whether to cache positive results from [WebElement.isEnabled] calls.
 * @param cacheSelected Whether to cache positive results from [WebElement.isSelected] calls.
 * @see WebElement.isDisplayed
 * @see WebElement.isEnabled
 * @see WebElement.isSelected
 */
public class ElementStateCacheDecorator(
    private val cacheDisplayed: Boolean = true,
    private val cacheEnabled: Boolean = false,
    private val cacheSelected: Boolean = false,
) : AbstractDecorator() {
    override fun decorateDriver(driver: WebDriver): WebDriver {
        return object : WebDriver by driver {
            override fun findElement(by: By): WebElement {
                val element = driver.findElement(by)
                return decorateElement(element)
            }

            override fun findElements(by: By): List<WebElement> =
                driver.findElements(by).map { element ->
                    decorateElement(element)
                }
        }
    }

    override fun decorateElement(element: WebElement): WebElement {
        return object : WebElement by element {
            private var cachedIsDisplayed: Boolean? = null
            private var cachedIsEnabled: Boolean? = null
            private var cachedIsSelected: Boolean? = null

            override fun isDisplayed(): Boolean {
                if (cacheDisplayed) {
                    cachedIsDisplayed?.let { return it }
                }

                return element.isDisplayed.also {
                    if (it && cacheDisplayed) cachedIsDisplayed = it
                }
            }

            override fun isEnabled(): Boolean {
                if (cacheEnabled) {
                    cachedIsEnabled?.let { return it }
                }

                return element.isEnabled.also {
                    if (it && cacheEnabled) cachedIsEnabled = it
                }
            }

            override fun isSelected(): Boolean {
                if (cacheSelected) {
                    cachedIsSelected?.let { return it }
                }

                return element.isSelected.also {
                    if (it && cacheSelected) cachedIsSelected = it
                }
            }

            override fun findElement(by: By): WebElement {
                val foundElement = element.findElement(by)
                return decorateElement(foundElement)
            }

            override fun findElements(by: By): List<WebElement> =
                element.findElements(by).map { foundElement ->
                    decorateElement(foundElement)
                }
        }
    }
}

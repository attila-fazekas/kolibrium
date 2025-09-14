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

package dev.kolibrium.core.selenium.decorators

import dev.kolibrium.common.WebElements
import org.openqa.selenium.By
import org.openqa.selenium.SearchContext
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement

/**
 * Caches positive results for element state checks to reduce WebDriver calls.
 *
 * The decorator wraps elements so that repeated calls to [WebElement.isDisplayed], [WebElement.isEnabled]
 * and/or [WebElement.isSelected] can short‑circuit once a `true` value has been observed. Negative results
 * are never cached. Each decorated element instance keeps its own independent cache.
 *
 * Caveats
 * - Dynamic UIs that toggle state may make caches stale. Prefer leaving [cacheSelected] false unless you
 *   clear caches after interactions. Consider using this decorator only for read‑mostly states.
 *
 * @param cacheDisplayed Cache positive results from [WebElement.isDisplayed]. Default: true.
 * @param cacheEnabled Cache positive results from [WebElement.isEnabled]. Default: false.
 * @param cacheSelected Cache positive results from [WebElement.isSelected]. Default: false.
 * @see WebElement.isDisplayed
 * @see WebElement.isEnabled
 * @see WebElement.isSelected
 */
public class ElementStateCacheDecorator(
    private val cacheDisplayed: Boolean = true,
    private val cacheEnabled: Boolean = false,
    private val cacheSelected: Boolean = false,
) : AbstractDecorator() {
    override fun decorateSearchContext(context: SearchContext): SearchContext {
        return object : SearchContext by context {
            override fun findElement(by: By): WebElement {
                val element = context.findElement(by)
                return decorateElement(element)
            }

            override fun findElements(by: By): WebElements =
                context.findElements(by).map { element ->
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

            override fun findElements(by: By): WebElements =
                element.findElements(by).map { foundElement ->
                    decorateElement(foundElement)
                }
        }
    }
}

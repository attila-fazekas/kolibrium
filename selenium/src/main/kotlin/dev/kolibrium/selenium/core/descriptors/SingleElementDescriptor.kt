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

package dev.kolibrium.selenium.core.descriptors

import dev.kolibrium.selenium.core.InternalKolibriumApi
import dev.kolibrium.selenium.core.WaitConfig
import dev.kolibrium.selenium.core.WebElementDescriptor
import dev.kolibrium.selenium.core.defaultElementReadyCondition
import org.openqa.selenium.By
import org.openqa.selenium.SearchContext
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ui.FluentWait
import kotlin.reflect.KProperty

/**
 * Descriptor/delegate for lazily locating a single [org.openqa.selenium.WebElement].
 *
 * Supports optional caching of the located element, configurable waiting via [waitConfig],
 * and a per‑element readiness predicate via [readyWhen]. The element is searched using the
 * provided [locatorStrategy] against the decorated [org.openqa.selenium.SearchContext].
 *
 * Typical usage is via higher‑level helpers (e.g., `id`, `cssSelector`, and Appium’s
 * `accessibilityId`) which supply [value] and [locatorStrategy].
 *
 * @param searchCtx The base [org.openqa.selenium.SearchContext] to perform the lookup in (driver/page/screen or a nested element).
 * @param value The raw locator value (e.g., CSS, XPath, id) passed to [locatorStrategy]. Must be non‑blank.
 * @param locatorStrategy Function that converts [value] into a Selenium [org.openqa.selenium.By].
 * @param cacheLookup When true, cache the first resolved [org.openqa.selenium.WebElement] and reuse it until cache is cleared.
 * @param waitConfig Optional [WaitConfig] to control timeout/polling/ignored exceptions; when `null`,
 *        the effective configuration defaults to [dev.kolibrium.selenium.core.defaultWaitConfig].
 * @param readyWhen Optional predicate that defines when the found element is considered ready; when `null`,
 *        the effective predicate defaults to [defaultElementReadyCondition].
 */
@InternalKolibriumApi
public class SingleElementDescriptor(
    searchCtx: SearchContext,
    value: String,
    locatorStrategy: (String) -> By,
    private val cacheLookup: Boolean,
    override val waitConfig: WaitConfig?,
    override val readyWhen: (WebElement.() -> Boolean)?,
) : AbstractElementDescriptor<SingleElementDescriptor, WebElement>(searchCtx),
    WebElementDescriptor {
    init {
        require(value.isNotBlank()) { "\"value\" must not be blank" }
    }

    override val by: By = locatorStrategy(value)

    private val effectiveWaitConfig: WaitConfig = resolveWaitConfig(waitConfig)
    private val effectiveReady: WebElement.() -> Boolean = readyWhen ?: defaultElementReadyCondition

    private var cachedWebElement: WebElement? = null
    private val wait: FluentWait<SingleElementDescriptor> by lazy { initializeWait(effectiveWaitConfig) }

    override fun getValue(
        thisRef: Any?,
        property: KProperty<*>,
    ): WebElement = get()

    override fun get(): WebElement = getValueInternal(wait)

    override fun findElement(): WebElement =
        if (cacheLookup) {
            cachedWebElement ?: searchContext.findElement(by).also { cachedWebElement = it }
        } else {
            searchContext.findElement(by)
        }

    override fun clearCache() {
        cachedWebElement = null
    }

    override fun isElementReady(element: WebElement): Boolean = element.effectiveReady()

    override fun toString(): String =
        buildDescriptorString(
            descriptorName = "ElementDescriptor",
            by = by,
            waitConfig = effectiveWaitConfig,
            cacheLookup = cacheLookup,
        )
}

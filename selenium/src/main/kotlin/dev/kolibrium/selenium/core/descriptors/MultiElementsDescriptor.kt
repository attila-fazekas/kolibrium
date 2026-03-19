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
import dev.kolibrium.selenium.core.WebElements
import dev.kolibrium.selenium.core.WebElementsDescriptor
import dev.kolibrium.selenium.core.defaultElementsReadyCondition
import org.openqa.selenium.By
import org.openqa.selenium.SearchContext
import org.openqa.selenium.support.ui.FluentWait
import kotlin.reflect.KProperty

/**
 * Descriptor/delegate for lazily locating [WebElements].
 *
 * Multi‑element delegates are intentionally non‑caching: each access performs a fresh lookup to
 * reflect the current DOM/UI state. Waiting behavior and readiness can be customized through
 * [waitConfig] and [readyWhen] respectively.
 *
 * Typical usage is via higher‑level helpers (e.g., `xpaths`, `cssSelectors`).
 *
 * @param searchCtx The base [org.openqa.selenium.SearchContext] to perform the lookup in (driver/page/screen or a nested element).
 * @param value The raw locator value (e.g., CSS, XPath, id) passed to [locatorStrategy]. Must be non‑blank.
 * @param locatorStrategy Function that converts [value] into a Selenium [org.openqa.selenium.By].
 * @param waitConfig Optional [WaitConfig] to control timeout/polling/ignored exceptions; when `null`,
 *        the effective configuration defaults to [dev.kolibrium.selenium.core.defaultWaitConfig].
 * @param readyWhen Optional predicate that defines when the found elements are considered ready; when `null`,
 *        the effective predicate defaults to [defaultElementsReadyCondition].
 */
@InternalKolibriumApi
public class MultiElementsDescriptor(
    searchCtx: SearchContext,
    value: String,
    locatorStrategy: (String) -> By,
    override val waitConfig: WaitConfig?,
    override val readyWhen: (WebElements.() -> Boolean)?,
) : AbstractElementDescriptor<MultiElementsDescriptor, WebElements>(searchCtx),
    WebElementsDescriptor {
    init {
        require(value.isNotBlank()) { "\"value\" must not be blank" }
    }

    override val by: By = locatorStrategy(value)

    private val effectiveWaitConfig: WaitConfig = resolveWaitConfig(waitConfig)
    private val effectiveReady: WebElements.() -> Boolean = readyWhen ?: defaultElementsReadyCondition

    private val wait: FluentWait<MultiElementsDescriptor> by lazy { initializeWait(effectiveWaitConfig) }

    override fun getValue(
        thisRef: Any?,
        property: KProperty<*>,
    ): WebElements = get()

    override fun get(): WebElements = getValueInternal(wait)

    override fun findElement(): WebElements = searchContext.findElements(by)

    override fun clearCache() { /* no-op: multi-element delegates are not cached */ }

    override fun isElementReady(element: WebElements): Boolean = element.effectiveReady()

    override fun toString(): String =
        buildDescriptorString(
            descriptorName = "ElementsDescriptor",
            by = by,
            waitConfig = effectiveWaitConfig,
        )
}

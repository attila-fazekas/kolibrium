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
 * Descriptor/delegate for lazily locating [WebElements] using a pre‑built [org.openqa.selenium.By].
 *
 * Unlike [MultiElementsDescriptor], this class accepts a fully constructed [org.openqa.selenium.By] instance directly,
 * making it suitable for composite locators produced by [org.openqa.selenium.support.pagefactory.ByChained],
 * [org.openqa.selenium.support.pagefactory.ByAll], or any other custom [org.openqa.selenium.By] subclass.
 *
 * Multi‑element delegates are intentionally non‑caching: each access performs a fresh lookup to
 * reflect the current DOM/UI state.
 *
 * @param searchCtx The base [org.openqa.selenium.SearchContext] to perform the lookup in (driver/page/screen or a nested element).
 * @param by The pre‑built Selenium [org.openqa.selenium.By] locator to use for element lookup.
 * @param waitConfig Optional [WaitConfig] to control timeout/polling/ignored exceptions; when `null`,
 *        the effective configuration defaults to [dev.kolibrium.selenium.core.defaultWaitConfig].
 * @param readyWhen Optional predicate that defines when the found elements are considered ready; when `null`,
 *        the effective predicate defaults to [defaultElementsReadyCondition].
 */
@InternalKolibriumApi
public class CompositeElementsDescriptor(
    searchCtx: SearchContext,
    override val by: By,
    override val waitConfig: WaitConfig?,
    override val readyWhen: (WebElements.() -> Boolean)?,
) : AbstractElementDescriptor<CompositeElementsDescriptor, WebElements>(searchCtx),
    WebElementsDescriptor {
    private val effectiveWaitConfig: WaitConfig = resolveWaitConfig(waitConfig)
    private val effectiveReady: WebElements.() -> Boolean = readyWhen ?: defaultElementsReadyCondition

    private val wait: FluentWait<CompositeElementsDescriptor> by lazy { initializeWait(effectiveWaitConfig) }

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
            descriptorName = "CompositeElementsDescriptor",
            by = by,
            waitConfig = effectiveWaitConfig,
        )
}

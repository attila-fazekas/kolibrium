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

package dev.kolibrium.webdriver.descriptors

import dev.kolibrium.annotations.InternalKolibriumApi
import dev.kolibrium.webdriver.WaitConfig
import dev.kolibrium.webdriver.WebElements
import dev.kolibrium.webdriver.WebElementsDescriptor
import org.openqa.selenium.By
import org.openqa.selenium.SearchContext
import org.openqa.selenium.support.ui.FluentWait
import kotlin.reflect.KProperty

/**
 * Descriptor/delegate for lazily locating [WebElements] using a pre‑built [By].
 *
 * Unlike [MultiElementsDescriptor], this class accepts a fully constructed [By] instance directly,
 * making it suitable for composite locators produced by [org.openqa.selenium.support.pagefactory.ByChained],
 * [org.openqa.selenium.support.pagefactory.ByAll], or any other custom [By] subclass.
 *
 * Multi‑element delegates are intentionally non‑caching: each access performs a fresh lookup to
 * reflect the current DOM/UI state.
 *
 * @param searchCtx The base [SearchContext] to perform the lookup in (driver/page/screen or a nested element).
 * @param by The pre‑built Selenium [By] locator to use for element lookup.
 * @param waitConfig [WaitConfig] to control timeout/polling/ignored exceptions.
 * @param readyWhen Predicate that defines when the found elements are considered ready.
 */
@InternalKolibriumApi
public open class CompositeElementsDescriptor(
    searchCtx: SearchContext,
    override val by: By,
    waitConfig: WaitConfig,
    private val readyWhen: WebElements.() -> Boolean,
) : AbstractElementDescriptor<CompositeElementsDescriptor, WebElements>(searchCtx),
    WebElementsDescriptor {
    protected val effectiveWaitConfig: WaitConfig = ensureNoSuchElementIgnored(waitConfig)

    private val wait: FluentWait<CompositeElementsDescriptor> by lazy { initializeWait(effectiveWaitConfig) }

    override fun getValue(
        thisRef: Any?,
        property: KProperty<*>,
    ): WebElements = get()

    override fun get(): WebElements = getValueInternal(wait)

    override fun findElement(): WebElements = searchContext.findElements(by)

    override fun clearCache() { /* no-op: multi-element delegates are not cached */ }

    override fun isElementReady(element: WebElements): Boolean = element.readyWhen()

    override fun toString(): String =
        buildDescriptorString(
            descriptorName = "CompositeElementsDescriptor",
            by = by,
            waitConfig = effectiveWaitConfig,
        )
}

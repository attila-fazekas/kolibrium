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

import dev.kolibrium.webdriver.InternalKolibriumApi
import dev.kolibrium.webdriver.WaitConfig
import dev.kolibrium.webdriver.WebElementDescriptor
import org.openqa.selenium.By
import org.openqa.selenium.SearchContext
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ui.FluentWait
import kotlin.reflect.KProperty

/**
 * Descriptor/delegate for lazily locating a single [WebElement] using a pre‑built [By].
 *
 * Unlike [SingleElementDescriptor], this class accepts a fully constructed [By] instance directly,
 * making it suitable for composite locators produced by [org.openqa.selenium.support.pagefactory.ByChained],
 * [org.openqa.selenium.support.pagefactory.ByAll], or any other custom [By] subclass.
 *
 * Supports optional caching of the located element, configurable waiting via [waitConfig],
 * and a per‑element readiness predicate via [readyWhen].
 *
 * @param searchCtx The base [SearchContext] to perform the lookup in (driver/page/screen or a nested element).
 * @param by The pre‑built Selenium [By] locator to use for element lookup.
 * @param cacheLookup When true, cache the first resolved [WebElement] and reuse it until cache is cleared.
 * @param waitConfig [WaitConfig] to control timeout/polling/ignored exceptions.
 * @param readyWhen Predicate that defines when the found element is considered ready.
 */
@InternalKolibriumApi
public open class CompositeElementDescriptor(
    searchCtx: SearchContext,
    override val by: By,
    private val cacheLookup: Boolean,
    waitConfig: WaitConfig,
    private val readyWhen: WebElement.() -> Boolean,
) : AbstractElementDescriptor<CompositeElementDescriptor, WebElement>(searchCtx),
    WebElementDescriptor {
    private val effectiveWaitConfig: WaitConfig = ensureNoSuchElementIgnored(waitConfig)

    private var cachedWebElement: WebElement? = null
    private val wait: FluentWait<CompositeElementDescriptor> by lazy { initializeWait(effectiveWaitConfig) }

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

    override fun isElementReady(element: WebElement): Boolean = element.readyWhen()

    override fun toString(): String =
        buildDescriptorString(
            descriptorName = "CompositeElementDescriptor",
            by = by,
            waitConfig = effectiveWaitConfig,
            cacheLookup = cacheLookup,
        )
}

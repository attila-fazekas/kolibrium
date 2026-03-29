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

import dev.kolibrium.selenium.core.decorators.AbstractDecorator
import dev.kolibrium.selenium.core.decorators.DecoratorResolver
import dev.kolibrium.webdriver.InternalKolibriumApi
import dev.kolibrium.webdriver.WaitConfig
import dev.kolibrium.webdriver.descriptors.CompositeElementDescriptor
import org.openqa.selenium.By
import org.openqa.selenium.SearchContext
import org.openqa.selenium.WebElement

/**
 * Decorator-aware variant of [CompositeElementDescriptor].
 *
 * Merges site‑level and test‑level decorators (via [DecoratorResolver]) and applies them to the
 * underlying [SearchContext] before element lookup.
 */
@InternalKolibriumApi
public class DecoratedCompositeElementDescriptor(
    searchCtx: SearchContext,
    by: By,
    cacheLookup: Boolean,
    waitConfig: WaitConfig,
    readyWhen: WebElement.() -> Boolean,
    siteLevelDecorators: List<AbstractDecorator>,
) : CompositeElementDescriptor(searchCtx, by, cacheLookup, waitConfig, readyWhen) {
    private val decoratorResolver = DecoratorResolver(siteLevelDecorators)

    override val searchContext: SearchContext by lazy { decoratorResolver.decorateSearchContext(searchCtx) }

    override fun toString(): String =
        buildDescriptorString(
            descriptorName = "CompositeElementDescriptor",
            by = by,
            waitConfig = effectiveWaitConfig,
            cacheLookup = cacheLookup,
            decoratorClassNames = decoratorResolver.decoratorClassNames(),
        )
}

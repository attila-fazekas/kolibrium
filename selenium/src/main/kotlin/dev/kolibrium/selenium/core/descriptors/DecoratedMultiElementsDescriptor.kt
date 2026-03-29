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
import dev.kolibrium.webdriver.WebElements
import dev.kolibrium.webdriver.descriptors.MultiElementsDescriptor
import org.openqa.selenium.By
import org.openqa.selenium.SearchContext

/**
 * Decorator-aware variant of [MultiElementsDescriptor].
 *
 * Merges site‑level and test‑level decorators (via [DecoratorResolver]) and applies them to the
 * underlying [SearchContext] before element lookup.
 */
@InternalKolibriumApi
public class DecoratedMultiElementsDescriptor(
    searchCtx: SearchContext,
    value: String,
    locatorStrategy: (String) -> By,
    waitConfig: WaitConfig,
    readyWhen: WebElements.() -> Boolean,
    siteLevelDecorators: List<AbstractDecorator> = emptyList(),
) : MultiElementsDescriptor(searchCtx, value, locatorStrategy, waitConfig, readyWhen) {
    private val decoratorResolver = DecoratorResolver(siteLevelDecorators)

    override val searchContext: SearchContext by lazy { decoratorResolver.decorateSearchContext(searchCtx) }

    override fun decoratorClassNames(): List<String> = decoratorResolver.decoratorClassNames()
}

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

package dev.kolibrium.selenium.core.decorators

import org.openqa.selenium.SearchContext

/**
 * Encapsulates the logic for merging site‑level and test‑level decorators, applying them to a
 * [SearchContext], and tracking which decorator class names were applied.
 *
 * Site‑level decorators are provided at construction; test‑level decorators are read from
 * [DecoratorManager] at resolution time. Merge order: site first, then test; de‑duplicated by
 * class with test‑level winning on conflicts.
 */
internal class DecoratorResolver(
    private val siteLevelDecorators: List<AbstractDecorator>,
) {
    var appliedDecoratorClassNames: List<String> = emptyList()
        private set

    fun decorateSearchContext(searchCtx: SearchContext): SearchContext {
        val merged = mergedDecorators()
        appliedDecoratorClassNames = merged.map { it::class.java.simpleName }
        return if (merged.isEmpty()) searchCtx else DecoratorManager.combine(merged)(searchCtx)
    }

    fun decoratorClassNames(): List<String> = appliedDecoratorClassNames.ifEmpty { mergedDecorators().map { it::class.java.simpleName } }

    private fun mergedDecorators(): List<AbstractDecorator> {
        val testLevelDecorators = DecoratorManager.getAllDecorators()
        val siteDedup = siteLevelDecorators.distinctBy { it::class }
        val testDedup = testLevelDecorators.distinctBy { it::class }
        val testClasses = testDedup.map { it::class }.toSet()
        val siteFiltered = siteDedup.filter { it::class !in testClasses }
        return siteFiltered + testDedup
    }
}

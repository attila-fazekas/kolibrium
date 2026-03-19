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
import dev.kolibrium.selenium.core.SessionContext
import dev.kolibrium.selenium.core.WaitConfig
import dev.kolibrium.selenium.core.configureWith
import dev.kolibrium.selenium.core.decorators.AbstractDecorator
import dev.kolibrium.selenium.core.decorators.DecoratorManager
import dev.kolibrium.selenium.core.defaultWaitConfig
import org.openqa.selenium.By
import org.openqa.selenium.NoSuchElementException
import org.openqa.selenium.SearchContext
import org.openqa.selenium.StaleElementReferenceException
import org.openqa.selenium.support.ui.FluentWait

/**
 * Base implementation for element locator delegates with built‑in waiting, caching and decoration.
 *
 * This abstract class powers Kolibrium's locator delegates by:
 * - Merging site‑level and test‑level decorators and applying them to the underlying [org.openqa.selenium.SearchContext]
 * - Providing a consistent wait loop via Selenium's [org.openqa.selenium.support.ui.FluentWait], configured by [WaitConfig]
 * - Ignoring transient lookup errors: always ignores [org.openqa.selenium.NoSuchElementException] and handles
 *   [org.openqa.selenium.StaleElementReferenceException] by clearing cache and retrying
 *
 * Subclasses implement how to locate the element(s) ([findElement]), how to drop any cache
 * ([clearCache]), and how to determine element readiness ([isElementReady]).
 *
 * @param T The self type of the concrete descriptor (used to type the [org.openqa.selenium.support.ui.FluentWait]).
 * @param R The result element type returned by this descriptor (e.g., [org.openqa.selenium.WebElement] or [dev.kolibrium.selenium.core.WebElements]).
 * @param searchCtx The original Selenium [org.openqa.selenium.SearchContext] used as the base for all lookups. It may be
 * decorated with site/test decorators before actual searching occurs.
 */
@InternalKolibriumApi
public abstract class AbstractElementDescriptor<T : AbstractElementDescriptor<T, R>, R>(
    protected val searchCtx: SearchContext,
) {
    protected var appliedDecoratorClassNames: List<String> = emptyList()

    protected val searchContext: SearchContext by lazy {
        val merged = mergedDecorators()
        appliedDecoratorClassNames = merged.map { it::class.java.simpleName }
        if (merged.isEmpty()) {
            searchCtx
        } else {
            DecoratorManager.combine(merged)(searchCtx)
        }
    }

    protected fun mergedDecoratorClassNames(): List<String> = mergedDecorators().map { it::class.java.simpleName }

    protected abstract fun findElement(): R

    protected abstract fun clearCache()

    protected abstract fun isElementReady(element: R): Boolean

    protected fun initializeWait(waitConfig: WaitConfig): FluentWait<T> =
        @Suppress("UNCHECKED_CAST")
        FluentWait(this as T).configureWith(waitConfig)

    protected fun getValueInternal(wait: FluentWait<T>): R {
        var last: R? = null
        wait.until {
            try {
                val element = findElement()
                last = element
                isElementReady(element)
            } catch (_: StaleElementReferenceException) {
                clearCache()
                last = null
                false
            }
        }
        return last ?: findElement()
    }

    // Returns a non-empty class identifier for the given instance, even for anonymous/proxy classes.
    protected fun classNameOf(obj: Any): String {
        val cls = obj.javaClass
        val simple = cls.simpleName
        if (simple.isNotBlank()) return simple.substringBefore('$')
        val name = cls.name
        if (name.isNotBlank()) return name.substringAfterLast('.').substringBefore('$')
        // Fallback to object's toString() prefix; should never be blank.
        return obj
            .toString()
            .substringBefore('@')
            .substringBefore('$')
            .ifBlank { "UnknownContext" }
    }

    // Ensure a minimal baseline of ignored exceptions regardless of caller's WaitConfig
    // Specifically, always ignore NoSuchElementException so waits don't fail on the first miss
    protected fun ensureNoSuchElementIgnored(wait: WaitConfig): WaitConfig =
        if (NoSuchElementException::class in wait.ignoring) {
            wait
        } else {
            wait.copy(ignoring = wait.ignoring + NoSuchElementException::class)
        }

    protected fun resolveWaitConfig(waitConfig: WaitConfig?): WaitConfig = ensureNoSuchElementIgnored(waitConfig ?: defaultWaitConfig)

    protected fun buildDescriptorString(
        descriptorName: String,
        by: By,
        waitConfig: WaitConfig,
        cacheLookup: Boolean? = null,
    ): String {
        val ctxName = classNameOf(searchCtx)
        val timeoutStr = waitConfig.timeout?.toString() ?: "N/A"
        val pollingStr = waitConfig.pollingInterval?.toString() ?: "N/A"
        val decorators = appliedDecoratorClassNames.ifEmpty { mergedDecoratorClassNames() }
        val decoratorsStr = if (decorators.isEmpty()) "N/A" else decorators.joinToString(prefix = "[", postfix = "]")
        val cacheStr = if (cacheLookup != null) ", cacheLookup=$cacheLookup" else ""
        return "$descriptorName(ctx=$ctxName, by=$by$cacheStr, waitConfig=(timeout=$timeoutStr, " +
            "polling=$pollingStr), decorators=$decoratorsStr)"
    }

    // Merge decorators deterministically: site first, then test; de-duplicate by class with test-level winning on conflicts.
    private fun mergedDecorators(): List<AbstractDecorator> {
        val siteLevelDecorators = SessionContext.get()?.site?.decorators ?: emptyList()
        val testLevelDecorators = DecoratorManager.getAllDecorators()
        val siteDedup = siteLevelDecorators.distinctBy { it::class }
        val testDedup = testLevelDecorators.distinctBy { it::class }
        val testClasses = testDedup.map { it::class }.toSet()
        val siteFiltered = siteDedup.filter { it::class !in testClasses }
        return siteFiltered + testDedup
    }
}

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
import dev.kolibrium.webdriver.configureWith
import org.openqa.selenium.By
import org.openqa.selenium.NoSuchElementException
import org.openqa.selenium.SearchContext
import org.openqa.selenium.StaleElementReferenceException
import org.openqa.selenium.support.ui.FluentWait

/**
 * Base implementation for element locator delegates with built‑in waiting.
 *
 * This abstract class powers Kolibrium's locator delegates by:
 * - Providing a consistent wait loop via Selenium's [FluentWait], configured by [WaitConfig]
 * - Ignoring transient lookup errors: always ignores [NoSuchElementException] and handles
 *   [StaleElementReferenceException] by clearing cache and retrying
 *
 * Subclasses implement how to locate the element(s) ([findElement]), how to drop any cache
 * ([clearCache]), and how to determine element readiness ([isElementReady]).
 *
 * @param T The self type of the concrete descriptor (used to type the [FluentWait]).
 * @param R The result element type returned by this descriptor (e.g., [org.openqa.selenium.WebElement] or [dev.kolibrium.webdriver.WebElements]).
 * @param searchCtx The original Selenium [SearchContext] used as the base for all lookups.
 */
@InternalKolibriumApi
public abstract class AbstractElementDescriptor<T : AbstractElementDescriptor<T, R>, R>(
    protected val searchCtx: SearchContext,
) {
    protected open val searchContext: SearchContext get() = searchCtx

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

    protected fun classNameOf(obj: Any): String {
        val cls = obj.javaClass
        val simple = cls.simpleName
        if (simple.isNotBlank()) return simple.substringBefore('$')
        val name = cls.name
        if (name.isNotBlank()) return name.substringAfterLast('.').substringBefore('$')
        return obj
            .toString()
            .substringBefore('@')
            .substringBefore('$')
            .ifBlank { "UnknownContext" }
    }

    protected fun ensureNoSuchElementIgnored(wait: WaitConfig): WaitConfig =
        if (NoSuchElementException::class in wait.ignoring) {
            wait
        } else {
            wait.copy(ignoring = wait.ignoring + NoSuchElementException::class)
        }

    protected fun buildDescriptorString(
        descriptorName: String,
        by: By,
        waitConfig: WaitConfig,
        cacheLookup: Boolean? = null,
        decoratorClassNames: List<String> = emptyList(),
    ): String {
        val ctxName = classNameOf(searchCtx)
        val timeoutStr = waitConfig.timeout?.toString() ?: "N/A"
        val pollingStr = waitConfig.pollingInterval?.toString() ?: "N/A"
        val decoratorsStr =
            if (decoratorClassNames.isEmpty()) "N/A" else decoratorClassNames.joinToString(prefix = "[", postfix = "]")
        val cacheStr = if (cacheLookup != null) ", cacheLookup=$cacheLookup" else ""
        return "$descriptorName(ctx=$ctxName, by=$by$cacheStr, waitConfig=(timeout=$timeoutStr, " +
            "polling=$pollingStr), decorators=$decoratorsStr)"
    }
}

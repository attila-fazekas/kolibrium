/*
 * Copyright 2023-2024 Attila Fazekas & contributors
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

package dev.kolibrium.selenium

import dev.kolibrium.core.WebElements
import dev.kolibrium.dsl.selenium.wait.SyncConfig
import dev.kolibrium.dsl.selenium.wait.WebElementSyncConfig
import dev.kolibrium.dsl.selenium.wait.WebElementsSyncConfig
import io.github.oshai.kotlinlogging.KotlinLogging
import org.openqa.selenium.By
import org.openqa.selenium.SearchContext
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ByIdOrName
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

private typealias WebElementProperty = ReadOnlyProperty<Any?, WebElement>
private typealias WebElementsProperty = ReadOnlyProperty<Any?, WebElements>

/**
 * Creates a property delegate that lazily finds an element using the className locator strategy.
 *
 * This function returns a property delegate that, when accessed, finds a web element
 * with the specified class name. The element lookup behavior can be configured to either
 * cache the initially found element or perform a fresh lookup each time.
 *
 * @param locator The value of the "class" attribute to search for. If multiple classes are
 *                specified, the element must have all of them to match.
 * @param cacheLookup If true (default), the element will be looked up only once and cached for
 *                    subsequent accesses, similarly to Page Factory's @CacheLookup annotation.
 *                    If false, a new lookup will be performed each time the element is accessed.
 * @param syncConfig A lambda with receiver to configure the synchronization behavior.
 *                   Default is an empty lambda, which uses the default synchronization settings.
 * @receiver The SearchContext instance used to search for the element.
 * @return A [ReadOnlyProperty] delegate that provides a [WebElement] when accessed.
 *
 * @see SyncConfig
 * @see WebElementProperty
 */
public fun SearchContext.className(
    locator: String,
    cacheLookup: Boolean = true,
    syncConfig: (SyncConfig<WebElement>.() -> Unit) = {},
): WebElementProperty = genericLocator(locator, By::className, cacheLookup, syncConfig)

/**
 * Creates a property delegate that lazily finds multiple elements using the className locator strategy.
 *
 * This function returns a property delegate that, when accessed, finds all web elements
 * with the specified class name. The elements lookup behavior can be configured to either
 * cache the initially found elements or perform a fresh lookup each time.
 *
 * @param locator The value of the "class" attribute to search for. If multiple classes are
 *                specified, elements must have all of them to match.
 * @param cacheLookup If true (default), the elements will be looked up only once and cached for
 *                    subsequent accesses, similarly to Page Factory's @CacheLookup annotation.
 *                    If false, a new lookup will be performed each time the elements are accessed.
 * @param syncConfig A lambda with receiver to configure the synchronization behavior.
 *                   Default is an empty lambda, which uses the default synchronization settings.
 * @receiver The SearchContext instance used to search for the elements.
 * @return A [ReadOnlyProperty] delegate that provides a [WebElements] collection when accessed.
 *
 * @see SyncConfig
 * @see WebElements
 */
public fun SearchContext.classNames(
    locator: String,
    cacheLookup: Boolean = true,
    syncConfig: (SyncConfig<WebElements>.() -> Unit) = {},
): WebElementsProperty = genericLocator(locator, By::className, cacheLookup, syncConfig)

/**
 * Creates a property delegate that lazily finds an element using the CSS selector locator strategy.
 *
 * This function returns a property delegate that, when accessed, finds a web element
 * matching the specified CSS selector. The element lookup behavior can be configured to either
 * cache the initially found element or perform a fresh lookup each time.
 *
 * @param locator The CSS selector to locate the element.
 * @param cacheLookup If true (default), the element will be looked up only once and cached for
 *                    subsequent accesses, similarly to Page Factory's @CacheLookup annotation.
 *                    If false, a new lookup will be performed each time the element is accessed.
 * @param syncConfig A lambda with receiver to configure the synchronization behavior.
 *                   Default is an empty lambda, which uses the default synchronization settings.
 * @receiver The SearchContext instance used to search for the element.
 * @return A [ReadOnlyProperty] delegate that provides a [WebElement] when accessed.
 *
 * @see SyncConfig
 * @see WebElementProperty
 */
public fun SearchContext.cssSelector(
    locator: String,
    cacheLookup: Boolean = true,
    syncConfig: SyncConfig<WebElement>.() -> Unit = {},
): WebElementProperty = genericLocator(locator, By::cssSelector, cacheLookup, syncConfig)

/**
 * Creates a property delegate that lazily finds multiple elements using the CSS selector locator strategy.
 *
 * This function returns a property delegate that, when accessed, finds all web elements
 * matching the specified CSS selector. The elements lookup behavior can be configured to either
 * cache the initially found elements or perform a fresh lookup each time.
 *
 * @param locator The CSS selector to locate the elements.
 * @param cacheLookup If true (default), the elements will be looked up only once and cached for
 *                    subsequent accesses, similarly to Page Factory's @CacheLookup annotation.
 *                    If false, a new lookup will be performed each time the elements are accessed.
 * @param syncConfig A lambda with receiver to configure the synchronization behavior.
 *                   Default is an empty lambda, which uses the default synchronization settings.
 * @receiver The SearchContext instance used to search for the elements.
 * @return A [ReadOnlyProperty] delegate that provides a [WebElements] collection when accessed.
 *
 * @see SyncConfig
 * @see WebElements
 */
public fun SearchContext.cssSelectors(
    locator: String,
    cacheLookup: Boolean = true,
    syncConfig: SyncConfig<WebElements>.() -> Unit = {},
): WebElementsProperty = genericLocator(locator, By::cssSelector, cacheLookup, syncConfig)

/**
 * Creates a property delegate that lazily finds an element using the id locator strategy.
 *
 * This function returns a property delegate that, when accessed, finds a web element
 * with the specified id attribute. The element lookup behavior can be configured to either
 * cache the initially found element or perform a fresh lookup each time.
 *
 * @param locator The value of the "id" attribute to search for.
 * @param cacheLookup If true (default), the element will be looked up only once and cached for
 *                    subsequent accesses, similarly to Page Factory's @CacheLookup annotation.
 *                    If false, a new lookup will be performed each time the element is accessed.
 * @param syncConfig A lambda with receiver to configure the synchronization behavior.
 *                   Default is an empty lambda, which uses the default synchronization settings.
 * @receiver The SearchContext instance used to search for the element.
 * @return A [ReadOnlyProperty] delegate that provides a [WebElement] when accessed.
 *
 * @see SyncConfig
 * @see WebElementProperty
 */
public fun SearchContext.id(
    locator: String,
    cacheLookup: Boolean = true,
    syncConfig: SyncConfig<WebElement>.() -> Unit = {},
): WebElementProperty = genericLocator(locator, By::id, cacheLookup, syncConfig)

/**
 * Creates a property delegate that lazily finds an element using either the id or name locator strategy.
 *
 * This function returns a property delegate that, when accessed, finds a web element
 * with the specified id or name attribute. The element lookup behavior can be configured to either
 * cache the initially found element or perform a fresh lookup each time.
 *
 * @param locator The value of either the "id" or "name" attribute to search for.
 * @param cacheLookup If true (default), the element will be looked up only once and cached for
 *                    subsequent accesses, similarly to Page Factory's @CacheLookup annotation.
 *                    If false, a new lookup will be performed each time the element is accessed.
 * @param syncConfig A lambda with receiver to configure the synchronization behavior.
 *                   Default is an empty lambda, which uses the default synchronization settings.
 * @receiver The SearchContext instance used to search for the element.
 * @return A [ReadOnlyProperty] delegate that provides a [WebElement] when accessed.
 *
 * @see SyncConfig
 * @see WebElementProperty
 * @see ByIdOrName
 */
public fun SearchContext.idOrName(
    locator: String,
    cacheLookup: Boolean = true,
    syncConfig: SyncConfig<WebElement>.() -> Unit = {},
): WebElementProperty = genericLocator(locator, ::ByIdOrName, cacheLookup, syncConfig)

/**
 * Creates a property delegate that lazily finds an element using the link text locator strategy.
 *
 * This function returns a property delegate that, when accessed, finds a web element
 * with the specified link text. The element lookup behavior can be configured to either
 * cache the initially found element or perform a fresh lookup each time.
 *
 * @param locator The exact text of the link to search for.
 * @param cacheLookup If true (default), the element will be looked up only once and cached for
 *                    subsequent accesses, similarly to Page Factory's @CacheLookup annotation.
 *                    If false, a new lookup will be performed each time the element is accessed.
 * @param syncConfig A lambda with receiver to configure the synchronization behavior.
 *                   Default is an empty lambda, which uses the default synchronization settings.
 * @receiver The SearchContext instance used to search for the element.
 * @return A [ReadOnlyProperty] delegate that provides a [WebElement] when accessed.
 *
 * @see SyncConfig
 * @see WebElementProperty
 */
public fun SearchContext.linkText(
    locator: String,
    cacheLookup: Boolean = true,
    syncConfig: SyncConfig<WebElement>.() -> Unit = {},
): WebElementProperty = genericLocator(locator, By::linkText, cacheLookup, syncConfig)

/**
 * Creates a property delegate that lazily finds multiple elements using the link text locator strategy.
 *
 * This function returns a property delegate that, when accessed, finds all web elements
 * with the specified link text. The elements lookup behavior can be configured to either
 * cache the initially found elements or perform a fresh lookup each time.
 *
 * @param locator The exact text of the links to search for.
 * @param cacheLookup If true (default), the elements will be looked up only once and cached for
 *                    subsequent accesses, similarly to Page Factory's @CacheLookup annotation.
 *                    If false, a new lookup will be performed each time the elements are accessed.
 * @param syncConfig A lambda with receiver to configure the synchronization behavior.
 *                   Default is an empty lambda, which uses the default synchronization settings.
 * @receiver The SearchContext instance used to search for the elements.
 * @return A [ReadOnlyProperty] delegate that provides a [WebElements] collection when accessed.
 *
 * @see SyncConfig
 * @see WebElements
 */
public fun SearchContext.linkTexts(
    locator: String,
    cacheLookup: Boolean = true,
    syncConfig: SyncConfig<WebElements>.() -> Unit = {},
): WebElementsProperty = genericLocator(locator, By::linkText, cacheLookup, syncConfig)

/**
 * Creates a property delegate that lazily finds an element using the name locator strategy.
 *
 * This function returns a property delegate that, when accessed, finds a web element
 * with the specified name attribute. The element lookup behavior can be configured to either
 * cache the initially found element or perform a fresh lookup each time.
 *
 * @param locator The value of the "name" attribute to search for.
 * @param cacheLookup If true (default), the element will be looked up only once and cached for
 *                    subsequent accesses, similarly to Page Factory's @CacheLookup annotation.
 *                    If false, a new lookup will be performed each time the element is accessed.
 * @param syncConfig A lambda with receiver to configure the synchronization behavior.
 *                   Default is an empty lambda, which uses the default synchronization settings.
 * @receiver The SearchContext instance used to search for the element.
 * @return A [ReadOnlyProperty] delegate that provides a [WebElement] when accessed.
 *
 * @see SyncConfig
 * @see WebElementProperty
 */
public fun SearchContext.name(
    locator: String,
    cacheLookup: Boolean = true,
    syncConfig: SyncConfig<WebElement>.() -> Unit = {},
): WebElementProperty = genericLocator(locator, By::name, cacheLookup, syncConfig)

/**
 * Creates a property delegate that lazily finds multiple elements using the name locator strategy.
 *
 * This function returns a property delegate that, when accessed, finds all web elements
 * with the specified name attribute. The elements lookup behavior can be configured to either
 * cache the initially found elements or perform a fresh lookup each time.
 *
 * @param locator The value of the "name" attribute to search for.
 * @param cacheLookup If true (default), the elements will be looked up only once and cached for
 *                    subsequent accesses, similarly to Page Factory's @CacheLookup annotation.
 *                    If false, a new lookup will be performed each time the elements are accessed.
 * @param syncConfig A lambda with receiver to configure the synchronization behavior.
 *                   Default is an empty lambda, which uses the default synchronization settings.
 * @receiver The SearchContext instance used to search for the elements.
 * @return A [ReadOnlyProperty] delegate that provides a [WebElements] collection when accessed.
 *
 * @see SyncConfig
 * @see WebElements
 */
public fun SearchContext.names(
    locator: String,
    cacheLookup: Boolean = true,
    syncConfig: SyncConfig<WebElements>.() -> Unit = {},
): WebElementsProperty = genericLocator(locator, By::name, cacheLookup, syncConfig)

/**
 * Creates a property delegate that lazily finds an element using the partial link text locator strategy.
 *
 * This function returns a property delegate that, when accessed, finds a web element
 * containing the specified partial link text. The element lookup behavior can be configured to either
 * cache the initially found element or perform a fresh lookup each time.
 *
 * @param locator The partial text of the link to search for.
 * @param cacheLookup If true (default), the element will be looked up only once and cached for
 *                    subsequent accesses, similarly to Page Factory's @CacheLookup annotation.
 *                    If false, a new lookup will be performed each time the element is accessed.
 * @param syncConfig A lambda with receiver to configure the synchronization behavior.
 *                   Default is an empty lambda, which uses the default synchronization settings.
 * @receiver The SearchContext instance used to search for the element.
 * @return A [ReadOnlyProperty] delegate that provides a [WebElement] when accessed.
 *
 * @see SyncConfig
 * @see WebElementProperty
 */
public fun SearchContext.partialLinkText(
    locator: String,
    cacheLookup: Boolean = true,
    syncConfig: SyncConfig<WebElement>.() -> Unit = {},
): WebElementProperty = genericLocator(locator, By::partialLinkText, cacheLookup, syncConfig)

/**
 * Creates a property delegate that lazily finds multiple elements using the partial link text locator strategy.
 *
 * This function returns a property delegate that, when accessed, finds all web elements
 * containing the specified partial link text. The elements lookup behavior can be configured to either
 * cache the initially found elements or perform a fresh lookup each time.
 *
 * @param locator The partial text of the links to search for.
 * @param cacheLookup If true (default), the elements will be looked up only once and cached for
 *                    subsequent accesses, similarly to Page Factory's @CacheLookup annotation.
 *                    If false, a new lookup will be performed each time the elements are accessed.
 * @param syncConfig A lambda with receiver to configure the synchronization behavior.
 *                   Default is an empty lambda, which uses the default synchronization settings.
 * @receiver The SearchContext instance used to search for the elements.
 * @return A [ReadOnlyProperty] delegate that provides a [WebElements] collection when accessed.
 *
 * @see SyncConfig
 * @see WebElements
 */
public fun SearchContext.partialLinkTexts(
    locator: String,
    cacheLookup: Boolean = true,
    syncConfig: SyncConfig<WebElements>.() -> Unit = {},
): WebElementsProperty = genericLocator(locator, By::partialLinkText, cacheLookup, syncConfig)

/**
 * Creates a property delegate that lazily finds an element using the tag name locator strategy.
 *
 * This function returns a property delegate that, when accessed, finds a web element
 * with the specified tag name. The element lookup behavior can be configured to either
 * cache the initially found element or perform a fresh lookup each time.
 *
 * @param locator The name of the HTML tag to search for.
 * @param cacheLookup If true (default), the element will be looked up only once and cached for
 *                    subsequent accesses, similarly to Page Factory's @CacheLookup annotation.
 *                    If false, a new lookup will be performed each time the element is accessed.
 * @param syncConfig A lambda with receiver to configure the synchronization behavior.
 *                   Default is an empty lambda, which uses the default synchronization settings.
 * @receiver The SearchContext instance used to search for the element.
 * @return A [ReadOnlyProperty] delegate that provides a [WebElement] when accessed.
 *
 * @see SyncConfig
 * @see WebElementProperty
 */
public fun SearchContext.tagName(
    locator: String,
    cacheLookup: Boolean = true,
    syncConfig: SyncConfig<WebElement>.() -> Unit = {},
): WebElementProperty = genericLocator(locator, By::tagName, cacheLookup, syncConfig)

/**
 * Creates a property delegate that lazily finds multiple elements using the tag name locator strategy.
 *
 * This function returns a property delegate that, when accessed, finds all web elements
 * with the specified tag name. The elements lookup behavior can be configured to either
 * cache the initially found elements or perform a fresh lookup each time.
 *
 * @param locator The name of the HTML tag to search for.
 * @param cacheLookup If true (default), the elements will be looked up only once and cached for
 *                    subsequent accesses, similarly to Page Factory's @CacheLookup annotation.
 *                    If false, a new lookup will be performed each time the elements are accessed.
 * @param syncConfig A lambda with receiver to configure the synchronization behavior.
 *                   Default is an empty lambda, which uses the default synchronization settings.
 * @receiver The SearchContext instance used to search for the elements.
 * @return A [ReadOnlyProperty] delegate that provides a [WebElements] collection when accessed.
 *
 * @see SyncConfig
 * @see WebElements
 */
public fun SearchContext.tagNames(
    locator: String,
    cacheLookup: Boolean = true,
    syncConfig: SyncConfig<WebElements>.() -> Unit = {},
): WebElementsProperty = genericLocator(locator, By::tagName, cacheLookup, syncConfig)

/**
 * Creates a property delegate that lazily finds an element using the XPath locator strategy.
 *
 * This function returns a property delegate that, when accessed, finds a web element
 * matching the specified XPath expression. The element lookup behavior can be configured to either
 * cache the initially found element or perform a fresh lookup each time.
 *
 * @param locator The XPath expression to locate the element.
 * @param cacheLookup If true (default), the element will be looked up only once and cached for
 *                    subsequent accesses, similarly to Page Factory's @CacheLookup annotation.
 *                    If false, a new lookup will be performed each time the element is accessed.
 * @param syncConfig A lambda with receiver to configure the synchronization behavior.
 *                   Default is an empty lambda, which uses the default synchronization settings.
 * @receiver The SearchContext instance used to search for the element.
 * @return A [ReadOnlyProperty] delegate that provides a [WebElement] when accessed.
 *
 * @see SyncConfig
 * @see WebElementProperty
 */
public fun SearchContext.xPath(
    locator: String,
    cacheLookup: Boolean = true,
    syncConfig: SyncConfig<WebElement>.() -> Unit = {},
): WebElementProperty = genericLocator(locator, By::xpath, cacheLookup, syncConfig)

/**
 * Creates a property delegate that lazily finds multiple elements using the XPath locator strategy.
 *
 * This function returns a property delegate that, when accessed, finds all web elements
 * matching the specified XPath expression. The elements lookup behavior can be configured to either
 * cache the initially found elements or perform a fresh lookup each time.
 *
 * @param locator The XPath expression to locate the elements.
 * @param cacheLookup If true (default), the elements will be looked up only once and cached for
 *                    subsequent accesses, similarly to Page Factory's @CacheLookup annotation.
 *                    If false, a new lookup will be performed each time the elements are accessed.
 * @param syncConfig A lambda with receiver to configure the synchronization behavior.
 *                   Default is an empty lambda, which uses the default synchronization settings.
 * @receiver The SearchContext instance used to search for the elements.
 * @return A [ReadOnlyProperty] delegate that provides a [WebElements] collection when accessed.
 *
 * @see SyncConfig
 * @see WebElements
 */
public fun SearchContext.xPaths(
    locator: String,
    cacheLookup: Boolean = true,
    syncConfig: SyncConfig<WebElements>.() -> Unit = {},
): WebElementsProperty = genericLocator(locator, By::xpath, cacheLookup, syncConfig)

/**
 * Creates a property delegate that lazily finds an element or elements using a generic locator strategy.
 *
 * This internal function is used by the public locator functions to create property delegates
 * for finding web elements. It supports both single element and multiple elements lookups.
 *
 * @param T The type of the result, either [WebElement] or [WebElements].
 * @param locator The locator string used to find the element(s).
 * @param by A function that converts the locator string to a Selenium [By] object.
 * @param cacheLookup If true (default), the element(s) will be looked up only once and cached for
 *                    subsequent accesses, similarly to Page Factory's @CacheLookup annotation.
 *                    If false, a new lookup will be performed each time the element(s) are accessed.
 * @param syncConfig A lambda with receiver to configure the synchronization behavior.
 * @receiver The SearchContext instance used to search for the element(s).
 * @return A [ReadOnlyProperty] delegate that provides either a [WebElement] or [WebElements] when accessed.
 *
 * @see SyncConfig
 * @see WebElementProperty
 * @see WebElements
 */
@Suppress("UNCHECKED_CAST")
internal inline fun <reified T> SearchContext.genericLocator(
    locator: String,
    noinline by: (String) -> By,
    cacheLookup: Boolean,
    noinline syncConfig: SyncConfig<T>.() -> Unit = {},
): ReadOnlyProperty<Any?, T> =
    when (T::class) {
        WebElement::class -> KWebElement(locator, by, cacheLookup, syncConfig as SyncConfig<WebElement>.() -> Unit)
        List::class -> KWebElements(locator, by, cacheLookup, syncConfig as SyncConfig<WebElements>.() -> Unit)
        else -> throw IllegalArgumentException("Unsupported type: ${T::class.simpleName}")
    } as ReadOnlyProperty<Any?, T>

context(SearchContext)
internal class KWebElement(
    private val locator: String,
    private val by: (String) -> By,
    private val cacheLookup: Boolean,
    private val syncConfig: SyncConfig<WebElement>.() -> Unit,
) : WebElementProperty {
    private val cachedWebElement: WebElement by lazy { findElement(by(locator)) }

    private val webElement: WebElement
        get() = if (cacheLookup) cachedWebElement else findElement(by(locator))

    override fun getValue(
        thisRef: Any?,
        property: KProperty<*>,
    ): WebElement =
        execute(property.name, by(locator)) {
            val config = WebElementSyncConfig().apply(syncConfig)
            val wait = setUpWait(this@SearchContext, config.wait)
            wait.until { config.until(webElement) }
            webElement
        }
}

context(SearchContext)
internal class KWebElements(
    private val locator: String,
    private val by: (String) -> By,
    private val cacheLookup: Boolean,
    private val syncConfig: SyncConfig<WebElements>.() -> Unit,
) : WebElementsProperty {
    private val cachedWebElements: WebElements by lazy { findElements(by(locator)) }

    private val webElements: WebElements
        get() = if (cacheLookup) cachedWebElements else findElements(by(locator))

    override fun getValue(
        thisRef: Any?,
        property: KProperty<*>,
    ): WebElements =
        execute(property.name, by(locator)) {
            val config = WebElementsSyncConfig().apply(syncConfig)
            val wait = setUpWait(this@SearchContext, config.wait)
            wait.until {
                val elements = findElements(by(locator))
                config.until(elements)
            }
            webElements
        }
}

private val logger = KotlinLogging.logger {}

private fun <T> execute(
    element: String,
    by: By,
    block: () -> T,
): T {
    logger.trace { "Waiting for \"$element\" with locator strategy of $by" }
    return block()
}

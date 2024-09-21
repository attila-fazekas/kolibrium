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
import org.openqa.selenium.WebDriver
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
 * with the specified class name. The element lookup is performed lazily and can be
 * configured with custom synchronization behavior.
 *
 * @param locator The value of the "class" attribute to search for. If multiple classes are
 *                specified, the element must have all of them to match.
 * @param syncConfig A lambda with receiver to configure the synchronization behavior.
 *                   Default is an empty lambda, which uses the default synchronization settings.
 * @receiver The WebDriver instance used to search for the element.
 * @return A [ReadOnlyProperty] delegate that provides a [WebElement] when accessed.
 *
 * @see SyncConfig
 * @see WebElementProperty
 */
public fun WebDriver.className(
    locator: String,
    syncConfig: (SyncConfig<WebElement>.() -> Unit) = {},
): WebElementProperty = genericLocator(locator, By::className, syncConfig)

/**
 * Creates a property delegate that lazily finds multiple elements using the className locator strategy.
 *
 * This function returns a property delegate that, when accessed, finds all web elements
 * with the specified class name. The elements lookup is performed lazily and can be
 * configured with custom synchronization behavior.
 *
 * @param locator The value of the "class" attribute to search for. If multiple classes are
 *                specified, elements must have all of them to match.
 * @param syncConfig A lambda with receiver to configure the synchronization behavior.
 *                   Default is an empty lambda, which uses the default synchronization settings.
 * @receiver The WebDriver instance used to search for the elements.
 * @return A [ReadOnlyProperty] delegate that provides a [WebElements] collection when accessed.
 *
 * @see SyncConfig
 * @see WebElements
 */
public fun WebDriver.classNames(
    locator: String,
    syncConfig: (SyncConfig<WebElements>.() -> Unit) = {},
): WebElementsProperty = genericLocator(locator, By::className, syncConfig)

/**
 * Creates a property delegate that lazily finds an element using the CSS selector locator strategy.
 *
 * This function returns a property delegate that, when accessed, finds a web element
 * matching the specified CSS selector. The element lookup is performed lazily and can be
 * configured with custom synchronization behavior.
 *
 * @param locator The CSS selector to locate the element.
 * @param syncConfig A lambda with receiver to configure the synchronization behavior.
 *                   Default is an empty lambda, which uses the default synchronization settings.
 * @receiver The WebDriver instance used to search for the element.
 * @return A [ReadOnlyProperty] delegate that provides a [WebElement] when accessed.
 *
 * @see SyncConfig
 * @see WebElementProperty
 */
public fun WebDriver.cssSelector(
    locator: String,
    syncConfig: SyncConfig<WebElement>.() -> Unit = {},
): WebElementProperty = genericLocator(locator, By::cssSelector, syncConfig)

/**
 * Creates a property delegate that lazily finds multiple elements using the CSS selector locator strategy.
 *
 * This function returns a property delegate that, when accessed, finds all web elements
 * matching the specified CSS selector. The elements lookup is performed lazily and can be
 * configured with custom synchronization behavior.
 *
 * @param locator The CSS selector to locate the elements.
 * @param syncConfig A lambda with receiver to configure the synchronization behavior.
 *                   Default is an empty lambda, which uses the default synchronization settings.
 * @receiver The WebDriver instance used to search for the elements.
 * @return A [ReadOnlyProperty] delegate that provides a [WebElements] collection when accessed.
 *
 * @see SyncConfig
 * @see WebElements
 */
public fun WebDriver.cssSelectors(
    locator: String,
    syncConfig: SyncConfig<WebElements>.() -> Unit = {},
): WebElementsProperty = genericLocator(locator, By::cssSelector, syncConfig)

/**
 * Creates a property delegate that lazily finds an element using the id locator strategy.
 *
 * This function returns a property delegate that, when accessed, finds a web element
 * with the specified id attribute. The element lookup is performed lazily and can be
 * configured with custom synchronization behavior.
 *
 * @param locator The value of the "id" attribute to search for.
 * @param syncConfig A lambda with receiver to configure the synchronization behavior.
 *                   Default is an empty lambda, which uses the default synchronization settings.
 * @receiver The WebDriver instance used to search for the element.
 * @return A [ReadOnlyProperty] delegate that provides a [WebElement] when accessed.
 *
 * @see SyncConfig
 * @see WebElementProperty
 */
public fun WebDriver.id(
    locator: String,
    syncConfig: SyncConfig<WebElement>.() -> Unit = {},
): WebElementProperty = genericLocator(locator, By::id, syncConfig)

/**
 * Creates a property delegate that lazily finds an element using either the id or name locator strategy.
 *
 * This function returns a property delegate that, when accessed, finds a web element
 * with the specified id or name attribute. The element lookup is performed lazily and can be
 * configured with custom synchronization behavior.
 *
 * @param locator The value of either the "id" or "name" attribute to search for.
 * @param syncConfig A lambda with receiver to configure the synchronization behavior.
 *                   Default is an empty lambda, which uses the default synchronization settings.
 * @receiver The WebDriver instance used to search for the element.
 * @return A [ReadOnlyProperty] delegate that provides a [WebElement] when accessed.
 *
 * @see SyncConfig
 * @see WebElementProperty
 * @see ByIdOrName
 */
public fun WebDriver.idOrName(
    locator: String,
    syncConfig: SyncConfig<WebElement>.() -> Unit = {},
): WebElementProperty = genericLocator(locator, ::ByIdOrName, syncConfig)

/**
 * Creates a property delegate that lazily finds an element using the link text locator strategy.
 *
 * This function returns a property delegate that, when accessed, finds a web element
 * with the specified link text. The element lookup is performed lazily and can be
 * configured with custom synchronization behavior.
 *
 * @param locator The exact text of the link to search for.
 * @param syncConfig A lambda with receiver to configure the synchronization behavior.
 *                   Default is an empty lambda, which uses the default synchronization settings.
 * @receiver The WebDriver instance used to search for the element.
 * @return A [ReadOnlyProperty] delegate that provides a [WebElement] when accessed.
 *
 * @see SyncConfig
 * @see WebElementProperty
 */
public fun WebDriver.linkText(
    locator: String,
    syncConfig: SyncConfig<WebElement>.() -> Unit = {},
): WebElementProperty = genericLocator(locator, By::linkText, syncConfig)

/**
 * Creates a property delegate that lazily finds multiple elements using the link text locator strategy.
 *
 * This function returns a property delegate that, when accessed, finds all web elements
 * with the specified link text. The elements lookup is performed lazily and can be
 * configured with custom synchronization behavior.
 *
 * @param locator The exact text of the links to search for.
 * @param syncConfig A lambda with receiver to configure the synchronization behavior.
 *                   Default is an empty lambda, which uses the default synchronization settings.
 * @receiver The WebDriver instance used to search for the elements.
 * @return A [ReadOnlyProperty] delegate that provides a [WebElements] collection when accessed.
 *
 * @see SyncConfig
 * @see WebElements
 */
public fun WebDriver.linkTexts(
    locator: String,
    syncConfig: SyncConfig<WebElements>.() -> Unit = {},
): WebElementsProperty = genericLocator(locator, By::linkText, syncConfig)

/**
 * Creates a property delegate that lazily finds an element using the name locator strategy.
 *
 * This function returns a property delegate that, when accessed, finds a web element
 * with the specified name attribute. The element lookup is performed lazily and can be
 * configured with custom synchronization behavior.
 *
 * @param locator The value of the "name" attribute to search for.
 * @param syncConfig A lambda with receiver to configure the synchronization behavior.
 *                   Default is an empty lambda, which uses the default synchronization settings.
 * @receiver The WebDriver instance used to search for the element.
 * @return A [ReadOnlyProperty] delegate that provides a [WebElement] when accessed.
 *
 * @see SyncConfig
 * @see WebElementProperty
 */
public fun WebDriver.name(
    locator: String,
    syncConfig: SyncConfig<WebElement>.() -> Unit = {},
): WebElementProperty = genericLocator(locator, By::name, syncConfig)

/**
 * Creates a property delegate that lazily finds multiple elements using the name locator strategy.
 *
 * This function returns a property delegate that, when accessed, finds all web elements
 * with the specified name attribute. The elements lookup is performed lazily and can be
 * configured with custom synchronization behavior.
 *
 * @param locator The value of the "name" attribute to search for.
 * @param syncConfig A lambda with receiver to configure the synchronization behavior.
 *                   Default is an empty lambda, which uses the default synchronization settings.
 * @receiver The WebDriver instance used to search for the elements.
 * @return A [ReadOnlyProperty] delegate that provides a [WebElements] collection when accessed.
 *
 * @see SyncConfig
 * @see WebElements
 */
public fun WebDriver.names(
    locator: String,
    syncConfig: SyncConfig<WebElements>.() -> Unit = {},
): WebElementsProperty = genericLocator(locator, By::name, syncConfig)

/**
 * Creates a property delegate that lazily finds an element using the partial link text locator strategy.
 *
 * This function returns a property delegate that, when accessed, finds a web element
 * containing the specified partial link text. The element lookup is performed lazily and can be
 * configured with custom synchronization behavior.
 *
 * @param locator The partial text of the link to search for.
 * @param syncConfig A lambda with receiver to configure the synchronization behavior.
 *                   Default is an empty lambda, which uses the default synchronization settings.
 * @receiver The WebDriver instance used to search for the element.
 * @return A [ReadOnlyProperty] delegate that provides a [WebElement] when accessed.
 *
 * @see SyncConfig
 * @see WebElementProperty
 */
public fun WebDriver.partialLinkText(
    locator: String,
    syncConfig: SyncConfig<WebElement>.() -> Unit = {},
): WebElementProperty = genericLocator(locator, By::partialLinkText, syncConfig)

/**
 * Creates a property delegate that lazily finds multiple elements using the partial link text locator strategy.
 *
 * This function returns a property delegate that, when accessed, finds all web elements
 * containing the specified partial link text. The elements lookup is performed lazily and can be
 * configured with custom synchronization behavior.
 *
 * @param locator The partial text of the links to search for.
 * @param syncConfig A lambda with receiver to configure the synchronization behavior.
 *                   Default is an empty lambda, which uses the default synchronization settings.
 * @receiver The WebDriver instance used to search for the elements.
 * @return A [ReadOnlyProperty] delegate that provides a [WebElements] collection when accessed.
 *
 * @see SyncConfig
 * @see WebElements
 */
public fun WebDriver.partialLinkTexts(
    locator: String,
    syncConfig: SyncConfig<WebElements>.() -> Unit = {},
): WebElementsProperty = genericLocator(locator, By::partialLinkText, syncConfig)

/**
 * Creates a property delegate that lazily finds an element using the tag name locator strategy.
 *
 * This function returns a property delegate that, when accessed, finds a web element
 * with the specified tag name. The element lookup is performed lazily and can be
 * configured with custom synchronization behavior.
 *
 * @param locator The name of the HTML tag to search for.
 * @param syncConfig A lambda with receiver to configure the synchronization behavior.
 *                   Default is an empty lambda, which uses the default synchronization settings.
 * @receiver The WebDriver instance used to search for the element.
 * @return A [ReadOnlyProperty] delegate that provides a [WebElement] when accessed.
 *
 * @see SyncConfig
 * @see WebElementProperty
 */
public fun WebDriver.tagName(
    locator: String,
    syncConfig: SyncConfig<WebElement>.() -> Unit = {},
): WebElementProperty = genericLocator(locator, By::tagName, syncConfig)

/**
 * Creates a property delegate that lazily finds multiple elements using the tag name locator strategy.
 *
 * This function returns a property delegate that, when accessed, finds all web elements
 * with the specified tag name. The elements lookup is performed lazily and can be
 * configured with custom synchronization behavior.
 *
 * @param locator The name of the HTML tag to search for.
 * @param syncConfig A lambda with receiver to configure the synchronization behavior.
 *                   Default is an empty lambda, which uses the default synchronization settings.
 * @receiver The WebDriver instance used to search for the elements.
 * @return A [ReadOnlyProperty] delegate that provides a [WebElements] collection when accessed.
 *
 * @see SyncConfig
 * @see WebElements
 */
public fun WebDriver.tagNames(
    locator: String,
    syncConfig: SyncConfig<WebElements>.() -> Unit = {},
): WebElementsProperty = genericLocator(locator, By::tagName, syncConfig)

/**
 * Creates a property delegate that lazily finds an element using the XPath locator strategy.
 *
 * This function returns a property delegate that, when accessed, finds a web element
 * matching the specified XPath expression. The element lookup is performed lazily and can be
 * configured with custom synchronization behavior.
 *
 * @param locator The XPath expression to locate the element.
 * @param syncConfig A lambda with receiver to configure the synchronization behavior.
 *                   Default is an empty lambda, which uses the default synchronization settings.
 * @receiver The WebDriver instance used to search for the element.
 * @return A [ReadOnlyProperty] delegate that provides a [WebElement] when accessed.
 *
 * @see SyncConfig
 * @see WebElementProperty
 */
public fun WebDriver.xpath(
    locator: String,
    syncConfig: SyncConfig<WebElement>.() -> Unit = {},
): WebElementProperty = genericLocator(locator, By::xpath, syncConfig)

/**
 * Creates a property delegate that lazily finds multiple elements using the XPath locator strategy.
 *
 * This function returns a property delegate that, when accessed, finds all web elements
 * matching the specified XPath expression. The elements lookup is performed lazily and can be
 * configured with custom synchronization behavior.
 *
 * @param locator The XPath expression to locate the elements.
 * @param syncConfig A lambda with receiver to configure the synchronization behavior.
 *                   Default is an empty lambda, which uses the default synchronization settings.
 * @receiver The WebDriver instance used to search for the elements.
 * @return A [ReadOnlyProperty] delegate that provides a [WebElements] collection when accessed.
 *
 * @see SyncConfig
 * @see WebElements
 */
public fun WebDriver.xpaths(
    locator: String,
    syncConfig: SyncConfig<WebElements>.() -> Unit = {},
): WebElementsProperty = genericLocator(locator, By::xpath, syncConfig)

/**
 * Creates a property delegate that lazily finds an element or elements using a generic locator strategy.
 *
 * This internal function is used by the public locator functions to create property delegates
 * for finding web elements. It supports both single element and multiple elements lookups.
 *
 * @param T The type of the result, either [WebElement] or [WebElements].
 * @param locator The locator string used to find the element(s).
 * @param by A function that converts the locator string to a Selenium [By] object.
 * @param syncConfig A lambda with receiver to configure the synchronization behavior.
 * @receiver The WebDriver instance used to search for the element(s).
 * @return A [ReadOnlyProperty] delegate that provides either a [WebElement] or [WebElements] when accessed.
 *
 * @see SyncConfig
 * @see WebElementProperty
 * @see WebElements
 */
@Suppress("UNCHECKED_CAST")
internal inline fun <reified T> WebDriver.genericLocator(
    locator: String,
    noinline by: (String) -> By,
    noinline syncConfig: SyncConfig<T>.() -> Unit = {},
): ReadOnlyProperty<Any?, T> =
    when (T::class) {
        WebElement::class -> KWebElement(locator, by, syncConfig as SyncConfig<WebElement>.() -> Unit)
        List::class -> KWebElements(locator, by, syncConfig as SyncConfig<WebElements>.() -> Unit)
        else -> throw IllegalArgumentException("Unsupported type: ${T::class.simpleName}")
    } as ReadOnlyProperty<Any?, T>

context(WebDriver)
internal class KWebElement(
    private val locator: String,
    private val by: (String) -> By,
    private val syncConfig: SyncConfig<WebElement>.() -> Unit,
) : WebElementProperty {
    private val webElement: WebElement by lazy { findElement(by(locator)) }

    override fun getValue(
        thisRef: Any?,
        property: KProperty<*>,
    ): WebElement {
        return execute(property.name) {
            val config = WebElementSyncConfig().apply(syncConfig)
            val wait = setUpWait(this@WebDriver, config.wait)
            wait.until { config.until(webElement) }
            webElement
        }
    }
}

context(WebDriver)
internal class KWebElements(
    private val locator: String,
    private val by: (String) -> By,
    private val syncConfig: SyncConfig<WebElements>.() -> Unit,
) : WebElementsProperty {
    private val webElements: WebElements by lazy { findElements(by(locator)) }

    override fun getValue(
        thisRef: Any?,
        property: KProperty<*>,
    ): WebElements {
        return execute(property.name) {
            val config = WebElementsSyncConfig().apply(syncConfig)
            val wait = setUpWait(this@WebDriver, config.wait)
            wait.until {
                val elements = findElements(by(locator))
                config.until(elements)
            }
            webElements
        }
    }
}

private val logger = KotlinLogging.logger {}

private fun <T> execute(
    element: String,
    block: () -> T,
): T {
    logger.trace { "Waiting for \"$element\"" }
    return block()
}

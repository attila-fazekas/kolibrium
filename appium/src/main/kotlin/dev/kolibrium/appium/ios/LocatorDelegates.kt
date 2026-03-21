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

package dev.kolibrium.appium.ios

import dev.kolibrium.selenium.core.WaitConfig
import dev.kolibrium.selenium.core.WebElementDescriptor
import dev.kolibrium.selenium.core.WebElements
import dev.kolibrium.selenium.core.WebElementsDescriptor
import dev.kolibrium.selenium.core.descriptors.MultiElementsDescriptor
import dev.kolibrium.selenium.core.descriptors.SingleElementDescriptor
import io.appium.java_client.AppiumBy
import org.openqa.selenium.SearchContext
import org.openqa.selenium.WebElement

/**
 * Creates a property delegate that lazily finds a single element using an iOS class chain query.
 *
 * Class chain queries are an XCUITest-specific locator strategy that provides a faster alternative
 * to XPath for navigating the iOS element hierarchy. The syntax resembles a simplified XPath
 * tailored to the XCUITest framework.
 *
 * Example usage:
 * ```
 * private val cell by iOSClassChain("XCUIElementTypeCell[`name == 'Product'`]")
 * private val button by iOSClassChain("XCUIElementTypeWindow/XCUIElementTypeButton[`label BEGINSWITH 'Add'`]")
 * ```
 *
 * @receiver The [SearchContext] instance used to search for the element.
 * @param value The iOS class chain query string.
 * @param cacheLookup If true (default), the element will be looked up only once and cached for
 *                    subsequent accesses. If false, a new lookup will be performed each time
 *                    the element is accessed.
 * @param waitConfig Configures the waiting behavior when looking up element. Specifies polling interval,
 *                   timeout, error message, and which exceptions to ignore during the wait.
 *                   Defaults come from defaultWaitConfig.
 * @param readyWhen A predicate that determines when the found element is considered ready for use.
 *                  It's called with [WebElement] as receiver. By default, checks if element is
 *                  displayed using [isDisplayed].
 * @return A [WebElementDescriptor] delegate that provides a [WebElement] when accessed.
 *
 * @see WaitConfig
 * @see WebElement
 */
public fun SearchContext.iOSClassChain(
    value: String,
    cacheLookup: Boolean = true,
    waitConfig: WaitConfig? = null,
    readyWhen: (WebElement.() -> Boolean)? = null,
): WebElementDescriptor =
    SingleElementDescriptor(
        searchCtx = this,
        value = value,
        locatorStrategy = AppiumBy::iOSClassChain,
        cacheLookup = cacheLookup,
        waitConfig = waitConfig,
        readyWhen = readyWhen,
    )

/**
 * Creates a property delegate that lazily finds all elements using an iOS class chain query.
 *
 * Example usage:
 * ```
 * private val cells by iOSClassChains("XCUIElementTypeCell")
 * ```
 *
 * Note: Multi-element delegates always perform a fresh lookup and are not cached.
 *
 * @receiver The [SearchContext] instance used to search for the elements.
 * @param value The iOS class chain query string.
 * @param waitConfig Configures the waiting behavior when looking up elements. Specifies polling interval,
 *                   timeout, error message, and which exceptions to ignore during the wait.
 *                   Defaults come from defaultWaitConfig.
 * @param readyWhen A predicate that determines when the found elements are considered ready for use.
 *                  It's called with [WebElements] as receiver. By default, requires the collection
 *                  to be non-empty and all elements to be displayed.
 * @return A [WebElementsDescriptor] delegate that provides a [WebElements] collection when accessed.
 *
 * @see WaitConfig
 * @see WebElements
 */
public fun SearchContext.iOSClassChains(
    value: String,
    waitConfig: WaitConfig? = null,
    readyWhen: (WebElements.() -> Boolean)? = null,
): WebElementsDescriptor =
    MultiElementsDescriptor(
        searchCtx = this,
        value = value,
        locatorStrategy = AppiumBy::iOSClassChain,
        waitConfig = waitConfig,
        readyWhen = readyWhen,
    )

/**
 * Creates a property delegate that lazily finds a single element using an iOS NSPredicate string.
 *
 * NSPredicate strings are an XCUITest-specific locator strategy that allows querying elements
 * using Apple's [NSPredicate](https://developer.apple.com/documentation/foundation/nspredicate)
 * syntax. This is often the most flexible iOS locator strategy, supporting complex conditions
 * on element attributes.
 *
 * Example usage:
 * ```
 * private val loginButton by iOSNsPredicate("label == 'Login' AND isEnabled == true")
 * private val searchField by iOSNsPredicate("type == 'XCUIElementTypeTextField' AND name CONTAINS 'search'")
 * ```
 *
 * @receiver The [SearchContext] instance used to search for the element.
 * @param value The NSPredicate string expression.
 * @param cacheLookup If true (default), the element will be looked up only once and cached for
 *                    subsequent accesses. If false, a new lookup will be performed each time
 *                    the element is accessed.
 * @param waitConfig Configures the waiting behavior when looking up element. Specifies polling interval,
 *                   timeout, error message, and which exceptions to ignore during the wait.
 *                   Defaults come from defaultWaitConfig.
 * @param readyWhen A predicate that determines when the found element is considered ready for use.
 *                  It's called with [WebElement] as receiver. By default, checks if element is
 *                  displayed using [isDisplayed].
 * @return A [WebElementDescriptor] delegate that provides a [WebElement] when accessed.
 *
 * @see WaitConfig
 * @see WebElement
 */
public fun SearchContext.iOSNsPredicate(
    value: String,
    cacheLookup: Boolean = true,
    waitConfig: WaitConfig? = null,
    readyWhen: (WebElement.() -> Boolean)? = null,
): WebElementDescriptor =
    SingleElementDescriptor(
        searchCtx = this,
        value = value,
        locatorStrategy = AppiumBy::iOSNsPredicateString,
        cacheLookup = cacheLookup,
        waitConfig = waitConfig,
        readyWhen = readyWhen,
    )

/**
 * Creates a property delegate that lazily finds all elements using an iOS NSPredicate string.
 *
 * Example usage:
 * ```
 * private val buttons by iOSNsPredicates("type == 'XCUIElementTypeButton'")
 * ```
 *
 * Note: Multi-element delegates always perform a fresh lookup and are not cached.
 *
 * @receiver The [SearchContext] instance used to search for the elements.
 * @param value The NSPredicate string expression.
 * @param waitConfig Configures the waiting behavior when looking up elements. Specifies polling interval,
 *                   timeout, error message, and which exceptions to ignore during the wait.
 *                   Defaults come from defaultWaitConfig.
 * @param readyWhen A predicate that determines when the found elements are considered ready for use.
 *                  It's called with [WebElements] as receiver. By default, requires the collection
 *                  to be non-empty and all elements to be displayed.
 * @return A [WebElementsDescriptor] delegate that provides a [WebElements] collection when accessed.
 *
 * @see WaitConfig
 * @see WebElements
 */
public fun SearchContext.iOSNsPredicates(
    value: String,
    waitConfig: WaitConfig? = null,
    readyWhen: (WebElements.() -> Boolean)? = null,
): WebElementsDescriptor =
    MultiElementsDescriptor(
        searchCtx = this,
        value = value,
        locatorStrategy = AppiumBy::iOSNsPredicateString,
        waitConfig = waitConfig,
        readyWhen = readyWhen,
    )

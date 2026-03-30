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

package dev.kolibrium.appium.android

import dev.kolibrium.webdriver.WaitConfig
import dev.kolibrium.webdriver.WebElementDescriptor
import dev.kolibrium.webdriver.WebElements
import dev.kolibrium.webdriver.WebElementsDescriptor
import dev.kolibrium.webdriver.descriptors.MultiElementsDescriptor
import dev.kolibrium.webdriver.descriptors.SingleElementDescriptor
import dev.kolibrium.webdriver.isDisplayed
import io.appium.java_client.AppiumBy
import org.openqa.selenium.SearchContext
import org.openqa.selenium.WebElement

// UIAutomator locator delegates

/**
 * Creates a property delegate that lazily finds a single element using Android's UIAutomator selector.
 *
 * UIAutomator selectors provide access to Android's
 * [UiSelector](https://developer.android.com/reference/androidx/test/uiautomator/UiSelector) API,
 * enabling powerful element lookups based on properties like text, description, class name,
 * and widget hierarchy.
 *
 * Example usage:
 * ```
 * private val loginButton by androidUIAutomator("new UiSelector().text(\"Login\")")
 * private val scrollableList by androidUIAutomator(
 *     "new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(new UiSelector().text(\"Item\"))"
 * )
 * ```
 *
 * @receiver The [SearchContext] instance used to search for the element.
 * @param value The UIAutomator selector expression.
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
 * @see dev.kolibrium.webdriver.WaitConfig
 * @see WebElement
 */
public fun SearchContext.androidUIAutomator(
    value: String,
    cacheLookup: Boolean = true,
    waitConfig: WaitConfig = WaitConfig.Default,
    readyWhen: WebElement.() -> Boolean = { isDisplayed },
): WebElementDescriptor =
    SingleElementDescriptor(
        searchCtx = this,
        value = value,
        locatorStrategy = AppiumBy::androidUIAutomator,
        cacheLookup = cacheLookup,
        waitConfig = waitConfig,
        readyWhen = readyWhen,
    )

/**
 * Creates a property delegate that lazily finds all elements using Android's UIAutomator selector.
 *
 * Example usage:
 * ```
 * private val textViews by androidUIAutomators("new UiSelector().className(\"android.widget.TextView\")")
 * ```
 *
 * Note: Multi-element delegates always perform a fresh lookup and are not cached.
 *
 * @receiver The [SearchContext] instance used to search for the elements.
 * @param value The UIAutomator selector expression.
 * @param waitConfig Configures the waiting behavior when looking up elements. Specifies polling interval,
 *                   timeout, error message, and which exceptions to ignore during the wait.
 *                   Defaults come from defaultWaitConfig.
 * @param readyWhen A predicate that determines when the found elements are considered ready for use.
 *                  It's called with [WebElements] as receiver. By default, requires the collection
 *                  to be non-empty and all elements to be displayed.
 * @return A [WebElementsDescriptor] delegate that provides a [WebElements] collection when accessed.
 *
 * @see dev.kolibrium.webdriver.WaitConfig
 * @see WebElements
 */
public fun SearchContext.androidUIAutomators(
    value: String,
    waitConfig: WaitConfig = WaitConfig.Default,
    readyWhen: WebElements.() -> Boolean = { isNotEmpty() && all { isDisplayed } },
): WebElementsDescriptor =
    MultiElementsDescriptor(
        searchCtx = this,
        value = value,
        locatorStrategy = AppiumBy::androidUIAutomator,
        waitConfig = waitConfig,
        readyWhen = readyWhen,
    )

// Espresso locator delegates

/**
 * Creates a property delegate that lazily finds a single element using Android's Espresso
 * [DataMatcher](https://developer.android.com/reference/android/support/test/espresso/DataInteraction)
 * JSON selector.
 *
 * Data matchers allow matching elements in `AdapterView` widgets (e.g., `ListView`, `Spinner`)
 * by their underlying data, rather than their rendered view properties.
 *
 * Example usage:
 * ```
 * private val item by androidDataMatcher(
 *     """{"name": "hasEntry", "args": ["title", "My Item"]}"""
 * )
 * ```
 *
 * @receiver The [SearchContext] instance used to search for the element.
 * @param value The JSON string representing the Espresso DataMatcher.
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
 * @see dev.kolibrium.webdriver.WaitConfig
 * @see WebElement
 */
public fun SearchContext.androidDataMatcher(
    value: String,
    cacheLookup: Boolean = true,
    waitConfig: WaitConfig = WaitConfig.Default,
    readyWhen: WebElement.() -> Boolean = { isDisplayed },
): WebElementDescriptor =
    SingleElementDescriptor(
        searchCtx = this,
        value = value,
        locatorStrategy = AppiumBy::androidDataMatcher,
        cacheLookup = cacheLookup,
        waitConfig = waitConfig,
        readyWhen = readyWhen,
    )

/**
 * Creates a property delegate that lazily finds all elements using Android's Espresso
 * DataMatcher JSON selector.
 *
 * Example usage:
 * ```
 * private val items by androidDataMatchers(
 *     """{"name": "hasEntry", "args": ["type", "product"]}"""
 * )
 * ```
 *
 * Note: Multi-element delegates always perform a fresh lookup and are not cached.
 *
 * @receiver The [SearchContext] instance used to search for the elements.
 * @param value The JSON string representing the Espresso DataMatcher.
 * @param waitConfig Configures the waiting behavior when looking up elements. Specifies polling interval,
 *                   timeout, error message, and which exceptions to ignore during the wait.
 *                   Defaults come from defaultWaitConfig.
 * @param readyWhen A predicate that determines when the found elements are considered ready for use.
 *                  It's called with [WebElements] as receiver. By default, requires the collection
 *                  to be non-empty and all elements to be displayed.
 * @return A [WebElementsDescriptor] delegate that provides a [WebElements] collection when accessed.
 *
 * @see dev.kolibrium.webdriver.WaitConfig
 * @see WebElements
 */
public fun SearchContext.androidDataMatchers(
    value: String,
    waitConfig: WaitConfig = WaitConfig.Default,
    readyWhen: WebElements.() -> Boolean = { isNotEmpty() && all { isDisplayed } },
): WebElementsDescriptor =
    MultiElementsDescriptor(
        searchCtx = this,
        value = value,
        locatorStrategy = AppiumBy::androidDataMatcher,
        waitConfig = waitConfig,
        readyWhen = readyWhen,
    )

/**
 * Creates a property delegate that lazily finds a single element using Android's Espresso
 * [ViewMatcher](https://developer.android.com/reference/android/support/test/espresso/matcher/ViewMatchers)
 * JSON selector.
 *
 * View matchers allow matching elements by their view properties using Espresso's Hamcrest-based
 * matcher API, serialized as JSON.
 *
 * Example usage:
 * ```
 * private val button by androidViewMatcher(
 *     """{"name": "withText", "args": ["Submit"]}"""
 * )
 * ```
 *
 * @receiver The [SearchContext] instance used to search for the element.
 * @param value The JSON string representing the Espresso ViewMatcher.
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
 * @see dev.kolibrium.webdriver.WaitConfig
 * @see WebElement
 */
public fun SearchContext.androidViewMatcher(
    value: String,
    cacheLookup: Boolean = true,
    waitConfig: WaitConfig = WaitConfig.Default,
    readyWhen: WebElement.() -> Boolean = { isDisplayed },
): WebElementDescriptor =
    SingleElementDescriptor(
        searchCtx = this,
        value = value,
        locatorStrategy = AppiumBy::androidViewMatcher,
        cacheLookup = cacheLookup,
        waitConfig = (waitConfig ?: WaitConfig.Default),
        readyWhen = (readyWhen ?: { isDisplayed }),
    )

/**
 * Creates a property delegate that lazily finds all elements using Android's Espresso
 * ViewMatcher JSON selector.
 *
 * Example usage:
 * ```
 * private val buttons by androidViewMatchers(
 *     """{"name": "withClassName", "args": ["android.widget.Button"]}"""
 * )
 * ```
 *
 * Note: Multi-element delegates always perform a fresh lookup and are not cached.
 *
 * @receiver The [SearchContext] instance used to search for the elements.
 * @param value The JSON string representing the Espresso ViewMatcher.
 * @param waitConfig Configures the waiting behavior when looking up elements. Specifies polling interval,
 *                   timeout, error message, and which exceptions to ignore during the wait.
 *                   Defaults come from defaultWaitConfig.
 * @param readyWhen A predicate that determines when the found elements are considered ready for use.
 *                  It's called with [WebElements] as receiver. By default, requires the collection
 *                  to be non-empty and all elements to be displayed.
 * @return A [WebElementsDescriptor] delegate that provides a [WebElements] collection when accessed.
 *
 * @see dev.kolibrium.webdriver.WaitConfig
 * @see WebElements
 */
public fun SearchContext.androidViewMatchers(
    value: String,
    waitConfig: WaitConfig = WaitConfig.Default,
    readyWhen: WebElements.() -> Boolean = { isNotEmpty() && all { isDisplayed } },
): WebElementsDescriptor =
    MultiElementsDescriptor(
        searchCtx = this,
        value = value,
        locatorStrategy = AppiumBy::androidViewMatcher,
        waitConfig = (waitConfig ?: WaitConfig.Default),
        readyWhen = (readyWhen ?: { isNotEmpty() && isDisplayed }),
    )

/**
 * Creates a property delegate that lazily finds a single element by its Android view tag.
 *
 * View tags are set programmatically via `View.setTag()` in Android and can be used as
 * a locator strategy when accessibility ids or resource ids are not available.
 *
 * Example usage:
 * ```
 * private val specialButton by androidViewTag("custom-tag")
 * private val header by androidViewTag("header-tag", cacheLookup = false)
 * ```
 *
 * @receiver The [SearchContext] instance used to search for the element.
 * @param value The view tag to search for.
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
 * @see dev.kolibrium.webdriver.WaitConfig
 * @see WebElement
 */
public fun SearchContext.androidViewTag(
    value: String,
    cacheLookup: Boolean = true,
    waitConfig: WaitConfig = WaitConfig.Default,
    readyWhen: WebElement.() -> Boolean = { isDisplayed },
): WebElementDescriptor =
    SingleElementDescriptor(
        searchCtx = this,
        value = value,
        locatorStrategy = AppiumBy::androidViewTag,
        cacheLookup = cacheLookup,
        waitConfig = waitConfig,
        readyWhen = readyWhen,
    )

/**
 * Creates a property delegate that lazily finds all elements by their Android view tag.
 *
 * Example usage:
 * ```
 * private val taggedElements by androidViewTags("list-item-tag")
 * ```
 *
 * Note: Multi-element delegates always perform a fresh lookup and are not cached.
 *
 * @receiver The [SearchContext] instance used to search for the elements.
 * @param value The view tag to search for.
 * @param waitConfig Configures the waiting behavior when looking up elements. Specifies polling interval,
 *                   timeout, error message, and which exceptions to ignore during the wait.
 *                   Defaults come from defaultWaitConfig.
 * @param readyWhen A predicate that determines when the found elements are considered ready for use.
 *                  It's called with [WebElements] as receiver. By default, requires the collection
 *                  to be non-empty and all elements to be displayed.
 * @return A [WebElementsDescriptor] delegate that provides a [WebElements] collection when accessed.
 *
 * @see dev.kolibrium.webdriver.WaitConfig
 * @see WebElements
 */
public fun SearchContext.androidViewTags(
    value: String,
    waitConfig: WaitConfig = WaitConfig.Default,
    readyWhen: WebElements.() -> Boolean = { isNotEmpty() && all { isDisplayed } },
): WebElementsDescriptor =
    MultiElementsDescriptor(
        searchCtx = this,
        value = value,
        locatorStrategy = AppiumBy::androidViewTag,
        waitConfig = waitConfig,
        readyWhen = readyWhen,
    )

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

package dev.kolibrium.appium

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
 * Creates a property delegate that lazily finds a single element by its accessibility id.
 *
 * Accessibility ids are the preferred cross-platform locator strategy for mobile apps.
 * On Android this maps to the element's content description; on iOS to the accessibility
 * identifier.
 *
 * Example usage:
 * ```
 * private val loginButton by accessibilityId("login-button")
 * private val avatar by accessibilityId("user-avatar", cacheLookup = false)
 * private val submitButton by accessibilityId("submit", readyWhen = { isEnabled })
 * ```
 *
 * @receiver The [SearchContext] instance used to search for the element.
 * @param value The accessibility id to search for.
 * @param cacheLookup If true (default), the element will be looked up only once and cached for
 *                    subsequent accesses. If false, a new lookup will be performed each time
 *                    the element is accessed.
 * @param waitConfig Configures the waiting behavior when looking up element. Specifies polling interval,
 *                   timeout, error message, and which exceptions to ignore during the wait.
 *                   Defaults come from defaultWaitConfig.
 * @param readyWhen A predicate that determines when the found element is considered ready for use.
 *                  It's called with [WebElement] as receiver. By default, checks if element is
 *                  displayed using [isDisplayed].
 * @return A [dev.kolibrium.selenium.core.WebElementDescriptor] delegate that provides a [WebElement] when accessed.
 *
 * @see dev.kolibrium.selenium.core.WaitConfig
 * @see WebElement
 */
public fun SearchContext.accessibilityId(
    value: String,
    cacheLookup: Boolean = true,
    waitConfig: WaitConfig? = null,
    readyWhen: (WebElement.() -> Boolean)? = null,
): WebElementDescriptor =
    SingleElementDescriptor(
        searchCtx = this,
        value = value,
        locatorStrategy = AppiumBy::accessibilityId,
        cacheLookup = cacheLookup,
        waitConfig = waitConfig,
        readyWhen = readyWhen,
    )

/**
 * Creates a property delegate that lazily finds all elements by their accessibility id.
 *
 * Example usage:
 * ```
 * private val menuItems by accessibilityIds("menu-item")
 * private val cards by accessibilityIds("product-card", readyWhen = { all { isEnabled } })
 * ```
 *
 * Note: Multi-element delegates always perform a fresh lookup and are not cached.
 *
 * @receiver The [SearchContext] instance used to search for the elements.
 * @param value The accessibility id to search for.
 * @param waitConfig Configures the waiting behavior when looking up elements. Specifies polling interval,
 *                   timeout, error message, and which exceptions to ignore during the wait.
 *                   Defaults come from defaultWaitConfig.
 * @param readyWhen A predicate that determines when the found elements are considered ready for use.
 *                  It's called with [dev.kolibrium.selenium.core.WebElements] as receiver. By default, requires the collection
 *                  to be non-empty and all elements to be displayed.
 * @return A [dev.kolibrium.selenium.core.WebElementsDescriptor] delegate that provides a [dev.kolibrium.selenium.core.WebElements] collection when accessed.
 *
 * @see dev.kolibrium.selenium.core.WaitConfig
 * @see dev.kolibrium.selenium.core.WebElements
 */
public fun SearchContext.accessibilityIds(
    value: String,
    waitConfig: WaitConfig? = null,
    readyWhen: (WebElements.() -> Boolean)? = null,
): WebElementsDescriptor =
    MultiElementsDescriptor(
        searchCtx = this,
        value = value,
        locatorStrategy = AppiumBy::accessibilityId,
        waitConfig = waitConfig,
        readyWhen = readyWhen,
    )

/**
 * Creates a property delegate that lazily finds a single element by its Android resource id.
 *
 * Notes on accepted values:
 * - At the root [SearchContext] (typically a driver or a top‑level screen), you can pass the
 *   full `resource-id` (e.g., `com.example.app:id/titleTV`).
 * - When the [SearchContext] is a nested [WebElement] (e.g., an item container), Appium allows
 *   using the short id (e.g., `titleTV`). Both forms are supported by this delegate.
 *
 * Example usage:
 * ```
 * private val productImage by resourceId("productIV")
 * private val title by resourceId("titleTV", cacheLookup = false)
 * ```
 *
 * @receiver The [SearchContext] instance used to search for the element.
 * @param value The resource id to search for (short or fully qualified).
 * @param cacheLookup If true (default), the element will be looked up only once and cached for
 *                    subsequent accesses. If false, a new lookup will be performed each time
 *                    the element is accessed.
 * @param waitConfig Configures the waiting behavior when looking up element. Specifies polling interval,
 *                   timeout, error message, and which exceptions to ignore during the wait.
 *                   Defaults come from defaultWaitConfig.
 * @param readyWhen A predicate that determines when the found element is considered ready for use.
 *                  It's called with [WebElement] as receiver. By default, checks if element is
 *                  displayed using [isDisplayed].
 * @return A [dev.kolibrium.selenium.core.WebElementDescriptor] delegate that provides a [WebElement] when accessed.
 *
 * @see dev.kolibrium.selenium.core.WaitConfig
 * @see WebElement
 */
public fun SearchContext.resourceId(
    value: String,
    cacheLookup: Boolean = true,
    waitConfig: WaitConfig? = null,
    readyWhen: (WebElement.() -> Boolean)? = null,
): WebElementDescriptor =
    SingleElementDescriptor(
        searchCtx = this,
        value = value,
        locatorStrategy = AppiumBy::id,
        cacheLookup = cacheLookup,
        waitConfig = waitConfig,
        readyWhen = readyWhen,
    )

/**
 * Creates a property delegate that lazily finds all elements by their Android resource id.
 *
 * Example usage:
 * ```
 * private val productImages by resourceIds("productIV")
 * ```
 *
 * Note: Multi‑element delegates always perform a fresh lookup and are not cached.
 *
 * @receiver The [SearchContext] instance used to search for the elements.
 * @param value The resource id to search for (short or fully qualified).
 * @param waitConfig Configures the waiting behavior when looking up elements. Specifies polling interval,
 *                   timeout, error message, and which exceptions to ignore during the wait.
 *                   Defaults come from defaultWaitConfig.
 * @param readyWhen A predicate that determines when the found elements are considered ready for use.
 *                  It's called with [dev.kolibrium.selenium.core.WebElements] as receiver. By default, requires the collection
 *                  to be non‑empty and all elements to be displayed.
 * @return A [dev.kolibrium.selenium.core.WebElementsDescriptor] delegate that provides a [dev.kolibrium.selenium.core.WebElements] collection when accessed.
 *
 * @see dev.kolibrium.selenium.core.WaitConfig
 * @see dev.kolibrium.selenium.core.WebElements
 */
public fun SearchContext.resourceIds(
    value: String,
    waitConfig: WaitConfig? = null,
    readyWhen: (WebElements.() -> Boolean)? = null,
): WebElementsDescriptor =
    MultiElementsDescriptor(
        searchCtx = this,
        value = value,
        locatorStrategy = AppiumBy::id,
        waitConfig = waitConfig,
        readyWhen = readyWhen,
    )

// Android platform locator delegates

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
 * @return A [dev.kolibrium.selenium.core.WebElementDescriptor] delegate that provides a [WebElement] when accessed.
 *
 * @see dev.kolibrium.selenium.core.WaitConfig
 * @see WebElement
 */
public fun SearchContext.androidUIAutomator(
    value: String,
    cacheLookup: Boolean = true,
    waitConfig: WaitConfig? = null,
    readyWhen: (WebElement.() -> Boolean)? = null,
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
 *                  It's called with [dev.kolibrium.selenium.core.WebElements] as receiver. By default, requires the collection
 *                  to be non-empty and all elements to be displayed.
 * @return A [dev.kolibrium.selenium.core.WebElementsDescriptor] delegate that provides a [dev.kolibrium.selenium.core.WebElements] collection when accessed.
 *
 * @see dev.kolibrium.selenium.core.WaitConfig
 * @see dev.kolibrium.selenium.core.WebElements
 */
public fun SearchContext.androidUIAutomators(
    value: String,
    waitConfig: WaitConfig? = null,
    readyWhen: (WebElements.() -> Boolean)? = null,
): WebElementsDescriptor =
    MultiElementsDescriptor(
        searchCtx = this,
        value = value,
        locatorStrategy = AppiumBy::androidUIAutomator,
        waitConfig = waitConfig,
        readyWhen = readyWhen,
    )

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
 * @return A [dev.kolibrium.selenium.core.WebElementDescriptor] delegate that provides a [WebElement] when accessed.
 *
 * @see dev.kolibrium.selenium.core.WaitConfig
 * @see WebElement
 */
public fun SearchContext.androidDataMatcher(
    value: String,
    cacheLookup: Boolean = true,
    waitConfig: WaitConfig? = null,
    readyWhen: (WebElement.() -> Boolean)? = null,
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
 *                  It's called with [dev.kolibrium.selenium.core.WebElements] as receiver. By default, requires the collection
 *                  to be non-empty and all elements to be displayed.
 * @return A [dev.kolibrium.selenium.core.WebElementsDescriptor] delegate that provides a [dev.kolibrium.selenium.core.WebElements] collection when accessed.
 *
 * @see dev.kolibrium.selenium.core.WaitConfig
 * @see dev.kolibrium.selenium.core.WebElements
 */
public fun SearchContext.androidDataMatchers(
    value: String,
    waitConfig: WaitConfig? = null,
    readyWhen: (WebElements.() -> Boolean)? = null,
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
 * @return A [dev.kolibrium.selenium.core.WebElementDescriptor] delegate that provides a [WebElement] when accessed.
 *
 * @see dev.kolibrium.selenium.core.WaitConfig
 * @see WebElement
 */
public fun SearchContext.androidViewMatcher(
    value: String,
    cacheLookup: Boolean = true,
    waitConfig: WaitConfig? = null,
    readyWhen: (WebElement.() -> Boolean)? = null,
): WebElementDescriptor =
    SingleElementDescriptor(
        searchCtx = this,
        value = value,
        locatorStrategy = AppiumBy::androidViewMatcher,
        cacheLookup = cacheLookup,
        waitConfig = waitConfig,
        readyWhen = readyWhen,
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
 *                  It's called with [dev.kolibrium.selenium.core.WebElements] as receiver. By default, requires the collection
 *                  to be non-empty and all elements to be displayed.
 * @return A [dev.kolibrium.selenium.core.WebElementsDescriptor] delegate that provides a [dev.kolibrium.selenium.core.WebElements] collection when accessed.
 *
 * @see dev.kolibrium.selenium.core.WaitConfig
 * @see dev.kolibrium.selenium.core.WebElements
 */
public fun SearchContext.androidViewMatchers(
    value: String,
    waitConfig: WaitConfig? = null,
    readyWhen: (WebElements.() -> Boolean)? = null,
): WebElementsDescriptor =
    MultiElementsDescriptor(
        searchCtx = this,
        value = value,
        locatorStrategy = AppiumBy::androidViewMatcher,
        waitConfig = waitConfig,
        readyWhen = readyWhen,
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
 * @return A [dev.kolibrium.selenium.core.WebElementDescriptor] delegate that provides a [WebElement] when accessed.
 *
 * @see dev.kolibrium.selenium.core.WaitConfig
 * @see WebElement
 */
public fun SearchContext.androidViewTag(
    value: String,
    cacheLookup: Boolean = true,
    waitConfig: WaitConfig? = null,
    readyWhen: (WebElement.() -> Boolean)? = null,
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
 *                  It's called with [dev.kolibrium.selenium.core.WebElements] as receiver. By default, requires the collection
 *                  to be non-empty and all elements to be displayed.
 * @return A [dev.kolibrium.selenium.core.WebElementsDescriptor] delegate that provides a [dev.kolibrium.selenium.core.WebElements] collection when accessed.
 *
 * @see dev.kolibrium.selenium.core.WaitConfig
 * @see dev.kolibrium.selenium.core.WebElements
 */
public fun SearchContext.androidViewTags(
    value: String,
    waitConfig: WaitConfig? = null,
    readyWhen: (WebElements.() -> Boolean)? = null,
): WebElementsDescriptor =
    MultiElementsDescriptor(
        searchCtx = this,
        value = value,
        locatorStrategy = AppiumBy::androidViewTag,
        waitConfig = waitConfig,
        readyWhen = readyWhen,
    )

// iOS platform locator delegates

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
 * @return A [dev.kolibrium.selenium.core.WebElementDescriptor] delegate that provides a [WebElement] when accessed.
 *
 * @see dev.kolibrium.selenium.core.WaitConfig
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
 *                  It's called with [dev.kolibrium.selenium.core.WebElements] as receiver. By default, requires the collection
 *                  to be non-empty and all elements to be displayed.
 * @return A [dev.kolibrium.selenium.core.WebElementsDescriptor] delegate that provides a [dev.kolibrium.selenium.core.WebElements] collection when accessed.
 *
 * @see dev.kolibrium.selenium.core.WaitConfig
 * @see dev.kolibrium.selenium.core.WebElements
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
 * @return A [dev.kolibrium.selenium.core.WebElementDescriptor] delegate that provides a [WebElement] when accessed.
 *
 * @see dev.kolibrium.selenium.core.WaitConfig
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
 *                  It's called with [dev.kolibrium.selenium.core.WebElements] as receiver. By default, requires the collection
 *                  to be non-empty and all elements to be displayed.
 * @return A [dev.kolibrium.selenium.core.WebElementsDescriptor] delegate that provides a [dev.kolibrium.selenium.core.WebElements] collection when accessed.
 *
 * @see dev.kolibrium.selenium.core.WaitConfig
 * @see dev.kolibrium.selenium.core.WebElements
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

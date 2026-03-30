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
 * @return A [WebElementDescriptor] delegate that provides a [WebElement] when accessed.
 *
 * @see dev.kolibrium.webdriver.WaitConfig
 * @see WebElement
 */
public fun SearchContext.accessibilityId(
    value: String,
    cacheLookup: Boolean = true,
    waitConfig: WaitConfig = WaitConfig.Default,
    readyWhen: WebElement.() -> Boolean = { isDisplayed },
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
 *                  It's called with [WebElements] as receiver. By default, requires the collection
 *                  to be non-empty and all elements to be displayed.
 * @return A [WebElementsDescriptor] delegate that provides a [WebElements] collection when accessed.
 *
 * @see dev.kolibrium.webdriver.WaitConfig
 * @see WebElements
 */
public fun SearchContext.accessibilityIds(
    value: String,
    waitConfig: WaitConfig = WaitConfig.Default,
    readyWhen: WebElements.() -> Boolean = { isNotEmpty() && all { isDisplayed } },
): WebElementsDescriptor =
    MultiElementsDescriptor(
        searchCtx = this,
        value = value,
        locatorStrategy = AppiumBy::accessibilityId,
        waitConfig = waitConfig,
        readyWhen = readyWhen,
    )

/**
 * Creates a property delegate that lazily finds a single element by its class name.
 *
 * Example usage:
 * ```
 * private val loginButton by className("android.widget.Button")
 * private val avatar by className("XCUIElementTypeImage", cacheLookup = false)
 * ```
 *
 * @receiver The [SearchContext] instance used to search for the element.
 * @param value The class name to search for.
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
public fun SearchContext.className(
    value: String,
    cacheLookup: Boolean = true,
    waitConfig: WaitConfig = WaitConfig.Default,
    readyWhen: WebElement.() -> Boolean = { isDisplayed },
): WebElementDescriptor =
    SingleElementDescriptor(
        searchCtx = this,
        value = value,
        locatorStrategy = AppiumBy::className,
        cacheLookup = cacheLookup,
        waitConfig = waitConfig,
        readyWhen = readyWhen,
    )

/**
 * Creates a property delegate that lazily finds all elements by their class name.
 *
 * Example usage:
 * ```
 * private val buttons by classNames("android.widget.Button")
 * private val images by classNames("XCUIElementTypeImage", readyWhen = { all { isEnabled } })
 * ```
 *
 * Note: Multi-element delegates always perform a fresh lookup and are not cached.
 *
 * @receiver The [SearchContext] instance used to search for the elements.
 * @param value The class name to search for.
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
public fun SearchContext.classNames(
    value: String,
    waitConfig: WaitConfig = WaitConfig.Default,
    readyWhen: WebElements.() -> Boolean = { isNotEmpty() && all { isDisplayed } },
): WebElementsDescriptor =
    MultiElementsDescriptor(
        searchCtx = this,
        value = value,
        locatorStrategy = AppiumBy::className,
        waitConfig = waitConfig,
        readyWhen = readyWhen,
    )

/**
 * Creates a property delegate that lazily finds a single element by its resource id.
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
 * @return A [WebElementDescriptor] delegate that provides a [WebElement] when accessed.
 *
 * @see dev.kolibrium.webdriver.WaitConfig
 * @see WebElement
 */
public fun SearchContext.resourceId(
    value: String,
    cacheLookup: Boolean = true,
    waitConfig: WaitConfig = WaitConfig.Default,
    readyWhen: WebElement.() -> Boolean = { isDisplayed },
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
 * Creates a property delegate that lazily finds all elements by their resource id.
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
 *                  It's called with [WebElements] as receiver. By default, requires the collection
 *                  to be non‑empty and all elements to be displayed.
 * @return A [WebElementsDescriptor] delegate that provides a [WebElements] collection when accessed.
 *
 * @see dev.kolibrium.webdriver.WaitConfig
 * @see WebElements
 */
public fun SearchContext.resourceIds(
    value: String,
    waitConfig: WaitConfig = WaitConfig.Default,
    readyWhen: WebElements.() -> Boolean = { isNotEmpty() && all { isDisplayed } },
): WebElementsDescriptor =
    MultiElementsDescriptor(
        searchCtx = this,
        value = value,
        locatorStrategy = AppiumBy::id,
        waitConfig = waitConfig,
        readyWhen = readyWhen,
    )

/**
 * Creates a property delegate that lazily finds a single element using an XPath expression.
 *
 * Example usage:
 * ```
 * private val submitButton by xpath("//android.widget.Button[@text='Login']")
 * private val header by xpath("//XCUIElementTypeStaticText[@name='header']", cacheLookup = false)
 * ```
 *
 * @receiver The [SearchContext] instance used to search for the element.
 * @param value The XPath expression.
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
public fun SearchContext.xpath(
    value: String,
    cacheLookup: Boolean = true,
    waitConfig: WaitConfig = WaitConfig.Default,
    readyWhen: WebElement.() -> Boolean = { isDisplayed },
): WebElementDescriptor =
    SingleElementDescriptor(
        searchCtx = this,
        value = value,
        locatorStrategy = AppiumBy::xpath,
        cacheLookup = cacheLookup,
        waitConfig = waitConfig,
        readyWhen = readyWhen,
    )

/**
 * Creates a property delegate that lazily finds all elements matching an XPath expression.
 *
 * Example usage:
 * ```
 * private val listItems by xpaths("//android.widget.TextView")
 * private val inputs by xpaths("//XCUIElementTypeTextField", readyWhen = { all { isClickable } })
 * ```
 *
 * Note: Multi-element delegates always perform a fresh lookup and are not cached.
 *
 * @receiver The [SearchContext] instance used to search for the elements.
 * @param value The XPath expression.
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
public fun SearchContext.xpaths(
    value: String,
    waitConfig: WaitConfig = WaitConfig.Default,
    readyWhen: WebElements.() -> Boolean = { isNotEmpty() && all { isDisplayed } },
): WebElementsDescriptor =
    MultiElementsDescriptor(
        searchCtx = this,
        value = value,
        locatorStrategy = AppiumBy::xpath,
        waitConfig = waitConfig,
        readyWhen = readyWhen,
    )

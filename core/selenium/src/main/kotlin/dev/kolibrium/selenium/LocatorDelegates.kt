/*
 * Copyright 2023-2025 Attila Fazekas & contributors
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

import dev.kolibrium.common.WebElements
import dev.kolibrium.selenium.configuration.SeleniumProjectConfiguration
import dev.kolibrium.selenium.decorators.DecoratorManager
import io.github.oshai.kotlinlogging.KotlinLogging
import org.openqa.selenium.By
import org.openqa.selenium.SearchContext
import org.openqa.selenium.StaleElementReferenceException
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ByIdOrName
import org.openqa.selenium.support.ui.FluentWait
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import kotlin.time.toJavaDuration

private typealias WebElementProperty = ReadOnlyProperty<Any?, WebElement>
private typealias WebElementsProperty = ReadOnlyProperty<Any?, WebElements>

private val logger = KotlinLogging.logger {}

/**
 * Creates a property delegate that lazily finds a single element by its class name.
 *
 * This function returns a property delegate that, when accessed, finds a web element
 * with the specified class name. The element lookup and synchronization behavior can
 * be configured through the parameters.
 *
 * Example usage:
 * ```
 * private val submitButton by className("submit-btn")
 * private val badge by className("badge", cacheLookup = false))
 * private val menuButton by className("nav-menu", readinessCondition = { isClickable })
 * ```
 *
 * @receiver The SearchContext instance used to search for the element.
 * @param value The exact value of the "class" attribute to search for.
 *              Only one class name should be used. If an element has
 *              multiple classes, please use cssSelector(String).
 * @param cacheLookup If true (default), the element will be looked up only once and cached for
 *                    subsequent accesses. If false, a new lookup will be performed each time
 *                    the element is accessed.
 * @param waitConfig Configures the waiting behavior when looking up element. Specifies polling interval,
 *                   timeout, error message, and which exceptions to ignore during the wait.
 *                   Defaults to a 10-second timeout with 200ms polling.
 * @param readyCondition A predicate that determines when the found element is considered ready for use.
 *                       It's called with [WebElement] as receiver. By default, checks if element is
 *                       displayed using [isDisplayed].
 * @return A [ReadOnlyProperty] delegate that provides a [WebElement] when accessed.
 *
 * @see Wait
 * @see WebElement
 */
public fun SearchContext.className(
    value: String,
    cacheLookup: Boolean = true,
    waitConfig: Wait = defaultWaitConfig,
    readyCondition: WebElement.() -> Boolean = defaultElementReadyCondition,
): WebElementProperty = genericLocator(value, By::className, cacheLookup, waitConfig, readyCondition)

/**
 * Creates a property delegate that lazily finds all elements by their class name.
 *
 * This function returns a property delegate that, when accessed, finds all web elements
 * with the specified class name. The elements lookup and synchronization behavior can be
 * configured through the parameters.
 *
 * Example usage:
 * ```
 * private val items by classNames("item-class")
 * private val badges by classNames("badge", cacheLookup = false)
 * private val menus by classNames("menu-item", readinessCondition = { all { isClickable } })
 * ```
 *
 * @receiver The SearchContext instance used to search for the elements.
 * @param value The exact value of the "class" attribute to search for.
 *              Only one class name should be used. If an element has
 *              multiple classes, please use cssSelector(String).
 * @param cacheLookup If true (default), the elements will be looked up only once and cached for
 *                    subsequent accesses. If false, a new lookup will be performed each time
 *                    the elements are accessed.
 * @param waitConfig Configures the waiting behavior when looking up element. Specifies polling interval,
 *                   timeout, error message, and which exceptions to ignore during the wait.
 *                   Defaults to a 10-second timeout with 200ms polling.
 * @param readyCondition A predicate that determines when the found element is considered ready for use.
 *                       It's called with [WebElement] as receiver. By default, checks if element is
 *                       displayed using [isDisplayed].
 * @return A [ReadOnlyProperty] delegate that provides a [WebElements] collection when accessed.
 *
 * @see Wait
 * @see WebElements
 */
public fun SearchContext.classNames(
    value: String,
    cacheLookup: Boolean = true,
    waitConfig: Wait = defaultWaitConfig,
    readyCondition: WebElements.() -> Boolean = defaultElementsReadyCondition,
): WebElementsProperty = genericLocator(value, By::className, cacheLookup, waitConfig, readyCondition)

/**
 * Creates a property delegate that lazily finds a single element using a CSS selector.
 *
 * This function returns a property delegate that, when accessed, finds a web element matching the
 * specified CSS selector. The element lookup and synchronization behavior can be configured
 * through the parameters.
 *
 * Example usage:
 * ```
 * private val container by cssSelector(".container")
 * private val label by cssSelector(".label", cacheLookup = false)
 * private val inputField by cssSelector("#input-field", readinessCondition = { isClickable })
 * ```
 *
 * @receiver The SearchContext instance used to search for the element.
 * @param value The CSS selector to locate the element.
 * @param cacheLookup If true (default), the element will be looked up only once and cached for
 *                    subsequent accesses. If false, a new lookup will be performed each time
 *                    the element is accessed.
 * @param waitConfig Configures the waiting behavior when looking up element. Specifies polling interval,
 *                   timeout, error message, and which exceptions to ignore during the wait.
 *                   Defaults to a 10-second timeout with 200ms polling.
 * @param readyCondition A predicate that determines when the found element is considered ready for use.
 *                       It's called with [WebElement] as receiver. By default, checks if element is
 *                       displayed using [isDisplayed].
 * @return A [ReadOnlyProperty] delegate that provides a [WebElement] when accessed.
 *
 * @see Wait
 * @see WebElement
 */
public fun SearchContext.cssSelector(
    value: String,
    cacheLookup: Boolean = true,
    waitConfig: Wait = defaultWaitConfig,
    readyCondition: WebElement.() -> Boolean = defaultElementReadyCondition,
): WebElementProperty = genericLocator(value, By::cssSelector, cacheLookup, waitConfig, readyCondition)

/**
 * Creates a property delegate that lazily finds all elements matching a CSS selector.
 *
 * This function returns a property delegate that, when accessed, finds all web elements matching the
 * specified CSS selector. The elements lookup and synchronization behavior can be configured
 * through the parameters.
 *
 * Example usage:
 * ```
 * private val cards by cssSelectors(".card")
 * private val labels by cssSelectors(".label", cacheLookup = false)
 * private val fields by cssSelectors(".form-field", readinessCondition = { all { isClickable } })
 * ```
 *
 * @receiver The SearchContext instance used to search for the elements.
 * @param value The CSS selector to locate the elements.
 * @param cacheLookup If true (default), the elements will be looked up only once and cached for
 *                    subsequent accesses. If false, a new lookup will be performed each time
 *                    the elements are accessed.
 * @param waitConfig Configures the waiting behavior when looking up element. Specifies polling interval,
 *                   timeout, error message, and which exceptions to ignore during the wait.
 *                   Defaults to a 10-second timeout with 200ms polling.
 * @param readyCondition A predicate that determines when the found element is considered ready for use.
 *                       It's called with [WebElement] as receiver. By default, checks if element is
 *                       displayed using [isDisplayed].
 * @return A [ReadOnlyProperty] delegate that provides a [WebElements] collection when accessed.
 *
 * @see Wait
 * @see WebElements
 */
public fun SearchContext.cssSelectors(
    value: String,
    cacheLookup: Boolean = true,
    waitConfig: Wait = defaultWaitConfig,
    readyCondition: WebElements.() -> Boolean = defaultElementsReadyCondition,
): WebElementsProperty = genericLocator(value, By::cssSelector, cacheLookup, waitConfig, readyCondition)

/**
 * Creates a property delegate that lazily finds a single element by its "data-test" attribute value.
 *
 * This function returns a property delegate that, when accessed, finds a web element
 * matching the specified data-test attribute value. The element lookup and synchronization behavior can
 * be configured through the parameters.
 *
 * Example usage:
 * ```
 * private val submitButton by dataTest("submit-btn")
 * private val headerLink by dataTest("header-link", cacheLookup = false)
 * private val actionButton by dataTest("action-btn", readinessCondition = { isClickable })
 * ```
 *
 * @receiver The SearchContext instance used to search for the element.
 * @param value The exact value of the "data-test" attribute to search for.
 * @param cacheLookup If true (default), the element will be looked up only once and cached for
 *                    subsequent accesses. If false, a new lookup will be performed each time
 *                    the element is accessed.
 * @param waitConfig Configures the waiting behavior when looking up element. Specifies polling interval,
 *                   timeout, error message, and which exceptions to ignore during the wait.
 *                   Defaults to a 10-second timeout with 200ms polling.
 * @param readyCondition A predicate that determines when the found element is considered ready for use.
 *                       It's called with [WebElement] as receiver. By default, checks if element is
 *                       displayed using [isDisplayed].
 * @return A [ReadOnlyProperty] delegate that provides a [WebElement] when accessed.
 *
 * @see Wait
 * @see WebElement
 */
public fun SearchContext.dataTest(
    value: String,
    cacheLookup: Boolean = true,
    waitConfig: Wait = defaultWaitConfig,
    readyCondition: WebElement.() -> Boolean = defaultElementReadyCondition,
): WebElementProperty = xPath("//*[@data-test=${value.escapeQuotes()}]", cacheLookup, waitConfig, readyCondition)

private fun String.escapeQuotes(): String = if (contains("'")) "concat('${replace("'", "',\"'\",'")}')" else "'$this'"

/**
 * Creates a property delegate that lazily finds all elements with the specified "data-test" attribute value.
 *
 * This function returns a property delegate that, when accessed, finds all web elements
 * matching the specified data-test attribute value. The elements lookup and synchronization behavior can
 * be configured through the parameters.
 *
 * Example usage:
 * ```
 * private val navButtons by dataTests("nav-btn")
 * private val productLinks by dataTests("product-link", cacheLookup = false)
 * private val formFields by dataTests("form-field", readinessCondition = { all { isClickable } })
 * ```
 *
 * @receiver The SearchContext instance used to search for the elements.
 * @param value The exact value of the "data-test" attribute to search for.
 * @param cacheLookup If true (default), the elements will be looked up only once and cached for
 *                    subsequent accesses. If false, a new lookup will be performed each time
 *                    the elements are accessed.
 * @param waitConfig Configures the waiting behavior when looking up element. Specifies polling interval,
 *                   timeout, error message, and which exceptions to ignore during the wait.
 *                   Defaults to a 10-second timeout with 200ms polling.
 * @param readyCondition A predicate that determines when the found element is considered ready for use.
 *                       It's called with [WebElement] as receiver. By default, checks if element is
 *                       displayed using [isDisplayed].
 * @return A [ReadOnlyProperty] delegate that provides a [WebElements] collection when accessed.
 *
 * @see Wait
 * @see WebElements
 */
public fun SearchContext.dataTests(
    value: String,
    cacheLookup: Boolean = true,
    waitConfig: Wait = defaultWaitConfig,
    readyCondition: WebElements.() -> Boolean = defaultElementsReadyCondition,
): WebElementsProperty = xPaths("//*[@data-test='$value']", cacheLookup, waitConfig, readyCondition)

/**
 * Creates a property delegate that lazily finds a single element by its "id" attribute.
 *
 * This function returns a property delegate that, when accessed, finds a web element with
 * the specified id attribute. The element lookup and synchronization behavior can be
 * configured through the parameters.
 *
 * Example usage:
 * ```
 * private val submitButton by id("submit")
 * private val searchBox by id("search", cacheLookup = false)
 * private val loginButton by id("login", readinessCondition = { isClickable })
 * ```
 *
 * @receiver The SearchContext instance used to search for the element.
 * @param value The value of the "id" attribute to search for.
 * @param cacheLookup If true (default), the element will be looked up only once and cached for
 *                    subsequent accesses. If false, a new lookup will be performed each time
 *                    the element is accessed.
 * @param waitConfig Configures the waiting behavior when looking up element. Specifies polling interval,
 *                   timeout, error message, and which exceptions to ignore during the wait.
 *                   Defaults to a 10-second timeout with 200ms polling.
 * @param readyCondition A predicate that determines when the found element is considered ready for use.
 *                       It's called with [WebElement] as receiver. By default, checks if element is
 *                       displayed using [isDisplayed].
 * @return A [ReadOnlyProperty] delegate that provides a [WebElement] when accessed.
 *
 * @see Wait
 * @see WebElement
 */
public fun SearchContext.id(
    value: String,
    cacheLookup: Boolean = true,
    waitConfig: Wait = defaultWaitConfig,
    readyCondition: WebElement.() -> Boolean = defaultElementReadyCondition,
): WebElementProperty = genericLocator(value, By::id, cacheLookup, waitConfig, readyCondition)

/**
 * Creates a property delegate that lazily finds a single element by its "id" or "name" attribute.
 *
 * This function returns a property delegate that, when accessed, finds a web element with the specified
 * id or name attribute. The element lookup and synchronization behavior can be configured through
 * the parameters.
 *
 * Example usage:
 * ```
 * private val userField by idOrName("user-input")
 * private val passwordField by idOrName("password", cacheLookup = false)
 * private val submitField by idOrName("submit", readinessCondition = { isClickable })
 * ```
 *
 * @receiver The SearchContext instance used to search for the element.
 * @param value The value of either the "id" or "name" attribute to search for.
 * @param cacheLookup If true (default), the element will be looked up only once and cached for
 *                    subsequent accesses. If false, a new lookup will be performed each time
 *                    the element is accessed.
 * @param waitConfig Configures the waiting behavior when looking up element. Specifies polling interval,
 *                   timeout, error message, and which exceptions to ignore during the wait.
 *                   Defaults to a 10-second timeout with 200ms polling.
 * @param readyCondition A predicate that determines when the found element is considered ready for use.
 *                       It's called with [WebElement] as receiver. By default, checks if element is
 *                       displayed using [isDisplayed].
 * @return A [ReadOnlyProperty] delegate that provides a [WebElement] when accessed.
 *
 * @see Wait
 * @see WebElement
 * @see ByIdOrName
 */
public fun SearchContext.idOrName(
    value: String,
    cacheLookup: Boolean = true,
    waitConfig: Wait = defaultWaitConfig,
    readyCondition: WebElement.() -> Boolean = defaultElementReadyCondition,
): WebElementProperty = genericLocator(value, ::ByIdOrName, cacheLookup, waitConfig, readyCondition)

/**
 * Creates a property delegate that lazily finds a single element by its exact link text.
 *
 * This function returns a property delegate that, when accessed, finds a web element with
 * the specified link text. The element lookup and synchronization behavior can be
 * configured through the parameters.
 *
 * Example usage:
 * ```
 * private val homeLink by linkText("Home")
 * private val aboutLink by linkText("About", cacheLookup = false)
 * private val contactLink by linkText("Contact", readinessCondition = { isClickable })
 * ```
 *
 * @receiver The SearchContext instance used to search for the element.
 * @param value The exact, case-sensitive link text to search for.
 * @param cacheLookup If true (default), the element will be looked up only once and cached for
 *                     subsequent accesses. If false, a new lookup will be performed each time
 *                     the element is accessed.
 * @param waitConfig Configures the waiting behavior when looking up element. Specifies polling interval,
 *                   timeout, error message, and which exceptions to ignore during the wait.
 *                   Defaults to a 10-second timeout with 200ms polling.
 * @param readyCondition A predicate that determines when the found element is considered ready for use.
 *                       It's called with [WebElement] as receiver. By default, checks if element is
 *                       displayed using [isDisplayed].
 * @return A [ReadOnlyProperty] delegate that provides a [WebElement] when accessed.
 *
 * @see Wait
 * @see WebElement
 */
public fun SearchContext.linkText(
    value: String,
    cacheLookup: Boolean = true,
    waitConfig: Wait = defaultWaitConfig,
    readyCondition: WebElement.() -> Boolean = defaultElementReadyCondition,
): WebElementProperty = genericLocator(value, By::linkText, cacheLookup, waitConfig, readyCondition)

/**
 * Creates a property delegate that lazily finds all elements by their exact link text.
 *
 * This function returns a property delegate that, when accessed, finds all web elements
 * with the specified link text. The elements lookup and synchronization behavior can be
 * configured through the parameters.
 *
 * Example usage:
 * ```
 * private val navLinks by linkTexts("NavItem")
 * private val footerLinks by linkTexts("FooterLink", cacheLookup = false)
 * private val sidebarLinks by linkTexts("SidebarLink", readinessCondition = { all { isClickable } })
 * ```
 *
 * @receiver The SearchContext instance used to search for the elements.
 * @param value The exact, case-sensitive link text to search for.
 * @param cacheLookup If true (default), the elements will be looked up only once and cached for
 *                    subsequent accesses. If false, a new lookup will be performed each time
 *                    the elements are accessed.
 * @param waitConfig Configures the waiting behavior when looking up element. Specifies polling interval,
 *                   timeout, error message, and which exceptions to ignore during the wait.
 *                   Defaults to a 10-second timeout with 200ms polling.
 * @param readyCondition A predicate that determines when the found element is considered ready for use.
 *                       It's called with [WebElement] as receiver. By default, checks if element is
 *                       displayed using [isDisplayed].
 * @return A [ReadOnlyProperty] delegate that provides a [WebElements] collection when accessed.
 *
 * @see Wait
 * @see WebElements
 */
public fun SearchContext.linkTexts(
    value: String,
    cacheLookup: Boolean = true,
    waitConfig: Wait = defaultWaitConfig,
    readyCondition: WebElements.() -> Boolean = defaultElementsReadyCondition,
): WebElementsProperty = genericLocator(value, By::linkText, cacheLookup, waitConfig, readyCondition)

/**
 * Creates a property delegate that lazily finds a single element by its "name" attribute.`
 *
 * This function returns a property delegate that, when accessed, finds a web element with
 * the specified name attribute. The element lookup and synchronization behavior can be
 * configured through the parameters.
 *
 * Example usage:
 * ```
 * private val userNameField by name("username")
 * private val emailField by name("email", cacheLookup = false)
 * private val submitButton by name("submit", readinessCondition = { isClickable })
 * ```
 *
 * @receiver The SearchContext instance used to search for the element.
 * @param value The value of the "name" attribute to search for.
 * @param cacheLookup If true (default), the element will be looked up only once and cached for
 *                    subsequent accesses. If false, a new lookup will be performed each time
 *                    the element is accessed.
 * @param waitConfig Configures the waiting behavior when looking up element. Specifies polling interval,
 *                   timeout, error message, and which exceptions to ignore during the wait.
 *                   Defaults to a 10-second timeout with 200ms polling.
 * @param readyCondition A predicate that determines when the found element is considered ready for use.
 *                       It's called with [WebElement] as receiver. By default, checks if element is
 *                       displayed using [isDisplayed].
 * @return A [ReadOnlyProperty] delegate that provides a [WebElement] when accessed.
 *
 * @see Wait
 * @see WebElement
 */
public fun SearchContext.name(
    value: String,
    cacheLookup: Boolean = true,
    waitConfig: Wait = defaultWaitConfig,
    readyCondition: WebElement.() -> Boolean = defaultElementReadyCondition,
): WebElementProperty = genericLocator(value, By::name, cacheLookup, waitConfig, readyCondition)

/**
 * Creates a property delegate that lazily finds all elements with the specified "name" attribute.
 *
 * This function returns a property delegate that, when accessed, finds all web elements with
 * the specified name attribute. The elements lookup and synchronization behavior can be
 * configured through the parameters.
 *
 * Example usage:
 * ```
 * private val inputFields by names("input")
 * private val dataFields by names("data", cacheLookup = false)
 * private val submitFields by names("submit", readinessCondition = { all { isClickable } })
 * ```
 *
 * @receiver The SearchContext instance used to search for the elements.
 * @param value The value of the "name" attribute to search for.
 * @param cacheLookup If true (default), the elements will be looked up only once and cached for
 *                    subsequent accesses. If false, a new lookup will be performed each time
 *                    the elements are accessed.
 * @param waitConfig Configures the waiting behavior when looking up element. Specifies polling interval,
 *                   timeout, error message, and which exceptions to ignore during the wait.
 *                   Defaults to a 10-second timeout with 200ms polling.
 * @param readyCondition A predicate that determines when the found element is considered ready for use.
 *                       It's called with [WebElement] as receiver. By default, checks if element is
 *                       displayed using [isDisplayed].
 * @return A [ReadOnlyProperty] delegate that provides a [WebElements] collection when accessed.
 *
 * @see Wait
 * @see WebElements
 */
public fun SearchContext.names(
    value: String,
    cacheLookup: Boolean = true,
    waitConfig: Wait = defaultWaitConfig,
    readyCondition: WebElements.() -> Boolean = defaultElementsReadyCondition,
): WebElementsProperty = genericLocator(value, By::name, cacheLookup, waitConfig, readyCondition)

/**
 * Creates a property delegate that lazily finds a single element containing a substring of the link text.
 *
 * This function returns a property delegate that, when accessed, finds a web element
 * containing the specified partial link text. The element lookup and synchronization behavior can
 * be configured through the parameters.
 *
 * Example usage:
 * ```
 * private val helpLink by partialLinkText("Help")
 * private val readMoreLink by partialLinkText("Read more", cacheLookup = false)
 * private val learnMoreLink by partialLinkText("Learn", readinessCondition = { isClickable })
 * ```
 *
 * @receiver The SearchContext instance used to search for the element.
 * @param value The partial text of the link to search for (case-sensitive substring).
 * @param cacheLookup If true (default), the element will be looked up only once and cached for
 *                    subsequent accesses. If false, a new lookup will be performed each time
 *                    the element is accessed.
 * @param waitConfig Configures the waiting behavior when looking up element. Specifies polling interval,
 *                   timeout, error message, and which exceptions to ignore during the wait.
 *                   Defaults to a 10-second timeout with 200ms polling.
 * @param readyCondition A predicate that determines when the found element is considered ready for use.
 *                       It's called with [WebElement] as receiver. By default, checks if element is
 *                       displayed using [isDisplayed].
 * @return A [ReadOnlyProperty] delegate that provides a [WebElement] when accessed.
 *
 * @see Wait
 * @see WebElement
 */
public fun SearchContext.partialLinkText(
    value: String,
    cacheLookup: Boolean = true,
    waitConfig: Wait = defaultWaitConfig,
    readyCondition: WebElement.() -> Boolean = defaultElementReadyCondition,
): WebElementProperty = genericLocator(value, By::partialLinkText, cacheLookup, waitConfig, readyCondition)

/**
 * Creates a property delegate that lazily finds all elements containing a substring of the link text.
 *
 * This function returns a property delegate that, when accessed, finds all web elements
 * containing the specified partial link text. The elements lookup and synchronization behavior can
 * be configured through the parameters.
 *
 * Example usage:
 * ```
 * private val infoLinks by partialLinkTexts("Info")
 * private val actionLinks by partialLinkTexts("Action", cacheLookup = false)
 * private val dynamicLinks by partialLinkTexts("Dynamic", readinessCondition = { all { isClickable } })
 * ```
 *
 * @receiver The SearchContext instance used to search for the elements.
 * @param value The partial text of the link to search for (case-sensitive substring).
 * @param cacheLookup If true (default), the elements will be looked up only once and cached for
 *                    subsequent accesses. If false, a new lookup will be performed each time
 *                    the elements are accessed.
 * @param waitConfig Configures the waiting behavior when looking up element. Specifies polling interval,
 *                   timeout, error message, and which exceptions to ignore during the wait.
 *                   Defaults to a 10-second timeout with 200ms polling.
 * @param readyCondition A predicate that determines when the found element is considered ready for use.
 *                       It's called with [WebElement] as receiver. By default, checks if element is
 *                       displayed using [isDisplayed].
 * @return A [ReadOnlyProperty] delegate that provides a [WebElements] collection when accessed.
 *
 * @see Wait
 * @see WebElements
 */
public fun SearchContext.partialLinkTexts(
    value: String,
    cacheLookup: Boolean = true,
    waitConfig: Wait = defaultWaitConfig,
    readyCondition: WebElements.() -> Boolean = defaultElementsReadyCondition,
): WebElementsProperty = genericLocator(value, By::partialLinkText, cacheLookup, waitConfig, readyCondition)

/**
 * Creates a property delegate that lazily finds a single element by its HTML tag name.
 *
 * This function returns a property delegate that, when accessed, finds a web element
 * with the specified tag name. The element lookup and synchronization behavior can
 * be configured through the parameters.
 *
 * Example usage:
 * ```
 * private val header by tagName("header")
 * private val footer by tagName("footer", cacheLookup = false)
 * private val mainContent by tagName("main", readinessCondition = { isClickable })
 * ```
 *
 * @receiver The SearchContext instance used to search for the element.
 * @param value The HTML tag name to search for (e.g., "div", "a").
 * @param cacheLookup If true (default), the element will be looked up only once and cached for
 *                    subsequent accesses. If false, a new lookup will be performed each time
 *                    the element is accessed.
 * @param waitConfig Configures the waiting behavior when looking up element. Specifies polling interval,
 *                   timeout, error message, and which exceptions to ignore during the wait.
 *                   Defaults to a 10-second timeout with 200ms polling.
 * @param readyCondition A predicate that determines when the found element is considered ready for use.
 *                       It's called with [WebElement] as receiver. By default, checks if element is
 *                       displayed using [isDisplayed].
 * @return A [ReadOnlyProperty] delegate that provides a [WebElement] when accessed.
 *
 * @see Wait
 * @see WebElement
 */
public fun SearchContext.tagName(
    value: String,
    cacheLookup: Boolean = true,
    waitConfig: Wait = defaultWaitConfig,
    readyCondition: WebElement.() -> Boolean = defaultElementReadyCondition,
): WebElementProperty = genericLocator(value, By::tagName, cacheLookup, waitConfig, readyCondition)

/**
 * Creates a property delegate that lazily finds all elements by their HTML tag name.
 *
 * This function returns a property delegate that, when accessed, finds all web elements
 * with the specified tag name. The elements lookup and synchronization behavior can
 * be configured through the parameters.
 *
 * Example usage:
 * ```
 * private val sections by tagNames("section")
 * private val paragraphs by tagNames("p", cacheLookup = false)
 * private val articles by tagNames("article", readinessCondition = { all { isClickable } })
 * ```
 *
 * @receiver The SearchContext instance used to search for the elements.
 * @param value The HTML tag name to search for (e.g., "div", "a").
 * @param cacheLookup If true (default), the elements will be looked up only once and cached for
 *                    subsequent accesses. If false, a new lookup will be performed each time
 *                    the elements are accessed.
 * @param waitConfig Configures the waiting behavior when looking up element. Specifies polling interval,
 *                   timeout, error message, and which exceptions to ignore during the wait.
 *                   Defaults to a 10-second timeout with 200ms polling.
 * @param readyCondition A predicate that determines when the found element is considered ready for use.
 *                       It's called with [WebElement] as receiver. By default, checks if element is
 *                       displayed using [isDisplayed].
 * @return A [ReadOnlyProperty] delegate that provides a [WebElements] collection when accessed.
 *
 * @see Wait
 * @see WebElements
 */
public fun SearchContext.tagNames(
    value: String,
    cacheLookup: Boolean = true,
    waitConfig: Wait = defaultWaitConfig,
    readyCondition: WebElements.() -> Boolean = defaultElementsReadyCondition,
): WebElementsProperty = genericLocator(value, By::tagName, cacheLookup, waitConfig, readyCondition)

/**
 * Creates a property delegate that lazily finds a single element using an XPath expression.
 *
 * This function returns a property delegate that, when accessed, finds a web element
 * matching the specified XPath expression. The element lookup and synchronization behavior can
 * be configured through the parameters.
 *
 * Example usage:
 * ```
 * private val submitButton by xPath("//button[@type='submit']")
 * private val headerLink by xPath("//a[contains(@class, 'header-link')]", cacheLookup = false)
 * private val actionButton by xPath("//button[text()='Click Me']", readinessCondition = { isClickable })
 * ```
 *
 * @receiver The SearchContext instance used to search for the element.
 * @param value The XPath expression. Ensure values are properly escaped (using [escapeQuotes] for dynamic values).
 * @param cacheLookup If true (default), the element will be looked up only once and cached for
 *                    subsequent accesses. If false, a new lookup will be performed each time
 *                    the element is accessed.
 * @param waitConfig Configures the waiting behavior when looking up element. Specifies polling interval,
 *                   timeout, error message, and which exceptions to ignore during the wait.
 *                   Defaults to a 10-second timeout with 200ms polling.
 * @param readyCondition A predicate that determines when the found element is considered ready for use.
 *                       It's called with [WebElement] as receiver. By default, checks if element is
 *                       displayed using [isDisplayed].
 * @return A [ReadOnlyProperty] delegate that provides a [WebElement] when accessed.
 *
 * @see Wait
 * @see WebElement
 */
public fun SearchContext.xPath(
    value: String,
    cacheLookup: Boolean = true,
    waitConfig: Wait = defaultWaitConfig,
    readyCondition: WebElement.() -> Boolean = defaultElementReadyCondition,
): WebElementProperty = genericLocator(value, By::xpath, cacheLookup, waitConfig, readyCondition)

/**
 * Creates a property delegate that lazily finds all elements matching an XPath expression.
 *
 * This function returns a property delegate that, when accessed, finds all web elements
 * matching the specified XPath expression. The elements lookup and synchronization behavior can
 * be configured through the parameters.
 *
 * Example usage:
 * ```
 * private val navButtons by xPaths("//button[contains(@class, 'nav')]")
 * private val productLinks by xPaths("//a[contains(@href, '/product')]", cacheLookup = false)
 * private val formFields by xPaths("//input", readinessCondition = { all { isClickable } })
 * ```
 *
 * @receiver The SearchContext instance used to search for the elements.
 * @param value The XPath expression. Ensure values are properly escaped (using [escapeQuotes] for dynamic values).
 * @param cacheLookup If true (default), the elements will be looked up only once and cached for
 *                    subsequent accesses. If false, a new lookup will be performed each time
 *                    the elements are accessed.
 * @param waitConfig Configures the waiting behavior when looking up element. Specifies polling interval,
 *                   timeout, error message, and which exceptions to ignore during the wait.
 *                   Defaults to a 10-second timeout with 200ms polling.
 * @param readyCondition A predicate that determines when the found element is considered ready for use.
 *                       It's called with [WebElement] as receiver. By default, checks if element is
 *                       displayed using [isDisplayed].
 * @return A [ReadOnlyProperty] delegate that provides a [WebElements] collection when accessed.
 *
 * @see Wait
 * @see WebElements
 */
public fun SearchContext.xPaths(
    value: String,
    cacheLookup: Boolean = true,
    waitConfig: Wait = defaultWaitConfig,
    readyCondition: WebElements.() -> Boolean = defaultElementsReadyCondition,
): WebElementsProperty = genericLocator(value, By::xpath, cacheLookup, waitConfig, readyCondition)

/**
 * Creates a property delegate that lazily finds an element or elements using a generic locator strategy.
 *
 * This internal function is used by the public locator functions to create property delegates
 * for finding web elements. It supports both single element and multiple elements lookups.
 *
 * @receiver The SearchContext instance used to search for the element(s).
 * @param T The type of result: [WebElement] for single elements or [WebElements] for collections.
 * @param value The locator value (e.g., CSS selector, class name, XPath) used to find the element(s).
 * @param locatorStrategy A factory function that converts [value] into a Selenium [By] locator.
 * @param cacheLookup If true (default), the element(s) will be looked up only once and cached for
 *                    subsequent accesses. If false, a new lookup will be performed each time the
 *                    element(s) are accessed.
 * @param waitConfig Configuration for waiting (timeout, polling interval, ignored exceptions).
 *                   Defaults to a 10-second timeout with 200ms polling.
 * @param readyCondition A predicate that determines when the found element(s) are considered ready for use.
 *                       It's called with either [WebElement] or [WebElements] as receiver. By default,
 *                       checks if the element(s) are displayed.
 * @return A [ReadOnlyProperty] delegate that provides either a [WebElement] or [WebElements] when accessed.
 *
 * @see WebElement
 * @see WebElements
 * @suppress UNCHECKED_CAST Safely enforced by reified type checks at runtime.
 */
@Suppress("UNCHECKED_CAST")
internal inline fun <reified T> SearchContext.genericLocator(
    value: String,
    noinline locatorStrategy: (String) -> By,
    cacheLookup: Boolean,
    waitConfig: Wait,
    noinline readyCondition: T.() -> Boolean,
): ReadOnlyProperty<Any?, T> {
    require(value.isNotBlank()) { "\"value\" must not be blank" }

    return when (T::class) {
        WebElement::class ->
            KWebElement(
                value,
                locatorStrategy,
                cacheLookup,
                waitConfig,
                readyCondition as WebElement.() -> Boolean,
            )

        List::class ->
            KWebElements(
                value,
                locatorStrategy,
                cacheLookup,
                waitConfig,
                readyCondition as WebElements.() -> Boolean,
            )

        else -> throw IllegalArgumentException("Unsupported type: ${T::class.simpleName}")
    } as ReadOnlyProperty<Any?, T>
}

context(SearchContext)
internal abstract class KWebElementBase<T : KWebElementBase<T, R>, R> {
    protected val searchContext by lazy {
        val projectLevelDecorators = SeleniumProjectConfiguration.actualConfig().decorators
        val testLevelDecorators = DecoratorManager.getAllDecorators()

        if (testLevelDecorators.isEmpty()) {
            if (projectLevelDecorators.isEmpty()) {
                this@SearchContext
            } else {
                DecoratorManager.combine(projectLevelDecorators)(this@SearchContext)
            }
        } else {
            DecoratorManager.combine(testLevelDecorators)(this@SearchContext)
        }
    }

    protected abstract fun findElement(): R

    protected abstract fun clearCache()

    protected abstract fun isElementReady(element: R): Boolean

    protected fun initializeWait(wait: Wait): FluentWait<T> =
        @Suppress("UNCHECKED_CAST")
        FluentWait(this as T).apply {
            wait.apply {
                timeout?.let { withTimeout(it.toJavaDuration()) }
                pollingInterval?.let { pollingEvery(it.toJavaDuration()) }
                message?.let { withMessage { it } }
                if (ignoring.isNotEmpty()) {
                    ignoreAll(ignoring.map { it.java })
                }
            }
        }

    protected fun getValueInternal(
        propertyName: String,
        value: String,
        by: (String) -> By,
        wait: FluentWait<T>,
    ): R {
        wait.until {
            logger.trace { "Waiting for \"${propertyName}\" with locator strategy of { ${by(value)} }" }
            try {
                val element = findElement()
                isElementReady(element)
            } catch (e: StaleElementReferenceException) {
                logger.warn { "\"$propertyName\" element(s) with locator strategy of { ${by(value)} } became stale. Relocating." }
                clearCache()
                false
            }
        }
        return findElement()
    }
}

context(SearchContext)
internal class KWebElement(
    private val value: String,
    private val locatorStrategy: (String) -> By,
    private val cacheLookup: Boolean,
    waitConfig: Wait,
    private val readyCondition: WebElement.() -> Boolean,
) : KWebElementBase<KWebElement, WebElement>(),
    WebElementProperty {
    private var cachedWebElement: WebElement? = null
    private val wait: FluentWait<KWebElement> by lazy { initializeWait(waitConfig) }

    override fun getValue(
        thisRef: Any?,
        property: KProperty<*>,
    ): WebElement = getValueInternal(property.name, value, locatorStrategy, wait)

    override fun findElement(): WebElement =
        if (cacheLookup) {
            cachedWebElement ?: searchContext.findElement(locatorStrategy(value)).also { cachedWebElement = it }
        } else {
            searchContext.findElement(locatorStrategy(value))
        }

    override fun clearCache() {
        cachedWebElement = null
    }

    override fun isElementReady(element: WebElement): Boolean = element.readyCondition()
}

context(SearchContext)
internal class KWebElements(
    private val value: String,
    private val locatorStrategy: (String) -> By,
    private val cacheLookup: Boolean,
    waitConfig: Wait,
    private val readyCondition: WebElements.() -> Boolean,
) : KWebElementBase<KWebElements, WebElements>(),
    WebElementsProperty {
    private var cachedWebElements: WebElements? = null
    private val wait: FluentWait<KWebElements> by lazy { initializeWait(waitConfig) }

    override fun getValue(
        thisRef: Any?,
        property: KProperty<*>,
    ): WebElements = getValueInternal(property.name, value, locatorStrategy, wait)

    override fun findElement(): WebElements =
        if (cacheLookup) {
            cachedWebElements ?: searchContext.findElements(locatorStrategy(value)).also { cachedWebElements = it }
        } else {
            searchContext.findElements(locatorStrategy(value))
        }

    override fun clearCache() {
        cachedWebElements = null
    }

    override fun isElementReady(element: WebElements): Boolean = element.readyCondition()
}

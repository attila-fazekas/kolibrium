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
 * Creates a property delegate that lazily finds an element using the className locator strategy.
 *
 * This function returns a property delegate that, when accessed, finds a web element
 * with the specified class name. The element lookup and synchronization behavior can
 * be configured through the parameters.
 *
 * Example usage:
 * ```
 * private val submitButton by className("submit-btn")
 * private val badge by className("badge", cacheLookup = false))
 * private val menuButton by className("nav-menu", readyWhen = { isDisplayed && isEnabled })
 * ```
 *
 * @receiver The SearchContext instance used to search for the element.
 * @param locator The value of the "class" attribute to search for. If multiple classes are
 *                specified, the element must have all of them to match.
 * @param cacheLookup If true (default), the elements will be looked up only once and cached for
 *                     subsequent accesses. If false, a new lookup will be performed each time
 *                     the elements are accessed.
 * @param wait Configures the waiting behavior when looking up elements. Specifies polling interval,
 *            timeout, error message, and which exceptions to ignore during the wait.
 *            Defaults to a 10-second timeout with 200ms polling.
 * @param readyWhen A predicate that determines when the found element is considered ready for use.
 *                  It's called with [WebElement] as receiver. By default, checks if element is
 *                  displayed using [isDisplayed].
 * @return A [ReadOnlyProperty] delegate that provides a [WebElement] when accessed.
 *
 * @see Wait
 * @see WebElement
 */
public fun SearchContext.className(
    locator: String,
    cacheLookup: Boolean = true,
    wait: Wait = defaultWait,
    readyWhen: WebElement.() -> Boolean = defaultElementReadyWhen,
): WebElementProperty = genericLocator(locator, By::className, cacheLookup, wait, readyWhen)

/**
 * Creates a property delegate that lazily finds multiple elements using the className locator strategy.
 *
 * This function returns a property delegate that, when accessed, finds all web elements
 * with the specified class name. The elements lookup and synchronization behavior can be
 * configured through the parameters.
 *
 * Example usage:
 * ```
 * private val items by classNames("item-class")
 * private val badges by classNames("badge", cacheLookup = false)
 * private val menus by classNames("menu-item", readyWhen = { all { isDisplayed && isEnabled } })
 * ```
 *
 * @receiver The SearchContext instance used to search for the elements.
 * @param locator The value of the "class" attribute to search for. If multiple classes are
 *                specified, elements must have all of them to match.
 * @param cacheLookup If true (default), the elements will be looked up only once and cached for
 *                     subsequent accesses. If false, a new lookup will be performed each time
 *                     the elements are accessed.
 * @param wait Configures the waiting behavior when looking up elements. Specifies polling interval,
 *            timeout, error message, and which exceptions to ignore during the wait.
 *            Defaults to a 10-second timeout with 200ms polling.
 * @param readyWhen A predicate that determines when the found elements are considered ready for use.
 *                  It's called with [WebElements] as receiver. By default, checks if elements are
 *                  displayed using [isDisplayed].
 * @return A [ReadOnlyProperty] delegate that provides a [WebElements] collection when accessed.
 *
 * @see Wait
 * @see WebElements
 */
public fun SearchContext.classNames(
    locator: String,
    cacheLookup: Boolean = true,
    wait: Wait = defaultWait,
    readyWhen: WebElements.() -> Boolean = defaultElementsReadyWhen,
): WebElementsProperty = genericLocator(locator, By::className, cacheLookup, wait, readyWhen)

/**
 * Creates a property delegate that lazily finds an element using the CSS selector locator strategy.
 *
 * This function returns a property delegate that, when accessed, finds a web element matching the
 * specified CSS selector. The element lookup and synchronization behavior can be configured
 * through the parameters.
 *
 * Example usage:
 * ```
 * private val container by cssSelector(".container")
 * private val label by cssSelector(".label", cacheLookup = false)
 * private val inputField by cssSelector("#input-field", readyWhen = { isDisplayed && isEnabled })
 * ```
 *
 * @receiver The SearchContext instance used to search for the elements.
 * @param locator The CSS selector to locate the element.
 * @param cacheLookup If true (default), the elements will be looked up only once and cached for
 *                     subsequent accesses. If false, a new lookup will be performed each time
 *                     the elements are accessed.
 * @param wait Configures the waiting behavior when looking up elements. Specifies polling interval,
 *            timeout, error message, and which exceptions to ignore during the wait.
 *            Defaults to a 10-second timeout with 200ms polling.
 * @param readyWhen A predicate that determines when the found element is considered ready for use.
 *                  It's called with [WebElement] as receiver. By default, checks if element is
 *                  displayed using [isDisplayed].
 * @return A [ReadOnlyProperty] delegate that provides a [WebElement] when accessed.
 *
 * @see Wait
 * @see WebElement
 */
public fun SearchContext.cssSelector(
    locator: String,
    cacheLookup: Boolean = true,
    wait: Wait = defaultWait,
    readyWhen: WebElement.() -> Boolean = defaultElementReadyWhen,
): WebElementProperty = genericLocator(locator, By::cssSelector, cacheLookup, wait, readyWhen)

/**
 * Creates a property delegate that lazily finds multiple elements using the CSS selector locator strategy.
 *
 * This function returns a property delegate that, when accessed, finds all web elements matching the
 * specified CSS selector. The elements lookup and synchronization behavior can be configured
 * through the parameters.
 *
 * Example usage:
 * ```
 * private val cards by cssSelectors(".card")
 * private val labels by cssSelectors(".label", cacheLookup = false)
 * private val fields by cssSelectors(".form-field", readyWhen = { all { isDisplayed && isEnabled } })
 * ```
 *
 * @receiver The SearchContext instance used to search for the elements.
 * @param locator The CSS selector to locate the elements.
 * @param cacheLookup If true (default), the elements will be looked up only once and cached for
 *                     subsequent accesses. If false, a new lookup will be performed each time
 *                     the elements are accessed.
 * @param wait Configures the waiting behavior when looking up elements. Specifies polling interval,
 *            timeout, error message, and which exceptions to ignore during the wait.
 *            Defaults to a 10-second timeout with 200ms polling.
 * @param readyWhen A predicate that determines when the found elements are considered ready for use.
 *                  It's called with [WebElements] as receiver. By default, checks if elements are
 *                  displayed using [isDisplayed].
 * @return A [ReadOnlyProperty] delegate that provides a [WebElements] collection when accessed.
 *
 * @see Wait
 * @see WebElements
 */
public fun SearchContext.cssSelectors(
    locator: String,
    cacheLookup: Boolean = true,
    wait: Wait = defaultWait,
    readyWhen: WebElements.() -> Boolean = defaultElementsReadyWhen,
): WebElementsProperty = genericLocator(locator, By::cssSelector, cacheLookup, wait, readyWhen)

/**
 * Creates a property delegate that lazily finds an element using the id locator strategy.
 *
 * This function returns a property delegate that, when accessed, finds a web element with
 * the specified id attribute. The element lookup and synchronization behavior can be
 * configured through the parameters.
 *
 * Example usage:
 * ```
 * private val submitButton by id("submit")
 * private val searchBox by id("search", cacheLookup = false)
 * private val loginButton by id("login", readyWhen = { isDisplayed && isEnabled })
 * ```
 *
 * @receiver The SearchContext instance used to search for the elements.
 * @param locator The value of the "id" attribute to search for.
 * @param cacheLookup If true (default), the elements will be looked up only once and cached for
 *                     subsequent accesses. If false, a new lookup will be performed each time
 *                     the elements are accessed.
 * @param wait Configures the waiting behavior when looking up elements. Specifies polling interval,
 *            timeout, error message, and which exceptions to ignore during the wait.
 *            Defaults to a 10-second timeout with 200ms polling.
 * @param readyWhen A predicate that determines when the found element is considered ready for use.
 *                  It's called with [WebElement] as receiver. By default, checks if element is
 *                  displayed using [isDisplayed].
 * @return A [ReadOnlyProperty] delegate that provides a [WebElement] when accessed.
 *
 * @see Wait
 * @see WebElement
 */
public fun SearchContext.id(
    locator: String,
    cacheLookup: Boolean = true,
    wait: Wait = defaultWait,
    readyWhen: WebElement.() -> Boolean = defaultElementReadyWhen,
): WebElementProperty = genericLocator(locator, By::id, cacheLookup, wait, readyWhen)

/**
 * Creates a property delegate that lazily finds an element using either the id or name locator strategy.
 *
 * This function returns a property delegate that, when accessed, finds a web element with the specified
 * id or name attribute. The element lookup and synchronization behavior can be configured through
 * the parameters.
 *
 * Example usage:
 * ```
 * private val userField by idOrName("user-input")
 * private val passwordField by idOrName("password", cacheLookup = false)
 * private val submitField by idOrName("submit", readyWhen = { isDisplayed && isEnabled })
 * ```
 *
 * @receiver The SearchContext instance used to search for the elements.
 * @param locator The value of either the "id" or "name" attribute to search for.
 * @param cacheLookup If true (default), the elements will be looked up only once and cached for
 *                     subsequent accesses. If false, a new lookup will be performed each time
 *                     the elements are accessed.
 * @param wait Configures the waiting behavior when looking up elements. Specifies polling interval,
 *            timeout, error message, and which exceptions to ignore during the wait.
 *            Defaults to a 10-second timeout with 200ms polling.
 * @param readyWhen A predicate that determines when the found element is considered ready for use.
 *                  It's called with [WebElement] as receiver. By default, checks if element is
 *                  displayed using [isDisplayed].
 * @return A [ReadOnlyProperty] delegate that provides a [WebElement] when accessed.
 *
 * @see Wait
 * @see WebElement
 * @see ByIdOrName
 */
public fun SearchContext.idOrName(
    locator: String,
    cacheLookup: Boolean = true,
    wait: Wait = defaultWait,
    readyWhen: WebElement.() -> Boolean = defaultElementReadyWhen,
): WebElementProperty = genericLocator(locator, ::ByIdOrName, cacheLookup, wait, readyWhen)

/**
 * Creates a property delegate that lazily finds an element using the link text locator strategy.
 *
 * This function returns a property delegate that, when accessed, finds a web element with
 * the specified link text. The element lookup and synchronization behavior can be
 * configured through the parameters.
 *
 * Example usage:
 * ```
 * private val homeLink by linkText("Home")
 * private val aboutLink by linkText("About", cacheLookup = false)
 * private val contactLink by linkText("Contact", readyWhen = { isDisplayed && isEnabled })
 * ```
 *
 * @receiver The SearchContext instance used to search for the elements.
 * @param locator The exact text of the link to search for.
 * @param cacheLookup If true (default), the elements will be looked up only once and cached for
 *                     subsequent accesses. If false, a new lookup will be performed each time
 *                     the elements are accessed.
 * @param wait Configures the waiting behavior when looking up elements. Specifies polling interval,
 *            timeout, error message, and which exceptions to ignore during the wait.
 *            Defaults to a 10-second timeout with 200ms polling.
 * @param readyWhen A predicate that determines when the found element is considered ready for use.
 *                  It's called with [WebElement] as receiver. By default, checks if element is
 *                  displayed using [isDisplayed].
 * @return A [ReadOnlyProperty] delegate that provides a [WebElement] when accessed.
 *
 * @see Wait
 * @see WebElement
 */
public fun SearchContext.linkText(
    locator: String,
    cacheLookup: Boolean = true,
    wait: Wait = defaultWait,
    readyWhen: WebElement.() -> Boolean = defaultElementReadyWhen,
): WebElementProperty = genericLocator(locator, By::linkText, cacheLookup, wait, readyWhen)

/**
 * Creates a property delegate that lazily finds multiple elements using the link text locator strategy.
 *
 * This function returns a property delegate that, when accessed, finds all web elements
 * with the specified link text. The elements lookup and synchronization behavior can be
 * configured through the parameters.
 *
 * Example usage:
 * ```
 * private val navLinks by linkTexts("NavItem")
 * private val footerLinks by linkTexts("FooterLink", cacheLookup = false)
 * private val sidebarLinks by linkTexts("SidebarLink", readyWhen = { all { isDisplayed && isEnabled } })
 * ```
 *
 * @receiver The SearchContext instance used to search for the elements.
 * @param locator The exact text of the links to search for.
 * @param cacheLookup If true (default), the elements will be looked up only once and cached for
 *                     subsequent accesses. If false, a new lookup will be performed each time
 *                     the elements are accessed.
 * @param wait Configures the waiting behavior when looking up elements. Specifies polling interval,
 *            timeout, error message, and which exceptions to ignore during the wait.
 *            Defaults to a 10-second timeout with 200ms polling.
 * @param readyWhen A predicate that determines when the found elements are considered ready for use.
 *                  It's called with [WebElements] as receiver. By default, checks if elements are
 *                  displayed using [isDisplayed].
 * @return A [ReadOnlyProperty] delegate that provides a [WebElements] collection when accessed.
 *
 * @see Wait
 * @see WebElements
 */
public fun SearchContext.linkTexts(
    locator: String,
    cacheLookup: Boolean = true,
    wait: Wait = defaultWait,
    readyWhen: WebElements.() -> Boolean = defaultElementsReadyWhen,
): WebElementsProperty = genericLocator(locator, By::linkText, cacheLookup, wait, readyWhen)

/**
 * Creates a property delegate that lazily finds an element using the name locator strategy.
 *
 * This function returns a property delegate that, when accessed, finds a web element with
 * the specified name attribute. The element lookup and synchronization behavior can be
 * configured through the parameters.
 *
 * Example usage:
 * ```
 * private val userNameField by name("username")
 * private val emailField by name("email", cacheLookup = false)
 * private val submitButton by name("submit", readyWhen = { isDisplayed && isEnabled })
 * ```
 *
 * @receiver The SearchContext instance used to search for the elements.
 * @param locator The value of the "name" attribute to search for.
 * @param cacheLookup If true (default), the elements will be looked up only once and cached for
 *                     subsequent accesses. If false, a new lookup will be performed each time
 *                     the elements are accessed.
 * @param wait Configures the waiting behavior when looking up elements. Specifies polling interval,
 *            timeout, error message, and which exceptions to ignore during the wait.
 *            Defaults to a 10-second timeout with 200ms polling.
 * @param readyWhen A predicate that determines when the found element is considered ready for use.
 *                  It's called with [WebElement] as receiver. By default, checks if element is
 *                  displayed using [isDisplayed].
 * @return A [ReadOnlyProperty] delegate that provides a [WebElement] when accessed.
 *
 * @see Wait
 * @see WebElement
 */
public fun SearchContext.name(
    locator: String,
    cacheLookup: Boolean = true,
    wait: Wait = defaultWait,
    readyWhen: WebElement.() -> Boolean = defaultElementReadyWhen,
): WebElementProperty = genericLocator(locator, By::name, cacheLookup, wait, readyWhen)

/**
 * Creates a property delegate that lazily finds multiple elements using the name locator strategy.
 *
 * This function returns a property delegate that, when accessed, finds all web elements with
 * the specified name attribute. The elements lookup and synchronization behavior can be
 * configured through the parameters.
 *
 * Example usage:
 * ```
 * private val inputFields by names("input")
 * private val dataFields by names("data", cacheLookup = false)
 * private val submitFields by names("submit", readyWhen = { all { isDisplayed && isEnabled } })
 * ```
 *
 * @receiver The SearchContext instance used to search for the elements.
 * @param locator The value of the "name" attribute to search for.
 * @param cacheLookup If true (default), the elements will be looked up only once and cached for
 *                     subsequent accesses. If false, a new lookup will be performed each time
 *                     the elements are accessed.
 * @param wait Configures the waiting behavior when looking up elements. Specifies polling interval,
 *            timeout, error message, and which exceptions to ignore during the wait.
 *            Defaults to a 10-second timeout with 200ms polling.
 * @param readyWhen A predicate that determines when the found elements are considered ready for use.
 *                  It's called with [WebElements] as receiver. By default, checks if elements are
 *                  displayed using [isDisplayed].
 * @return A [ReadOnlyProperty] delegate that provides a [WebElements] collection when accessed.
 *
 * @see Wait
 * @see WebElements
 */
public fun SearchContext.names(
    locator: String,
    cacheLookup: Boolean = true,
    wait: Wait = defaultWait,
    readyWhen: WebElements.() -> Boolean = defaultElementsReadyWhen,
): WebElementsProperty = genericLocator(locator, By::name, cacheLookup, wait, readyWhen)

/**
 * Creates a property delegate that lazily finds an element using the partial link text locator strategy.
 *
 * This function returns a property delegate that, when accessed, finds a web element
 * containing the specified partial link text. The element lookup and synchronization behavior can
 * be configured through the parameters.
 *
 * Example usage:
 * ```
 * private val helpLink by partialLinkText("Help")
 * private val readMoreLink by partialLinkText("Read more", cacheLookup = false)
 * private val learnMoreLink by partialLinkText("Learn", readyWhen = { isDisplayed && isEnabled })
 * ```
 *
 * @receiver The SearchContext instance used to search for the elements.
 * @param locator The value of the "name" attribute to search for.
 * @param cacheLookup If true (default), the elements will be looked up only once and cached for
 *                     subsequent accesses. If false, a new lookup will be performed each time
 *                     the elements are accessed.
 * @param wait Configures the waiting behavior when looking up elements. Specifies polling interval,
 *            timeout, error message, and which exceptions to ignore during the wait.
 *            Defaults to a 10-second timeout with 200ms polling.
 * @param readyWhen A predicate that determines when the found element is considered ready for use.
 *                  It's called with [WebElement] as receiver. By default, checks if element is
 *                  displayed using [isDisplayed].
 * @return A [ReadOnlyProperty] delegate that provides a [WebElement] when accessed.
 *
 * @see Wait
 * @see WebElement
 */
public fun SearchContext.partialLinkText(
    locator: String,
    cacheLookup: Boolean = true,
    wait: Wait = defaultWait,
    readyWhen: WebElement.() -> Boolean = defaultElementReadyWhen,
): WebElementProperty = genericLocator(locator, By::partialLinkText, cacheLookup, wait, readyWhen)

/**
 * Creates a property delegate that lazily finds multiple elements using the partial link text locator strategy.
 *
 * This function returns a property delegate that, when accessed, finds all web elements
 * containing the specified partial link text. The elements lookup and synchronization behavior can
 * be configured through the parameters.
 *
 * Example usage:
 * ```
 * private val infoLinks by partialLinkTexts("Info")
 * private val actionLinks by partialLinkTexts("Action", cacheLookup = false)
 * private val dynamicLinks by partialLinkTexts("Dynamic", readyWhen = { all { isDisplayed && isEnabled } })
 * ```
 *
 * @receiver The SearchContext instance used to search for the elements.
 * @param locator The value of the "name" attribute to search for.
 * @param cacheLookup If true (default), the elements will be looked up only once and cached for
 *                     subsequent accesses. If false, a new lookup will be performed each time
 *                     the elements are accessed.
 * @param wait Configures the waiting behavior when looking up elements. Specifies polling interval,
 *            timeout, error message, and which exceptions to ignore during the wait.
 *            Defaults to a 10-second timeout with 200ms polling.
 * @param readyWhen A predicate that determines when the found elements are considered ready for use.
 *                  It's called with [WebElements] as receiver. By default, checks if elements are
 *                  displayed using [isDisplayed].
 * @return A [ReadOnlyProperty] delegate that provides a [WebElements] collection when accessed.
 *
 * @see Wait
 * @see WebElements
 */
public fun SearchContext.partialLinkTexts(
    locator: String,
    cacheLookup: Boolean = true,
    wait: Wait = defaultWait,
    readyWhen: WebElements.() -> Boolean = defaultElementsReadyWhen,
): WebElementsProperty = genericLocator(locator, By::partialLinkText, cacheLookup, wait, readyWhen)

/**
 * Creates a property delegate that lazily finds an element using the tag name locator strategy.
 *
 * This function returns a property delegate that, when accessed, finds a web element
 * with the specified tag name. The element lookup and synchronization behavior can
 * be configured through the parameters.
 *
 * Example usage:
 * ```
 * private val header by tagName("header")
 * private val footer by tagName("footer", cacheLookup = false)
 * private val mainContent by tagName("main", readyWhen = { isDisplayed && isEnabled })
 * ```
 *
 * @receiver The SearchContext instance used to search for the elements.
 * @param locator The value of the "name" attribute to search for.
 * @param cacheLookup If true (default), the elements will be looked up only once and cached for
 *                     subsequent accesses. If false, a new lookup will be performed each time
 *                     the elements are accessed.
 * @param wait Configures the waiting behavior when looking up elements. Specifies polling interval,
 *            timeout, error message, and which exceptions to ignore during the wait.
 *            Defaults to a 10-second timeout with 200ms polling.
 * @param readyWhen A predicate that determines when the found element is considered ready for use.
 *                  It's called with [WebElement] as receiver. By default, checks if element is
 *                  displayed using [isDisplayed].
 * @return A [ReadOnlyProperty] delegate that provides a [WebElement] when accessed.
 *
 * @see Wait
 * @see WebElement
 */
public fun SearchContext.tagName(
    locator: String,
    cacheLookup: Boolean = true,
    wait: Wait = defaultWait,
    readyWhen: WebElement.() -> Boolean = defaultElementReadyWhen,
): WebElementProperty = genericLocator(locator, By::tagName, cacheLookup, wait, readyWhen)

/**
 * Creates a property delegate that lazily finds multiple elements using the tag name locator strategy.
 *
 * This function returns a property delegate that, when accessed, finds all web elements
 * with the specified tag name. The elements lookup and synchronization behavior can
 * be configured through the parameters.
 *
 * Example usage:
 * ```
 * private val sections by tagNames("section")
 * private val paragraphs by tagNames("p", cacheLookup = false)
 * private val articles by tagNames("article", readyWhen = { all { isDisplayed && isEnabled } })
 * ```
 *
 * @receiver The SearchContext instance used to search for the elements.
 * @param locator The value of the "name" attribute to search for.
 * @param cacheLookup If true (default), the elements will be looked up only once and cached for
 *                     subsequent accesses. If false, a new lookup will be performed each time
 *                     the elements are accessed.
 * @param wait Configures the waiting behavior when looking up elements. Specifies polling interval,
 *            timeout, error message, and which exceptions to ignore during the wait.
 *            Defaults to a 10-second timeout with 200ms polling.
 * @param readyWhen A predicate that determines when the found elements are considered ready for use.
 *                  It's called with [WebElements] as receiver. By default, checks if elements are
 *                  displayed using [isDisplayed].
 * @return A [ReadOnlyProperty] delegate that provides a [WebElements] collection when accessed.
 *
 * @see Wait
 * @see WebElements
 */
public fun SearchContext.tagNames(
    locator: String,
    cacheLookup: Boolean = true,
    wait: Wait = defaultWait,
    readyWhen: WebElements.() -> Boolean = defaultElementsReadyWhen,
): WebElementsProperty = genericLocator(locator, By::tagName, cacheLookup, wait, readyWhen)

/**
 * Creates a property delegate that lazily finds an element using the XPath locator strategy.
 *
 * This function returns a property delegate that, when accessed, finds a web element
 * matching the specified XPath expression. The element lookup and synchronization behavior can
 * be configured through the parameters.
 *
 * Example usage:
 * ```
 * private val submitButton by xPath("//button[@type='submit']")
 * private val headerLink by xPath("//a[contains(@class, 'header-link')]", cacheLookup = false)
 * private val actionButton by xPath("//button[text()='Click Me']", readyWhen = { isDisplayed && isEnabled })
 * ```
 *
 * @receiver The SearchContext instance used to search for the elements.
 * @param locator The value of the "name" attribute to search for.
 * @param cacheLookup If true (default), the elements will be looked up only once and cached for
 *                     subsequent accesses. If false, a new lookup will be performed each time
 *                     the elements are accessed.
 * @param wait Configures the waiting behavior when looking up elements. Specifies polling interval,
 *            timeout, error message, and which exceptions to ignore during the wait.
 *            Defaults to a 10-second timeout with 200ms polling.
 * @param readyWhen A predicate that determines when the found element is considered ready for use.
 *                  It's called with [WebElement] as receiver. By default, checks if element is
 *                  displayed using [isDisplayed].
 * @return A [ReadOnlyProperty] delegate that provides a [WebElement] when accessed.
 *
 * @see Wait
 * @see WebElement
 */
public fun SearchContext.xPath(
    locator: String,
    cacheLookup: Boolean = true,
    wait: Wait = defaultWait,
    readyWhen: WebElement.() -> Boolean = defaultElementReadyWhen,
): WebElementProperty = genericLocator(locator, By::xpath, cacheLookup, wait, readyWhen)

/**
 * Creates a property delegate that lazily finds multiple elements using the XPath locator strategy.
 *
 * This function returns a property delegate that, when accessed, finds all web elements
 * matching the specified XPath expression. The elements lookup and synchronization behavior can
 * be configured through the parameters.
 *
 * Example usage:
 * ```
 * private val navButtons by xPaths("//button[contains(@class, 'nav')]")
 * private val productLinks by xPaths("//a[contains(@href, '/product')]", cacheLookup = false)
 * private val formFields by xPaths("//input", readyWhen = { all { isDisplayed && isEnabled } })
 * ```
 *
 * @receiver The SearchContext instance used to search for the elements.
 * @param locator The value of the "name" attribute to search for.
 * @param cacheLookup If true (default), the elements will be looked up only once and cached for
 *                     subsequent accesses. If false, a new lookup will be performed each time
 *                     the elements are accessed.
 * @param wait Configures the waiting behavior when looking up elements. Specifies polling interval,
 *            timeout, error message, and which exceptions to ignore during the wait.
 *            Defaults to a 10-second timeout with 200ms polling.
 * @param readyWhen A predicate that determines when the found elements are considered ready for use.
 *                  It's called with [WebElements] as receiver. By default, checks if elements are
 *                  displayed using [isDisplayed].
 * @return A [ReadOnlyProperty] delegate that provides a [WebElements] collection when accessed.
 *
 * @see Wait
 * @see WebElements
 */
public fun SearchContext.xPaths(
    locator: String,
    cacheLookup: Boolean = true,
    wait: Wait = defaultWait,
    readyWhen: WebElements.() -> Boolean = defaultElementsReadyWhen,
): WebElementsProperty = genericLocator(locator, By::xpath, cacheLookup, wait, readyWhen)

/**
 * Creates a property delegate that lazily finds an element or elements using a generic locator strategy.
 *
 * This internal function is used by the public locator functions to create property delegates
 * for finding web elements. It supports both single element and multiple elements lookups.
 *
 * @receiver The SearchContext instance used to search for the element(s).
 * @param T The type of the result, either [WebElement] or [WebElements].
 * @param locator The locator string used to find the element(s).
 * @param by A function that converts the locator string to a Selenium [By] object.
 * @param cacheLookup If true (default), the element(s) will be looked up only once and cached for
 *                    subsequent accesses. If false, a new lookup will be performed each time the
 *                    element(s) are accessed.
 * @param wait Configures the waiting behavior when looking up elements. Specifies polling interval,
 *             timeout, error message, and which exceptions to ignore during the wait.
 *             Defaults to a 10-second timeout with 200ms polling.
 * @param readyWhen A predicate that determines when the found element(s) are considered ready for use.
 *                  It's called with either [WebElement] or [WebElements] as receiver. By default,
 *                  checks if the element(s) are displayed.
 * @return A [ReadOnlyProperty] delegate that provides either a [WebElement] or [WebElements] when accessed.
 *
 * @see WebElement
 * @see WebElements
 */
@Suppress("UNCHECKED_CAST")
internal inline fun <reified T> SearchContext.genericLocator(
    locator: String,
    noinline by: (String) -> By,
    cacheLookup: Boolean,
    wait: Wait,
    noinline readyWhen: T.() -> Boolean,
): ReadOnlyProperty<Any?, T> {
    require(locator.isNotBlank()) { "\"locator\" must not be blank" }

    return when (T::class) {
        WebElement::class -> KWebElement(locator, by, cacheLookup, wait, readyWhen as WebElement.() -> Boolean)
        List::class -> KWebElements(locator, by, cacheLookup, wait, readyWhen as WebElements.() -> Boolean)
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
        locator: String,
        by: (String) -> By,
        wait: FluentWait<T>,
    ): R {
        wait.until {
            logger.trace { "Waiting for \"${propertyName}\" with locator strategy of { ${by(locator)} }" }
            try {
                val element = findElement()
                isElementReady(element)
            } catch (e: StaleElementReferenceException) {
                logger.warn { "\"$propertyName\" element(s) with locator strategy of { ${by(locator)} } became stale. Relocating." }
                clearCache()
                false
            }
        }
        return findElement()
    }
}

context(SearchContext)
internal class KWebElement(
    private val locator: String,
    private val by: (String) -> By,
    private val cacheLookup: Boolean,
    wait: Wait,
    private val readyWhen: WebElement.() -> Boolean,
) : KWebElementBase<KWebElement, WebElement>(),
    WebElementProperty {
    private var cachedWebElement: WebElement? = null
    private val wait: FluentWait<KWebElement> by lazy { initializeWait(wait) }

    override fun getValue(
        thisRef: Any?,
        property: KProperty<*>,
    ): WebElement = getValueInternal(property.name, locator, by, wait)

    override fun findElement(): WebElement =
        if (cacheLookup) {
            cachedWebElement ?: searchContext.findElement(by(locator)).also { cachedWebElement = it }
        } else {
            searchContext.findElement(by(locator))
        }

    override fun clearCache() {
        cachedWebElement = null
    }

    override fun isElementReady(element: WebElement): Boolean = element.readyWhen()
}

context(SearchContext)
internal class KWebElements(
    private val locator: String,
    private val by: (String) -> By,
    private val cacheLookup: Boolean,
    wait: Wait,
    private val readyWhen: WebElements.() -> Boolean,
) : KWebElementBase<KWebElements, WebElements>(),
    WebElementsProperty {
    private var cachedWebElements: WebElements? = null
    private val wait: FluentWait<KWebElements> by lazy { initializeWait(wait) }

    override fun getValue(
        thisRef: Any?,
        property: KProperty<*>,
    ): WebElements = getValueInternal(property.name, locator, by, wait)

    override fun findElement(): WebElements =
        if (cacheLookup) {
            cachedWebElements ?: searchContext.findElements(by(locator)).also { cachedWebElements = it }
        } else {
            searchContext.findElements(by(locator))
        }

    override fun clearCache() {
        cachedWebElements = null
    }

    override fun isElementReady(element: WebElements): Boolean = element.readyWhen()
}

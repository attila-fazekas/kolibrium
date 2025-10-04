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

@file:OptIn(InternalKolibriumApi::class)

package dev.kolibrium.core.selenium

import dev.kolibrium.common.InternalKolibriumApi
import dev.kolibrium.common.WebElements
import dev.kolibrium.core.selenium.decorators.AbstractDecorator
import dev.kolibrium.core.selenium.decorators.DecoratorManager
import org.openqa.selenium.By
import org.openqa.selenium.NoSuchElementException
import org.openqa.selenium.SearchContext
import org.openqa.selenium.StaleElementReferenceException
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ByIdOrName
import org.openqa.selenium.support.ui.FluentWait
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

// Descriptor APIs: expose By and act as a property delegate for element resolution

/**
 * Marker interface exposing the Selenium By locator used to find element(s).
 * Useful for debugging and to integrate with custom find/wait utilities.
 */
public interface HasBy {
    /** The Selenium By locator associated with this descriptor. */
    public val by: By
}

/**
 * Descriptor for a single Selenium WebElement found by a locator delegate.
 *
 * Exposes the underlying By for debugging, optional wait configuration, and an optional readiness
 * predicate. It also acts as a Kotlin ReadOnlyProperty to enable `by ...` delegation in pages.
 *
 * Thread-safety: Descriptors may cache resolved elements for performance and are not thread-safe.
 * They are intended for single-threaded test usage (typical for page objects). Avoid sharing the same
 * descriptor instance across threads. If you must access from multiple threads, create separate owning
 * page instances or disable caching via `cacheLookup = false`.
 *
 * toString() expectations
 * - Calling toString() on a descriptor yields a stable, human-friendly summary including:
 *   by, cacheLookup, ctx, and waitConfig=(timeout=..., polling=...), plus a decorators field.
 *   Values not applicable are shown as "N/A". ctx shows the underlying, undecorated SearchContext type.
 *   decorators is always present: it's a class list like [HighlighterDecorator, SlowMotionDecorator] when
 *   any decorators are applied, or "N/A" when none are applied.
 * - Important: Calling toString() on the delegated WebElement (e.g., val e by id("..."); e.toString())
 *   will print Selenium's element string, not the descriptor summary. Keep a reference to the descriptor
 *   itself if you need its diagnostic string.
 */
public interface WebElementDescriptor :
    ReadOnlyProperty<Any?, WebElement>,
    HasBy {
    /** Optional override for wait behavior; if null, site/library defaults apply. */
    public val waitConfig: WaitConfig?

    /** Optional predicate indicating when the element is considered ready for use. */
    public val readyWhen: (WebElement.() -> Boolean)?

    /** Resolve the WebElement immediately, applying wait and readiness checks. */
    public fun get(): WebElement
}

/**
 * Descriptor for a list-like collection of WebElements found by a locator delegate.
 *
 * Provides the By locator for diagnostics, an optional wait configuration, and an optional
 * collection-level readiness predicate. Also acts as a ReadOnlyProperty for delegation.
 *
 * Thread-safety: Descriptors may cache resolved element collections and are not thread-safe.
 * They are intended for single-threaded test usage (typical for page objects). Avoid sharing the same
 * descriptor instance across threads. If concurrent access is unavoidable, create separate owning page
 * instances or disable caching via `cacheLookup = false`.
 *
 * toString() expectations
 * - Calling toString() on a descriptor yields a stable, human-friendly summary including:
 *   by, cacheLookup, ctx, and waitConfig=(timeout=..., polling=...), plus a decorators field.
 *   Values not applicable are shown as "N/A". ctx shows the underlying, undecorated SearchContext type.
 *   decorators is always present: it's a class list like [HighlighterDecorator, SlowMotionDecorator] when
 *   any decorators are applied, or "N/A" when none are applied.
 * - Calling toString() on the delegated WebElements value prints Selenium's collection string, not
 *   the descriptor's summary. Keep a reference to the descriptor if you need its diagnostics.
 */
public interface WebElementsDescriptor :
    ReadOnlyProperty<Any?, WebElements>,
    HasBy {
    /** Optional override for wait behavior; if null, site/library defaults apply. */
    public val waitConfig: WaitConfig?

    /** Optional predicate that tells when the located collection is considered ready. */
    public val readyWhen: (WebElements.() -> Boolean)?

    /** Resolve the collection immediately, applying wait and readiness checks. */
    public fun get(): WebElements
}

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
 * private val menuButton by className("nav-menu", readyWhen = { isClickable })
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
 * @param readyWhen A predicate that determines when the found element is considered ready for use.
 *                       It's called with [WebElement] as receiver. By default, checks if element is
 *                       displayed using [isDisplayed].
 * @return A [ReadOnlyProperty] delegate that provides a [WebElement] when accessed.
 *
 * @see WaitConfig
 * @see WebElement
 */
public fun SearchContext.className(
    value: String,
    cacheLookup: Boolean = true,
    waitConfig: WaitConfig? = null,
    readyWhen: (WebElement.() -> Boolean)? = null,
): WebElementDescriptor =
    SingleElementDescriptor(
        searchCtx = this,
        value = value,
        locatorStrategy = By::className,
        cacheLookup = cacheLookup,
        waitConfig = waitConfig,
        readyWhen = readyWhen,
    )

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
 * private val menus by classNames("menu-item", readyWhen = { all { isClickable } })
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
 *                   Defaults come from defaultWaitConfig.
 * @param readyWhen A predicate that determines when the found elements are considered ready for use.
 *                       It's called with [WebElements] as receiver. By default, checks if elements are
 *                       displayed using [isDisplayed].
 * @return A [ReadOnlyProperty] delegate that provides a [WebElements] collection when accessed.
 *
 * @see WaitConfig
 * @see WebElements
 */
public fun SearchContext.classNames(
    value: String,
    cacheLookup: Boolean = true,
    waitConfig: WaitConfig? = null,
    readyWhen: (WebElements.() -> Boolean)? = null,
): WebElementsDescriptor =
    MultiElementsDescriptor(
        searchCtx = this,
        value = value,
        locatorStrategy = By::className,
        cacheLookup = cacheLookup,
        waitConfig = waitConfig,
        readyWhen = readyWhen,
    )

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
 * private val inputField by cssSelector("#input-field", readyWhen = { isClickable })
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
 * @param readyWhen A predicate that determines when the found element is considered ready for use.
 *                       It's called with [WebElement] as receiver. By default, checks if element is
 *                       displayed using [isDisplayed].
 * @return A [ReadOnlyProperty] delegate that provides a [WebElement] when accessed.
 *
 * @see WaitConfig
 * @see WebElement
 */
public fun SearchContext.cssSelector(
    value: String,
    cacheLookup: Boolean = true,
    waitConfig: WaitConfig? = null,
    readyWhen: (WebElement.() -> Boolean)? = null,
): WebElementDescriptor =
    SingleElementDescriptor(
        searchCtx = this,
        value = value,
        locatorStrategy = By::cssSelector,
        cacheLookup = cacheLookup,
        waitConfig = waitConfig,
        readyWhen = readyWhen,
    )

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
 * private val fields by cssSelectors(".form-field", readyWhen = { all { isClickable } })
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
 * @param readyWhen A predicate that determines when the found element is considered ready for use.
 *                       It's called with [WebElement] as receiver. By default, checks if element is
 *                       displayed using [isDisplayed].
 * @return A [ReadOnlyProperty] delegate that provides a [WebElements] collection when accessed.
 *
 * @see WaitConfig
 * @see WebElements
 */
public fun SearchContext.cssSelectors(
    value: String,
    cacheLookup: Boolean = true,
    waitConfig: WaitConfig? = null,
    readyWhen: (WebElements.() -> Boolean)? = null,
): WebElementsDescriptor =
    MultiElementsDescriptor(
        searchCtx = this,
        value = value,
        locatorStrategy = By::cssSelector,
        cacheLookup = cacheLookup,
        waitConfig = waitConfig,
        readyWhen = readyWhen,
    )

/**
 * Creates a property delegate that lazily finds a single element by its "data-qa" attribute value.
 *
 * This function returns a property delegate that, when accessed, finds a web element
 * matching the specified data-qa attribute value. The element lookup and synchronization behavior can
 * be configured through the parameters.
 *
 * Example usage:
 * ```
 * private val submitButton by dataQa("submit-btn")
 * private val headerLink by dataQa("header-link", cacheLookup = false)
 * private val actionButton by dataQa("action-btn", readyWhen = { isClickable })
 * ```
 *
 * @receiver The SearchContext instance used to search for the element.
 * @param value The exact value of the "data-qa" attribute to search for.
 * @param cacheLookup If true (default), the element will be looked up only once and cached for
 *                    subsequent accesses. If false, a new lookup will be performed each time
 *                    the element is accessed.
 * @param waitConfig Configures the waiting behavior when looking up element. Specifies polling interval,
 *                   timeout, error message, and which exceptions to ignore during the wait.
 *                   Defaults to a 10-second timeout with 200ms polling.
 * @param readyWhen A predicate that determines when the found element is considered ready for use.
 *                       It's called with [WebElement] as receiver. By default, checks if element is
 *                       displayed using [isDisplayed].
 * @return A [ReadOnlyProperty] delegate that provides a [WebElement] when accessed.
 *
 * @see WaitConfig
 * @see WebElement
 */
public fun SearchContext.dataQa(
    value: String,
    cacheLookup: Boolean = true,
    waitConfig: WaitConfig? = null,
    readyWhen: (WebElement.() -> Boolean)? = null,
): WebElementDescriptor =
    xpath(
        value = ".//*[@data-qa=${value.escapeQuotes()}]",
        cacheLookup = cacheLookup,
        waitConfig = waitConfig,
        readyWhen = readyWhen,
    )

/**
 * Creates a property delegate that lazily finds all elements with the specified "data-qa" attribute value.
 *
 * This function returns a property delegate that, when accessed, finds all web elements
 * matching the specified data-qa attribute value. The elements lookup and synchronization behavior can
 * be configured through the parameters.
 *
 * Example usage:
 * ```
 * private val navButtons by dataQas("nav-btn")
 * private val productLinks by dataQas("product-link", cacheLookup = false)
 * private val formFields by dataQas("form-field", readyWhen = { all { isClickable } })
 * ```
 *
 * @receiver The SearchContext instance used to search for the elements.
 * @param value The exact value of the "data-qa" attribute to search for.
 * @param cacheLookup If true (default), the elements will be looked up only once and cached for
 *                    subsequent accesses. If false, a new lookup will be performed each time
 *                    the elements are accessed.
 * @param waitConfig Configures the waiting behavior when looking up element. Specifies polling interval,
 *                   timeout, error message, and which exceptions to ignore during the wait.
 *                   Defaults to a 10-second timeout with 200ms polling.
 * @param readyWhen A predicate that determines when the found elements are considered ready for use.
 *                       It's called with [WebElements] as receiver. By default, checks if elements are
 *                       displayed using [isDisplayed].
 * @return A [ReadOnlyProperty] delegate that provides a [WebElements] collection when accessed.
 *
 * @see WaitConfig
 * @see WebElements
 */
public fun SearchContext.dataQas(
    value: String,
    cacheLookup: Boolean = true,
    waitConfig: WaitConfig? = null,
    readyWhen: (WebElements.() -> Boolean)? = null,
): WebElementsDescriptor =
    xpaths(
        value = ".//*[@data-qa=${value.escapeQuotes()}]",
        cacheLookup = cacheLookup,
        waitConfig = waitConfig,
        readyWhen = readyWhen,
    )

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
 * private val actionButton by dataTest("action-btn", readyWhen = { isClickable })
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
 * @param readyWhen A predicate that determines when the found element is considered ready for use.
 *                       It's called with [WebElement] as receiver. By default, checks if element is
 *                       displayed using [isDisplayed].
 * @return A [ReadOnlyProperty] delegate that provides a [WebElement] when accessed.
 *
 * @see WaitConfig
 * @see WebElement
 */
public fun SearchContext.dataTest(
    value: String,
    cacheLookup: Boolean = true,
    waitConfig: WaitConfig? = null,
    readyWhen: (WebElement.() -> Boolean)? = null,
): WebElementDescriptor =
    xpath(
        value = ".//*[@data-test=${value.escapeQuotes()}]",
        cacheLookup = cacheLookup,
        waitConfig = waitConfig,
        readyWhen = readyWhen,
    )

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
 * private val formFields by dataTests("form-field", readyWhen = { all { isClickable } })
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
 * @param readyWhen A predicate that determines when the found element is considered ready for use.
 *                       It's called with [WebElement] as receiver. By default, checks if element is
 *                       displayed using [isDisplayed].
 * @return A [ReadOnlyProperty] delegate that provides a [WebElements] collection when accessed.
 *
 * @see WaitConfig
 * @see WebElements
 */
public fun SearchContext.dataTests(
    value: String,
    cacheLookup: Boolean = true,
    waitConfig: WaitConfig? = null,
    readyWhen: (WebElements.() -> Boolean)? = null,
): WebElementsDescriptor =
    xpaths(
        value = ".//*[@data-test=${value.escapeQuotes()}]",
        cacheLookup = cacheLookup,
        waitConfig = waitConfig,
        readyWhen = readyWhen,
    )

/**
 * Creates a property delegate that lazily finds a single element by its "data-testid" attribute value.
 *
 * This function returns a property delegate that, when accessed, finds a web element
 * matching the specified data-testid attribute value. The element lookup and synchronization behavior can
 * be configured through the parameters.
 *
 * Example usage:
 * ```
 * private val submitButton by dataTestId("submit-btn")
 * private val headerLink by dataTestId("header-link", cacheLookup = false)
 * private val actionButton by dataTestId("action-btn", readyWhen = { isClickable })
 * ```
 *
 * @receiver The SearchContext instance used to search for the element.
 * @param value The exact value of the "data-testid" attribute to search for.
 * @param cacheLookup If true (default), the element will be looked up only once and cached for
 *                    subsequent accesses. If false, a new lookup will be performed each time
 *                    the element is accessed.
 * @param waitConfig Configures the waiting behavior when looking up element. Specifies polling interval,
 *                   timeout, error message, and which exceptions to ignore during the wait.
 *                   Defaults to a 10-second timeout with 200ms polling.
 * @param readyWhen A predicate that determines when the found element is considered ready for use.
 *                       It's called with [WebElement] as receiver. By default, checks if element is
 *                       displayed using [isDisplayed].
 * @return A [ReadOnlyProperty] delegate that provides a [WebElement] when accessed.
 *
 * @see WaitConfig
 * @see WebElement
 */
public fun SearchContext.dataTestId(
    value: String,
    cacheLookup: Boolean = true,
    waitConfig: WaitConfig? = null,
    readyWhen: (WebElement.() -> Boolean)? = null,
): WebElementDescriptor =
    xpath(
        value = ".//*[@data-testid=${value.escapeQuotes()}]",
        cacheLookup = cacheLookup,
        waitConfig = waitConfig,
        readyWhen = readyWhen,
    )

/**
 * Creates a property delegate that lazily finds all elements with the specified "data-testid" attribute value.
 *
 * This function returns a property delegate that, when accessed, finds all web elements
 * matching the specified data-testid attribute value. The elements lookup and synchronization behavior can
 * be configured through the parameters.
 *
 * Example usage:
 * ```
 * private val navButtons by dataTestIds("nav-btn")
 * private val productLinks by dataTestIds("product-link", cacheLookup = false)
 * private val formFields by dataTestIds("form-field", readyWhen = { all { isClickable } })
 * ```
 *
 * @receiver The SearchContext instance used to search for the elements.
 * @param value The exact value of the "data-testid" attribute to search for.
 * @param cacheLookup If true (default), the elements will be looked up only once and cached for
 *                    subsequent accesses. If false, a new lookup will be performed each time
 *                    the elements are accessed.
 * @param waitConfig Configures the waiting behavior when looking up element. Specifies polling interval,
 *                   timeout, error message, and which exceptions to ignore during the wait.
 *                   Defaults to a 10-second timeout with 200ms polling.
 * @param readyWhen A predicate that determines when the found elements are considered ready for use.
 *                       It's called with [WebElements] as receiver. By default, checks if elements are
 *                       displayed using [isDisplayed].
 * @return A [ReadOnlyProperty] delegate that provides a [WebElements] collection when accessed.
 *
 * @see WaitConfig
 * @see WebElements
 */
public fun SearchContext.dataTestIds(
    value: String,
    cacheLookup: Boolean = true,
    waitConfig: WaitConfig? = null,
    readyWhen: (WebElements.() -> Boolean)? = null,
): WebElementsDescriptor =
    xpaths(
        value = ".//*[@data-testid=${value.escapeQuotes()}]",
        cacheLookup = cacheLookup,
        waitConfig = waitConfig,
        readyWhen = readyWhen,
    )

private fun String.escapeQuotes(): String = if (contains("'")) "concat('${replace("'", "',\"'\",'")}')" else "'$this'"

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
 * private val loginButton by id("login", readyWhen = { isClickable })
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
 * @param readyWhen A predicate that determines when the found element is considered ready for use.
 *                       It's called with [WebElement] as receiver. By default, checks if element is
 *                       displayed using [isDisplayed].
 * @return A [ReadOnlyProperty] delegate that provides a [WebElement] when accessed.
 *
 * @see WaitConfig
 * @see WebElement
 */
public fun SearchContext.id(
    value: String,
    cacheLookup: Boolean = true,
    waitConfig: WaitConfig? = null,
    readyWhen: (WebElement.() -> Boolean)? = null,
): WebElementDescriptor =
    SingleElementDescriptor(
        searchCtx = this,
        value = value,
        locatorStrategy = By::id,
        cacheLookup = cacheLookup,
        waitConfig = waitConfig,
        readyWhen = readyWhen,
    )

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
 * private val submitField by idOrName("submit", readyWhen = { isClickable })
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
 * @param readyWhen A predicate that determines when the found element is considered ready for use.
 *                       It's called with [WebElement] as receiver. By default, checks if element is
 *                       displayed using [isDisplayed].
 * @return A [ReadOnlyProperty] delegate that provides a [WebElement] when accessed.
 *
 * @see WaitConfig
 * @see WebElement
 * @see ByIdOrName
 */
public fun SearchContext.idOrName(
    value: String,
    cacheLookup: Boolean = true,
    waitConfig: WaitConfig? = null,
    readyWhen: (WebElement.() -> Boolean)? = null,
): WebElementDescriptor =
    SingleElementDescriptor(
        searchCtx = this,
        value = value,
        locatorStrategy = ::ByIdOrName,
        cacheLookup = cacheLookup,
        waitConfig = waitConfig,
        readyWhen = readyWhen,
    )

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
 * private val contactLink by linkText("Contact", readyWhen = { isClickable })
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
 * @param readyWhen A predicate that determines when the found element is considered ready for use.
 *                       It's called with [WebElement] as receiver. By default, checks if element is
 *                       displayed using [isDisplayed].
 * @return A [ReadOnlyProperty] delegate that provides a [WebElement] when accessed.
 *
 * @see WaitConfig
 * @see WebElement
 */
public fun SearchContext.linkText(
    value: String,
    cacheLookup: Boolean = true,
    waitConfig: WaitConfig? = null,
    readyWhen: (WebElement.() -> Boolean)? = null,
): WebElementDescriptor =
    SingleElementDescriptor(
        searchCtx = this,
        value = value,
        locatorStrategy = By::linkText,
        cacheLookup = cacheLookup,
        waitConfig = waitConfig,
        readyWhen = readyWhen,
    )

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
 * private val sidebarLinks by linkTexts("SidebarLink", readyWhen = { all { isClickable } })
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
 * @param readyWhen A predicate that determines when the found element is considered ready for use.
 *                       It's called with [WebElement] as receiver. By default, checks if element is
 *                       displayed using [isDisplayed].
 * @return A [ReadOnlyProperty] delegate that provides a [WebElements] collection when accessed.
 *
 * @see WaitConfig
 * @see WebElements
 */
public fun SearchContext.linkTexts(
    value: String,
    cacheLookup: Boolean = true,
    waitConfig: WaitConfig? = null,
    readyWhen: (WebElements.() -> Boolean)? = null,
): WebElementsDescriptor =
    MultiElementsDescriptor(
        searchCtx = this,
        value = value,
        locatorStrategy = By::linkText,
        cacheLookup = cacheLookup,
        waitConfig = waitConfig,
        readyWhen = readyWhen,
    )

/**
 * Creates a property delegate that lazily finds a single element by its "name" attribute.
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
 * @param readyWhen A predicate that determines when the found element is considered ready for use.
 *                       It's called with [WebElement] as receiver. By default, checks if element is
 *                       displayed using [isDisplayed].
 * @return A [ReadOnlyProperty] delegate that provides a [WebElement] when accessed.
 *
 * @see WaitConfig
 * @see WebElement
 */
public fun SearchContext.name(
    value: String,
    cacheLookup: Boolean = true,
    waitConfig: WaitConfig? = null,
    readyWhen: (WebElement.() -> Boolean)? = null,
): WebElementDescriptor =
    SingleElementDescriptor(
        searchCtx = this,
        value = value,
        locatorStrategy = By::name,
        cacheLookup = cacheLookup,
        waitConfig = waitConfig,
        readyWhen = readyWhen,
    )

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
 * @param readyWhen A predicate that determines when the found element is considered ready for use.
 *                       It's called with [WebElement] as receiver. By default, checks if element is
 *                       displayed using [isDisplayed].
 * @return A [ReadOnlyProperty] delegate that provides a [WebElements] collection when accessed.
 *
 * @see WaitConfig
 * @see WebElements
 */
public fun SearchContext.names(
    value: String,
    cacheLookup: Boolean = true,
    waitConfig: WaitConfig? = null,
    readyWhen: (WebElements.() -> Boolean)? = null,
): WebElementsDescriptor =
    MultiElementsDescriptor(
        this,
        value = value,
        locatorStrategy = By::name,
        cacheLookup = cacheLookup,
        waitConfig = waitConfig,
        readyWhen = readyWhen,
    )

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
 * @param readyWhen A predicate that determines when the found element is considered ready for use.
 *                       It's called with [WebElement] as receiver. By default, checks if element is
 *                       displayed using [isDisplayed].
 * @return A [ReadOnlyProperty] delegate that provides a [WebElement] when accessed.
 *
 * @see WaitConfig
 * @see WebElement
 */
public fun SearchContext.partialLinkText(
    value: String,
    cacheLookup: Boolean = true,
    waitConfig: WaitConfig? = null,
    readyWhen: (WebElement.() -> Boolean)? = null,
): WebElementDescriptor =
    SingleElementDescriptor(
        searchCtx = this,
        value = value,
        locatorStrategy = By::partialLinkText,
        cacheLookup = cacheLookup,
        waitConfig = waitConfig,
        readyWhen = readyWhen,
    )

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
 * @param readyWhen A predicate that determines when the found element is considered ready for use.
 *                       It's called with [WebElement] as receiver. By default, checks if element is
 *                       displayed using [isDisplayed].
 * @return A [ReadOnlyProperty] delegate that provides a [WebElements] collection when accessed.
 *
 * @see WaitConfig
 * @see WebElements
 */
public fun SearchContext.partialLinkTexts(
    value: String,
    cacheLookup: Boolean = true,
    waitConfig: WaitConfig? = null,
    readyWhen: (WebElements.() -> Boolean)? = null,
): WebElementsDescriptor =
    MultiElementsDescriptor(
        searchCtx = this,
        value = value,
        locatorStrategy = By::partialLinkText,
        cacheLookup = cacheLookup,
        waitConfig = waitConfig,
        readyWhen = readyWhen,
    )

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
 * @param readyWhen A predicate that determines when the found element is considered ready for use.
 *                       It's called with [WebElement] as receiver. By default, checks if element is
 *                       displayed using [isDisplayed].
 * @return A [ReadOnlyProperty] delegate that provides a [WebElement] when accessed.
 *
 * @see WaitConfig
 * @see WebElement
 */
public fun SearchContext.tagName(
    value: String,
    cacheLookup: Boolean = true,
    waitConfig: WaitConfig? = null,
    readyWhen: (WebElement.() -> Boolean)? = null,
): WebElementDescriptor =
    SingleElementDescriptor(
        searchCtx = this,
        value = value,
        locatorStrategy = By::tagName,
        cacheLookup = cacheLookup,
        waitConfig = waitConfig,
        readyWhen = readyWhen,
    )

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
 * @param readyWhen A predicate that determines when the found element is considered ready for use.
 *                       It's called with [WebElement] as receiver. By default, checks if element is
 *                       displayed using [isDisplayed].
 * @return A [ReadOnlyProperty] delegate that provides a [WebElements] collection when accessed.
 *
 * @see WaitConfig
 * @see WebElements
 */
public fun SearchContext.tagNames(
    value: String,
    cacheLookup: Boolean = true,
    waitConfig: WaitConfig? = null,
    readyWhen: (WebElements.() -> Boolean)? = null,
): WebElementsDescriptor =
    MultiElementsDescriptor(
        searchCtx = this,
        value = value,
        locatorStrategy = By::tagName,
        cacheLookup = cacheLookup,
        waitConfig = waitConfig,
        readyWhen = readyWhen,
    )

/**
 * Creates a property delegate that lazily finds a single element using an XPath expression.
 *
 * This function returns a property delegate that, when accessed, finds a web element
 * matching the specified XPath expression. The element lookup and synchronization behavior can
 * be configured through the parameters.
 *
 * Example usage:
 * ```
 * private val submitButton by xpath("//button[@type='submit']")
 * private val headerLink by xpath("//a[contains(@class, 'header-link')]", cacheLookup = false)
 * private val actionButton by xpath("//button[text()='Click Me']", readinessCondition = { isClickable })
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
 * @param readyWhen A predicate that determines when the found element is considered ready for use.
 *                       It's called with [WebElement] as receiver. By default, checks if element is
 *                       displayed using [isDisplayed].
 * @return A [ReadOnlyProperty] delegate that provides a [WebElement] when accessed.
 *
 * @see WaitConfig
 * @see WebElement
 */
public fun SearchContext.xpath(
    value: String,
    cacheLookup: Boolean = true,
    waitConfig: WaitConfig? = null,
    readyWhen: (WebElement.() -> Boolean)? = null,
): WebElementDescriptor =
    SingleElementDescriptor(
        searchCtx = this,
        value = value,
        locatorStrategy = By::xpath,
        cacheLookup = cacheLookup,
        waitConfig = waitConfig,
        readyWhen = readyWhen,
    )

/**
 * Creates a property delegate that lazily finds all elements matching an XPath expression.
 *
 * This function returns a property delegate that, when accessed, finds all web elements
 * matching the specified XPath expression. The elements lookup and synchronization behavior can
 * be configured through the parameters.
 *
 * Example usage:
 * ```
 * private val navButtons by xpaths("//button[contains(@class, 'nav')]")
 * private val productLinks by xpaths("//a[contains(@href, '/product')]", cacheLookup = false)
 * private val formFields by xpaths("//input", readinessCondition = { all { isClickable } })
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
 * @param readyWhen A predicate that determines when the found element is considered ready for use.
 *                       It's called with [WebElement] as receiver. By default, checks if element is
 *                       displayed using [isDisplayed].
 * @return A [ReadOnlyProperty] delegate that provides a [WebElements] collection when accessed.
 *
 * @see WaitConfig
 * @see WebElements
 */
public fun SearchContext.xpaths(
    value: String,
    cacheLookup: Boolean = true,
    waitConfig: WaitConfig? = null,
    readyWhen: (WebElements.() -> Boolean)? = null,
): WebElementsDescriptor =
    MultiElementsDescriptor(
        searchCtx = this,
        value = value,
        locatorStrategy = By::xpath,
        cacheLookup = cacheLookup,
        waitConfig = waitConfig,
        readyWhen = readyWhen,
    )

// Ensure a minimal baseline of ignored exceptions regardless of caller's WaitConfig
// Specifically, always ignore NoSuchElementException so waits don't fail on the first miss
private fun ensureNoSuchElementIgnored(wait: WaitConfig): WaitConfig =
    if (NoSuchElementException::class in wait.ignoring) {
        wait
    } else {
        wait.copy(ignoring = wait.ignoring + NoSuchElementException::class)
    }

internal abstract class AbstractElementDescriptor<T : AbstractElementDescriptor<T, R>, R>(
    protected val searchCtx: SearchContext,
) {
    protected var appliedDecoratorClassNames: List<String> = emptyList()

    // Class names of the merged decorators, used by toString() without forcing decoration
    protected fun mergedDecoratorClassNames(): List<String> = mergedDecorators().map { it::class.java.simpleName }

    protected val searchContext by lazy {
        val merged = mergedDecorators()
        appliedDecoratorClassNames = merged.map { it::class.java.simpleName }
        if (merged.isEmpty()) {
            searchCtx
        } else {
            DecoratorManager.combine(merged)(searchCtx)
        }
    }

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

    // Merge decorators deterministically: site first, then test; de-duplicate by class with test-level winning on conflicts.
    private fun mergedDecorators(): List<AbstractDecorator> {
        val siteLevelDecorators = SiteContext.get()?.decorators ?: emptyList()
        val testLevelDecorators = DecoratorManager.getAllDecorators()
        val siteDedup = siteLevelDecorators.distinctBy { it::class }
        val testDedup = testLevelDecorators.distinctBy { it::class }
        val testClasses = testDedup.map { it::class }.toSet()
        val siteFiltered = siteDedup.filter { it::class !in testClasses }
        return siteFiltered + testDedup
    }
}

internal class SingleElementDescriptor(
    searchCtx: SearchContext,
    value: String,
    locatorStrategy: (String) -> By,
    private val cacheLookup: Boolean,
    override val waitConfig: WaitConfig?,
    override val readyWhen: (WebElement.() -> Boolean)?,
) : AbstractElementDescriptor<SingleElementDescriptor, WebElement>(searchCtx),
    WebElementDescriptor {
    init {
        require(value.isNotBlank()) { "\"value\" must not be blank" }
    }

    // Pure metadata, safe to read without DOM access
    override val by: By = locatorStrategy(value)

    // Internals for element resolution (outside of SiteDsl.ensureReady):
    // If descriptor values are null, fall back to library-level defaults and
    // enforce a minimal baseline of ignored exceptions (NoSuchElementException).
    private val effectiveWaitConfig: WaitConfig = ensureNoSuchElementIgnored(waitConfig ?: defaultWaitConfig)
    private val effectiveReady: WebElement.() -> Boolean = readyWhen ?: defaultElementReadyCondition

    private var cachedWebElement: WebElement? = null
    private val wait: FluentWait<SingleElementDescriptor> by lazy { initializeWait(effectiveWaitConfig) }

    override fun getValue(
        thisRef: Any?,
        property: KProperty<*>,
    ): WebElement = get()

    override fun get(): WebElement = getValueInternal(wait)

    override fun findElement(): WebElement =
        if (cacheLookup) {
            cachedWebElement ?: searchContext.findElement(by).also { cachedWebElement = it }
        } else {
            searchContext.findElement(by)
        }

    override fun clearCache() {
        cachedWebElement = null
    }

    override fun isElementReady(element: WebElement): Boolean = element.effectiveReady()

    override fun toString(): String {
        val ctxName = classNameOf(searchCtx)
        val timeoutStr = effectiveWaitConfig.timeout?.toString() ?: "N/A"
        val pollingStr = effectiveWaitConfig.pollingInterval?.toString() ?: "N/A"
        val decorators = appliedDecoratorClassNames.ifEmpty { mergedDecoratorClassNames() }
        val decoratorsStr = if (decorators.isEmpty()) "N/A" else decorators.joinToString(prefix = "[", postfix = "]")
        return "ElementDescriptor(ctx=$ctxName, by=$by, cacheLookup=$cacheLookup, waitConfig=(timeout=$timeoutStr, " +
            "polling=$pollingStr), decorators=$decoratorsStr)"
    }
}

internal class MultiElementsDescriptor(
    searchCtx: SearchContext,
    value: String,
    locatorStrategy: (String) -> By,
    private val cacheLookup: Boolean,
    override val waitConfig: WaitConfig?,
    override val readyWhen: (WebElements.() -> Boolean)?,
) : AbstractElementDescriptor<MultiElementsDescriptor, WebElements>(searchCtx),
    WebElementsDescriptor {
    init {
        require(value.isNotBlank()) { "\"value\" must not be blank" }
    }

    override val by: By = locatorStrategy(value)

    // If descriptor values are null, fall back to library-level defaults for resolution
    // and enforce a minimal baseline of ignored exceptions (NoSuchElementException).
    private val effectiveWaitConfig: WaitConfig = ensureNoSuchElementIgnored(waitConfig ?: defaultWaitConfig)
    private val effectiveReady: WebElements.() -> Boolean = readyWhen ?: defaultElementsReadyCondition

    private var cachedWebElements: WebElements? = null
    private val wait: FluentWait<MultiElementsDescriptor> by lazy { initializeWait(effectiveWaitConfig) }

    override fun getValue(
        thisRef: Any?,
        property: KProperty<*>,
    ): WebElements = get()

    override fun get(): WebElements = getValueInternal(wait)

    override fun findElement(): WebElements =
        if (cacheLookup) {
            cachedWebElements ?: searchContext.findElements(by).also { cachedWebElements = it }
        } else {
            searchContext.findElements(by)
        }

    override fun clearCache() {
        cachedWebElements = null
    }

    override fun isElementReady(element: WebElements): Boolean = element.effectiveReady()

    override fun toString(): String {
        val ctxName = classNameOf(searchCtx)
        val timeoutStr = effectiveWaitConfig.timeout?.toString() ?: "N/A"
        val pollingStr = effectiveWaitConfig.pollingInterval?.toString() ?: "N/A"
        val decorators = appliedDecoratorClassNames.ifEmpty { mergedDecoratorClassNames() }
        val decoratorsStr = if (decorators.isEmpty()) "N/A" else decorators.joinToString(prefix = "[", postfix = "]")
        return "ElementsDescriptor(ctx=$ctxName, by=$by, cacheLookup=$cacheLookup, waitConfig=(timeout=$timeoutStr, " +
            "polling=$pollingStr), decorators=$decoratorsStr)"
    }
}

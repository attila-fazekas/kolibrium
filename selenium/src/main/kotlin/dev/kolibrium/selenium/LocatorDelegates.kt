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
import dev.kolibrium.dsl.selenium.wait.Synchronization
import dev.kolibrium.dsl.selenium.wait.Synchronizations
import io.github.oshai.kotlinlogging.KotlinLogging
import org.openqa.selenium.By
import org.openqa.selenium.By.className
import org.openqa.selenium.By.cssSelector
import org.openqa.selenium.By.id
import org.openqa.selenium.By.linkText
import org.openqa.selenium.By.name
import org.openqa.selenium.By.partialLinkText
import org.openqa.selenium.By.tagName
import org.openqa.selenium.By.xpath
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ByIdOrName
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Finds element by [className] locator strategy.
 * [locator] is the value of the "class" attribute to search for.
 * [waitUntil] can be defined to wait for the element.
 */
context(WebDriver)
public fun <T : WebElement> className(
    locator: String,
    synchronization: (Synchronization.() -> Unit) = {},
): ReadOnlyProperty<Any, T> = Element(className(locator), synchronization)

/**
 * Finds element by [cssSelector] locator strategy.
 * [locator] is the CSS expression to search for.
 * [waitUntil] can be defined to wait for the element.
 */
context(WebDriver)
public fun <T : WebElement> css(
    locator: String,
    synchronization: (Synchronization.() -> Unit) = {},
): ReadOnlyProperty<Any, T> = Element(cssSelector(locator), synchronization)

/**
 * Finds element by [id] locator strategy.
 * [locator] is the value of the "id" attribute to search for.
 * [waitUntil] can be defined to wait for the element.
 */
context(WebDriver)
public fun <T : WebElement> id(
    locator: String,
    synchronization: (Synchronization.() -> Unit) = {},
): ReadOnlyProperty<Any, T> = Element(id(locator), synchronization)

/**
 * Tries to find element by [ByIdOrName] locator strategy.
 * [locator] is the value of the "id" or "name" attribute to search for.
 * [waitUntil] can be defined to wait for the element.
 */
context(WebDriver)
public fun <T : WebElement> idOrName(
    locator: String,
    synchronization: (Synchronization.() -> Unit) = {},
): ReadOnlyProperty<Any, T> = Element(ByIdOrName(locator), synchronization)

/**
 * Finds element by [linkText] locator strategy.
 * [locator] is the exact text to match against.
 * [waitUntil] can be defined to wait for the element.
 */
context(WebDriver)
public fun <T : WebElement> linkText(
    locator: String,
    synchronization: (Synchronization.() -> Unit) = {},
): ReadOnlyProperty<Any, T> = Element(linkText(locator), synchronization)

/**
 * Finds element by [name] locator strategy.
 * [locator] is the value of the "name" attribute to search for.
 * [waitUntil] can be defined to wait for the element.
 */
context(WebDriver)
public fun <T : WebElement> name(
    locator: String,
    synchronization: (Synchronization.() -> Unit) = {},
): ReadOnlyProperty<Any, T> = Element(name(locator), synchronization)

/**
 * Finds element by [partialLinkText] locator strategy.
 * [locator] is the partial text in link to match against.
 * [waitUntil] can be defined to wait for the element.
 */
context(WebDriver)
public fun <T : WebElement> partialLinkText(
    locator: String,
    synchronization: (Synchronization.() -> Unit) = {},
): ReadOnlyProperty<Any, T> = Element(partialLinkText(locator), synchronization)

/**
 * Finds element by [tagName] locator strategy.
 * [locator] is the element's tag name.
 * [waitUntil] can be defined to wait for the element.
 */
context(WebDriver)
public fun <T : WebElement> tagName(
    locator: String,
    synchronization: (Synchronization.() -> Unit) = {},
): ReadOnlyProperty<Any, T> = Element(tagName(locator), synchronization)

/**
 * Finds element by [xpath] locator strategy.
 * [locator] is the XPath to use.
 * [waitUntil] can be defined to wait for the element.
 */
context(WebDriver)
public fun <T : WebElement> xpath(
    locator: String,
    synchronization: (Synchronization.() -> Unit) = {},
): ReadOnlyProperty<Any, T> = Element(xpath(locator), synchronization)

context(WebDriver)
private class Element<T : WebElement>(
    private val by: By,
    private val synchronization: Synchronization.() -> Unit,
) : ReadOnlyProperty<Any, T> {
    @Suppress("UNCHECKED_CAST")
    private val webElement: T by lazy { findElement(by) as T }

    override fun getValue(
        thisRef: Any,
        property: KProperty<*>,
    ): T {
        return execute(property.name) {
            val synchronization = Synchronization().apply(synchronization)
            val wait = setUpWait(this@WebDriver, synchronization.wait)
            wait.until { synchronization.until.invoke(webElement) }
            webElement
        }
    }
}

// WebElements

/**
 * Finds elements by [className] locator strategy.
 * [locator] is the value of the "class" attribute to search for.
 * [waitUntil] can be defined to wait for the elements.
 */
context(WebDriver)
@JvmName("classNames")
public fun <T : WebElements> className(
    locator: String,
    synchronizations: (Synchronizations.() -> Unit) = {},
): ReadOnlyProperty<Any, T> = KolibriumElements(className(locator), synchronizations)

/**
 * Finds elements by [cssSelector] locator strategy.
 * [locator] is the CSS expression to search for.
 * [waitUntil] can be defined to wait for the elements.
 */
context(WebDriver)
@JvmName("csss")
public fun <T : WebElements> css(
    locator: String,
    synchronizations: (Synchronizations.() -> Unit) = {},
): ReadOnlyProperty<Any, T> = KolibriumElements(cssSelector(locator), synchronizations)

/**
 * Finds elements by [linkText] locator strategy.
 * [locator] is the exact text to match against.
 * [waitUntil] can be defined to wait for the elements.
 */
context(WebDriver)
@JvmName("linkTexts")
public fun <T : WebElements> linkText(
    locator: String,
    synchronizations: (Synchronizations.() -> Unit) = {},
): ReadOnlyProperty<Any, T> = KolibriumElements(linkText(locator), synchronizations)

/**
 * Finds elements by [name] locator strategy.
 * [locator] is the value of the "name" attribute to search for.
 * [waitUntil] can be defined to wait for the elements.
 */
context(WebDriver)
@JvmName("names")
public fun <T : WebElements> name(
    locator: String,
    synchronizations: (Synchronizations.() -> Unit) = {},
): ReadOnlyProperty<Any, T> = KolibriumElements(name(locator), synchronizations)

/**
 * Finds elements by [partialLinkText] locator strategy.
 * [locator] is the partial text to match against.
 * [waitUntil] can be defined to wait for the elements.
 */
context(WebDriver)
@JvmName("partialLinkTexts")
public fun <T : WebElements> partialLinkText(
    locator: String,
    synchronizations: (Synchronizations.() -> Unit) = {},
): ReadOnlyProperty<Any, T> = KolibriumElements(partialLinkText(locator), synchronizations)

/**
 * Finds elements by [tagName] locator strategy.
 * [locator] is the element's tag name.
 * [waitUntil] can be defined to wait for the elements.
 */
context(WebDriver)
@JvmName("tagNames")
public fun <T : WebElements> tagName(
    locator: String,
    synchronizations: (Synchronizations.() -> Unit) = {},
): ReadOnlyProperty<Any, T> = KolibriumElements(tagName(locator), synchronizations)

/**
 * Finds elements by [xpath] locator strategy.
 * [locator] is the XPath to use.
 * [waitUntil] can be defined to wait for the elements.
 */
context(WebDriver)
@JvmName("xpaths")
public fun <T : WebElements> xpath(
    locator: String,
    synchronizations: (Synchronizations.() -> Unit) = {},
): ReadOnlyProperty<Any, T> = KolibriumElements(xpath(locator), synchronizations)

context(WebDriver)
private class KolibriumElements<T : WebElements>(
    private val by: By,
    private val synchronizations: Synchronizations.() -> Unit,
) : ReadOnlyProperty<Any, T> {
    @Suppress("UNCHECKED_CAST")
    private val webElements: T by lazy { findElements(by) as T }

    override fun getValue(
        thisRef: Any,
        property: KProperty<*>,
    ): T {
        return execute(property.name) {
            val synchronizations = Synchronizations().apply(synchronizations)
            val wait = setUpWait(this@WebDriver, synchronizations.wait)
            wait.until {
                val elements = findElements(by)
                synchronizations.until.invoke(elements)
            }
            webElements
        }
    }
}

private val logger = KotlinLogging.logger {}

context(WebDriver)
private fun <T> execute(
    element: String,
    block: () -> T,
): T {
    logger.trace { "Waiting for \"$element\"" }
    return block()
}

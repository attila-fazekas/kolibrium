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
    waitUntil: ((WebElement) -> Boolean) = { it.isDisplayed },
): ReadOnlyProperty<Any?, WebElement> = Element(className(locator), waitUntil)

/**
 * Finds element by [cssSelector] locator strategy.
 * [locator] is the CSS expression to search for.
 * [waitUntil] can be defined to wait for the element.
 */
context(WebDriver)
public fun <T : WebElement> css(
    locator: String,
    waitUntil: ((WebElement) -> Boolean) = { it.isDisplayed },
): ReadOnlyProperty<Any?, WebElement> = Element(cssSelector(locator), waitUntil)

/**
 * Finds element by [id] locator strategy.
 * [locator] is the value of the "id" attribute to search for.
 * [waitUntil] can be defined to wait for the element.
 */
context(WebDriver)
public fun <T : WebElement> id(
    locator: String,
    waitUntil: ((WebElement) -> Boolean) = { it.isDisplayed },
): ReadOnlyProperty<Any?, WebElement> = Element(id(locator), waitUntil)

/**
 * Tries to find element by [ByIdOrName] locator strategy.
 * [locator] is the value of the "id" or "name" attribute to search for.
 * [waitUntil] can be defined to wait for the element.
 */
context(WebDriver)
public fun <T : WebElement> idOrName(
    locator: String,
    waitUntil: ((WebElement) -> Boolean) = { it.isDisplayed },
): ReadOnlyProperty<Any?, WebElement> = Element(ByIdOrName(locator), waitUntil)

/**
 * Finds element by [linkText] locator strategy.
 * [locator] is the exact text to match against.
 * [waitUntil] can be defined to wait for the element.
 */
context(WebDriver)
public fun <T : WebElement> linkText(
    locator: String,
    waitUntil: ((WebElement) -> Boolean) = { it.isDisplayed },
): ReadOnlyProperty<Any?, WebElement> = Element(linkText(locator), waitUntil)

/**
 * Finds element by [name] locator strategy.
 * [locator] is the value of the "name" attribute to search for.
 * [waitUntil] can be defined to wait for the element.
 */
context(WebDriver)
public fun <T : WebElement> name(
    locator: String,
    waitUntil: ((WebElement) -> Boolean) = { it.isDisplayed },
): ReadOnlyProperty<Any?, WebElement> = Element(name(locator), waitUntil)

/**
 * Finds element by [partialLinkText] locator strategy.
 * [locator] is the partial text in link to match against.
 * [waitUntil] can be defined to wait for the element.
 */
context(WebDriver)
public fun <T : WebElement> partialLinkText(
    locator: String,
    waitUntil: ((WebElement) -> Boolean) = { it.isDisplayed },
): ReadOnlyProperty<Any?, WebElement> = Element(partialLinkText(locator), waitUntil)

/**
 * Finds element by [tagName] locator strategy.
 * [locator] is the element's tag name.
 * [waitUntil] can be defined to wait for the element.
 */
context(WebDriver)
public fun <T : WebElement> tagName(
    locator: String,
    waitUntil: ((WebElement) -> Boolean) = { it.isDisplayed },
): ReadOnlyProperty<Any?, WebElement> = Element(tagName(locator), waitUntil)

/**
 * Finds element by [xpath] locator strategy.
 * [locator] is the XPath to use.
 * [waitUntil] can be defined to wait for the element.
 */
context(WebDriver)
public fun <T : WebElement> xpath(
    locator: String,
    waitUntil: ((WebElement) -> Boolean) = { it.isDisplayed },
): ReadOnlyProperty<Any?, WebElement> = Element(xpath(locator), waitUntil)

context(WebDriver)
private class Element(
    private val by: By,
    private val waitUntil: ((WebElement) -> Boolean),
) : ReadOnlyProperty<Any?, WebElement> {
    private val wait = setUpWait(this@WebDriver)

    private val webElement: WebElement by lazy { findElement(by) }

    override fun getValue(
        thisRef: Any?,
        property: KProperty<*>,
    ): WebElement {
        return execute(property.name) {
            wait.until { waitUntil.invoke(webElement) }
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
    waitUntil: ((WebElements) -> Boolean) = { it.isDisplayed },
): ReadOnlyProperty<Any?, WebElements> = KolibriumElements<WebElements>(className(locator), waitUntil)

/**
 * Finds elements by [cssSelector] locator strategy.
 * [locator] is the CSS expression to search for.
 * [waitUntil] can be defined to wait for the elements.
 */
context(WebDriver)
@JvmName("csss")
public fun <T : WebElements> css(
    locator: String,
    waitUntil: ((WebElements) -> Boolean) = { it.isDisplayed },
): ReadOnlyProperty<Any?, WebElements> = KolibriumElements<WebElements>(cssSelector(locator), waitUntil)

/**
 * Finds elements by [linkText] locator strategy.
 * [locator] is the exact text to match against.
 * [waitUntil] can be defined to wait for the elements.
 */
context(WebDriver)
@JvmName("linkTexts")
public fun <T : WebElements> linkText(
    locator: String,
    waitUntil: ((WebElements) -> Boolean) = { it.isDisplayed },
): ReadOnlyProperty<Any?, WebElements> = KolibriumElements<WebElements>(linkText(locator), waitUntil)

/**
 * Finds elements by [name] locator strategy.
 * [locator] is the value of the "name" attribute to search for.
 * [waitUntil] can be defined to wait for the elements.
 */
context(WebDriver)
@JvmName("names")
public fun <T : WebElements> name(
    locator: String,
    waitUntil: ((WebElements) -> Boolean) = { it.isDisplayed },
): ReadOnlyProperty<Any?, WebElements> = KolibriumElements<WebElements>(name(locator), waitUntil)

/**
 * Finds elements by [partialLinkText] locator strategy.
 * [locator] is the partial text to match against.
 * [waitUntil] can be defined to wait for the elements.
 */
context(WebDriver)
@JvmName("partialLinkTexts")
public fun <T : WebElements> partialLinkText(
    locator: String,
    waitUntil: ((WebElements) -> Boolean) = { it.isDisplayed },
): ReadOnlyProperty<Any?, WebElements> = KolibriumElements<WebElements>(partialLinkText(locator), waitUntil)

/**
 * Finds elements by [tagName] locator strategy.
 * [locator] is the element's tag name.
 * [waitUntil] can be defined to wait for the elements.
 */
context(WebDriver)
@JvmName("tagNames")
public fun <T : WebElements> tagName(
    locator: String,
    waitUntil: ((WebElements) -> Boolean) = { it.isDisplayed },
): ReadOnlyProperty<Any?, WebElements> = KolibriumElements<WebElements>(tagName(locator), waitUntil)

/**
 * Finds elements by [xpath] locator strategy.
 * [locator] is the XPath to use.
 * [waitUntil] can be defined to wait for the elements.
 */
context(WebDriver)
@JvmName("xpaths")
public fun <T : WebElements> xpath(
    locator: String,
    waitUntil: ((WebElements) -> Boolean) = { it.isDisplayed },
): ReadOnlyProperty<Any?, WebElements> = KolibriumElements<WebElements>(xpath(locator), waitUntil)

context(WebDriver)
private class KolibriumElements<T : WebElements>(
    private val by: By,
    private val waitUntil: ((WebElements) -> Boolean),
) : ReadOnlyProperty<Any?, WebElements> {
    private val wait = setUpWait(this@WebDriver)

    private val element: WebElements by lazy { findElements(by) }

    override fun getValue(
        thisRef: Any?,
        property: KProperty<*>,
    ): WebElements =
        execute(property.name) {
            wait.until {
                val e = findElements(by)
                waitUntil.invoke(e)
            }
            element
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

/*
 * Copyright 2023 Attila Fazekas
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

@file:Suppress("TooManyFunctions")

package io.kolibrium.selenium

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
import org.openqa.selenium.NoSuchElementException
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ByIdOrName
import org.openqa.selenium.support.ui.FluentWait
import java.time.Duration.ofSeconds
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
    waitUntil: ((T) -> Boolean) = { it.isDisplayed }
): ReadOnlyProperty<Any?, T> = KolibriumElement(className(locator), waitUntil)

/**
 * Finds element by [cssSelector] locator strategy.
 * [locator] is the CSS expression to search for.
 * [waitUntil] can be defined to wait for the element.
 */
context(WebDriver)
public fun <T : WebElement> css(
    locator: String,
    waitUntil: ((T) -> Boolean) = { it.isDisplayed }
): ReadOnlyProperty<Any?, T> = KolibriumElement(cssSelector(locator), waitUntil)

/**
 * Finds element by [id] locator strategy.
 * [locator] is the value of the "id" attribute to search for.
 * [waitUntil] can be defined to wait for the element.
 */
context(WebDriver)
public fun <T : WebElement> id(
    locator: String,
    waitUntil: ((T) -> Boolean) = { it.isDisplayed }
): ReadOnlyProperty<Any?, T> = KolibriumElement(id(locator), waitUntil)

/**
 * Tries to find element by [ByIdOrName] locator strategy.
 * [locator] is the value of the "id" or "name" attribute to search for.
 * [waitUntil] can be defined to wait for the element.
 */
context(WebDriver)
public fun <T : WebElement> idOrName(
    locator: String,
    waitUntil: ((T) -> Boolean) = { it.isDisplayed }
): ReadOnlyProperty<Any?, T> = KolibriumElement(ByIdOrName(locator), waitUntil)

/**
 * Finds element by [linkText] locator strategy.
 * [locator] is the exact text to match against.
 * [waitUntil] can be defined to wait for the element.
 */
context(WebDriver)
public fun <T : WebElement> linkText(
    locator: String,
    waitUntil: ((T) -> Boolean) = { it.isDisplayed }
): ReadOnlyProperty<Any?, T> = KolibriumElement(linkText(locator), waitUntil)

/**
 * Finds element by [name] locator strategy.
 * [locator] is the value of the "name" attribute to search for.
 * [waitUntil] can be defined to wait for the element.
 */
context(WebDriver)
public fun <T : WebElement> name(
    locator: String,
    waitUntil: ((T) -> Boolean) = { it.isDisplayed }
): ReadOnlyProperty<Any?, T> = KolibriumElement(name(locator), waitUntil)

/**
 * Finds element by [partialLinkText] locator strategy.
 * [locator] is the partial text in link to match against.
 * [waitUntil] can be defined to wait for the element.
 */
context(WebDriver)
public fun <T : WebElement> partialLinkText(
    locator: String,
    waitUntil: ((T) -> Boolean) = { it.isDisplayed }
): ReadOnlyProperty<Any?, T> = KolibriumElement(partialLinkText(locator), waitUntil)

/**
 * Finds element by [tagName] locator strategy.
 * [locator] is the element's tag name.
 * [waitUntil] can be defined to wait for the element.
 */
context(WebDriver)
public fun <T : WebElement> tagName(
    locator: String,
    waitUntil: ((T) -> Boolean) = { it.isDisplayed }
): ReadOnlyProperty<Any?, T> = KolibriumElement(tagName(locator), waitUntil)

/**
 * Finds element by [xpath] locator strategy.
 * [locator] is the XPath to use.
 * [waitUntil] can be defined to wait for the element.
 */
context(WebDriver)
public fun <T : WebElement> xpath(
    locator: String,
    waitUntil: ((T) -> Boolean) = { it.isDisplayed }
): ReadOnlyProperty<Any?, T> = KolibriumElement(xpath(locator), waitUntil)

context(WebDriver)
private class KolibriumElement<T : WebElement>(
    by: By,
    private val waitUntil: ((T) -> Boolean)
) : ReadOnlyProperty<Any?, T> {

    @Suppress("UNCHECKED_CAST")
    private val element: T by lazy {
        findElement(by) as T
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return execute(property.name) {
            val wait = setUpWait(this@WebDriver)
            wait.until { waitUntil.invoke(element) }
            element
        }
    }
}

// WebElements

private fun WebElements.isEnabled() = all { it.isEnabled }

/**
 * Finds elements by [className] locator strategy.
 * [locator] is the value of the "class" attribute to search for.
 * [waitUntil] can be defined to wait for the elements.
 */
context(WebDriver)
@JvmName("classNames")
public fun <T : WebElements> className(
    locator: String,
    waitUntil: ((T) -> Boolean) = { it.isEnabled() }
): ReadOnlyProperty<Any?, T> = KolibriumElements(className(locator), waitUntil)

/**
 * Finds elements by [cssSelector] locator strategy.
 * [locator] is the CSS expression to search for.
 * [waitUntil] can be defined to wait for the elements.
 */
context(WebDriver)
@JvmName("csss")
public fun <T : WebElements> css(
    locator: String,
    waitUntil: ((T) -> Boolean) = { it.isEnabled() }
): ReadOnlyProperty<Any?, T> = KolibriumElements(cssSelector(locator), waitUntil)

/**
 * Finds elements by [linkText] locator strategy.
 * [locator] is the exact text to match against.
 * [waitUntil] can be defined to wait for the elements.
 */
context(WebDriver)
@JvmName("linkTexts")
public fun <T : WebElements> linkText(
    locator: String,
    waitUntil: ((T) -> Boolean) = { it.isEnabled() }
): ReadOnlyProperty<Any?, T> = KolibriumElements(linkText(locator), waitUntil)

/**
 * Finds elements by [name] locator strategy.
 * [locator] is the value of the "name" attribute to search for.
 * [waitUntil] can be defined to wait for the elements.
 */
context(WebDriver)
@JvmName("names")
public fun <T : WebElements> name(
    locator: String,
    waitUntil: ((T) -> Boolean) = { it.isEnabled() }
): ReadOnlyProperty<Any?, T> = KolibriumElements(name(locator), waitUntil)

/**
 * Finds elements by [partialLinkText] locator strategy.
 * [locator] is the partial text to match against.
 * [waitUntil] can be defined to wait for the elements.
 */
context(WebDriver)
@JvmName("partialLinkTexts")
public fun <T : WebElements> partialLinkText(
    locator: String,
    waitUntil: ((T) -> Boolean) = { it.isEnabled() }
): ReadOnlyProperty<Any?, T> = KolibriumElements(partialLinkText(locator), waitUntil)

/**
 * Finds elements by [tagName] locator strategy.
 * [locator] is the element's tag name.
 * [waitUntil] can be defined to wait for the elements.
 */
context(WebDriver)
@JvmName("tagNames")
public fun <T : WebElements> tagName(
    locator: String,
    waitUntil: ((T) -> Boolean) = { it.isEnabled() }
): ReadOnlyProperty<Any?, T> = KolibriumElements(tagName(locator), waitUntil)

/**
 * Finds elements by [xpath] locator strategy.
 * [locator] is the XPath to use.
 * [waitUntil] can be defined to wait for the elements.
 */
context(WebDriver)
@JvmName("xpaths")
public fun <T : WebElements> xpath(
    locator: String,
    waitUntil: ((T) -> Boolean) = { it.isEnabled() }
): ReadOnlyProperty<Any?, T> = KolibriumElements(xpath(locator), waitUntil)

context(WebDriver)
@Suppress("UNCHECKED_CAST")
private class KolibriumElements<T : WebElements>(
    private val by: By,
    private val waitUntil: ((T) -> Boolean)
) : ReadOnlyProperty<Any?, T> {

    private val elements: T by lazy {
        findElements(by) as T
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return execute(property.name) {
            val wait = setUpWait(this@WebDriver)

            wait.until {
                val e: T = findElements(by) as T
                waitUntil.invoke(e)
            }
            elements
        }
    }
}

private val logger = KotlinLogging.logger {}

context(WebDriver)
private fun <T> execute(
    element: String,
    block: () -> T
): T {
    logger.trace { "Waiting for \"$element\"" }
    return block()
}

private const val TIMEOUT: Long = 10
private const val POOLING_INTERVAL: Long = 1

private fun setUpWait(driver: WebDriver) = FluentWait(driver)
    .withTimeout(ofSeconds(TIMEOUT))
    .pollingEvery(ofSeconds(POOLING_INTERVAL))
    .ignoring(NoSuchElementException::class.java)

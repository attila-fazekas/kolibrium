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
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ByIdOrName
import org.openqa.selenium.support.ui.ExpectedCondition
import org.openqa.selenium.support.ui.FluentWait
import java.time.Duration.ofSeconds
import kotlin.properties.ReadOnlyProperty

/**
 * Finds element by [className] locator strategy.
 * [locator] is the value of the "class" attribute to search for.
 * [expectedCondition] can be defined to wait for the element.
 */
context(WebDriver)
public fun <T : WebElement> className(
    locator: String,
    expectedCondition: ((By) -> ExpectedCondition<T>)? = null
): ReadOnlyProperty<Any?, T> = findElement(className(locator), expectedCondition)

/**
 * Finds element by [cssSelector] locator strategy.
 * [locator] is the CSS expression to search for.
 * [expectedCondition] can be defined to wait for the element.
 */
context(WebDriver)
public fun <T : WebElement> css(
    locator: String,
    expectedCondition: ((By) -> ExpectedCondition<T>)? = null
): ReadOnlyProperty<Any?, T> = findElement(cssSelector(locator), expectedCondition)

/**
 * Finds element by [id] locator strategy.
 * [locator] is the value of the "id" attribute to search for.
 * [expectedCondition] can be defined to wait for the element.
 */
context(WebDriver)
public fun <T : WebElement> id(
    locator: String,
    expectedCondition: ((By) -> ExpectedCondition<T>)? = null
): ReadOnlyProperty<Any?, T> = findElement(id(locator), expectedCondition)

/**
 * Tries to find element by [ByIdOrName] locator strategy.
 * [locator] is the value of the "id" or "name" attribute to search for.
 * [expectedCondition] can be defined to wait for the element.
 */
context(WebDriver)
public fun <T : WebElement> idOrName(
    locator: String,
    expectedCondition: ((By) -> ExpectedCondition<T>)? = null
): ReadOnlyProperty<Any?, T> = findElement(ByIdOrName(locator), expectedCondition)

/**
 * Finds element by [linkText] locator strategy.
 * [locator] is the exact text to match against.
 * [expectedCondition] can be defined to wait for the element.
 */
context(WebDriver)
public fun <T : WebElement> linkText(
    locator: String,
    expectedCondition: ((By) -> ExpectedCondition<T>)? = null
): ReadOnlyProperty<Any?, T> = findElement(linkText(locator), expectedCondition)

/**
 * Finds element by [name] locator strategy.
 * [locator] is the value of the "name" attribute to search for.
 * [expectedCondition] can be defined to wait for the element.
 */
context(WebDriver)
public fun <T : WebElement> name(
    locator: String,
    expectedCondition: ((By) -> ExpectedCondition<T>)? = null
): ReadOnlyProperty<Any?, T> = findElement(name(locator), expectedCondition)

/**
 * Finds element by [partialLinkText] locator strategy.
 * [locator] is the partial text in link to match against.
 * [expectedCondition] can be defined to wait for the element.
 */
context(WebDriver)
public fun <T : WebElement> partialLinkText(
    locator: String,
    expectedCondition: ((By) -> ExpectedCondition<T>)? = null
): ReadOnlyProperty<Any?, T> = findElement(partialLinkText(locator), expectedCondition)

/**
 * Finds element by [tagName] locator strategy.
 * [locator] is the element's tag name.
 * [expectedCondition] can be defined to wait for the element.
 */
context(WebDriver)
public fun <T : WebElement> tagName(
    locator: String,
    expectedCondition: ((By) -> ExpectedCondition<T>)? = null
): ReadOnlyProperty<Any?, T> = findElement(tagName(locator), expectedCondition)

/**
 * Finds element by [xpath] locator strategy.
 * [locator] is the XPath to use.
 * [expectedCondition] can be defined to wait for the element.
 */
context(WebDriver)
public fun <T : WebElement> xpath(
    locator: String,
    expectedCondition: ((By) -> ExpectedCondition<T>)? = null
): ReadOnlyProperty<Any?, T> = findElement(xpath(locator), expectedCondition)

context(WebDriver)
@Suppress("UNCHECKED_CAST")
private fun <T : WebElement> findElement(
    by: By,
    expectedCondition: ((By) -> ExpectedCondition<T>)?
): ReadOnlyProperty<Any?, T> =
    ReadOnlyProperty { _, property ->
        execute(property.name) {
            val wait = setUpWait(this@WebDriver)
            if (expectedCondition != null) {
                wait.until(expectedCondition(by))
            } else {
                wait.until { findElement(by) }
            } as T
        }
    }

// WebElements

/**
 * Finds elements by [className] locator strategy.
 * [locator] is the value of the "class" attribute to search for.
 * [expectedCondition] can be defined to wait for the elements.
 */
context(WebDriver)
@JvmName("classNames")
public fun <T : WebElements> className(
    locator: String,
    expectedCondition: ((By) -> ExpectedCondition<T>)? = null
): ReadOnlyProperty<Any?, T> = findElements(className(locator), expectedCondition)

/**
 * Finds elements by [cssSelector] locator strategy.
 * [locator] is the CSS expression to search for.
 * [expectedCondition] can be defined to wait for the elements.
 */
context(WebDriver)
@JvmName("csss")
public fun <T : WebElements> css(
    locator: String,
    expectedCondition: ((By) -> ExpectedCondition<T>)? = null
): ReadOnlyProperty<Any?, T> = findElements(cssSelector(locator), expectedCondition)

/**
 * Finds elements by [linkText] locator strategy.
 * [locator] is the exact text to match against.
 * [expectedCondition] can be defined to wait for the elements.
 */
context(WebDriver)
@JvmName("linkTexts")
public fun <T : WebElements> linkText(
    locator: String,
    expectedCondition: ((By) -> ExpectedCondition<T>)? = null
): ReadOnlyProperty<Any?, T> = findElements(linkText(locator), expectedCondition)

/**
 * Finds elements by [name] locator strategy.
 * [locator] is the value of the "name" attribute to search for.
 * [expectedCondition] can be defined to wait for the elements.
 */
context(WebDriver)
@JvmName("names")
public fun <T : WebElements> name(
    locator: String,
    expectedCondition: ((By) -> ExpectedCondition<T>)? = null
): ReadOnlyProperty<Any?, T> = findElements(name(locator), expectedCondition)

/**
 * Finds elements by [partialLinkText] locator strategy.
 * [locator] is the partial text to match against.
 * [expectedCondition] can be defined to wait for the elements.
 */
context(WebDriver)
@JvmName("partialLinkTexts")
public fun <T : WebElements> partialLinkText(
    locator: String,
    expectedCondition: ((By) -> ExpectedCondition<T>)? = null
): ReadOnlyProperty<Any?, T> =
    findElements(partialLinkText(locator), expectedCondition)

/**
 * Finds elements by [tagName] locator strategy.
 * [locator] is the element's tag name.
 * [expectedCondition] can be defined to wait for the elements.
 */
context(WebDriver)
@JvmName("tagNames")
public fun <T : WebElements> tagName(
    locator: String,
    expectedCondition: ((By) -> ExpectedCondition<T>)? = null
): ReadOnlyProperty<Any?, T> = findElements(tagName(locator), expectedCondition)

/**
 * Finds elements by [xpath] locator strategy.
 * [locator] is the XPath to use.
 * [expectedCondition] can be defined to wait for the elements.
 */
context(WebDriver)
@JvmName("xpaths")
public fun <T : WebElements> xpath(
    locator: String,
    expectedCondition: ((By) -> ExpectedCondition<T>)? = null
): ReadOnlyProperty<Any?, T> = findElements(xpath(locator), expectedCondition)

context(WebDriver)
@Suppress("UNCHECKED_CAST")
@JvmName("createWebElements")
private fun <T : WebElements> findElements(
    by: By,
    expectedCondition: ((By) -> ExpectedCondition<T>)?
): ReadOnlyProperty<Any?, T> =
    ReadOnlyProperty { _, property ->
        execute(property.name) {
            val wait = setUpWait(this@WebDriver)
            if (expectedCondition != null) {
                wait.until(expectedCondition(by))
            } else {
                wait.until { findElements(by) }
            } as T
        }
    }

// WebElements with number

/**
 * Finds the [number] of elements by [className] locator strategy.
 * [locator] is the value of the "class" attribute to search for.
 * [expectedCondition] defines the condition to wait for.
 */
context(WebDriver)
public fun <T : WebElements> className(
    locator: String,
    number: Int,
    expectedCondition: ((By, Int) -> ExpectedCondition<T>)
): ReadOnlyProperty<Any?, T> =
    findElements(className(locator), number, expectedCondition)

/**
 * Finds the [number] of elements by [cssSelector] locator strategy.
 * [locator] is the CSS expression to search for.
 * [expectedCondition] defines the condition to wait for.
 */
context(WebDriver)
public fun <T : WebElements> css(
    locator: String,
    number: Int,
    expectedCondition: ((By, Int) -> ExpectedCondition<T>)
): ReadOnlyProperty<Any?, T> =
    findElements(cssSelector(locator), number, expectedCondition)

/**
 * Finds the [number] of elements by [linkText] locator strategy.
 * [locator] is the exact text to match against.
 * [expectedCondition] defines the condition to wait for.
 */
context(WebDriver)
public fun <T : WebElements> linkText(
    locator: String,
    number: Int,
    expectedCondition: ((By, Int) -> ExpectedCondition<T>)
): ReadOnlyProperty<Any?, T> =
    findElements(linkText(locator), number, expectedCondition)

/**
 * Finds the [number] of elements by [name] locator strategy.
 * [locator] is the value of the "name" attribute to search for.
 * [expectedCondition] defines the condition to wait for.
 */
context(WebDriver)
public fun <T : WebElements> name(
    locator: String,
    number: Int,
    expectedCondition: ((By, Int) -> ExpectedCondition<T>)
): ReadOnlyProperty<Any?, T> =
    findElements(name(locator), number, expectedCondition)

/**
 * Finds the [number] of elements by [partialLinkText] locator strategy.
 * [locator] is the partial text to match against.
 * [expectedCondition] defines the condition to wait for.
 */
context(WebDriver)
public fun <T : WebElements> partialLinkText(
    locator: String,
    number: Int,
    expectedCondition: ((By, Int) -> ExpectedCondition<T>)
): ReadOnlyProperty<Any?, T> =
    findElements(partialLinkText(locator), number, expectedCondition)

/**
 * Finds the [number] of elements by [tagName] locator strategy.
 * [locator] is the element's tag name.
 * [expectedCondition] defines the condition to wait for.
 */
context(WebDriver)
public fun <T : WebElements> tagName(
    locator: String,
    number: Int,
    expectedCondition: ((By, Int) -> ExpectedCondition<T>)
): ReadOnlyProperty<Any?, T> =
    findElements(tagName(locator), number, expectedCondition)

/**
 * Finds the [number] of elements by [xpath] locator strategy.
 * [locator] is the XPath to use.
 * [expectedCondition] defines the condition to wait for.
 */
context(WebDriver)
public fun <T : WebElements> xpath(
    locator: String,
    number: Int,
    expectedCondition: ((By, Int) -> ExpectedCondition<T>)
): ReadOnlyProperty<Any?, T> =
    findElements(xpath(locator), number, expectedCondition)

context(WebDriver)
private fun <T : WebElements> findElements(
    by: By,
    number: Int,
    expectedCondition: ((By, Int) -> ExpectedCondition<T>)
): ReadOnlyProperty<Any?, T> =
    ReadOnlyProperty { _, property ->
        execute(property.name) {
            val wait = setUpWait(this@WebDriver)
            wait.until(expectedCondition(by, number))
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
    .ignoring(org.openqa.selenium.NoSuchElementException::class.java)

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

package io.kolibrium.core

import arrow.core.Either
import arrow.core.Either.Companion.catchOrThrow
import mu.KotlinLogging
import org.openqa.selenium.By
import org.openqa.selenium.By.className
import org.openqa.selenium.By.cssSelector
import org.openqa.selenium.By.id
import org.openqa.selenium.By.linkText
import org.openqa.selenium.By.name
import org.openqa.selenium.By.partialLinkText
import org.openqa.selenium.By.tagName
import org.openqa.selenium.By.xpath
import org.openqa.selenium.NoSuchWindowException
import org.openqa.selenium.SessionNotCreatedException
import org.openqa.selenium.TimeoutException
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebDriverException
import org.openqa.selenium.WebElement
import org.openqa.selenium.remote.UnreachableBrowserException
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
): ReadOnlyProperty<Any, Either<Error, T>> = create(className(locator), expectedCondition)

/**
 * Finds element by [cssSelector] locator strategy.
 * [locator] is the CSS expression to search for.
 * [expectedCondition] can be defined to wait for the element.
 */
context(WebDriver)
public fun <T : WebElement> css(
    locator: String,
    expectedCondition: ((By) -> ExpectedCondition<T>)? = null
): ReadOnlyProperty<Any, Either<Error, T>> = create(cssSelector(locator), expectedCondition)

/**
 * Finds element by [id] locator strategy.
 * [locator] is the value of the "id" attribute to search for.
 * [expectedCondition] can be defined to wait for the element.
 */
context(WebDriver)
public fun <T : WebElement> id(
    locator: String,
    expectedCondition: ((By) -> ExpectedCondition<T>)? = null
): ReadOnlyProperty<Any, Either<Error, T>> = create(id(locator), expectedCondition)

/**
 * Tries to find element by [ByIdOrName] locator strategy.
 * [locator] is the value of the "id" or "name" attribute to search for.
 * [expectedCondition] can be defined to wait for the element.
 */
context(WebDriver)
public fun <T : WebElement> idOrName(
    locator: String,
    expectedCondition: ((By) -> ExpectedCondition<T>)? = null
): ReadOnlyProperty<Any, Either<Error, T>> = create(ByIdOrName(locator), expectedCondition)

/**
 * Finds element by [linkText] locator strategy.
 * [locator] is the exact text to match against.
 * [expectedCondition] can be defined to wait for the element.
 */
context(WebDriver)
public fun <T : WebElement> linkText(
    locator: String,
    expectedCondition: ((By) -> ExpectedCondition<T>)? = null
): ReadOnlyProperty<Any, Either<Error, T>> = create(linkText(locator), expectedCondition)

/**
 * Finds element by [name] locator strategy.
 * [locator] is the value of the "name" attribute to search for.
 * [expectedCondition] can be defined to wait for the element.
 */
context(WebDriver)
public fun <T : WebElement> name(
    locator: String,
    expectedCondition: ((By) -> ExpectedCondition<T>)? = null
): ReadOnlyProperty<Any, Either<Error, T>> = create(name(locator), expectedCondition)

/**
 * Finds element by [partialLinkText] locator strategy.
 * [locator] is the partial text to match against.
 * [expectedCondition] can be defined to wait for the element.
 */
context(WebDriver)
public fun <T : WebElement> partialLinkText(
    locator: String,
    expectedCondition: ((By) -> ExpectedCondition<T>)? = null
): ReadOnlyProperty<Any, Either<Error, T>> = create(partialLinkText(locator), expectedCondition)

/**
 * Finds element by [tagName] locator strategy.
 * [locator] is the element's tag name.
 * [expectedCondition] can be defined to wait for the element.
 */
context(WebDriver)
public fun <T : WebElement> tagName(
    locator: String,
    expectedCondition: ((By) -> ExpectedCondition<T>)? = null
): ReadOnlyProperty<Any, Either<Error, T>> = create(tagName(locator), expectedCondition)

/**
 * Finds element by [xpath] locator strategy.
 * [locator] is the XPath to use.
 * [expectedCondition] can be defined to wait for the element.
 */
context(WebDriver)
public fun <T : WebElement> xpath(
    locator: String,
    expectedCondition: ((By) -> ExpectedCondition<T>)? = null
): ReadOnlyProperty<Any, Either<Error, T>> = create(xpath(locator), expectedCondition)

context(WebDriver)
@Suppress("UNCHECKED_CAST")
private fun <T : WebElement> create(
    by: By,
    expectedCondition: ((By) -> ExpectedCondition<T>)?
): ReadOnlyProperty<Any, Either<Error, T>> =
    ReadOnlyProperty { _, property ->
        execute(property.name, by) {
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
): ReadOnlyProperty<Any, Either<Error, T>> = create(className(locator), expectedCondition)

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
): ReadOnlyProperty<Any, Either<Error, T>> = create(cssSelector(locator), expectedCondition)

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
): ReadOnlyProperty<Any, Either<Error, T>> = create(linkText(locator), expectedCondition)

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
): ReadOnlyProperty<Any, Either<Error, T>> = create(name(locator), expectedCondition)

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
): ReadOnlyProperty<Any, Either<Error, T>> =
    create(partialLinkText(locator), expectedCondition)

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
): ReadOnlyProperty<Any, Either<Error, T>> = create(tagName(locator), expectedCondition)

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
): ReadOnlyProperty<Any, Either<Error, T>> = create(xpath(locator), expectedCondition)

context(WebDriver)
@Suppress("UNCHECKED_CAST")
@JvmName("createWebElements")
private fun <T : WebElements> create(
    by: By,
    expectedCondition: ((By) -> ExpectedCondition<T>)?
): ReadOnlyProperty<Any, Either<Error, T>> =
    ReadOnlyProperty { _, property ->
        execute(property.name, by) {
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
): ReadOnlyProperty<Any, Either<Error, T>> =
    create(className(locator), number, expectedCondition)

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
): ReadOnlyProperty<Any, Either<Error, T>> =
    create(cssSelector(locator), number, expectedCondition)

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
): ReadOnlyProperty<Any, Either<Error, T>> =
    create(linkText(locator), number, expectedCondition)

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
): ReadOnlyProperty<Any, Either<Error, T>> =
    create(name(locator), number, expectedCondition)

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
): ReadOnlyProperty<Any, Either<Error, T>> =
    create(partialLinkText(locator), number, expectedCondition)

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
): ReadOnlyProperty<Any, Either<Error, T>> =
    create(tagName(locator), number, expectedCondition)

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
): ReadOnlyProperty<Any, Either<Error, T>> =
    create(xpath(locator), number, expectedCondition)

context(WebDriver)
private fun <T : WebElements> create(
    by: By,
    number: Int,
    expectedCondition: ((By, Int) -> ExpectedCondition<T>)
): ReadOnlyProperty<Any, Either<Error, T>> =
    ReadOnlyProperty { _, property ->
        execute(property.name, by) {
            val wait = setUpWait(this@WebDriver)
            wait.until(expectedCondition(by, number))
        }
    }

private val logger = KotlinLogging.logger {}

context(WebDriver)
private fun <T> execute(
    element: String,
    by: By,
    block: () -> T
): Either<Error, T> {
    logger.trace("Waiting for \"$element\"")

    return catchOrThrow<WebDriverException, T> {
        block()
    }.mapLeft { e ->
        when (e) {
            is NoSuchWindowException -> Error.NoSuchWindow
            is SessionNotCreatedException -> Error.SessionNotCreatedException
            is TimeoutException -> Error.ElementNotFound(by.toString())
            is UnreachableBrowserException -> Error.UnreachableBrowser
            else -> throw e
        }
    }
}

private const val TIMEOUT: Long = 10
private const val POOLING_INTERVAL: Long = 1

private fun setUpWait(driver: WebDriver) = FluentWait(driver)
    .withTimeout(ofSeconds(TIMEOUT))
    .pollingEvery(ofSeconds(POOLING_INTERVAL))
    .ignoring(org.openqa.selenium.NoSuchElementException::class.java)

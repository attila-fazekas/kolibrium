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

package io.kolibrium.core

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
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ByIdOrName
import org.openqa.selenium.support.ui.ExpectedCondition
import org.openqa.selenium.support.ui.FluentWait
import java.time.Duration.ofSeconds
import kotlin.properties.ReadOnlyProperty

private const val TIMEOUT: Long = 10
private const val POOLING_INTERVAL: Long = 1

private val logger = KotlinLogging.logger {}

/**
 * Find element by [className] locator strategy.
 * [locator] is the value of the "class" attribute to search for.
 * [expectedCondition] can be defined to wait for the element.
 */
context(WebDriver)
public fun <T : WebElement> className(
    locator: String,
    expectedCondition: ((By) -> ExpectedCondition<T>)? = null
): ReadOnlyProperty<Any, WebElement> = create(className(locator), expectedCondition)

/**
 * Find element by [cssSelector] locator strategy.
 * [locator] is the CSS expression to search for.
 * [expectedCondition] can be defined to wait for the element.
 */
context(WebDriver)
public fun <T : WebElement> css(
    locator: String,
    expectedCondition: ((By) -> ExpectedCondition<T>)? = null
): ReadOnlyProperty<Any, WebElement> = create(cssSelector(locator), expectedCondition)

/**
 * Find element by [id] locator strategy.
 * [locator] is the value of the "id" attribute to search for.
 * [expectedCondition] can be defined to wait for the element.
 */
context(WebDriver)
public fun <T : WebElement> id(
    locator: String,
    expectedCondition: ((By) -> ExpectedCondition<T>)? = null
): ReadOnlyProperty<Any, WebElement> = create(id(locator), expectedCondition)

/**
 * Tries to find element by [ByIdOrName] locator strategy.
 * [locator] is the value of the "id" or "name" attribute to search for.
 * [expectedCondition] can be defined to wait for the element.
 */
context(WebDriver)
public fun <T : WebElement> idOrName(
    locator: String,
    expectedCondition: ((By) -> ExpectedCondition<T>)? = null
): ReadOnlyProperty<Any, WebElement> = create(ByIdOrName(locator), expectedCondition)

/**
 * Find element by [linkText] locator strategy.
 * [locator] is the exact text to match against.
 * [expectedCondition] can be defined to wait for the element.
 */
context(WebDriver)
public fun <T : WebElement> linkText(
    locator: String,
    expectedCondition: ((By) -> ExpectedCondition<T>)? = null
): ReadOnlyProperty<Any, WebElement> = create(linkText(locator), expectedCondition)

/**
 * Find element by [name] locator strategy.
 * [locator] is the value of the "name" attribute to search for.
 * [expectedCondition] can be defined to wait for the element.
 */
context(WebDriver)
public fun <T : WebElement> name(
    locator: String,
    expectedCondition: ((By) -> ExpectedCondition<T>)? = null
): ReadOnlyProperty<Any, WebElement> = create(name(locator), expectedCondition)

/**
 * Find element by [partialLinkText] locator strategy.
 * [locator] is the partial text to match against.
 * [expectedCondition] can be defined to wait for the element.
 */
context(WebDriver)
public fun <T : WebElement> partialLinkText(
    locator: String,
    expectedCondition: ((By) -> ExpectedCondition<T>)? = null
): ReadOnlyProperty<Any, WebElement> = create(partialLinkText(locator), expectedCondition)

/**
 * Find element by [tagName] locator strategy.
 * [locator] is the element's tag name.
 * [expectedCondition] can be defined to wait for the element.
 */
context(WebDriver)
public fun <T : WebElement> tagName(
    locator: String,
    expectedCondition: ((By) -> ExpectedCondition<T>)? = null
): ReadOnlyProperty<Any, WebElement> = create(tagName(locator), expectedCondition)

/**
 * Find element by [xpath] locator strategy.
 * [locator] is the XPath to use.
 * [expectedCondition] can be defined to wait for the element.
 */
context(WebDriver)
public fun <T : WebElement> xpath(
    locator: String,
    expectedCondition: ((By) -> ExpectedCondition<T>)? = null
): ReadOnlyProperty<Any, WebElement> = create(xpath(locator), expectedCondition)

context(WebDriver)
private fun <T : WebElement> create(
    by: By,
    expectedCondition: ((By) -> ExpectedCondition<T>)?
): ReadOnlyProperty<Any, WebElement> =
    ReadOnlyProperty { _, property ->
        logger.trace("Waiting for \"${property.name}\"")
        val wait = FluentWait(this@WebDriver)
            .withTimeout(ofSeconds(TIMEOUT))
            .pollingEvery(ofSeconds(POOLING_INTERVAL))
            .ignoring(NoSuchElementException::class.java)

        if (expectedCondition != null) {
            wait.until(expectedCondition(by))
        } else {
            wait.until { findElement(by) }
        }
    }

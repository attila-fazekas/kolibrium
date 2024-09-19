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
import dev.kolibrium.dsl.selenium.wait.SyncConfig
import dev.kolibrium.dsl.selenium.wait.WebElementSyncConfig
import dev.kolibrium.dsl.selenium.wait.WebElementsSyncConfig
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

context(WebDriver)
public fun className(
    locator: String,
    syncConfig: (SyncConfig<WebElement>.() -> Unit) = {},
): ReadOnlyProperty<Any, WebElement> = genericLocator(locator, By::className, syncConfig)

context(WebDriver)
public fun classNames(
    locator: String,
    syncConfig: (SyncConfig<WebElements>.() -> Unit) = {},
): ReadOnlyProperty<Any, WebElements> = genericLocator(locator, By::className, syncConfig)

context(WebDriver)
public fun cssSelector(
    locator: String,
    syncConfig: SyncConfig<WebElement>.() -> Unit = {},
): ReadOnlyProperty<Any, WebElement> = genericLocator(locator, By::cssSelector, syncConfig)

context(WebDriver)
public fun cssSelectors(
    locator: String,
    syncConfig: SyncConfig<WebElements>.() -> Unit = {},
): ReadOnlyProperty<Any, WebElements> = genericLocator(locator, By::cssSelector, syncConfig)

context(WebDriver)
public fun id(
    locator: String,
    syncConfig: SyncConfig<WebElement>.() -> Unit = {},
): ReadOnlyProperty<Any, WebElement> = genericLocator(locator, By::id, syncConfig)

context(WebDriver)
public fun idOrName(
    locator: String,
    syncConfig: SyncConfig<WebElement>.() -> Unit = {},
): ReadOnlyProperty<Any, WebElement> = genericLocator(locator, ::ByIdOrName, syncConfig)

context(WebDriver)
public fun linkText(
    locator: String,
    syncConfig: SyncConfig<WebElement>.() -> Unit = {},
): ReadOnlyProperty<Any, WebElement> = genericLocator(locator, By::linkText, syncConfig)

context(WebDriver)
public fun linkTexts(
    locator: String,
    syncConfig: SyncConfig<WebElements>.() -> Unit = {},
): ReadOnlyProperty<Any, WebElements> = genericLocator(locator, By::linkText, syncConfig)

context(WebDriver)
public fun name(
    locator: String,
    syncConfig: SyncConfig<WebElement>.() -> Unit = {},
): ReadOnlyProperty<Any, WebElement> = genericLocator(locator, By::name, syncConfig)

context(WebDriver)
public fun names(
    locator: String,
    syncConfig: SyncConfig<WebElements>.() -> Unit = {},
): ReadOnlyProperty<Any, WebElements> = genericLocator(locator, By::name, syncConfig)

context(WebDriver)
public fun partialLinkText(
    locator: String,
    syncConfig: SyncConfig<WebElement>.() -> Unit = {},
): ReadOnlyProperty<Any, WebElement> = genericLocator(locator, By::partialLinkText, syncConfig)

context(WebDriver)
public fun partialLinkTexts(
    locator: String,
    syncConfig: SyncConfig<WebElements>.() -> Unit = {},
): ReadOnlyProperty<Any, WebElements> = genericLocator(locator, By::partialLinkText, syncConfig)

context(WebDriver)
public fun tagName(
    locator: String,
    syncConfig: SyncConfig<WebElement>.() -> Unit = {},
): ReadOnlyProperty<Any, WebElement> = genericLocator(locator, By::tagName, syncConfig)

context(WebDriver)
public fun tagNames(
    locator: String,
    syncConfig: SyncConfig<WebElements>.() -> Unit = {},
): ReadOnlyProperty<Any, WebElements> = genericLocator(locator, By::tagName, syncConfig)

context(WebDriver)
public fun xpath(
    locator: String,
    syncConfig: SyncConfig<WebElement>.() -> Unit = {},
): ReadOnlyProperty<Any, WebElement> = genericLocator(locator, By::xpath, syncConfig)

context(WebDriver)
public fun xpaths(
    locator: String,
    syncConfig: SyncConfig<WebElements>.() -> Unit = {},
): ReadOnlyProperty<Any, WebElements> = genericLocator(locator, By::xpath, syncConfig)

context(WebDriver)
@Suppress("UNCHECKED_CAST")
internal inline fun <reified T> genericLocator(
    locator: String,
    noinline by: (String) -> By,
    noinline syncConfig: SyncConfig<T>.() -> Unit = {},
): ReadOnlyProperty<Any, T> =
    when (T::class) {
        WebElement::class -> KWebElement(locator, by, syncConfig as SyncConfig<WebElement>.() -> Unit)
        List::class -> KWebElements(locator, by, syncConfig as SyncConfig<WebElements>.() -> Unit)
        else -> throw IllegalArgumentException("Unsupported type: ${T::class.simpleName}")
    } as ReadOnlyProperty<Any, T>

context(WebDriver)
internal class KWebElement(
    private val locator: String,
    private val by: (String) -> By,
    private val syncConfig: SyncConfig<WebElement>.() -> Unit,
) : ReadOnlyProperty<Any, WebElement> {
    private val webElement: WebElement by lazy { findElement(by(locator)) }

    override fun getValue(
        thisRef: Any,
        property: KProperty<*>,
    ): WebElement {
        return execute(property.name) {
            val config = WebElementSyncConfig().apply(syncConfig)
            val wait = setUpWait(this@WebDriver, config.wait)
            wait.until { config.until(webElement) }
            webElement
        }
    }
}

context(WebDriver)
internal class KWebElements(
    private val locator: String,
    private val by: (String) -> By,
    private val syncConfig: SyncConfig<WebElements>.() -> Unit,
) : ReadOnlyProperty<Any, WebElements> {
    private val webElements: WebElements by lazy { findElements(by(locator)) }

    override fun getValue(
        thisRef: Any,
        property: KProperty<*>,
    ): WebElements {
        return execute(property.name) {
            val config = WebElementsSyncConfig().apply(syncConfig)
            val wait = setUpWait(this@WebDriver, config.wait)
            wait.until {
                val elements = findElements(by(locator))
                config.until(elements)
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

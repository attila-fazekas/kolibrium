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
public inline fun <reified T> className(
    locator: String,
    noinline syncConfig: (SyncConfig<T>.() -> Unit) = {},
): ReadOnlyProperty<Any, T> = genericLocator(className(locator), syncConfig)

context(WebDriver)
public inline fun <reified T> css(
    locator: String,
    noinline syncConfig: SyncConfig<T>.() -> Unit = {},
): ReadOnlyProperty<Any, T> = genericLocator(cssSelector(locator), syncConfig)

context(WebDriver)
public inline fun <reified T> id(
    locator: String,
    noinline syncConfig: SyncConfig<T>.() -> Unit = {},
): ReadOnlyProperty<Any, T> = genericLocator(id(locator), syncConfig)

context(WebDriver)
public inline fun <reified T> idOrName(
    locator: String,
    noinline syncConfig: SyncConfig<T>.() -> Unit = {},
): ReadOnlyProperty<Any, T> = genericLocator(ByIdOrName(locator), syncConfig)

context(WebDriver)
public inline fun <reified T> linkText(
    locator: String,
    noinline syncConfig: SyncConfig<T>.() -> Unit = {},
): ReadOnlyProperty<Any, T> = genericLocator(linkText(locator), syncConfig)

context(WebDriver)
public inline fun <reified T> name(
    locator: String,
    noinline syncConfig: SyncConfig<T>.() -> Unit = {},
): ReadOnlyProperty<Any, T> = genericLocator(name(locator), syncConfig)

context(WebDriver)
public inline fun <reified T> partialLinkText(
    locator: String,
    noinline syncConfig: SyncConfig<T>.() -> Unit = {},
): ReadOnlyProperty<Any, T> = genericLocator(partialLinkText(locator), syncConfig)

context(WebDriver)
public inline fun <reified T> tagName(
    locator: String,
    noinline syncConfig: SyncConfig<T>.() -> Unit = {},
): ReadOnlyProperty<Any, T> = genericLocator(tagName(locator), syncConfig)

context(WebDriver)
public inline fun <reified T> xpath(
    locator: String,
    noinline syncConfig: SyncConfig<T>.() -> Unit = {},
): ReadOnlyProperty<Any, T> = genericLocator(xpath(locator), syncConfig)

context(WebDriver)
@Suppress("UNCHECKED_CAST")
public inline fun <reified T> genericLocator(
    by: By,
    noinline syncConfig: SyncConfig<T>.() -> Unit = {},
): ReadOnlyProperty<Any, T> =
    when (T::class) {
        WebElement::class -> KWebElement(by, syncConfig as SyncConfig<WebElement>.() -> Unit)
        List::class -> KWebElements(by, syncConfig as SyncConfig<WebElements>.() -> Unit)
        else -> throw IllegalArgumentException(
            "'$by' locator has unsupported type: ${T::class.simpleName}. " +
                "Please use either WebElement or WebElements instead.",
        )
    } as ReadOnlyProperty<Any, T>

context(WebDriver)
public class KWebElement(
    private val by: By,
    private val syncConfig: SyncConfig<WebElement>.() -> Unit,
) : ReadOnlyProperty<Any, WebElement> {
    private val webElement: WebElement by lazy { findElement(by) }

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
public class KWebElements(
    private val by: By,
    private val syncConfig: SyncConfig<WebElements>.() -> Unit,
) : ReadOnlyProperty<Any, WebElements> {
    private val webElements: WebElements by lazy { findElements(by) }

    override fun getValue(
        thisRef: Any,
        property: KProperty<*>,
    ): WebElements {
        return execute(property.name) {
            val config = WebElementsSyncConfig().apply(syncConfig)
            val wait = setUpWait(this@WebDriver, config.wait)
            wait.until {
                val elements = findElements(by)
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

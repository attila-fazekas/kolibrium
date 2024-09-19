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
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ByIdOrName
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

private typealias WebElementProperty = ReadOnlyProperty<Any, WebElement>
private typealias WebElementsProperty = ReadOnlyProperty<Any, WebElements>

public fun WebDriver.className(
    locator: String,
    syncConfig: (SyncConfig<WebElement>.() -> Unit) = {},
): WebElementProperty = genericLocator(locator, By::className, syncConfig)

public fun WebDriver.classNames(
    locator: String,
    syncConfig: (SyncConfig<WebElements>.() -> Unit) = {},
): WebElementsProperty = genericLocator(locator, By::className, syncConfig)

public fun WebDriver.cssSelector(
    locator: String,
    syncConfig: SyncConfig<WebElement>.() -> Unit = {},
): WebElementProperty = genericLocator(locator, By::cssSelector, syncConfig)

public fun WebDriver.cssSelectors(
    locator: String,
    syncConfig: SyncConfig<WebElements>.() -> Unit = {},
): WebElementsProperty = genericLocator(locator, By::cssSelector, syncConfig)

public fun WebDriver.id(
    locator: String,
    syncConfig: SyncConfig<WebElement>.() -> Unit = {},
): WebElementProperty = genericLocator(locator, By::id, syncConfig)

public fun WebDriver.idOrName(
    locator: String,
    syncConfig: SyncConfig<WebElement>.() -> Unit = {},
): WebElementProperty = genericLocator(locator, ::ByIdOrName, syncConfig)

public fun WebDriver.linkText(
    locator: String,
    syncConfig: SyncConfig<WebElement>.() -> Unit = {},
): WebElementProperty = genericLocator(locator, By::linkText, syncConfig)

public fun WebDriver.linkTexts(
    locator: String,
    syncConfig: SyncConfig<WebElements>.() -> Unit = {},
): WebElementsProperty = genericLocator(locator, By::linkText, syncConfig)

public fun WebDriver.name(
    locator: String,
    syncConfig: SyncConfig<WebElement>.() -> Unit = {},
): WebElementProperty = genericLocator(locator, By::name, syncConfig)

public fun WebDriver.names(
    locator: String,
    syncConfig: SyncConfig<WebElements>.() -> Unit = {},
): WebElementsProperty = genericLocator(locator, By::name, syncConfig)

public fun WebDriver.partialLinkText(
    locator: String,
    syncConfig: SyncConfig<WebElement>.() -> Unit = {},
): WebElementProperty = genericLocator(locator, By::partialLinkText, syncConfig)

public fun WebDriver.partialLinkTexts(
    locator: String,
    syncConfig: SyncConfig<WebElements>.() -> Unit = {},
): WebElementsProperty = genericLocator(locator, By::partialLinkText, syncConfig)

public fun WebDriver.tagName(
    locator: String,
    syncConfig: SyncConfig<WebElement>.() -> Unit = {},
): WebElementProperty = genericLocator(locator, By::tagName, syncConfig)

public fun WebDriver.tagNames(
    locator: String,
    syncConfig: SyncConfig<WebElements>.() -> Unit = {},
): WebElementsProperty = genericLocator(locator, By::tagName, syncConfig)

public fun WebDriver.xpath(
    locator: String,
    syncConfig: SyncConfig<WebElement>.() -> Unit = {},
): WebElementProperty = genericLocator(locator, By::xpath, syncConfig)

public fun WebDriver.xpaths(
    locator: String,
    syncConfig: SyncConfig<WebElements>.() -> Unit = {},
): WebElementsProperty = genericLocator(locator, By::xpath, syncConfig)

@Suppress("UNCHECKED_CAST")
internal inline fun <reified T> WebDriver.genericLocator(
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
) : WebElementProperty {
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
) : WebElementsProperty {
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

private fun <T> execute(
    element: String,
    block: () -> T,
): T {
    logger.trace { "Waiting for \"$element\"" }
    return block()
}

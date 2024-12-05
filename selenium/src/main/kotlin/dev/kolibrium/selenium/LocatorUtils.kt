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
import dev.kolibrium.selenium.configuration.DefaultSeleniumProjectConfiguration
import dev.kolibrium.selenium.configuration.SeleniumProjectConfiguration
import org.openqa.selenium.WebElement

/**
 * Checks if the [WebElement] is both displayed and enabled, making it clickable in the UI.
 *
 * This property combines Selenium's [isDisplayed][org.openqa.selenium.WebElement.isDisplayed] and
 * [isEnabled][org.openqa.selenium.WebElement.isEnabled] checks to determine if the element is ready
 * for user interaction through clicking.
 *
 * @receiver The [WebElement] to check for clickability.
 * @return `true` if the element is both displayed and enabled, `false` otherwise.
 */
public val WebElement.clickable: Boolean
    get() = isDisplayed && isEnabled

/**
 * Checks if the [WebElement]s are both displayed and enabled, making them clickable in the UI.
 *
 * This property combines Selenium's [isDisplayed][org.openqa.selenium.WebElement.isDisplayed] and
 * [isEnabled][org.openqa.selenium.WebElement.isEnabled] checks to determine if the elements are ready
 * for user interaction through clicking.
 *
 * @receiver The [WebElement]s to check for clickability.
 * @return `true` if the elements are both displayed and enabled, `false` otherwise.
 */
public val WebElements.clickable: Boolean
    get() = all { isDisplayed && isEnabled }

/**
 * Checks if all [WebElements] in this collection are displayed in the UI.
 *
 * This extension property on a collection of [WebElement]s provides a convenient way to verify
 * that all elements are visible. Returns `true` only if every element in the collection is displayed.
 *
 * @receiver Collection of [WebElement]s to check for visibility.
 * @return `true` if all elements in the collection are displayed, `false` if any element is not displayed.
 */
public val WebElements.isDisplayed: Boolean
    get() = all { it.isDisplayed }

/**
 * Checks if all [WebElements] in this collection are enabled in the UI.
 *
 * This extension property on a collection of [WebElement]s provides a convenient way to verify
 * that all elements are enabled. Returns `true` only if every element in the collection is enabled.
 *
 * @receiver Collection of [WebElement]s to check for enabled state.
 * @return `true` if all elements in the collection are enabled, `false` if any element is not enabled.
 */
public val WebElements.isEnabled: Boolean
    get() = all { it.isEnabled }

/**
 * Default readyWhen used for single element lookup.
 */
public val defaultElementReadyWhen: WebElement.() -> Boolean by lazy {
    SeleniumProjectConfiguration.actualConfig().elementReadyWhen ?: DefaultSeleniumProjectConfiguration.elementReadyWhen
}

/**
 * Default readyWhen used for multiple elements lookup.
 */
public val defaultElementsReadyWhen: WebElements.() -> Boolean by lazy {
    SeleniumProjectConfiguration.actualConfig().elementsReadyWhen
        ?: DefaultSeleniumProjectConfiguration.elementsReadyWhen
}

/**
 * Default wait used for element lookup.
 */
public val defaultWait: Wait by lazy {
    SeleniumProjectConfiguration.actualConfig().wait ?: DefaultSeleniumProjectConfiguration.wait
}

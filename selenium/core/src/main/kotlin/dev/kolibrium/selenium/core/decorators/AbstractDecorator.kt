/*
 * Copyright 2023-2026 Attila Fazekas & contributors
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

package dev.kolibrium.selenium.core.decorators

import org.openqa.selenium.SearchContext
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement

/**
 * Base type for Kolibrium decorators.
 *
 * A decorator augments Selenium objects while keeping the original API untouched. In Kolibrium
 * we decorate the [SearchContext] contract itself (both [WebDriver] and [WebElement] implement it)
 * so that returned elements are also decorated, enabling safe chaining across nested component trees.
 *
 * Notes
 * - This mechanism is independent of Selenium's EventFiringDecorator. Some decorators may still use
 *   a [org.openqa.selenium.support.events.WebDriverListener] internally for interaction callbacks,
 *   but chaining is always preserved through the Kolibrium wrappers.
 */
public abstract class AbstractDecorator {
    internal fun decorate(context: SearchContext): SearchContext = decorateSearchContext(context)

    internal abstract fun decorateSearchContext(context: SearchContext): SearchContext

    internal abstract fun decorateElement(element: WebElement): WebElement
}

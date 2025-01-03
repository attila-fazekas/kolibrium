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

package dev.kolibrium.selenium.decorators

import org.openqa.selenium.SearchContext
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement

/**
 * Base abstract class for implementing WebDriver decorators following the decorator pattern.
 * Provides a structured way to add behaviors to both WebDriver and WebElement instances.
 *
 * This class handles the initial [SearchContext] type checking and routing to appropriate
 * decoration methods, while concrete implementations define specific behaviors by overriding
 * [decorateDriver] and [decorateElement].
 *
 * @see SearchContext
 * @see WebDriver
 * @see WebElement
 */
public abstract class AbstractDecorator {
    /**
     * Decorates a SearchContext (WebDriver or WebElement) with additional capabilities.
     *
     * @param context The SearchContext to decorate.
     * @return The decorated SearchContext with added capabilities.
     */
    public fun decorate(context: SearchContext): SearchContext =
        when (context) {
            is WebDriver -> decorateDriver(context)
            is WebElement -> decorateElement(context)
            else -> context
        }

    internal abstract fun decorateDriver(driver: WebDriver): WebDriver

    internal abstract fun decorateElement(element: WebElement): WebElement
}

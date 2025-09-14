/*
 * Copyright 2023-2025 Attila Fazekas & contributors
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

package dev.kolibrium.core.selenium.decorators

import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.SearchContext
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.WrapsDriver
import org.openqa.selenium.support.events.EventFiringDecorator
import org.openqa.selenium.support.events.WebDriverListener

/**
 * Wraps the provided [SearchContext] with an [EventFiringDecorator] using the given [listener]
 * if the context is a [WebDriver]. Otherwise, returns the original context unchanged.
 */
internal fun wrapWithListenerIfDriver(
    context: SearchContext,
    listener: WebDriverListener,
): SearchContext =
    if (context is WebDriver) {
        EventFiringDecorator<WebDriver>(listener).decorate(context)
    } else {
        context
    }

/**
 * Attempts to obtain a [JavascriptExecutor] from the provided [WebElement] by unwrapping
 * its [WebDriver] using [WrapsDriver]. Returns null if not available.
 */
internal fun WebElement.tryGetJsExecutor(): JavascriptExecutor? = (this as? WrapsDriver)?.wrappedDriver as? JavascriptExecutor

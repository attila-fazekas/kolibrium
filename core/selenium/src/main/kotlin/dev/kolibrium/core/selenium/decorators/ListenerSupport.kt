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
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.WrapsDriver
import org.openqa.selenium.support.events.WebDriverListener

/**
 * Interface for decorators that want to receive [org.openqa.selenium.WebDriver] interaction callbacks.
 */
public interface InteractionAware {
    /**
     * Supplies a Selenium [WebDriverListener] that will receive interaction callbacks
     * (e.g., beforeClick, beforeSendKeys) for a decorated [WebDriver].
     *
     * Implementors may return `null` to indicate they do not need interaction callbacks.
     * The listener, when present, will be registered once via Kolibrium's dispatcher so that
     * multiple decorators can receive events without stacking multiple driver proxies.
     */
    public fun interactionListener(): WebDriverListener?
}

/**
 * A simple multiplexer that dispatches WebDriverListener callbacks to multiple listeners in order.
 * Exceptions from individual listeners are caught and ignored to avoid breaking the chain.
 */
internal class ListenerMultiplexer(
    private val listeners: List<WebDriverListener>,
) : WebDriverListener {
    override fun beforeClick(element: WebElement) {
        listeners.forEach { runCatching { it.beforeClick(element) } }
    }

    override fun beforeSendKeys(
        element: WebElement,
        vararg keysToSend: CharSequence,
    ) {
        listeners.forEach { runCatching { it.beforeSendKeys(element, *keysToSend) } }
    }
}

/**
 * Attempts to obtain a [JavascriptExecutor] from the provided [WebElement] by unwrapping
 * its [WebDriver] using [WrapsDriver]. Returns null if not available.
 */
internal fun WebElement.tryGetJsExecutor(): JavascriptExecutor? = (this as? WrapsDriver)?.wrappedDriver as? JavascriptExecutor

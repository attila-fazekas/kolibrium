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

import dev.kolibrium.common.WebElements
import org.openqa.selenium.By
import org.openqa.selenium.SearchContext
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.events.WebDriverListener
import java.lang.Thread.sleep
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

/**
 * Decorator that inserts delays to make browser actions easier to observe.
 *
 * By default the delay is applied only before element interactions (click, sendKeys) using a
 * Selenium [org.openqa.selenium.support.events.WebDriverListener]. Chaining is preserved by
 * wrapping elements returned from `findElement(s)`.
 *
 * Typical uses: demos, debugging flakiness visually, recordings.
 *
 * @param wait Duration of the delay to add. Must be non-negative. Default: 1 second.
 */
public class SlowMotionDecorator(
    private val wait: Duration = 1.seconds,
) : AbstractDecorator(),
    InteractionAware {
    init {
        require(!wait.isNegative()) { "wait must not be negative." }
    }

    override fun decorateSearchContext(context: SearchContext): SearchContext {
        return object : SearchContext by context {
            override fun findElement(by: By): WebElement {
                val foundElement = context.findElement(by)
                addDelay()
                return decorateElement(foundElement)
            }

            override fun findElements(by: By): WebElements {
                val elements = context.findElements(by)
                addDelay()
                return elements.map { foundElement ->
                    decorateElement(foundElement)
                }
            }
        }
    }

    override fun decorateElement(element: WebElement): WebElement {
        return object : WebElement by element {
            override fun findElement(by: By): WebElement {
                val foundElement = element.findElement(by)
                addDelay()
                return decorateElement(foundElement)
            }

            override fun findElements(by: By): WebElements {
                val elements = element.findElements(by)
                addDelay()
                return elements.map { foundElement ->
                    decorateElement(foundElement)
                }
            }
        }
    }

    override fun interactionListener(): WebDriverListener = SlowListener()

    internal inner class SlowListener : WebDriverListener {
        override fun beforeClick(element: WebElement) {
            addDelay()
        }

        override fun beforeSendKeys(
            element: WebElement,
            vararg keysToSend: CharSequence,
        ) {
            addDelay()
        }
    }

    private fun addDelay() {
        try {
            sleep(wait.toJavaDuration())
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }
}

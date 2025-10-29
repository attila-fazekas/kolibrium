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

import dev.kolibrium.core.selenium.WebElements
import io.github.oshai.kotlinlogging.KotlinLogging
import org.openqa.selenium.By
import org.openqa.selenium.SearchContext
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.events.WebDriverListener

private val logger = KotlinLogging.logger {}

private const val MIN = 1
private const val MAX = 20

/**
 * Decorator that visually highlights elements.
 *
 * The decorator uses a Selenium [WebDriverListener] under the hood
 * to highlight elements before interactions (click, sendKeys). It also preserves chaining by decorating
 * the elements returned from `findElement(s)` so that nested searches remain highlighted on interaction.
 *
 * Implementation notes
 * - Uses CSS `outline` instead of `border` to avoid layout shifts.
 * - Marks the currently highlighted element with `data-kolibrium-highlight="true"` and removes the
 *   marker and outline from the previously highlighted element.
 *
 * @param style Border line style to use for highlighting. Default: [BorderStyle.Solid].
 * @param color Border color to use for highlighting. Default: [Color.Red].
 * @param width Outline width in pixels. Must be between 1 and 20 (inclusive). Default: 5.
 */
public class HighlighterDecorator(
    private val style: BorderStyle = BorderStyle.Solid,
    private val color: Color = Color.Red,
    private val width: Int = 5,
) : AbstractDecorator(),
    InteractionAware {
    init {
        require(width in MIN..MAX) { "width must be between 1 and 20." }
    }

    override fun decorateSearchContext(context: SearchContext): SearchContext {
        return object : SearchContext by context {
            override fun findElement(by: By): WebElement {
                val foundElement = context.findElement(by)
                foundElement.highlightElement()
                return decorateElement(foundElement)
            }

            override fun findElements(by: By): WebElements =
                context.findElements(by).map { foundElement ->
                    foundElement.highlightElement()
                    decorateElement(foundElement)
                }
        }
    }

    override fun decorateElement(element: WebElement): WebElement {
        return object : WebElement by element {
            override fun findElement(by: By): WebElement {
                val foundElement = element.findElement(by)
                foundElement.highlightElement()
                return decorateElement(foundElement)
            }

            override fun findElements(by: By): WebElements =
                element.findElements(by).map { foundElement ->
                    foundElement.highlightElement()
                    decorateElement(foundElement)
                }
        }
    }

    override fun interactionListener(): WebDriverListener = HighlighterListener()

    internal inner class HighlighterListener : WebDriverListener {
        override fun beforeClick(element: WebElement) {
            element.highlightElement()
        }

        override fun beforeSendKeys(
            element: WebElement,
            vararg keysToSend: CharSequence,
        ) {
            element.highlightElement()
        }
    }

    private fun WebElement.highlightElement() {
        try {
            val js = tryGetJsExecutor() ?: return
            js.executeScript(
                (
                    """
                    const prev = document.querySelector('[data-kolibrium-highlight="true"]');
                    if (prev && prev !== arguments[0]) {
                        prev.style.removeProperty('outline');
                        prev.removeAttribute('data-kolibrium-highlight');
                    }
                    arguments[0].setAttribute('data-kolibrium-highlight','true');
                    arguments[0].style.outline = '${width}px ${style.name.lowercase()} ${color.name.lowercase()}';
                    """
                ).trimIndent(),
                this,
            )
        } catch (e: Exception) {
            logger.error { "Failed to highlight element: ${e.message}" }
        }
    }
}

/**
 * Defines the available border styles for element highlighting.
 */
public enum class BorderStyle {
    /** Dashed border line style. */
    Dashed,

    /** Dotted border line style. */
    Dotted,

    /** Solid border line style. */
    Solid,
}

/**
 * Defines the available colors for element highlighting.
 */
public enum class Color {
    /** Black color. */
    Black,

    /** Blue color. */
    Blue,

    /** Gray color. */
    Gray,

    /** Green color. */
    Green,

    /** Orange color. */
    Orange,

    /** Pink color. */
    Pink,

    /** Purple color. */
    Purple,

    /** Red color. */
    Red,

    /** Yellow color. */
    Yellow,

    /** White color. */
    White,
}

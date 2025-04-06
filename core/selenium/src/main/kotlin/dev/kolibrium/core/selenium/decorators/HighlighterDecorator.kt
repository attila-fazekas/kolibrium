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
import dev.kolibrium.core.selenium.decorators.BorderStyle.SOLID
import dev.kolibrium.core.selenium.decorators.Color.RED
import io.github.oshai.kotlinlogging.KotlinLogging
import org.openqa.selenium.By
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.remote.RemoteWebElement

private val logger = KotlinLogging.logger {}

private const val MIN = 1
private const val MAX = 20

/**
 * Decorator that adds visual highlighting to web elements during Selenium operations.
 * Highlights elements by adding a configurable border around them when they are found or interacted with.
 *
 * @param style The border style to use for highlighting (default: SOLID).
 * @param color The color to use for highlighting (default: RED).
 * @param width The border width in pixels (default: 5).
 */
public class HighlighterDecorator(
    private val style: BorderStyle = SOLID,
    private val color: Color = RED,
    private val width: Int = 5,
) : AbstractDecorator() {
    init {
        require(width in MIN..MAX) { "width must be between 1 and 20." }
    }

    override fun decorateDriver(driver: WebDriver): WebDriver {
        return object : WebDriver by driver {
            override fun findElement(by: By): WebElement {
                val element = driver.findElement(by)
                element.highlightElement()
                return decorateElement(element)
            }

            override fun findElements(by: By): WebElements =
                driver.findElements(by).map { element ->
                    element.highlightElement()
                    decorateElement(element)
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

    private fun WebElement.highlightElement() {
        try {
            // Get the WebDriver from the element itself
            val driver =
                when (this) {
                    is RemoteWebElement -> wrappedDriver
                    else -> null
                } ?: return // Skip highlighting if we can't get the driver

            (driver as JavascriptExecutor).executeScript(
                """
                const elements = document.querySelectorAll('[style*="border"]');
                elements.forEach(el => el.style.removeProperty('border'));
                arguments[0].style.border = '${style.name.lowercase()} ${color.name.lowercase()} ${width}px';
                """.trimIndent(),
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
    DASHED,

    /** Dotted border line style. */
    DOTTED,

    /** Solid border line style. */
    SOLID,
}

/**
 * Defines the available colors for element highlighting.
 */
public enum class Color {
    /** Black color. */
    BLACK,

    /** Blue color. */
    BLUE,

    /** Gray color. */
    GRAY,

    /** Green color. */
    GREEN,

    /** Orange color. */
    ORANGE,

    /** Pink color. */
    PINK,

    /** Purple color. */
    PURPLE,

    /** Red color. */
    RED,

    /** Yellow color. */
    YELLOW,

    /** White color. */
    WHITE,
}

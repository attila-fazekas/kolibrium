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
import org.openqa.selenium.StaleElementReferenceException
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.events.WebDriverListener

private val logger = KotlinLogging.logger {}

private const val MAX_PREVIEW_CHARS = 20
private const val ELLIPSIS = "…"

/**
 * Decorator that logs element discovery and basic interactions.
 *
 * The logger is intentionally lightweight and uses trace/debug levels by default so
 * it can be enabled in troubleshooting scenarios without cluttering normal output.
 *
 * Returned elements are re-wrapped so child lookups also receive the decorator (preserves chaining).
 *
 * Logging points:
 * - findElement(s) on any SearchContext (driver or element)
 * - before click and sendKeys via WebDriverListener (if context is a WebDriver)
 */
public class LoggerDecorator :
    AbstractDecorator(),
    InteractionAware {
    override fun decorateSearchContext(context: SearchContext): SearchContext {
        return object : SearchContext by context {
            override fun findElement(by: By): WebElement {
                logFind("findElement", by)
                val found = context.findElement(by)
                logFound(found)
                return decorateElement(found)
            }

            override fun findElements(by: By): WebElements {
                logFind("findElements", by)
                val elements = context.findElements(by)
                logger.trace { "found ${elements.size} element(s) for locator { $by }" }
                return elements.map { decorateElement(it) }
            }
        }
    }

    override fun decorateElement(element: WebElement): WebElement {
        return object : WebElement by element {
            override fun findElement(by: By): WebElement {
                logFind("findElement", by)
                val found = element.findElement(by)
                logFound(found)
                return decorateElement(found)
            }

            override fun findElements(by: By): WebElements {
                logFind("findElements", by)
                val elements = element.findElements(by)
                logger.trace { "found ${elements.size} element(s) for locator { $by }" }
                return elements.map { decorateElement(it) }
            }
        }
    }

    override fun interactionListener(): WebDriverListener = InteractionLogger()

    private fun logFind(
        operation: String,
        by: By,
    ) {
        logger.trace { "$operation with locator { $by }" }
    }

    private fun logFound(element: WebElement) {
        runCatching { element.tagName }
            .onSuccess { tag ->
                logger.trace { "found element <$tag>" }
            }.onFailure { e ->
                when (e) {
                    is StaleElementReferenceException ->
                        logger.debug { "stale element reference while reading element’s HTML tag; element reference is no longer valid" }
                    else ->
                        logger.trace { "found element" }
                }
            }
    }

    internal inner class InteractionLogger : WebDriverListener {
        override fun beforeClick(element: WebElement) {
            logger.debug { "beforeClick on <${safeTag(element)}>" }
        }

        override fun beforeSendKeys(
            element: WebElement,
            vararg keysToSend: CharSequence,
        ) {
            val preview =
                keysToSend
                    .joinToString(separator = "")
                    .let { if (it.length > MAX_PREVIEW_CHARS) it.take(MAX_PREVIEW_CHARS) + ELLIPSIS else it }
            logger.debug { "beforeSendKeys '$preview' on <${safeTag(element)}>" }
        }
    }

    private fun safeTag(element: WebElement): String = runCatching { element.tagName }.getOrDefault("?")
}

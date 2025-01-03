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

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import java.lang.Thread.sleep
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

/**
 * Decorator that adds configurable delays to Selenium WebDriver operations.
 * Useful for debugging, demonstrations, or slowing down test execution for visualization purposes.
 */
public object SlowMotionDecorator : AbstractDecorator() {
    private val waitDuration = ThreadLocal.withInitial { 1.seconds }

    /**
     * Configures the wait duration for the decorator.
     *
     * @param wait The duration to wait after each decorated operation.
     * @return The decorator instance for method chaining.
     */
    public fun configure(wait: Duration): SlowMotionDecorator {
        waitDuration.set(wait)
        return this
    }

    override fun decorateDriver(driver: WebDriver): WebDriver {
        return object : WebDriver by driver {
            override fun findElement(by: By): WebElement {
                val element = driver.findElement(by)
                addDelay()
                return decorateElement(element)
            }

            override fun findElements(by: By): List<WebElement> {
                val elements = driver.findElements(by)
                addDelay()
                return elements.map { element ->
                    decorateElement(element)
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

            override fun findElements(by: By): List<WebElement> {
                val elements = element.findElements(by)
                addDelay()
                return elements.map { foundElement ->
                    decorateElement(foundElement)
                }
            }
        }
    }

    private fun addDelay() {
        try {
            sleep(waitDuration.get().toJavaDuration())
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }
}

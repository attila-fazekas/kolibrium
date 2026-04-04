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

package dev.kolibrium.webdriver.descriptors

import dev.kolibrium.webdriver.WaitConfig
import dev.kolibrium.webdriver.isNotEmptyAndDisplayed
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test
import org.openqa.selenium.By
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.TimeoutException
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class MultiElementsDescriptorIntegrationTest : AbstractElementDescriptorIntegrationTest() {
    override fun html() =
        """
        <!DOCTYPE html><html><body>
            <ul id="list">
                <li class="item">A</li>
                <li class="item">B</li>
                <li class="item">C</li>
            </ul>
            <div id="lazy" style="display:none">Late</div>
        </body></html>
        """.trimIndent()

    private val fastWait = WaitConfig(pollingInterval = 50.milliseconds, timeout = 200.milliseconds)
    private val patientWait = WaitConfig(pollingInterval = 100.milliseconds, timeout = 2.seconds)

    @Test
    fun `get() returns all matching elements`() {
        val descriptor =
            MultiElementsDescriptor(
                searchCtx = driver,
                value = ".item",
                locatorStrategy = By::cssSelector,
                waitConfig = patientWait,
                readyWhen = { isNotEmptyAndDisplayed },
            )
        descriptor.get() shouldHaveSize 3
    }

    @Test
    fun `get() returns empty list without throwing when readyWhen allows it`() {
        val descriptor =
            MultiElementsDescriptor(
                searchCtx = driver,
                value = ".nonexistent",
                locatorStrategy = By::cssSelector,
                waitConfig = fastWait,
                readyWhen = { true }, // accepts empty list
            )
        descriptor.get() shouldHaveSize 0
    }

    @Test
    fun `get() throws TimeoutException when readyWhen is never satisfied`() {
        val descriptor =
            MultiElementsDescriptor(
                searchCtx = driver,
                value = ".nonexistent",
                locatorStrategy = By::cssSelector,
                waitConfig = fastWait,
                readyWhen = { isNotEmptyAndDisplayed }, // requires at least one element
            )
        shouldThrow<TimeoutException> { descriptor.get() }
    }

    @Test
    fun `reflects DOM changes between calls (no caching)`() {
        val descriptor =
            MultiElementsDescriptor(
                searchCtx = driver,
                value = ".item",
                locatorStrategy = By::cssSelector,
                waitConfig = patientWait,
                readyWhen = { isNotEmptyAndDisplayed },
            )
        descriptor.get() shouldHaveSize 3

        (driver as JavascriptExecutor).executeScript(
            "document.querySelector('.item').remove();",
        )

        descriptor.get() shouldHaveSize 2
    }

    @Test
    fun `waits for lazily-added elements when readyWhen requires non-empty`() {
        val descriptor =
            MultiElementsDescriptor(
                searchCtx = driver,
                value = ".dynamic",
                locatorStrategy = By::cssSelector,
                waitConfig = patientWait,
                readyWhen = { isNotEmptyAndDisplayed },
            )

        (driver as JavascriptExecutor).executeScript(
            """
            setTimeout(function() {
                var el = document.createElement('div');
                el.className = 'dynamic';
                el.textContent = 'appeared';
                document.body.appendChild(el);
            }, 400);
            """,
        )

        descriptor.get() shouldHaveSize 1
    }

    @Test
    fun `toString contains locator and wait config info`() {
        val descriptor =
            MultiElementsDescriptor(
                searchCtx = driver,
                value = ".item",
                locatorStrategy = By::cssSelector,
                waitConfig = patientWait,
                readyWhen = { isNotEmptyAndDisplayed },
            )
        val str = descriptor.toString()
        str shouldContain ".item"
        str shouldContain "2s"
    }
}

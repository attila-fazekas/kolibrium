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
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test
import org.openqa.selenium.By
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.TimeoutException
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class SingleElementDescriptorIntegrationTest : AbstractElementDescriptorIntegrationTest() {
    override fun html() =
        """
        <!DOCTYPE html><html><body>
            <h1 id="title">Hello</h1>
            <button id="btn" disabled>Click me</button>
            <div id="hidden" style="display:none">Secret</div>
        </body></html>
        """.trimIndent()

    private val fastWait = WaitConfig(pollingInterval = 50.milliseconds, timeout = 200.milliseconds)
    private val patientWait = WaitConfig(pollingInterval = 100.milliseconds, timeout = 2.seconds)

    @Test
    fun `get() resolves element by id`() {
        val descriptor =
            SingleElementDescriptor(
                searchCtx = driver,
                value = "title",
                locatorStrategy = By::id,
                cacheLookup = false,
                waitConfig = patientWait,
                readyWhen = { isDisplayed },
            )
        descriptor.get().text shouldBe "Hello"
    }

    @Test
    fun `cacheLookup = true returns same instance on repeated access`() {
        val descriptor =
            SingleElementDescriptor(
                searchCtx = driver,
                value = "title",
                locatorStrategy = By::id,
                cacheLookup = true,
                waitConfig = patientWait,
                readyWhen = { isDisplayed },
            )
        val first = descriptor.get()
        val second = descriptor.get()
        // Same underlying DOM element — both respond identically
        first.text shouldBe second.text
        // Verify it really is the cached reference
        first shouldBe second
    }

    @Test
    fun `cacheLookup = false re-queries on every call`() {
        val descriptor =
            SingleElementDescriptor(
                searchCtx = driver,
                value = "title",
                locatorStrategy = By::id,
                cacheLookup = false,
                waitConfig = patientWait,
                readyWhen = { isDisplayed },
            )
        val first = descriptor.get()
        val second = descriptor.get()
        first.text shouldBe second.text
    }

    @Test
    fun `stale element is retried and resolved when cacheLookup = false`() {
        val descriptor =
            SingleElementDescriptor(
                searchCtx = driver,
                value = "title",
                locatorStrategy = By::id,
                cacheLookup = false,
                waitConfig = patientWait,
                readyWhen = { isDisplayed },
            )
        // Warm up once
        descriptor.get().text shouldBe "Hello"

        // Replace DOM node to make the old reference stale
        (driver as JavascriptExecutor).executeScript(
            """
            var old = document.getElementById('title');
            var fresh = document.createElement('h1');
            fresh.id = 'title';
            fresh.textContent = 'Updated';
            old.parentNode.replaceChild(fresh, old);
            """,
        )

        descriptor.get().text shouldBe "Updated"
    }

    @Test
    fun `readyWhen blocks until condition is met`() {
        val descriptor =
            SingleElementDescriptor(
                searchCtx = driver,
                value = "btn",
                locatorStrategy = By::id,
                cacheLookup = false,
                waitConfig = patientWait,
                readyWhen = { isEnabled },
            )

        (driver as JavascriptExecutor).executeScript(
            "setTimeout(() => document.getElementById('btn').disabled = false, 400);",
        )

        descriptor.get().isEnabled shouldBe true
    }

    @Test
    fun `get() throws TimeoutException when element is never ready`() {
        val descriptor =
            SingleElementDescriptor(
                searchCtx = driver,
                value = "btn",
                locatorStrategy = By::id,
                cacheLookup = false,
                waitConfig = fastWait, // short window — btn stays disabled
                readyWhen = { isEnabled },
            )

        shouldThrow<TimeoutException> { descriptor.get() }
    }

    @Test
    fun `get() throws TimeoutException for non-existent element`() {
        val descriptor =
            SingleElementDescriptor(
                searchCtx = driver,
                value = "ghost",
                locatorStrategy = By::id,
                cacheLookup = false,
                waitConfig = fastWait,
                readyWhen = { isDisplayed },
            )

        shouldThrow<TimeoutException> { descriptor.get() }
    }

    @Test
    fun `toString contains locator value, cacheLookup flag and wait config`() {
        val descriptor =
            SingleElementDescriptor(
                searchCtx = driver,
                value = "title",
                locatorStrategy = By::id,
                cacheLookup = true,
                waitConfig = patientWait,
                readyWhen = { isDisplayed },
            )
        val str = descriptor.toString()
        str shouldContain "title"
        str shouldContain "cacheLookup=true"
        str shouldContain "2s" // timeout from patientWait
    }

    @Test
    fun `by property reflects the correct Selenium By`() {
        val descriptor =
            SingleElementDescriptor(
                searchCtx = driver,
                value = "title",
                locatorStrategy = By::id,
                cacheLookup = false,
                waitConfig = patientWait,
                readyWhen = { isDisplayed },
            )
        descriptor.by shouldBe By.id("title")
    }
}

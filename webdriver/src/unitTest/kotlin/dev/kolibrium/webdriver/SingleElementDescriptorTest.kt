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

package dev.kolibrium.webdriver

import dev.kolibrium.webdriver.WaitConfig
import dev.kolibrium.webdriver.descriptors.SingleElementDescriptor
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.openqa.selenium.By
import org.openqa.selenium.SearchContext
import org.openqa.selenium.WebElement
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class SingleElementDescriptorTest {
    private val fastWait = WaitConfig(pollingInterval = 10.milliseconds, timeout = 100.milliseconds)

    private fun descriptor(
        value: String = "my-id",
        cacheLookup: Boolean = false,
        waitConfig: WaitConfig = fastWait,
        readyWhen: WebElement.() -> Boolean = { true },
        searchCtx: SearchContext = mockk(),
    ) = SingleElementDescriptor(
        searchCtx = searchCtx,
        value = value,
        locatorStrategy = By::id,
        cacheLookup = cacheLookup,
        waitConfig = waitConfig,
        readyWhen = readyWhen,
    )

    @Test
    fun `should throw when value is blank`() {
        val ex =
            shouldThrow<IllegalArgumentException> {
                descriptor(value = "   ")
            }
        ex.message shouldContain "'value' must not be blank"
    }

    @Test
    fun `toString contains locator and cache info`() {
        val d = descriptor(value = "submit", cacheLookup = true)
        val str = d.toString()
        str shouldContain "cacheLookup=true"
        str shouldContain "submit"
    }

    @Test
    fun `caches element on second access when cacheLookup is true`() {
        val element = mockk<WebElement> { every { isDisplayed } returns true }
        val ctx = mockk<SearchContext> { every { findElement(any()) } returns element }

        val d = descriptor(cacheLookup = true, searchCtx = ctx)
        d.get()
        d.get()

        // SearchContext should only be called once due to caching
        verify(exactly = 1) { ctx.findElement(any()) }
    }

    @Test
    fun `does not cache element when cacheLookup is false`() {
        val element = mockk<WebElement> { every { isDisplayed } returns true }
        val ctx = mockk<SearchContext> { every { findElement(any()) } returns element }

        val d = descriptor(cacheLookup = false, searchCtx = ctx)
        d.get()
        d.get()

        verify(exactly = 2) { ctx.findElement(any()) }
    }

    @Test
    fun `get() returns element when readyWhen is satisfied`() {
        val element = mockk<WebElement>()
        val ctx = mockk<SearchContext> { every { findElement(any()) } returns element }

        val d = descriptor(searchCtx = ctx, readyWhen = { true })
        d.get() shouldBe element
    }

    @Test
    fun `by property reflects the locator strategy applied to value`() {
        val d = descriptor(value = "login-btn")
        d.by shouldBe By.id("login-btn")
    }
}

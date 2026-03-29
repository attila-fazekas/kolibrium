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

package dev.kolibrium.selenium.core.descriptors

import dev.kolibrium.selenium.core.SessionContext
import dev.kolibrium.webdriver.WaitConfig
import dev.kolibrium.webdriver.isDisplayed
import dev.kolibrium.webdriver.isEnabled
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.openqa.selenium.By
import org.openqa.selenium.NoSuchElementException
import org.openqa.selenium.SearchContext
import org.openqa.selenium.StaleElementReferenceException
import org.openqa.selenium.TimeoutException
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.pagefactory.ByAll
import org.openqa.selenium.support.pagefactory.ByChained
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class CompositeElementDescriptorTest {
    private lateinit var mockSearchContext: SearchContext
    private lateinit var mockElement: WebElement

    @BeforeEach
    fun setup() {
        mockSearchContext = mockk()
        mockElement = mockk(relaxed = true)
        SessionContext.clear()
    }

    @AfterEach
    fun tearDown() {
        SessionContext.clear()
        clearAllMocks()
    }

    @Test
    fun `should find element with simple By`() {
        val by = By.id("test-id")
        val descriptor =
            DecoratedCompositeElementDescriptor(
                searchCtx = mockSearchContext,
                by = by,
                cacheLookup = true,
                waitConfig = WaitConfig.Quick,
                readyWhen = { isDisplayed },
                siteLevelDecorators = emptyList(),
            )

        every { mockSearchContext.findElement(by) } returns mockElement
        every { mockElement.isDisplayed } returns true

        val result = descriptor.get()

        result shouldBe mockElement
    }

    @Test
    fun `should find element with ByChained`() {
        val by = ByChained(By.id("container"), By.className("title"))
        val descriptor =
            DecoratedCompositeElementDescriptor(
                searchCtx = mockSearchContext,
                by = by,
                cacheLookup = false,
                waitConfig = WaitConfig.Quick,
                readyWhen = { isDisplayed },
                siteLevelDecorators = emptyList(),
            )

        every { mockSearchContext.findElement(by) } returns mockElement
        every { mockElement.isDisplayed } returns true

        val result = descriptor.get()

        result shouldBe mockElement
    }

    @Test
    fun `should find element with ByAll`() {
        val by = ByAll(By.id("primary"), By.cssSelector(".fallback"))
        val descriptor =
            DecoratedCompositeElementDescriptor(
                searchCtx = mockSearchContext,
                by = by,
                cacheLookup = false,
                waitConfig = WaitConfig.Quick,
                readyWhen = { isDisplayed },
                siteLevelDecorators = emptyList(),
            )

        every { mockSearchContext.findElement(by) } returns mockElement
        every { mockElement.isDisplayed } returns true

        val result = descriptor.get()

        result shouldBe mockElement
    }

    @Test
    fun `should cache element when cacheLookup is true`() {
        val by = By.id("cached")
        val descriptor =
            DecoratedCompositeElementDescriptor(
                searchCtx = mockSearchContext,
                by = by,
                cacheLookup = true,
                waitConfig = WaitConfig.Quick,
                readyWhen = { isDisplayed },
                siteLevelDecorators = emptyList(),
            )

        every { mockSearchContext.findElement(by) } returns mockElement
        every { mockElement.isDisplayed } returns true

        val first = descriptor.get()
        val second = descriptor.get()

        first shouldBe mockElement
        second shouldBe mockElement
        verify(exactly = 1) { mockSearchContext.findElement(by) }
    }

    @Test
    fun `should not cache element when cacheLookup is false`() {
        val by = By.id("uncached")
        val descriptor =
            DecoratedCompositeElementDescriptor(
                searchCtx = mockSearchContext,
                by = by,
                cacheLookup = false,
                waitConfig = WaitConfig.Quick,
                readyWhen = { isDisplayed },
                siteLevelDecorators = emptyList(),
            )

        every { mockSearchContext.findElement(by) } returns mockElement
        every { mockElement.isDisplayed } returns true

        val first = descriptor.get()
        val second = descriptor.get()

        first shouldBe mockElement
        second shouldBe mockElement
        verify(exactly = 2) { mockSearchContext.findElement(by) }
    }

    @Test
    fun `should wait until element is ready`() {
        val by = By.cssSelector(".loading")
        val descriptor =
            DecoratedCompositeElementDescriptor(
                searchCtx = mockSearchContext,
                by = by,
                cacheLookup = false,
                waitConfig =
                    WaitConfig(
                        timeout = 2.seconds,
                        pollingInterval = 100.milliseconds,
                    ),
                readyWhen = { isDisplayed },
                siteLevelDecorators = emptyList(),
            )

        every { mockSearchContext.findElement(by) } returns mockElement
        every { mockElement.isDisplayed } returnsMany listOf(false, false, true)

        val result = descriptor.get()

        result shouldBe mockElement
        verify(atLeast = 2) { mockElement.isDisplayed }
    }

    @Test
    fun `should use custom readyWhen condition`() {
        val by = By.id("button")
        val descriptor =
            DecoratedCompositeElementDescriptor(
                searchCtx = mockSearchContext,
                by = by,
                cacheLookup = false,
                waitConfig = WaitConfig.Quick,
                readyWhen = { isDisplayed && isEnabled },
                siteLevelDecorators = emptyList(),
            )

        every { mockSearchContext.findElement(by) } returns mockElement
        every { mockElement.isDisplayed } returnsMany listOf(true, true, true)
        every { mockElement.isEnabled } returnsMany listOf(false, false, true)

        val result = descriptor.get()

        result shouldBe mockElement
        verify(atLeast = 2) { mockElement.isEnabled }
    }

    @Test
    fun `should clear cache and retry on StaleElementReferenceException`() {
        val freshElement = mockk<WebElement>(relaxed = true)
        val by = By.id("stale")
        val descriptor =
            DecoratedCompositeElementDescriptor(
                searchCtx = mockSearchContext,
                by = by,
                cacheLookup = true,
                waitConfig = WaitConfig.Quick,
                readyWhen = { isDisplayed },
                siteLevelDecorators = emptyList(),
            )

        every { mockSearchContext.findElement(by) } returnsMany listOf(mockElement, freshElement)
        every { mockElement.isDisplayed } throws StaleElementReferenceException("stale")
        every { freshElement.isDisplayed } returns true

        val result = descriptor.get()

        result shouldBe freshElement
        verify(exactly = 2) { mockSearchContext.findElement(by) }
    }

    @Test
    fun `should throw exception when element not found within timeout`() {
        val by = By.id("missing")
        val descriptor =
            DecoratedCompositeElementDescriptor(
                searchCtx = mockSearchContext,
                by = by,
                cacheLookup = false,
                waitConfig =
                    WaitConfig(
                        timeout = 500.milliseconds,
                        pollingInterval = 100.milliseconds,
                        message = "Element not found",
                    ),
                readyWhen = { isDisplayed },
                siteLevelDecorators = emptyList(),
            )

        every { mockSearchContext.findElement(by) } throws NoSuchElementException("not found")

        shouldThrow<TimeoutException> {
            descriptor.get()
        }
    }

    @Test
    fun `should always ignore NoSuchElementException in wait config`() {
        val by = By.id("eventual")
        val descriptor =
            DecoratedCompositeElementDescriptor(
                searchCtx = mockSearchContext,
                by = by,
                cacheLookup = false,
                waitConfig =
                    WaitConfig(
                        timeout = 1.seconds,
                        pollingInterval = 100.milliseconds,
                        ignoring = emptySet(),
                    ),
                readyWhen = { isDisplayed },
                siteLevelDecorators = emptyList(),
            )

        var callCount = 0
        every { mockSearchContext.findElement(by) } answers {
            callCount++
            if (callCount < 3) throw NoSuchElementException("not yet")
            mockElement
        }
        every { mockElement.isDisplayed } returns true

        val result = descriptor.get()

        result shouldBe mockElement
        callCount shouldBe 3
    }

    @Test
    fun `toString should include descriptor name, by, cache, and wait info`() {
        val by = ByChained(By.id("container"), By.className("title"))
        val descriptor =
            DecoratedCompositeElementDescriptor(
                searchCtx = mockSearchContext,
                by = by,
                cacheLookup = true,
                waitConfig =
                    WaitConfig(
                        timeout = 5.seconds,
                        pollingInterval = 250.milliseconds,
                    ),
                readyWhen = { isDisplayed },
                siteLevelDecorators = emptyList(),
            )

        val result = descriptor.toString()

        result shouldContain "CompositeElementDescriptor"
        result shouldContain "cacheLookup=true"
        result shouldContain "timeout=5s"
        result shouldContain "polling=250ms"
    }

    @Test
    fun `toString without cache should include cacheLookup false`() {
        val by = By.id("test")
        val descriptor =
            DecoratedCompositeElementDescriptor(
                searchCtx = mockSearchContext,
                by = by,
                cacheLookup = false,
                waitConfig = WaitConfig.Quick,
                readyWhen = { isDisplayed },
                siteLevelDecorators = emptyList(),
            )

        val result = descriptor.toString()

        result shouldContain "cacheLookup=false"
    }

    @Test
    fun `property delegate should work correctly`() {
        val by = By.id("delegated")

        every { mockSearchContext.findElement(by) } returns mockElement
        every { mockElement.isDisplayed } returns true

        val descriptor =
            DecoratedCompositeElementDescriptor(
                searchCtx = mockSearchContext,
                by = by,
                cacheLookup = true,
                waitConfig = WaitConfig.Quick,
                readyWhen = { isDisplayed },
                siteLevelDecorators = emptyList(),
            )

        val element by descriptor

        element shouldBe mockElement
    }
}

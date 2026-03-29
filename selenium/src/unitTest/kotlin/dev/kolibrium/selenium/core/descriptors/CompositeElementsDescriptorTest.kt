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
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
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

class CompositeElementsDescriptorTest {
    private lateinit var mockSearchContext: SearchContext
    private lateinit var mockElement1: WebElement
    private lateinit var mockElement2: WebElement
    private lateinit var mockElement3: WebElement

    @BeforeEach
    fun setup() {
        mockSearchContext = mockk()
        mockElement1 = mockk(relaxed = true)
        mockElement2 = mockk(relaxed = true)
        mockElement3 = mockk(relaxed = true)
        SessionContext.clear()
    }

    @AfterEach
    fun tearDown() {
        SessionContext.clear()
        clearAllMocks()
    }

    @Test
    fun `should find elements with simple By`() {
        val by = By.className("item")
        val descriptor =
            DecoratedCompositeElementsDescriptor(
                searchCtx = mockSearchContext,
                by = by,
                waitConfig = WaitConfig.Quick,
                readyWhen = { isNotEmpty() && all { it.isDisplayed } },
                siteLevelDecorators = emptyList(),
            )

        every { mockSearchContext.findElements(by) } returns listOf(mockElement1, mockElement2)
        every { mockElement1.isDisplayed } returns true
        every { mockElement2.isDisplayed } returns true

        val result = descriptor.get()

        result shouldHaveSize 2
        result[0] shouldBe mockElement1
        result[1] shouldBe mockElement2
    }

    @Test
    fun `should find elements with ByChained`() {
        val by = ByChained(By.id("list"), By.className("item"))
        val descriptor =
            DecoratedCompositeElementsDescriptor(
                searchCtx = mockSearchContext,
                by = by,
                waitConfig = WaitConfig.Quick,
                readyWhen = { isNotEmpty() && all { it.isDisplayed } },
                siteLevelDecorators = emptyList(),
            )

        every { mockSearchContext.findElements(by) } returns listOf(mockElement1, mockElement2, mockElement3)
        every { mockElement1.isDisplayed } returns true
        every { mockElement2.isDisplayed } returns true
        every { mockElement3.isDisplayed } returns true

        val result = descriptor.get()

        result shouldHaveSize 3
    }

    @Test
    fun `should find elements with ByAll`() {
        val by = ByAll(By.className("primary"), By.className("secondary"))
        val descriptor =
            DecoratedCompositeElementsDescriptor(
                searchCtx = mockSearchContext,
                by = by,
                waitConfig = WaitConfig.Quick,
                readyWhen = { isNotEmpty() && all { it.isDisplayed } },
                siteLevelDecorators = emptyList(),
            )

        every { mockSearchContext.findElements(by) } returns listOf(mockElement1)
        every { mockElement1.isDisplayed } returns true

        val result = descriptor.get()

        result shouldHaveSize 1
    }

    @Test
    fun `should not cache elements between calls`() {
        val by = By.id("dynamic")
        val descriptor =
            DecoratedCompositeElementsDescriptor(
                searchCtx = mockSearchContext,
                by = by,
                waitConfig = WaitConfig.Quick,
                readyWhen = { isNotEmpty() && all { it.isDisplayed } },
                siteLevelDecorators = emptyList(),
            )

        every { mockSearchContext.findElements(by) } returns listOf(mockElement1, mockElement2)
        every { mockElement1.isDisplayed } returns true
        every { mockElement2.isDisplayed } returns true

        descriptor.get()
        descriptor.get()

        verify(exactly = 2) { mockSearchContext.findElements(by) }
    }

    @Test
    fun `should wait until all elements are displayed`() {
        val by = By.className("loading")
        val descriptor =
            DecoratedCompositeElementsDescriptor(
                searchCtx = mockSearchContext,
                by = by,
                waitConfig =
                    WaitConfig(
                        timeout = 2.seconds,
                        pollingInterval = 100.milliseconds,
                    ),
                readyWhen = { isNotEmpty() && all { it.isDisplayed } },
                siteLevelDecorators = emptyList(),
            )

        every { mockSearchContext.findElements(by) } returnsMany
            listOf(
                emptyList(),
                emptyList(),
                listOf(mockElement1, mockElement2),
            )
        every { mockElement1.isDisplayed } returns true
        every { mockElement2.isDisplayed } returns true

        val result = descriptor.get()

        result shouldHaveSize 2
        verify(atLeast = 3) { mockSearchContext.findElements(by) }
    }

    @Test
    fun `should use custom readyWhen condition`() {
        val by = By.className("btn")
        val descriptor =
            DecoratedCompositeElementsDescriptor(
                searchCtx = mockSearchContext,
                by = by,
                waitConfig = WaitConfig.Quick,
                readyWhen = { size >= 2 && all { it.isEnabled } },
                siteLevelDecorators = emptyList(),
            )

        every { mockSearchContext.findElements(by) } returnsMany
            listOf(
                listOf(mockElement1),
                listOf(mockElement1, mockElement2),
            )
        every { mockElement1.isEnabled } returns true
        every { mockElement2.isEnabled } returns true

        val result = descriptor.get()

        result shouldHaveSize 2
    }

    @Test
    fun `should retry on StaleElementReferenceException`() {
        val by = By.className("stale")
        val descriptor =
            DecoratedCompositeElementsDescriptor(
                searchCtx = mockSearchContext,
                by = by,
                waitConfig = WaitConfig.Quick,
                readyWhen = { isNotEmpty() && all { it.isDisplayed } },
                siteLevelDecorators = emptyList(),
            )

        var callCount = 0
        every { mockSearchContext.findElements(by) } answers {
            callCount++
            if (callCount == 1) throw StaleElementReferenceException("stale")
            listOf(mockElement1)
        }
        every { mockElement1.isDisplayed } returns true

        val result = descriptor.get()

        result shouldHaveSize 1
        callCount shouldBe 2
    }

    @Test
    fun `should throw exception when elements not ready within timeout`() {
        val by = By.id("missing")
        val descriptor =
            DecoratedCompositeElementsDescriptor(
                searchCtx = mockSearchContext,
                by = by,
                waitConfig =
                    WaitConfig(
                        timeout = 500.milliseconds,
                        pollingInterval = 100.milliseconds,
                        message = "Elements not found",
                    ),
                readyWhen = { isNotEmpty() && all { it.isDisplayed } },
                siteLevelDecorators = emptyList(),
            )

        every { mockSearchContext.findElements(by) } returns emptyList()

        shouldThrow<TimeoutException> {
            descriptor.get()
        }
    }

    @Test
    fun `should always ignore NoSuchElementException in wait config`() {
        val by = By.id("eventual")
        val descriptor =
            DecoratedCompositeElementsDescriptor(
                searchCtx = mockSearchContext,
                by = by,
                waitConfig =
                    WaitConfig(
                        timeout = 1.seconds,
                        pollingInterval = 100.milliseconds,
                        ignoring = emptySet(),
                    ),
                readyWhen = { isNotEmpty() && all { it.isDisplayed } },
                siteLevelDecorators = emptyList(),
            )

        var callCount = 0
        every { mockSearchContext.findElements(by) } answers {
            callCount++
            if (callCount < 3) throw NoSuchElementException("not yet")
            listOf(mockElement1)
        }
        every { mockElement1.isDisplayed } returns true

        val result = descriptor.get()

        result shouldHaveSize 1
        callCount shouldBe 3
    }

    @Test
    fun `toString should include descriptor name, by, and wait info`() {
        val by = ByChained(By.id("container"), By.className("item"))
        val descriptor =
            DecoratedCompositeElementsDescriptor(
                searchCtx = mockSearchContext,
                by = by,
                waitConfig =
                    WaitConfig(
                        timeout = 5.seconds,
                        pollingInterval = 250.milliseconds,
                    ),
                readyWhen = { isNotEmpty() && all { it.isDisplayed } },
                siteLevelDecorators = emptyList(),
            )

        val result = descriptor.toString()

        result shouldContain "CompositeElementsDescriptor"
        result shouldContain "timeout=5s"
        result shouldContain "polling=250ms"
        result shouldNotContain "cacheLookup"
    }

    @Test
    fun `property delegate should work correctly`() {
        val by = By.className("delegated")

        every { mockSearchContext.findElements(by) } returns listOf(mockElement1, mockElement2)
        every { mockElement1.isDisplayed } returns true
        every { mockElement2.isDisplayed } returns true

        val descriptor =
            DecoratedCompositeElementsDescriptor(
                searchCtx = mockSearchContext,
                by = by,
                waitConfig = WaitConfig.Quick,
                readyWhen = { isNotEmpty() && all { it.isDisplayed } },
                siteLevelDecorators = emptyList(),
            )

        val elements by descriptor

        elements shouldHaveSize 2
    }
}

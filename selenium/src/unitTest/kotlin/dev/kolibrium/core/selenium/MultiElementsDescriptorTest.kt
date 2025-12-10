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

package dev.kolibrium.core.selenium

import dev.kolibrium.core.Page
import dev.kolibrium.core.Session
import dev.kolibrium.core.SessionContext
import dev.kolibrium.core.Site
import dev.kolibrium.core.WaitConfig
import dev.kolibrium.core.WebElements
import dev.kolibrium.core.classNames
import dev.kolibrium.core.cssSelectors
import dev.kolibrium.core.dataQas
import dev.kolibrium.core.dataTestIds
import dev.kolibrium.core.dataTests
import dev.kolibrium.core.decorators.DecoratorManager.withDecorators
import dev.kolibrium.core.decorators.LoggerDecorator
import dev.kolibrium.core.decorators.SlowMotionDecorator
import dev.kolibrium.core.isClickable
import dev.kolibrium.core.isDisplayed
import dev.kolibrium.core.linkTexts
import dev.kolibrium.core.names
import dev.kolibrium.core.partialLinkTexts
import dev.kolibrium.core.tagNames
import dev.kolibrium.core.xpaths
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.string.shouldContain
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.openqa.selenium.By
import org.openqa.selenium.SearchContext
import org.openqa.selenium.StaleElementReferenceException
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class MultiElementsDescriptorTest {
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
        // If you have DecoratorManager.clear(), call it here
    }

    @AfterEach
    fun tearDown() {
        SessionContext.clear()
        clearAllMocks()
    }

    @Test
    fun `should return all matching elements`() {
        // Given
        val descriptor =
            mockSearchContext.classNames(
                value = "item",
                waitConfig = WaitConfig.Quick,
            )

        val elements = listOf(mockElement1, mockElement2, mockElement3)
        every { mockSearchContext.findElements(By.className("item")) } returns elements
        every { mockElement1.isDisplayed } returns true
        every { mockElement2.isDisplayed } returns true
        every { mockElement3.isDisplayed } returns true

        // When
        val result = descriptor.get()

        // Then
        result shouldHaveSize 3
        result shouldContainExactly elements
    }

    @Test
    fun `should not cache elements between calls`() {
        // Given
        val descriptor =
            mockSearchContext.cssSelectors(
                value = ".dynamic",
                waitConfig = WaitConfig.Quick,
            )

        val firstCallElements = listOf(mockElement1, mockElement2)
        val secondCallElements = listOf(mockElement1, mockElement2, mockElement3)

        every { mockSearchContext.findElements(By.cssSelector(".dynamic")) } returnsMany
            listOf(
                firstCallElements,
                secondCallElements,
            )
        every { mockElement1.isDisplayed } returns true
        every { mockElement2.isDisplayed } returns true
        every { mockElement3.isDisplayed } returns true

        // When
        val first = descriptor.get()
        val second = descriptor.get()

        // Then
        first shouldHaveSize 2
        second shouldHaveSize 3
        verify(exactly = 2) { mockSearchContext.findElements(By.cssSelector(".dynamic")) }
    }

    @Test
    fun `should wait until all elements are displayed`() {
        // Given
        val descriptor =
            mockSearchContext.tagNames(
                value = "li",
                waitConfig =
                    WaitConfig(
                        timeout = 2.seconds,
                        pollingInterval = 100.milliseconds,
                    ),
            )

        val elements = listOf(mockElement1, mockElement2)
        every { mockSearchContext.findElements(By.tagName("li")) } returns elements

        // First two calls: not all displayed
        every { mockElement1.isDisplayed } returnsMany listOf(false, true, true)
        every { mockElement2.isDisplayed } returnsMany listOf(false, false, true)

        // When
        val result = descriptor.get()

        // Then
        result shouldHaveSize 2
        verify(atLeast = 2) { mockElement1.isDisplayed }
        verify(atLeast = 2) { mockElement2.isDisplayed }
        verify(atLeast = 3) { mockSearchContext.findElements(By.tagName("li")) }
    }

    @Test
    fun `should use custom readyWhen condition for all elements`() {
        // Given
        val descriptor =
            mockSearchContext.names(
                value = "checkbox",
                waitConfig = WaitConfig.Quick,
                readyWhen = { all { it.isDisplayed && it.isEnabled } },
            )

        val elements = listOf(mockElement1, mockElement2)
        every { mockSearchContext.findElements(By.name("checkbox")) } returns elements
        every { mockElement1.isDisplayed } returns true
        every { mockElement2.isDisplayed } returns true
        every { mockElement1.isEnabled } returnsMany listOf(false, true)
        every { mockElement2.isEnabled } returnsMany listOf(false, true)

        // When
        val result = descriptor.get()

        // Then
        result shouldHaveSize 2
        verify(atLeast = 1) { mockElement1.isEnabled }
        verify(atLeast = 1) { mockElement2.isEnabled }
    }

    @Test
    fun `should handle empty list when no elements found initially`() {
        // Given
        val descriptor =
            mockSearchContext.xpaths(
                value = "//div[@class='future']",
                waitConfig =
                    WaitConfig(
                        timeout = 1.seconds,
                        pollingInterval = 100.milliseconds,
                    ),
            )

        val elements = listOf(mockElement1)
        every { mockSearchContext.findElements(By.xpath("//div[@class='future']")) } returnsMany
            listOf(
                emptyList(),
                emptyList(),
                elements,
            )
        every { mockElement1.isDisplayed } returns true

        // When
        val result = descriptor.get()

        // Then
        result shouldHaveSize 1
        verify(atLeast = 3) { mockSearchContext.findElements(By.xpath("//div[@class='future']")) }
    }

    @Test
    fun `should retry on StaleElementReferenceException`() {
        // Given
        val freshElements =
            listOf(
                mockk<WebElement>(relaxed = true),
                mockk<WebElement>(relaxed = true),
            )
        val descriptor =
            mockSearchContext.linkTexts(
                value = "Read more",
                waitConfig = WaitConfig.Quick,
            )

        val staleElements = listOf(mockElement1, mockElement2)
        every { mockSearchContext.findElements(By.linkText("Read more")) } returnsMany
            listOf(
                staleElements,
                freshElements,
            )
        every { mockElement1.isDisplayed } throws StaleElementReferenceException("stale")
        every { freshElements[0].isDisplayed } returns true
        every { freshElements[1].isDisplayed } returns true

        // When
        val result = descriptor.get()

        // Then
        result shouldContainExactly freshElements
        verify(exactly = 2) { mockSearchContext.findElements(By.linkText("Read more")) }
    }

    @Test
    fun `should throw exception when elements not ready within timeout`() {
        // Given
        val descriptor =
            mockSearchContext.classNames(
                value = "loading",
                waitConfig =
                    WaitConfig(
                        timeout = 500.milliseconds,
                        pollingInterval = 100.milliseconds,
                        message = "Elements not ready",
                    ),
            )

        every { mockSearchContext.findElements(By.className("loading")) } returns emptyList()

        // When/Then
        shouldThrow<org.openqa.selenium.TimeoutException> {
            descriptor.get()
        }
    }

    @Test
    fun `should always ignore NoSuchElementException in wait config`() {
        // Given - WaitConfig without NoSuchElementException in ignoring set
        val customWaitConfig =
            WaitConfig(
                timeout = 1.seconds,
                pollingInterval = 100.milliseconds,
                ignoring = emptySet(),
            )

        val descriptor =
            mockSearchContext.cssSelectors(
                value = ".delayed",
                waitConfig = customWaitConfig,
            )

        val elements = listOf(mockElement1)
        every { mockSearchContext.findElements(By.cssSelector(".delayed")) } answers {
            throw org.openqa.selenium.NoSuchElementException("not yet")
        } andThenAnswer {
            throw org.openqa.selenium.NoSuchElementException("still not")
        } andThenAnswer {
            elements
        }
        every { mockElement1.isDisplayed } returns true

        // When
        val result = descriptor.get()

        // Then - Should successfully wait and find elements despite NoSuchElementException
        result shouldHaveSize 1
    }

    @Test
    fun `should use default wait config when none provided`() {
        // Given
        val descriptor =
            mockSearchContext.tagNames(
                value = "button",
            )

        val elements = listOf(mockElement1)
        every { mockSearchContext.findElements(By.tagName("button")) } returns elements
        every { mockElement1.isDisplayed } returns true

        // When
        val result = descriptor.get()

        // Then
        result shouldHaveSize 1
        descriptor.toString() shouldContain "timeout=10s"
        descriptor.toString() shouldContain "polling=200ms"
    }

    @Test
    fun `should use site-level wait config when set`() {
        // Given
        val site =
            object : Site("https://example.com") {
                override val waitConfig = WaitConfig.Patient
            }

        val driver = mockk<WebDriver>(relaxed = true)
        SessionContext.withSession(Session(driver, site)) {
            val descriptor = mockSearchContext.names("field")

            val elements = listOf(mockElement1)
            every { mockSearchContext.findElements(By.name("field")) } returns elements
            every { mockElement1.isDisplayed } returns true

            // When
            descriptor.get()

            // Then
            descriptor.toString() shouldContain "timeout=30s"
            descriptor.toString() shouldContain "polling=500ms"
        }
    }

    @Test
    fun `should use custom elements ready condition from site`() {
        // Given
        val site =
            object : Site("https://example.com") {
                override val elementsReadyCondition: WebElements.() -> Boolean = {
                    isNotEmpty() && all { it.isDisplayed && it.isEnabled }
                }
            }

        val driver = mockk<WebDriver>(relaxed = true)
        SessionContext.withSession(Session(driver, site)) {
            val descriptor = mockSearchContext.cssSelectors(".items")

            val elements = listOf(mockElement1, mockElement2)
            every { mockSearchContext.findElements(By.cssSelector(".items")) } returns elements
            every { mockElement1.isDisplayed } returns true
            every { mockElement2.isDisplayed } returns true
            every { mockElement1.isEnabled } returnsMany listOf(false, true)
            every { mockElement2.isEnabled } returns true

            // When
            val result = descriptor.get()

            // Then
            result shouldHaveSize 2
            verify(atLeast = 1) { mockElement1.isEnabled }
        }
    }

    @Test
    fun `should escape CSS attribute values correctly for dataQas`() {
        // Given
        val descriptor =
            mockSearchContext.dataQas(
                value = "test\"value\\with",
                waitConfig = WaitConfig.Quick,
            )

        val elements = listOf(mockElement1)
        every {
            mockSearchContext.findElements(By.cssSelector("[data-qa=\"test\\\"value\\\\with\"]"))
        } returns elements
        every { mockElement1.isDisplayed } returns true

        // When
        val result = descriptor.get()

        // Then
        result shouldHaveSize 1
        descriptor.by.toString() shouldContain "test\\\"value\\\\with"
    }

    @Test
    fun `should throw when value is blank`() {
        // When/Then
        shouldThrow<IllegalArgumentException> {
            mockSearchContext.classNames(value = "   ")
        }.message shouldContain "must not be blank"
    }

    @Test
    fun `toString should include context, by, and wait info`() {
        // Given
        val descriptor =
            mockSearchContext.partialLinkTexts(
                value = "More",
                waitConfig =
                    WaitConfig(
                        timeout = 5.seconds,
                        pollingInterval = 250.milliseconds,
                    ),
            )

        // When
        val result = descriptor.toString()

        // Then
        result shouldContain "ElementsDescriptor"
        result shouldContain "partialLinkText: More"
        result shouldContain "timeout=5s"
        result shouldContain "polling=250ms"
    }

    @Test
    fun `should work with all locator strategies`() {
        // Test multiple locator types
        val locators =
            listOf(
                mockSearchContext.classNames("test-class"),
                mockSearchContext.cssSelectors(".test"),
                mockSearchContext.dataTests("test-data"),
                mockSearchContext.dataTestIds("test-id"),
                mockSearchContext.linkTexts("Click here"),
                mockSearchContext.names("element-name"),
                mockSearchContext.partialLinkTexts("Click"),
                mockSearchContext.tagNames("div"),
                mockSearchContext.xpaths("//div"),
            )

        val elements = listOf(mockElement1)
        // Mock all possible findElements calls
        every { mockSearchContext.findElements(any()) } returns elements
        every { mockElement1.isDisplayed } returns true

        // When/Then - all should work without throwing
        locators.forEach { descriptor ->
            descriptor.get() shouldHaveSize 1
        }
    }

    @Test
    fun `should handle custom readyWhen with size check`() {
        // Given
        val descriptor =
            mockSearchContext.tagNames(
                value = "option",
                waitConfig = WaitConfig.Quick,
                readyWhen = { size >= 3 && isDisplayed },
            )

        val twoElements = listOf(mockElement1, mockElement2)
        val threeElements = listOf(mockElement1, mockElement2, mockElement3)

        every { mockSearchContext.findElements(By.tagName("option")) } returnsMany
            listOf(
                twoElements,
                threeElements,
            )
        every { mockElement1.isDisplayed } returns true
        every { mockElement2.isDisplayed } returns true
        every { mockElement3.isDisplayed } returns true

        // When
        val result = descriptor.get()

        // Then
        result shouldHaveSize 3
        verify(exactly = 2) { mockSearchContext.findElements(By.tagName("option")) }
    }

    @Test
    fun `should handle readyWhen checking isClickable for all elements`() {
        // Given
        val descriptor =
            mockSearchContext.cssSelectors(
                value = "button.action",
                waitConfig = WaitConfig.Quick,
                readyWhen = { isClickable }, // Uses extension property from LocatorUtils
            )

        val elements = listOf(mockElement1, mockElement2)
        every { mockSearchContext.findElements(By.cssSelector("button.action")) } returns elements
        every { mockElement1.isDisplayed } returnsMany listOf(true, true)
        every { mockElement1.isEnabled } returnsMany listOf(false, true)
        every { mockElement2.isDisplayed } returns true
        every { mockElement2.isEnabled } returns true

        // When
        val result = descriptor.get()

        // Then
        result shouldHaveSize 2
        verify(atLeast = 1) { mockElement1.isEnabled }
    }

    @Test
    fun `should handle empty result after timeout with custom message`() {
        // Given
        val descriptor =
            mockSearchContext.xpaths(
                value = "//div[@data-loaded='true']",
                waitConfig =
                    WaitConfig(
                        timeout = 500.milliseconds,
                        pollingInterval = 100.milliseconds,
                        message = "Elements never became ready",
                    ),
            )

        val elements = listOf(mockElement1)
        every { mockSearchContext.findElements(By.xpath("//div[@data-loaded='true']")) } returns elements
        every { mockElement1.isDisplayed } returns false

        // When/Then
        val exception =
            shouldThrow<org.openqa.selenium.TimeoutException> {
                descriptor.get()
            }
        exception.message shouldContain "Elements never became ready"
    }

    @Test
    fun `should handle mix of displayed and hidden elements`() {
        // Given
        val descriptor =
            mockSearchContext.classNames(
                value = "item",
                waitConfig = WaitConfig.Quick,
                readyWhen = { isNotEmpty() }, // Don't require all to be displayed
            )

        val elements = listOf(mockElement1, mockElement2, mockElement3)
        every { mockSearchContext.findElements(By.className("item")) } returns elements
        every { mockElement1.isDisplayed } returns true
        every { mockElement2.isDisplayed } returns false
        every { mockElement3.isDisplayed } returns true

        // When
        val result = descriptor.get()

        // Then
        result shouldHaveSize 3
        result shouldContainExactly elements
    }

    @Test
    fun `property delegate should work correctly`() {
        // Given
        val page =
            object : Page<Site>() {
                val items by mockSearchContext.cssSelectors(".list-item")
            }

        val elements = listOf(mockElement1, mockElement2)
        every { mockSearchContext.findElements(By.cssSelector(".list-item")) } returns elements
        every { mockElement1.isDisplayed } returns true
        every { mockElement2.isDisplayed } returns true

        // When
        val result = page.items

        // Then
        result shouldHaveSize 2
    }

    @Test
    fun `should merge decorators - site first then test with dedup and test winning (multi)`() {
        val site =
            object : Site("https://example.test") {
                override val decorators = listOf(LoggerDecorator())
            }

        val driver = mockk<WebDriver>(relaxed = true)
        SessionContext.withSession(Session(driver, site)) {
            val elements = listOf(mockElement1, mockElement2)
            every { mockSearchContext.findElements(By.className("items")) } returns elements
            every { mockElement1.isDisplayed } returns true
            every { mockElement2.isDisplayed } returns true

            withDecorators(SlowMotionDecorator(wait = 1.seconds), LoggerDecorator()) {
                val descriptor = mockSearchContext.classNames("items", waitConfig = WaitConfig.Quick)
                descriptor.get()
                descriptor.toString() shouldContain "decorators=[SlowMotionDecorator, LoggerDecorator]"
            }
        }
    }
}

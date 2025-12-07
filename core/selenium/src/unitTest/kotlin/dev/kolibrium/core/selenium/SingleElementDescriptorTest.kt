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

import dev.kolibrium.core.selenium.decorators.DecoratorManager.withDecorators
import dev.kolibrium.core.selenium.decorators.LoggerDecorator
import dev.kolibrium.core.selenium.decorators.SlowMotionDecorator
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
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
import org.openqa.selenium.TimeoutException
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class SingleElementDescriptorTest {
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
    fun `should cache element when cacheLookup is true`() {
        // Given
        val descriptor =
            mockSearchContext.id(
                value = "test-id",
                cacheLookup = true,
                waitConfig = WaitConfig.Quick,
            )

        every { mockSearchContext.findElement(By.id("test-id")) } returns mockElement
        every { mockElement.isDisplayed } returns true

        // When
        val first = descriptor.get()
        val second = descriptor.get()

        // Then
        first shouldBe mockElement
        second shouldBe mockElement
        verify(exactly = 1) { mockSearchContext.findElement(By.id("test-id")) }
    }

    @Test
    fun `should not cache element when cacheLookup is false`() {
        // Given
        val descriptor =
            mockSearchContext.id(
                value = "test-id",
                cacheLookup = false,
                waitConfig = WaitConfig.Quick,
            )

        every { mockSearchContext.findElement(By.id("test-id")) } returns mockElement
        every { mockElement.isDisplayed } returns true

        // When
        val first = descriptor.get()
        val second = descriptor.get()

        // Then
        first shouldBe mockElement
        second shouldBe mockElement
        verify(exactly = 2) { mockSearchContext.findElement(By.id("test-id")) }
    }

    @Test
    fun `should wait until element is displayed`() {
        // Given
        val descriptor =
            mockSearchContext.cssSelector(
                value = ".loading",
                cacheLookup = false,
                waitConfig =
                    WaitConfig(
                        timeout = 2.seconds,
                        pollingInterval = 100.milliseconds,
                    ),
            )

        every { mockSearchContext.findElement(By.cssSelector(".loading")) } returns mockElement
        every { mockElement.isDisplayed } returnsMany listOf(false, false, true)

        // When
        val result = descriptor.get()

        // Then
        result shouldBe mockElement
        verify(atLeast = 2) { mockElement.isDisplayed }
        verify(atLeast = 3) { mockSearchContext.findElement(By.cssSelector(".loading")) }
    }

    @Test
    fun `should use custom readyWhen condition`() {
        // Given
        val descriptor =
            mockSearchContext.id(
                value = "button",
                cacheLookup = false,
                waitConfig = WaitConfig.Quick,
                readyWhen = { isDisplayed && isEnabled },
            )

        every { mockSearchContext.findElement(By.id("button")) } returns mockElement
        every { mockElement.isDisplayed } returnsMany listOf(true, true, true)
        every { mockElement.isEnabled } returnsMany listOf(false, false, true)

        // When
        val result = descriptor.get()

        // Then
        result shouldBe mockElement
        verify(atLeast = 2) { mockElement.isEnabled }
    }

    @Test
    fun `should clear cache and retry on StaleElementReferenceException`() {
        // Given
        val freshElement = mockk<WebElement>(relaxed = true)
        val descriptor =
            mockSearchContext.name(
                value = "username",
                cacheLookup = true,
                waitConfig = WaitConfig.Quick,
            )

        every { mockSearchContext.findElement(By.name("username")) } returnsMany
            listOf(
                mockElement,
                freshElement,
            )
        every { mockElement.isDisplayed } throws StaleElementReferenceException("stale")
        every { freshElement.isDisplayed } returns true

        // When
        val result = descriptor.get()

        // Then
        result shouldBe freshElement
        verify(exactly = 2) { mockSearchContext.findElement(By.name("username")) }
    }

    @Test
    fun `should throw exception when element not found within timeout`() {
        // Given
        val descriptor =
            mockSearchContext.id(
                value = "missing",
                cacheLookup = false,
                waitConfig =
                    WaitConfig(
                        timeout = 500.milliseconds,
                        pollingInterval = 100.milliseconds,
                        message = "Element not found",
                    ),
            )

        every { mockSearchContext.findElement(By.id("missing")) } throws org.openqa.selenium.NoSuchElementException("not found")

        // When/Then
        shouldThrow<TimeoutException> {
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
            mockSearchContext.id(
                value = "test",
                waitConfig = customWaitConfig,
            )

        var calls = 0
        every { mockSearchContext.findElement(By.id("test")) } answers {
            calls++
            if (calls == 1) throw org.openqa.selenium.NoSuchElementException("not yet") else mockElement
        }
        every { mockElement.isDisplayed } returns true

        every { mockSearchContext.findElement(By.id("test")) } answers {
            throw org.openqa.selenium.NoSuchElementException("not yet")
        } andThenAnswer { mockElement }

        // When
        val result = descriptor.get()

        // Then - Should successfully wait and find element despite NoSuchElementException
        result shouldBe mockElement
    }

    @Test
    fun `should use default wait config when none provided`() {
        // Given
        val descriptor =
            mockSearchContext.id(
                value = "default-wait",
                cacheLookup = false,
            )

        every { mockSearchContext.findElement(By.id("default-wait")) } returns mockElement
        every { mockElement.isDisplayed } returns true

        // When
        val result = descriptor.get()

        // Then
        result shouldBe mockElement
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
            val descriptor =
                mockSearchContext.cssSelector(
                    value = ".test",
                    cacheLookup = false,
                )

            every { mockSearchContext.findElement(By.cssSelector(".test")) } returns mockElement
            every { mockElement.isDisplayed } returns true

            // When
            descriptor.get()

            // Then
            descriptor.toString() shouldContain "timeout=30s"
            descriptor.toString() shouldContain "polling=500ms"
        }
    }

    @Test
    fun `should escape CSS attribute values correctly for dataQa`() {
        // Given
        val descriptor =
            mockSearchContext.dataQa(
                value = "test\"value\\with",
                cacheLookup = false,
                waitConfig = WaitConfig.Quick,
            )

        every {
            mockSearchContext.findElement(By.cssSelector("[data-qa=\"test\\\"value\\\\with\"]"))
        } returns mockElement
        every { mockElement.isDisplayed } returns true

        // When
        val result = descriptor.get()

        // Then
        result shouldBe mockElement
        descriptor.by.toString() shouldContain "test\\\"value\\\\with"
    }

    @Test
    fun `should throw when value is blank`() {
        // When/Then
        shouldThrow<IllegalArgumentException> {
            mockSearchContext.id(
                value = "   ",
                cacheLookup = false,
            )
        }.message shouldContain "must not be blank"
    }

    @Test
    fun `toString should include context, by, cache, and wait info`() {
        // Given
        val descriptor =
            mockSearchContext.xpath(
                value = "//div[@id='test']",
                cacheLookup = true,
                waitConfig =
                    WaitConfig(
                        timeout = 5.seconds,
                        pollingInterval = 250.milliseconds,
                    ),
            )

        // When
        val result = descriptor.toString()

        // Then
        result shouldContain "ElementDescriptor"
        result shouldContain "xpath: //div[@id='test']"
        result shouldContain "cacheLookup=true"
        result shouldContain "timeout=5s"
        result shouldContain "polling=250ms"
    }

    @Test
    fun `should work with all locator strategies`() {
        // Test multiple locator types
        val locators =
            listOf(
                mockSearchContext.className("test-class"),
                mockSearchContext.cssSelector(".test"),
                mockSearchContext.dataTest("test-data"),
                mockSearchContext.dataTestId("test-id"),
                mockSearchContext.id("element-id"),
                mockSearchContext.idOrName("element"),
                mockSearchContext.linkText("Click here"),
                mockSearchContext.name("element-name"),
                mockSearchContext.partialLinkText("Click"),
                mockSearchContext.tagName("div"),
                mockSearchContext.xpath("//div"),
            )

        // Mock all possible findElement calls
        every { mockSearchContext.findElement(any()) } returns mockElement
        every { mockElement.isDisplayed } returns true

        // When/Then - all should work without throwing
        locators.forEach { descriptor ->
            descriptor.get() shouldNotBe null
        }
    }

    @Test
    fun `should handle custom element ready condition from site`() {
        // Given
        val site =
            object : Site("https://example.com") {
                override val elementReadyCondition: WebElement.() -> Boolean = {
                    isDisplayed && isEnabled
                }
            }

        val driver = mockk<WebDriver>(relaxed = true)
        SessionContext.withSession(Session(driver, site)) {
            val descriptor =
                mockSearchContext.id(
                    value = "custom-ready",
                    cacheLookup = false,
                )

            every { mockSearchContext.findElement(By.id("custom-ready")) } returns mockElement
            every { mockElement.isDisplayed } returns true
            every { mockElement.isEnabled } returnsMany listOf(false, true)

            // When
            val result = descriptor.get()

            // Then
            result shouldBe mockElement
            verify(atLeast = 1) { mockElement.isEnabled }
        }
    }

    @Test
    fun `should merge decorators - site first then test with dedup and test winning`() {
        val site =
            object : Site("https://example.test") {
                override val decorators = listOf(LoggerDecorator())
            }

        val driver = mockk<WebDriver>(relaxed = true)
        SessionContext.withSession(Session(driver, site)) {
            every { mockSearchContext.findElement(By.id("x")) } returns mockElement
            every { mockElement.isDisplayed } returns true

            withDecorators(SlowMotionDecorator(wait = 1.seconds), LoggerDecorator()) {
                val descriptor = mockSearchContext.id("x", waitConfig = WaitConfig.Quick)
                descriptor.get() // trigger lazy merge and decoration
                descriptor.toString() shouldContain "decorators=[SlowMotionDecorator, LoggerDecorator]"
            }
        }
    }
}

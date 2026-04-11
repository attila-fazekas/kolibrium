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

package dev.kolibrium.selenium.core

import dev.kolibrium.selenium.core.descriptors.DecoratedCompositeElementDescriptor
import dev.kolibrium.selenium.core.descriptors.DecoratedCompositeElementsDescriptor
import dev.kolibrium.webdriver.WaitConfig
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.openqa.selenium.By
import org.openqa.selenium.SearchContext
import org.openqa.selenium.WebElement

class ElementByDelegateTest {
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

    @Nested
    inner class ElementByDelegateTest {
        @Test
        fun `element(by) should return CompositeElementDescriptor`() {
            val by = By.id("test-id")

            val descriptor = mockSearchContext.element(by)

            descriptor.shouldBeInstanceOf<DecoratedCompositeElementDescriptor>()
        }

        @Test
        fun `element(by) should find element with simple By`() {
            val by = By.id("test-id")

            every { mockSearchContext.findElement(by) } returns mockElement
            every { mockElement.isDisplayed } returns true

            val descriptor = mockSearchContext.element(by, waitConfig = WaitConfig.Quick)
            val result = descriptor.get()

            result shouldBe mockElement
        }

        @Test
        fun `element(by) should work with chained locators`() {
            val by = chained(By.id("container"), By.className("title"))

            every { mockSearchContext.findElement(by) } returns mockElement
            every { mockElement.isDisplayed } returns true

            val descriptor = mockSearchContext.element(by, waitConfig = WaitConfig.Quick)
            val result = descriptor.get()

            result shouldBe mockElement
        }

        @Test
        fun `element(by) should work with anyOf locators`() {
            val by = anyOf(By.id("primary"), By.cssSelector(".fallback"))

            every { mockSearchContext.findElement(by) } returns mockElement
            every { mockElement.isDisplayed } returns true

            val descriptor = mockSearchContext.element(by, waitConfig = WaitConfig.Quick)
            val result = descriptor.get()

            result shouldBe mockElement
        }

        @Test
        fun `element(by) should work with nested mixed composition`() {
            val by =
                chained(
                    By.id("form"),
                    anyOf(By.name("submit"), By.className("submit-btn")),
                )

            every { mockSearchContext.findElement(by) } returns mockElement
            every { mockElement.isDisplayed } returns true

            val descriptor = mockSearchContext.element(by, waitConfig = WaitConfig.Quick)
            val result = descriptor.get()

            result shouldBe mockElement
        }

        @Test
        fun `element(by) should cache element by default`() {
            val by = By.id("cached")

            every { mockSearchContext.findElement(by) } returns mockElement
            every { mockElement.isDisplayed } returns true

            val descriptor = mockSearchContext.element(by, waitConfig = WaitConfig.Quick)

            descriptor.get()
            descriptor.get()

            verify(exactly = 1) { mockSearchContext.findElement(by) }
        }

        @Test
        fun `element(by) should not cache when cacheLookup is false`() {
            val by = By.id("uncached")

            every { mockSearchContext.findElement(by) } returns mockElement
            every { mockElement.isDisplayed } returns true

            val descriptor =
                mockSearchContext.element(
                    by,
                    cacheLookup = false,
                    waitConfig = WaitConfig.Quick,
                )

            descriptor.get()
            descriptor.get()

            verify(exactly = 2) { mockSearchContext.findElement(by) }
        }
    }

    @Nested
    inner class ElementsByDelegateTest {
        private lateinit var mockElement2: WebElement

        @BeforeEach
        fun setupElements() {
            mockElement2 = mockk(relaxed = true)
        }

        @Test
        fun `elements(by) should return CompositeElementsDescriptor`() {
            val by = By.className("item")

            val descriptor = mockSearchContext.elements(by)

            descriptor.shouldBeInstanceOf<DecoratedCompositeElementsDescriptor>()
        }

        @Test
        fun `elements(by) should find elements with simple By`() {
            val by = By.className("item")
            val mockElements = listOf(mockElement, mockElement2)

            every { mockSearchContext.findElements(by) } returns mockElements
            every { mockElement.isDisplayed } returns true
            every { mockElement2.isDisplayed } returns true

            val descriptor = mockSearchContext.elements(by, waitConfig = WaitConfig.Quick)
            val result = descriptor.get()

            result shouldBe mockElements
        }

        @Test
        fun `elements(by) should work with chained locators`() {
            val by = chained(By.id("list"), By.className("item"))
            val mockElements = listOf(mockElement, mockElement2)

            every { mockSearchContext.findElements(by) } returns mockElements
            every { mockElement.isDisplayed } returns true
            every { mockElement2.isDisplayed } returns true

            val descriptor = mockSearchContext.elements(by, waitConfig = WaitConfig.Quick)
            val result = descriptor.get()

            result shouldBe mockElements
        }

        @Test
        fun `elements(by) should work with anyOf locators`() {
            val by = anyOf(By.cssSelector(".primary-link"), By.tagName("a"))
            val mockElements = listOf(mockElement, mockElement2)

            every { mockSearchContext.findElements(by) } returns mockElements
            every { mockElement.isDisplayed } returns true
            every { mockElement2.isDisplayed } returns true

            val descriptor = mockSearchContext.elements(by, waitConfig = WaitConfig.Quick)
            val result = descriptor.get()

            result shouldBe mockElements
        }

        @Test
        fun `elements(by) should work with nested mixed composition`() {
            val by =
                chained(
                    By.id("container"),
                    anyOf(By.className("row"), By.cssSelector(".item")),
                )
            val mockElements = listOf(mockElement, mockElement2)

            every { mockSearchContext.findElements(by) } returns mockElements
            every { mockElement.isDisplayed } returns true
            every { mockElement2.isDisplayed } returns true

            val descriptor = mockSearchContext.elements(by, waitConfig = WaitConfig.Quick)
            val result = descriptor.get()

            result shouldBe mockElements
        }

        @Test
        fun `elements(by) should always perform fresh lookup`() {
            val by = By.className("item")
            val mockElements = listOf(mockElement)

            every { mockSearchContext.findElements(by) } returns mockElements
            every { mockElement.isDisplayed } returns true

            val descriptor = mockSearchContext.elements(by, waitConfig = WaitConfig.Quick)

            descriptor.get()
            descriptor.get()

            verify(exactly = 2) { mockSearchContext.findElements(by) }
        }
    }
}

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

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.openqa.selenium.WebElement

class LocatorUtilsTest {
    @Test
    fun `isClickable is true when element is displayed and enabled`() {
        val element =
            mockk<WebElement> {
                every { isDisplayed } returns true
                every { isEnabled } returns true
            }
        element.isClickable shouldBe true
    }

    @Test
    fun `isClickable is false when element is not displayed`() {
        val element =
            mockk<WebElement> {
                every { isDisplayed } returns false
                every { isEnabled } returns true
            }
        element.isClickable shouldBe false
    }

    @Test
    fun `isClickable is false when element is not enabled`() {
        val element =
            mockk<WebElement> {
                every { isDisplayed } returns true
                every { isEnabled } returns false
            }
        element.isClickable shouldBe false
    }

    @Test
    fun `WebElements isClickable is true when all elements are displayed and enabled`() {
        val elements: WebElements =
            List(3) {
                mockk<WebElement> {
                    every { isDisplayed } returns true
                    every { isEnabled } returns true
                }
            }
        elements.isClickable shouldBe true
    }

    @Test
    fun `WebElements isClickable is false when one element is not enabled`() {
        val elements: WebElements =
            listOf(
                mockk {
                    every { isDisplayed } returns true
                    every { isEnabled } returns true
                },
                mockk {
                    every { isDisplayed } returns true
                    every { isEnabled } returns false
                },
            )
        elements.isClickable shouldBe false
    }

    @Test
    fun `WebElements isDisplayed is true when all elements are displayed`() {
        val elements: WebElements = List(2) { mockk { every { isDisplayed } returns true } }
        elements.isDisplayed shouldBe true
    }

    @Test
    fun `WebElements isDisplayed is false when any element is not displayed`() {
        val elements: WebElements =
            listOf(
                mockk { every { isDisplayed } returns true },
                mockk { every { isDisplayed } returns false },
            )
        elements.isDisplayed shouldBe false
    }

    @Test
    fun `WebElements isEnabled is true when all elements are enabled`() {
        val elements: WebElements = List(2) { mockk { every { isEnabled } returns true } }
        elements.isEnabled shouldBe true
    }

    @Test
    fun `isNotEmptyAndDisplayed returns false for empty list`() {
        val elements: WebElements = emptyList()
        elements.isNotEmptyAndDisplayed shouldBe false
    }

    @Test
    fun `isNotEmptyAndDisplayed returns true only when non-empty and all displayed`() {
        val elements: WebElements = List(2) { mockk { every { isDisplayed } returns true } }
        elements.isNotEmptyAndDisplayed shouldBe true
    }

    @Test
    fun `isNotEmptyAndDisplayed returns false when non-empty but some hidden`() {
        val elements: WebElements =
            listOf(
                mockk { every { isDisplayed } returns true },
                mockk { every { isDisplayed } returns false },
            )
        elements.isNotEmptyAndDisplayed shouldBe false
    }
}

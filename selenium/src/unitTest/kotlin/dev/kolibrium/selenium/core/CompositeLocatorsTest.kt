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

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test
import org.openqa.selenium.By
import org.openqa.selenium.support.pagefactory.ByAll
import org.openqa.selenium.support.pagefactory.ByChained

class CompositeLocatorsTest {
    @Test
    fun `chained should return ByChained instance`() {
        val result = chained(By.id("container"), By.className("title"))

        result.shouldBeInstanceOf<ByChained>()
    }

    @Test
    fun `chained should contain correct inner locators`() {
        val locator1 = By.id("parent")
        val locator2 = By.cssSelector(".child")
        val locator3 = By.tagName("span")

        val result = chained(locator1, locator2, locator3)

        result.shouldBeInstanceOf<ByChained>()
        result.toString() shouldBe "By.chained({By.id: parent,By.cssSelector: .child,By.tagName: span})"
    }

    @Test
    fun `chained with single locator should work`() {
        val result = chained(By.id("single"))

        result.shouldBeInstanceOf<ByChained>()
        result.toString() shouldBe "By.chained({By.id: single})"
    }

    @Test
    fun `anyOf should return ByAll instance`() {
        val result = anyOf(By.id("primary"), By.cssSelector(".fallback"))

        result.shouldBeInstanceOf<ByAll>()
    }

    @Test
    fun `anyOf should contain correct inner locators`() {
        val locator1 = By.id("option1")
        val locator2 = By.name("option2")
        val locator3 = By.xpath("//div[@class='option3']")

        val result = anyOf(locator1, locator2, locator3)

        result.shouldBeInstanceOf<ByAll>()
        result.toString() shouldBe "By.all({By.id: option1,By.name: option2,By.xpath: //div[@class='option3']})"
    }

    @Test
    fun `anyOf with single locator should work`() {
        val result = anyOf(By.name("single"))

        result.shouldBeInstanceOf<ByAll>()
        result.toString() shouldBe "By.all({By.name: single})"
    }

    @Test
    fun `chained and anyOf can be nested`() {
        val nested = chained(
            By.id("container"),
            anyOf(By.className("primary"), By.className("secondary")),
        )

        nested.shouldBeInstanceOf<ByChained>()
        nested.toString() shouldBe "By.chained({By.id: container,By.all({By.className: primary,By.className: secondary})})"
    }
}

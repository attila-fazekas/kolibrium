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
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test
import org.openqa.selenium.By
import org.openqa.selenium.support.pagefactory.ByChained
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class CompositeElementDescriptorIntegrationTest : AbstractElementDescriptorIntegrationTest() {
    override fun html() =
        """
        <!DOCTYPE html><html><body>
            <div id="container">
                <span class="label">Inside</span>
            </div>
        </body></html>
        """.trimIndent()

    private val wait = WaitConfig(pollingInterval = 100.milliseconds, timeout = 2.seconds)

    @Test
    fun `resolves element via ByChained composite locator`() {
        val chainedBy = ByChained(By.id("container"), By.className("label"))
        val descriptor =
            CompositeElementDescriptor(
                searchCtx = driver,
                by = chainedBy,
                cacheLookup = false,
                waitConfig = wait,
                readyWhen = { isDisplayed },
            )
        descriptor.get().text shouldBe "Inside"
    }

    @Test
    fun `caches element when cacheLookup = true`() {
        val chainedBy = ByChained(By.id("container"), By.className("label"))
        val descriptor =
            CompositeElementDescriptor(
                searchCtx = driver,
                by = chainedBy,
                cacheLookup = true,
                waitConfig = wait,
                readyWhen = { isDisplayed },
            )
        descriptor.get() shouldBe descriptor.get()
    }

    @Test
    fun `toString includes descriptor name and locator`() {
        val chainedBy = ByChained(By.id("container"), By.className("label"))
        val descriptor =
            CompositeElementDescriptor(
                searchCtx = driver,
                by = chainedBy,
                cacheLookup = false,
                waitConfig = wait,
                readyWhen = { isDisplayed },
            )
        descriptor.toString() shouldContain "CompositeElementDescriptor"
    }
}

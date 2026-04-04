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
import dev.kolibrium.webdriver.configureWith
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import io.kotest.matchers.longs.shouldBeLessThan
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.openqa.selenium.TimeoutException
import org.openqa.selenium.support.ui.FluentWait
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.measureTime

class FluentWaitConfigureWithIntegrationTest {
    @Test
    fun `configureWith applies timeout - wait expires close to configured duration`() {
        val config = WaitConfig(pollingInterval = 50.milliseconds, timeout = 300.milliseconds)
        val wait = FluentWait("ignored").configureWith(config)

        val elapsed =
            measureTime {
                shouldThrow<TimeoutException> { wait.until { false } }
            }

        elapsed.inWholeMilliseconds shouldBeGreaterThanOrEqualTo 300L
        elapsed.inWholeMilliseconds shouldBeLessThan 1000L
    }

    @Test
    fun `configureWith applies message - TimeoutException carries configured message`() {
        val config =
            WaitConfig(
                pollingInterval = 50.milliseconds,
                timeout = 200.milliseconds,
                message = "custom wait message",
            )
        val wait = FluentWait("ignored").configureWith(config)

        val ex = shouldThrow<TimeoutException> { wait.until { false } }
        ex.message?.contains("custom wait message") shouldBe true
    }

    @Test
    fun `configureWith ignoring - listed exception does not abort the wait`() {
        val config =
            WaitConfig(
                pollingInterval = 50.milliseconds,
                timeout = 500.milliseconds,
                ignoring = setOf(IllegalStateException::class),
            )
        val wait = FluentWait("ignored").configureWith(config)
        var calls = 0

        shouldThrow<TimeoutException> {
            wait.until {
                calls++
                throw IllegalStateException("tolerated")
            }
        }

        // Should have polled multiple times instead of failing on the first throw
        (calls > 1) shouldBe true
    }
}

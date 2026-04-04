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

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class WaitConfigTest {
    @Test
    fun `should accept valid timeout and pollingInterval`() {
        val config = WaitConfig(pollingInterval = 100.milliseconds, timeout = 5.seconds)
        config.timeout shouldBe 5.seconds
        config.pollingInterval shouldBe 100.milliseconds
    }

    @Test
    fun `should throw when pollingInterval is negative`() {
        val ex =
            shouldThrow<IllegalArgumentException> {
                WaitConfig(pollingInterval = (-1).milliseconds)
            }
        ex.message shouldContain "pollingInterval must not be negative"
    }

    @Test
    fun `should throw when pollingInterval is below 10ms`() {
        val ex =
            shouldThrow<IllegalArgumentException> {
                WaitConfig(pollingInterval = 5.milliseconds)
            }
        ex.message shouldContain "at least 10ms"
    }

    @Test
    fun `should throw when timeout is negative`() {
        val ex =
            shouldThrow<IllegalArgumentException> {
                WaitConfig(timeout = (-1).milliseconds)
            }
        ex.message shouldContain "timeout must not be negative"
    }

    @Test
    fun `should throw when timeout is below 100ms`() {
        val ex =
            shouldThrow<IllegalArgumentException> {
                WaitConfig(timeout = 50.milliseconds)
            }
        ex.message shouldContain "at least 100ms"
    }

    @Test
    fun `should throw when pollingInterval exceeds timeout`() {
        val ex =
            shouldThrow<IllegalArgumentException> {
                WaitConfig(pollingInterval = 2.seconds, timeout = 1.seconds)
            }
        ex.message shouldContain "must not be greater than timeout"
    }

    @Test
    fun `copy should override only specified fields`() {
        val original = WaitConfig.Default
        val copy = original.copy(timeout = 20.seconds)

        copy.timeout shouldBe 20.seconds
        copy.pollingInterval shouldBe original.pollingInterval
        copy.message shouldBe original.message
    }

    @Test
    fun `Default preset has expected values`() {
        WaitConfig.Default.timeout shouldBe 10.seconds
        WaitConfig.Default.pollingInterval shouldBe 200.milliseconds
    }

    @Test
    fun `Quick preset has expected values`() {
        WaitConfig.Quick.timeout shouldBe 2.seconds
        WaitConfig.Quick.pollingInterval shouldBe 100.milliseconds
    }

    @Test
    fun `Patient preset has expected values`() {
        WaitConfig.Patient.timeout shouldBe 30.seconds
        WaitConfig.Patient.pollingInterval shouldBe 500.milliseconds
    }
}

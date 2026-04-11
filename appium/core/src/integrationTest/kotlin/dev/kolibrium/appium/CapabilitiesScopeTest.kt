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

package dev.kolibrium.appium

import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import kotlin.collections.get

class CapabilitiesScopeTest {
    @Test
    fun `settings should add appium settings to capabilities map`() {
        val scope =
            CapabilitiesScope().apply {
                settings {
                    ignoreUnimportantViews = true
                    allowInvisibleElements = false
                }
            }

        scope.capabilities shouldHaveSize 1
        val settingsMap = scope.capabilities["appium:settings"] as Map<*, *>
        settingsMap shouldHaveSize 2
        settingsMap["ignoreUnimportantViews"] shouldBe true
        settingsMap["allowInvisibleElements"] shouldBe false
    }

    @Test
    fun `settings should not add appium settings when block is empty`() {
        val scope =
            CapabilitiesScope().apply {
                settings { }
            }

        scope.capabilities.shouldBeEmpty()
    }
}

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

import io.appium.java_client.AppiumDriver
import io.appium.java_client.HasSettings
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class RuntimeSettingsTest {
    @Test
    fun `AppScope settings should call setSettings on driver`() {
        val driver = mockk<AppiumDriver>(relaxed = true)
        val entry = AppScope<AndroidApp>(driver)

        entry.settings {
            ignoreUnimportantViews = true
            waitForIdleTimeout = 5000L
        }

        verify {
            (driver as HasSettings).settings =
                mapOf(
                    "ignoreUnimportantViews" to true,
                    "waitForIdleTimeout" to 5000L,
                )
        }
    }

    @Test
    fun `ScreenScope settings should call setSettings on driver`() {
        val driver = mockk<AppiumDriver>(relaxed = true)
        val mockScreen = mockk<Screen<AndroidApp>>(relaxed = true)
        val scope = ScreenScope(mockScreen, driver)

        scope.settings {
            allowInvisibleElements = true
        }

        verify {
            (driver as HasSettings).settings =
                mapOf(
                    "allowInvisibleElements" to true,
                )
        }
    }

    @Test
    fun `ScreenScope settings should return same scope for chaining`() {
        val driver = mockk<AppiumDriver>(relaxed = true)
        val mockScreen = mockk<Screen<AndroidApp>>(relaxed = true)
        val scope = ScreenScope(mockScreen, driver)

        val result =
            scope.settings {
                ignoreUnimportantViews = true
            }

        result shouldBe scope
    }
}

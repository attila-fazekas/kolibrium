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

package dev.kolibrium.core

import dev.kolibrium.core.InternalKolibriumApi
import dev.kolibrium.core.Session
import dev.kolibrium.core.SessionContext
import dev.kolibrium.core.Site
import dev.kolibrium.core.support.FakeWebDriver
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class WithDriverGuardsTest {
    @Test
    @OptIn(InternalKolibriumApi::class)
    fun `withDriver rejects different driver when session is active`() {
        val driver1 = FakeWebDriver()
        val driver2 = FakeWebDriver()
        val site = object : Site("https://example.test") {}
        val session = Session(driver = driver1, site = site)

        SessionContext.withSession(session) {
            val ex =
                assertThrows(IllegalStateException::class.java) {
                    withDriver(driver2) { /* no-op */ }
                }
            assert(ex.message!!.contains("withDriver() received a WebDriver different from the active Session's driver")) {
                "Unexpected error message: ${ex.message}"
            }
        }
    }

    @Test
    @OptIn(InternalKolibriumApi::class)
    fun `withDriver allows same driver when session is active`() {
        val driver = FakeWebDriver()
        val site = object : Site("https://example.test") {}
        val session = Session(driver = driver, site = site)

        val result =
            SessionContext.withSession(session) {
                withDriver(driver) { 42 }
            }
        assertEquals(42, result)
    }
}

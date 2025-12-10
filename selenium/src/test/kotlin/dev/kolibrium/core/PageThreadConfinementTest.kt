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

import dev.kolibrium.core.support.FakeWebDriver
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicReference

private object TestSite : Site(baseUrl = "https://example.test")

private class TestPage : Page<Site>() {
    fun doRefresh() = refresh()
}

class PageThreadConfinementTest {
    @Test
    @OptIn(InternalKolibriumApi::class)
    fun `refresh runs on same thread when session and driver are bound`() {
        val driver = FakeWebDriver()
        val session = Session(driver = driver, site = TestSite)

        SessionContext.withSession(session) {
            withDriver(driver) {
                TestPage().doRefresh()
            }
        }

        assertEquals(1, driver.refreshCount, "refresh() should delegate to driver.navigate().refresh() exactly once")
    }

    @Test
    @OptIn(InternalKolibriumApi::class)
    fun `refresh from different thread fails with clear error`() {
        val driver = FakeWebDriver()
        val session = Session(driver = driver, site = TestSite)

        // Prepare a page on the owning thread
        val page = TestPage()

        SessionContext.withSession(session) {
            withDriver(driver) {
                // Intentionally do nothing here; we just want an attached page instance
            }
        }

        val errorRef = AtomicReference<Throwable?>()

        val thread =
            Thread {
                try {
                    // No session/driver bound on this new thread -> must fail fast
                    page.doRefresh()
                } catch (t: Throwable) {
                    errorRef.set(t)
                }
            }

        thread.start()
        thread.join()

        val thrown = errorRef.get()
        assertNotNull(thrown, "Calling page.refresh() from a different thread must throw")
        val message = thrown!!.message ?: ""
        // Depending on configuration we either see a confinement error or the no-context error.
        assertTrue(
            message.contains("thread confinement violation", ignoreCase = true) ||
                message.contains("has no active WebDriver context", ignoreCase = true),
            "Unexpected error message: $message",
        )
    }
}

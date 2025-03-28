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

package dev.kolibrium.dsl.selenium.creation.invalid

import dev.kolibrium.dsl.selenium.creation.chromeDriver
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.openqa.selenium.net.PortProber
import java.net.ServerSocket

class DriverTest {
    @Test
    fun `ChromeDriver shall not be created - port in use`() {
        val port = PortProber.findFreePort()

        ServerSocket(port).use {
            val exception =
                assertThrows<RuntimeException> {
                    chromeDriver {
                        driverService {
                            this.port = port
                        }
                    }
                }

            exception.message shouldBe
                """
                    |DriverService is not set up properly:
                    |Port $port already in use
                """.trimMargin()
        }
    }

    @Test
    fun `ChromeDriver shall not be created - wrong executable path`() {
        val exception =
            assertThrows<RuntimeException> {
                chromeDriver {
                    driverService {
                        executable = "does not exist"
                    }
                }
            }

        exception.message shouldBe
            """
                |DriverService is not set up properly:
                |The following file does not exist at the specified path: does not exist
            """.trimMargin()
    }
}

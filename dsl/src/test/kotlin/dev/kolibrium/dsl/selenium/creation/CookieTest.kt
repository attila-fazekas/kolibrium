/*
 * Copyright 2023-2024 Attila Fazekas & contributors
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

package dev.kolibrium.dsl.selenium.creation

import dev.kolibrium.dsl.selenium.cookie.SameSite.STRICT
import dev.kolibrium.dsl.selenium.cookie.cookies
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant.now
import java.util.Date

class CookieTest {
    private val driver =
        chromeDriver {
            options {
                arguments {
                    +Arguments.Chrome.disable_search_engine_choice_screen
                }
            }
        }

    @BeforeEach
    fun openUrl() {
        driver["https://bonigarcia.dev/selenium-webdriver-java/cookies.html"]
        driver.manage().deleteAllCookies()
    }

    @AfterEach
    fun quitDriver() {
        driver.quit()
    }

    @Test
    fun `add cookies`() {
        val now = Date.from(now().plusSeconds(60))

        driver.cookies {
            cookie(
                name = "username",
                value = "test",
                domain = "bonigarcia.dev",
                path = "/selenium-webdriver-java",
                expiresOn = now,
                isSecure = false,
                isHttpOnly = false,
                sameSite = STRICT,
            )
        }

        assertSoftly(driver.manage().cookies.first()) {
            name shouldBe "username"
            value shouldBe "test"
            domain shouldBe ".bonigarcia.dev"
            path shouldBe "/selenium-webdriver-java"
            expiry.toString() shouldBe now.toString()
            isSecure shouldBe true
            isHttpOnly shouldBe false
            sameSite shouldBe "Strict"
        }
    }
}

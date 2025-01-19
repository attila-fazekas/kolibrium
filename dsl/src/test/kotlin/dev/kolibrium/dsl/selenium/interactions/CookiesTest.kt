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

package dev.kolibrium.dsl.selenium.interactions

import dev.kolibrium.dsl.selenium.creation.Arguments.Chrome.disable_search_engine_choice_screen
import dev.kolibrium.dsl.selenium.creation.chromeDriver
import dev.kolibrium.dsl.selenium.interactions.SameSite.STRICT
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant.now
import java.util.Date

class CookiesTest {
    private val driver =
        chromeDriver {
            options {
                arguments {
                    +disable_search_engine_choice_screen
                }
            }
        }

    @BeforeEach
    fun openUrl() {
        driver.apply {
            get("https://bonigarcia.dev/selenium-webdriver-java/cookies.html")
            manage().deleteAllCookies()
        }
    }

    @AfterEach
    fun quitDriver() {
        driver.quit()
    }

    @Test
    fun `add cookie`() {
        val expiresOn = Date.from(now().plusSeconds(60))

        driver.cookies {
            addCookie(
                name = "username",
                value = "test",
                domain = "bonigarcia.dev",
                path = "/selenium-webdriver-java",
                expiresOn = expiresOn,
                isSecure = true,
                isHttpOnly = false,
                sameSite = STRICT,
            )
        }

        assertSoftly(driver.manage().cookies) {
            size shouldBe 1
            with(first()) {
                name shouldBe "username"
                value shouldBe "test"
                domain shouldBe ".bonigarcia.dev"
                path shouldBe "/selenium-webdriver-java"
                expiry.toString() shouldBe expiresOn.toString()
                isSecure shouldBe true
                isHttpOnly shouldBe false
                sameSite shouldBe "Strict"
            }
        }
    }

    @Test
    fun `get cookie`() {
        driver.cookies {
            addCookie(name = "username", value = "test")
        }

        driver.cookies {
            getCookie("username") shouldBe driver.manage().cookies.first()
        }
    }

    @Test
    fun `get all cookies`() {
        driver.cookies {
            addCookie(name = "username", value = "test")
            addCookie(name = "password", value = "secret")

            getCookies() shouldBe driver.manage().cookies
        }
    }

    @Test
    fun `delete cookie`() {
        driver.cookies {
            val cookie = addCookie(name = "username", value = "test")
            addCookie(name = "password", value = "secret")

            deleteCookie(cookie)
            deleteCookie(name = "password")

            getCookies().shouldBeEmpty()
        }
    }

    @Test
    fun `delete all cookies`() {
        driver.cookies {
            addCookie(name = "username", value = "test")
            addCookie(name = "password", value = "secret")

            deleteCookies()

            getCookies().shouldBeEmpty()
        }
    }
}

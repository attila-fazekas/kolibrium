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

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver

class LoginPage(
    private val driver: WebDriver,
) {
    private val usernameInput = By.name("user-name")
    private val passwordInput = By.id("password")
    private val loginButton = By.name("login-button")

    fun login(
        username: String = "standard_user",
        password: String = "secret_sauce",
    ) = with(driver) {
        findElement(usernameInput).sendKeys(username)
        findElement(passwordInput).sendKeys(password)
        findElement(loginButton).click()
    }
}

class SeleniumTest {
    private lateinit var driver: WebDriver

    @BeforeEach
    fun setUp() {
        driver =
            ChromeDriver().apply {
                get("https://www.saucedemo.com")
            }
    }

    @AfterEach
    fun tearDown() {
        driver.quit()
    }

    @Test
    fun `taste of Kotlin`() {
        LoginPage(driver).login()

        assert(driver.title == "Swag Labs")
    }
}

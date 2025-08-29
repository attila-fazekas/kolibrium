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

package dev.kolibrium.test.pages

import dev.kolibrium.core.selenium.Page
import dev.kolibrium.core.selenium.idOrName
import dev.kolibrium.core.selenium.name
import org.openqa.selenium.WebDriver

class LoginPage(driver: WebDriver) : Page(driver) {
    private val usernameInput by name("user-name")
    private val passwordInput by idOrName("password")
    private val loginButton by name("login-button")

    fun login(username: String = "standard_user", password: String = "secret_sauce"): InventoryPage {
        usernameInput.sendKeys(username)
        passwordInput.sendKeys(password)
        loginButton.click()

        return InventoryPage()
    }

    companion object {
        context(driver: WebDriver)
        operator fun invoke(): LoginPage = LoginPage(driver)
    }
}

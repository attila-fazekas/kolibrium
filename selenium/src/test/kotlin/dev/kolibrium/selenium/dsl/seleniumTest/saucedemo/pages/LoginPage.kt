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

package dev.kolibrium.selenium.dsl.seleniumTest.saucedemo.pages

import dev.kolibrium.selenium.Page
import dev.kolibrium.selenium.dataTest
import dev.kolibrium.selenium.dsl.seleniumTest.saucedemo.SauceDemo
import dev.kolibrium.selenium.dsl.seleniumTest.saucedemo.User
import dev.kolibrium.selenium.idOrName
import dev.kolibrium.selenium.name
import org.openqa.selenium.WebDriver

class LoginPage : Page<SauceDemo>() {
    private val usernameInput by name("user-name")
    private val passwordInput by idOrName("password")
    private val loginButton by name("login-button")

    private val error by dataTest("error")

    fun login(
        user: User = User.Standard,
    ) {
        login(username = user.username, password = user.password)
    }

    fun login(
        username: String = User.Standard.username,
        password: String = User.Standard.password,
    ) {
        usernameInput.sendKeys(username)
        passwordInput.sendKeys(password)
        loginButton.click()
    }

    fun loginAsLockedOutUser() {
        login(User.LockedOut.username)
    }

    fun errorText(): String = error.text

    companion object {
        context(driver: WebDriver)
        operator fun invoke(): LoginPage = LoginPage()
    }
}

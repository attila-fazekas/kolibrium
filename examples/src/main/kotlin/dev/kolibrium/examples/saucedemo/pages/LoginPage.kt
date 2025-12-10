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

package dev.kolibrium.examples.saucedemo.pages

import dev.kolibrium.core.Page
import dev.kolibrium.core.idOrName
import dev.kolibrium.core.name
import dev.kolibrium.examples.saucedemo.SauceDemo
import dev.kolibrium.examples.saucedemo.User

class LoginPage : Page<SauceDemo>() {
    private val usernameInput = name("user-name")
    private val passwordInput by idOrName("password")
    private val loginButton by name("login-button")

    fun login(
        user: User = User.Standard,
        password: String = "secret_sauce",
    ): InventoryPage {
        println(usernameInput.toString())
        usernameInput.get().sendKeys(user.username)
        passwordInput.sendKeys(password)
        loginButton.click()
        return InventoryPage()
    }
}

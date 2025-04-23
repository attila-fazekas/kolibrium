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

package dev.kolibrium.core.selenium

import org.openqa.selenium.WebDriver

/**
 * Base class for Page Object Model implementations.
 *
 * This class serves as a foundation for creating page-specific classes that represent web pages
 * in your application. It delegates to the provided WebDriver instance to implement the
 * SearchContext interface, which makes all locator functions and extensions automatically
 * available on Page instances without requiring explicit forwarding methods.
 *
 * Example usage:
 * ```
 * class LoginPage(driver: WebDriver) : Page(driver) {
 *     private val username by name("user-name")
 *     private val password by idOrName("password")
 *     private val button by name("login-button")
 *
 *     fun login(username: String, password: String) {
 *         this.username.sendKeys(username)
 *         this.password.sendKeys(password)
 *         button.click()
 *     }
 * }
 * ```
 *
 * @property driver The WebDriver instance used to interact with the browser.
 */
public abstract class Page(
    protected val driver: WebDriver,
) : WebDriver by driver

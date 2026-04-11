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

package dev.kolibrium.playwright.pages

import dev.kolibrium.playwright.PlaywrightPage
import dev.kolibrium.playwright.SauceDemo

class LoginPage : PlaywrightPage<SauceDemo>() {
    fun login(
        username: String = "standard_user",
        password: String = "secret_sauce",
    ) {
        page.fill("[data-test='username']", username)
        page.fill("[data-test='password']", password)
        page.click("[data-test='login-button']")
    }
}

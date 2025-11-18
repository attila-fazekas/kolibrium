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

import dev.kolibrium.core.selenium.decorators.HighlighterDecorator
import org.openqa.selenium.Cookie
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver

// 1) Declare your Site
object MySite : Site(baseUrl = "https://www.saucedemo.com") {
    override val cookies: Set<Cookie> = setOf(Cookie("ab", "test"))
    override val decorators = listOf(HighlighterDecorator())
    override val waitConfig: WaitConfig = WaitConfig.Default

    override fun configureSite() {
        // compute site‑level policy (no WebDriver access here)
    }

    override fun onSessionReady(driver: WebDriver) {
        // session‑aware tweaks; do not navigate here
    }
}

// 2) Define a Page

class LoginPage : Page<MySite>() {
    private val usernameInput = name("user-name")
    private val passwordInput by idOrName("password")
    private val loginButton by name("login-button")

    override fun awaitReady() { /* optionally assert key elements are ready */ }

    fun login(
        username: String = "standard_user",
        password: String = "secret_sauce",
    ) {
        usernameInput.get().sendKeys(username)
        passwordInput.sendKeys(password)
        loginButton.click()
    }
}

fun main() {
    val driver = ChromeDriver()
    try {
        // 3) Bind a site to the thread (optional but recommended)
        SiteContext.withSite(MySite) {
            // 4) Perform the steps the DSL normally does for you
            driver.navigate().to(MySite.baseUrl)
            // Apply declarative cookies before first real navigation if you need them on initial request
            if (MySite.cookies.isNotEmpty()) {
                MySite.cookies.forEach { driver.manage().addCookie(it) }
                driver.navigate().to(MySite.baseUrl) // re‑navigate so cookies take effect
            }
            // Let the site finalize configuration for this session
            MySite.configureSite()
            MySite.onSessionReady(driver)

            // 5) Create a page and use it inside a WebDriver context
            val loginPage = LoginPage()
            withDriver(driver) {
                loginPage.awaitReady()
                // 6) Interact using locator delegates
                loginPage.login()
            }
        }
    } finally {
        driver.quit()
    }
}

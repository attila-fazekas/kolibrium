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

package dev.kolibrium.selenium.core.internal.decorators

import dev.kolibrium.selenium.core.decorators.BorderStyle
import dev.kolibrium.selenium.core.decorators.Color
import dev.kolibrium.selenium.core.decorators.DecoratorManager
import dev.kolibrium.selenium.core.decorators.HighlighterDecorator
import dev.kolibrium.selenium.core.decorators.LoggerDecorator
import dev.kolibrium.selenium.core.decorators.SlowMotionDecorator
import dev.kolibrium.selenium.core.idOrName
import dev.kolibrium.selenium.core.name
import org.junit.jupiter.api.Test
import org.openqa.selenium.chrome.ChromeDriver
import kotlin.time.Duration.Companion.seconds

class WithDecoratorsTest {
    @Test
    fun test() {
        ChromeDriver().apply {
            DecoratorManager.withDecorators(
                SlowMotionDecorator(wait = 1.seconds),
                HighlighterDecorator(
                    style = BorderStyle.Dashed,
                    color = Color.Green,
                ),
                LoggerDecorator(),
            ) {
                get("https://www.saucedemo.com")
                val usernameInput by name("user-name")
                val passwordInput by idOrName("password")
                val loginButton by name("login-button")

                usernameInput.sendKeys("standard_user")
                passwordInput.sendKeys("secret_sauce")
                loginButton.click()
            }
            quit()
        }
    }
}

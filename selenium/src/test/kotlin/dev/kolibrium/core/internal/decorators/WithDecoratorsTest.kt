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

package dev.kolibrium.core.internal.decorators

import dev.kolibrium.core.decorators.BorderStyle
import dev.kolibrium.core.decorators.Color
import dev.kolibrium.core.decorators.DecoratorManager
import dev.kolibrium.core.decorators.HighlighterDecorator
import dev.kolibrium.core.decorators.LoggerDecorator
import dev.kolibrium.core.decorators.SlowMotionDecorator
import dev.kolibrium.core.idOrName
import dev.kolibrium.core.name
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

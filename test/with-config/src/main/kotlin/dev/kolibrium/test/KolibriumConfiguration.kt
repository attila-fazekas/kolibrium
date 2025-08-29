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

package dev.kolibrium.test

import com.google.auto.service.AutoService
import dev.kolibrium.common.WebElements
import dev.kolibrium.core.selenium.configuration.AbstractSeleniumProjectConfiguration
import dev.kolibrium.core.selenium.decorators.BorderStyle.DOTTED
import dev.kolibrium.core.selenium.decorators.Color.BLUE
import dev.kolibrium.core.selenium.decorators.ElementStateCacheDecorator
import dev.kolibrium.core.selenium.decorators.HighlighterDecorator
import dev.kolibrium.core.selenium.decorators.SlowMotionDecorator
import dev.kolibrium.core.selenium.isClickable
import dev.kolibrium.dsl.selenium.creation.Arguments.Chrome.disable_search_engine_choice_screen
import dev.kolibrium.dsl.selenium.creation.Arguments.Chrome.headless
import dev.kolibrium.dsl.selenium.creation.Arguments.Chrome.incognito
import dev.kolibrium.dsl.selenium.creation.chromeDriver
import kotlin.time.Duration.Companion.milliseconds
import org.openqa.selenium.WebElement

@AutoService(AbstractSeleniumProjectConfiguration::class)
object SeleniumConfiguration : AbstractSeleniumProjectConfiguration() {
    override val baseUrl = "http://localhost:3003"

//    override val cookies = setOf(Cookie("session-username", "standard_user"))

    override val elementReadyCondition: (WebElement.() -> Boolean) = { isClickable }

    override val elementsReadyCondition: (WebElements.() -> Boolean) = { isClickable }

    override val decorators = listOf(
        HighlighterDecorator(style = DOTTED, color = BLUE, width = 10),
        SlowMotionDecorator(wait = 500.milliseconds),
        ElementStateCacheDecorator(
            cacheDisplayed = true,
            cacheEnabled = true,
            cacheSelected = true
        )
    )

    override val chromeDriver = {
        chromeDriver {
            options {
                arguments {
                    +disable_search_engine_choice_screen
                    +headless
                    +incognito
                }
            }
        }
    }
}

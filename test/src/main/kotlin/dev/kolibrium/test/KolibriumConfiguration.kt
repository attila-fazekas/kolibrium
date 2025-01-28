/*
 * Copyright 2023-2024 Attila Fazekas & contributors
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
import dev.kolibrium.dsl.selenium.creation.Arguments.Chrome.disable_search_engine_choice_screen
import dev.kolibrium.dsl.selenium.creation.Arguments.Chrome.headless
import dev.kolibrium.dsl.selenium.creation.Arguments.Chrome.incognito
import dev.kolibrium.dsl.selenium.creation.chromeDriver
import dev.kolibrium.junit.configuration.AbstractJUnitProjectConfiguration
import dev.kolibrium.selenium.Wait
import dev.kolibrium.selenium.Wait.Companion.DEFAULT
import dev.kolibrium.selenium.configuration.AbstractSeleniumProjectConfiguration
import dev.kolibrium.selenium.decorators.BorderStyle.DOTTED
import dev.kolibrium.selenium.decorators.Color.BLUE
import dev.kolibrium.selenium.decorators.HighlighterDecorator
import dev.kolibrium.selenium.decorators.SlowMotionDecorator
import dev.kolibrium.selenium.isClickable
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import org.openqa.selenium.WebElement

@AutoService(AbstractJUnitProjectConfiguration::class)
object JUnitConfiguration : AbstractJUnitProjectConfiguration() {
    override val baseUrl = "https://www.saucedemo.com"

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

@AutoService(AbstractSeleniumProjectConfiguration::class)
object SeleniumConfiguration : AbstractSeleniumProjectConfiguration() {
    override val elementReadinessCondition: (WebElement.() -> Boolean) = { isClickable }

    override val decorators = listOf(
        HighlighterDecorator(style = DOTTED, color = BLUE, width = 10),
        SlowMotionDecorator(wait = 500.milliseconds)
    )

    override val wait: Wait = DEFAULT.copy(timeout = 1.seconds)
}

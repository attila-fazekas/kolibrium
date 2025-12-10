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

package dev.kolibrium.dsl.webtest.saucedemo

import dev.kolibrium.core.Site
import dev.kolibrium.core.WaitConfig
import dev.kolibrium.core.WaitConfig.Companion.Quick
import dev.kolibrium.core.decorators.AbstractDecorator
import dev.kolibrium.core.decorators.BorderStyle
import dev.kolibrium.core.decorators.Color
import dev.kolibrium.core.decorators.HighlighterDecorator
import dev.kolibrium.core.decorators.LoggerDecorator
import dev.kolibrium.core.decorators.SlowMotionDecorator
import dev.kolibrium.core.isClickable
import org.openqa.selenium.WebElement
import kotlin.time.Duration.Companion.milliseconds

object SauceDemo : Site(baseUrl = "https://www.saucedemo.com") {
    override val elementReadyCondition: (WebElement.() -> Boolean) = { isClickable }

    override val waitConfig: WaitConfig = Quick

    override val decorators: List<AbstractDecorator> =
        listOf(
            HighlighterDecorator(
                style = BorderStyle.Dashed,
                color = Color.Green,
            ),
            SlowMotionDecorator(wait = 300.milliseconds),
            LoggerDecorator(),
        )
}

object Twitter : Site(baseUrl = "https://www.x.com") {
    override val elementReadyCondition: (WebElement.() -> Boolean) = { isClickable }

    override val waitConfig: WaitConfig = Quick

    override val decorators: List<AbstractDecorator> =
        listOf(
            HighlighterDecorator(
                style = BorderStyle.Dashed,
                color = Color.Green,
            ),
            SlowMotionDecorator(wait = 300.milliseconds),
            LoggerDecorator(),
        )
}

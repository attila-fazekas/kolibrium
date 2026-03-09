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

package dev.kolibrium.selenium.dsl.seleniumTest.browserstackdemo

import dev.kolibrium.selenium.core.Site
import dev.kolibrium.selenium.core.WaitConfig
import dev.kolibrium.selenium.core.WaitConfig.Companion.Quick
import dev.kolibrium.selenium.core.decorators.AbstractDecorator
import dev.kolibrium.selenium.core.decorators.BorderStyle
import dev.kolibrium.selenium.core.decorators.Color
import dev.kolibrium.selenium.core.decorators.HighlighterDecorator
import dev.kolibrium.selenium.core.decorators.LoggerDecorator
import dev.kolibrium.selenium.core.decorators.SlowMotionDecorator
import dev.kolibrium.selenium.core.isClickable
import org.openqa.selenium.WebElement
import kotlin.time.Duration.Companion.milliseconds

object BrowserStackDemo : Site(baseUrl = "https://bstackdemo.com") {
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

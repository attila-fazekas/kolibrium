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

package dev.kolibrium.dsl.selenium.webtest.bstackdemo

import dev.kolibrium.core.selenium.Site
import dev.kolibrium.core.selenium.WaitConfig
import dev.kolibrium.core.selenium.WaitConfig.Companion.QUICK
import dev.kolibrium.core.selenium.decorators.AbstractDecorator
import dev.kolibrium.core.selenium.decorators.BorderStyle
import dev.kolibrium.core.selenium.decorators.Color
import dev.kolibrium.core.selenium.decorators.HighlighterDecorator
import dev.kolibrium.core.selenium.decorators.LoggerDecorator
import dev.kolibrium.core.selenium.decorators.SlowMotionDecorator
import dev.kolibrium.core.selenium.isClickable
import org.openqa.selenium.WebElement
import kotlin.time.Duration.Companion.seconds

data object BrowserStackDemo : Site(baseUrl = "https://bstackdemo.com") {
    override val elementReadyCondition: (WebElement.() -> Boolean) = { isClickable }

    override val waitConfig: WaitConfig = QUICK

    override val decorators: List<AbstractDecorator> =
        listOf(
            HighlighterDecorator(
                style = BorderStyle.DASHED,
                color = Color.GREEN,
            ),
            SlowMotionDecorator(wait = 1.seconds),
            LoggerDecorator(),
        )
}

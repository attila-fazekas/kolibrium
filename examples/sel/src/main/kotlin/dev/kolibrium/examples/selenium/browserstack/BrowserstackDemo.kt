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

package dev.kolibrium.examples.selenium.browserstack

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
import dev.kolibrium.dsl.DriverFactory
import dev.kolibrium.dsl.SiteEntry
import dev.kolibrium.dsl.creation.Arguments.Chrome.disable_search_engine_choice_screen
import dev.kolibrium.dsl.creation.Arguments.Chrome.incognito
import dev.kolibrium.dsl.creation.chromeDriver
import dev.kolibrium.dsl.webTest
import org.openqa.selenium.WebElement
import kotlin.time.Duration.Companion.milliseconds
import org.openqa.selenium.chrome.ChromeDriver

object BrowserstackDemo : Site(baseUrl = "https://bstackdemo.com") {
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

fun browserstackDemoTest(
    driverFactory: DriverFactory = { ChromeDriver() },
    keepBrowserOpen: Boolean = false,
    startup: SiteEntry<BrowserstackDemo>.(Unit) -> Unit = { _ -> },
    block: SiteEntry<BrowserstackDemo>.(Unit) -> Unit,
) = webTest(
    site = BrowserstackDemo,
    keepBrowserOpen = keepBrowserOpen,
    driverFactory = driverFactory,
    startup = startup,
    block = block,
)

fun <T> browserstackDemoTest(
    driverFactory: DriverFactory = browserstackDemoDriver,
    keepBrowserOpen: Boolean = false,
    prepare: () -> T,
    startup: SiteEntry<BrowserstackDemo>.(T) -> Unit = { },
    block: SiteEntry<BrowserstackDemo>.(T) -> Unit,
) = webTest(
    site = BrowserstackDemo,
    keepBrowserOpen = keepBrowserOpen,
    driverFactory = driverFactory,
    prepare = prepare,
    startup = startup,
    block = block,
)

private val browserstackDemoDriver = {
    chromeDriver {
        options {
            arguments {
                +disable_search_engine_choice_screen
                +incognito
            }
        }
    }
}

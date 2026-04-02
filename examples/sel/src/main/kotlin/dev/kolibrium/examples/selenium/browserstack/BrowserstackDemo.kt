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

package dev.kolibrium.examples.selenium.browserstack

import dev.kolibrium.selenium.core.Site
import dev.kolibrium.selenium.core.decorators.AbstractDecorator
import dev.kolibrium.selenium.core.decorators.BorderStyle
import dev.kolibrium.selenium.core.decorators.Color
import dev.kolibrium.selenium.core.decorators.HighlighterDecorator
import dev.kolibrium.selenium.core.decorators.LoggerDecorator
import dev.kolibrium.selenium.core.decorators.SlowMotionDecorator
import dev.kolibrium.webdriver.isClickable
import dev.kolibrium.selenium.dsl.DriverFactory
import dev.kolibrium.selenium.dsl.SiteEntry
import dev.kolibrium.selenium.dsl.creation.Arguments.Chrome.disable_search_engine_choice_screen
import dev.kolibrium.selenium.dsl.creation.Arguments.Chrome.incognito
import dev.kolibrium.selenium.dsl.creation.chromeDriver
import dev.kolibrium.selenium.dsl.seleniumTest
import dev.kolibrium.webdriver.WaitConfig
import dev.kolibrium.webdriver.WaitConfig.Companion.Quick
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

fun browserStackDemoTest(
    driverFactory: DriverFactory = { ChromeDriver() },
    keepBrowserOpen: Boolean = false,
    block: SiteEntry<BrowserstackDemo>.(Unit) -> Unit,
) = seleniumTest(
    site = BrowserstackDemo,
    keepBrowserOpen = keepBrowserOpen,
    driverFactory = driverFactory,
    block = block,
)

fun <T> browserStackDemoTest(
    driverFactory: DriverFactory = browserStackDemoDriver,
    keepBrowserOpen: Boolean = false,
    setUp: () -> T,
    block: SiteEntry<BrowserstackDemo>.(T) -> Unit,
) = seleniumTest(
    site = BrowserstackDemo,
    keepBrowserOpen = keepBrowserOpen,
    driverFactory = driverFactory,
    setUp = setUp,
    block = block,
)

private val browserStackDemoDriver = {
    chromeDriver {
        options {
            arguments {
                +disable_search_engine_choice_screen
                +incognito
            }
        }
    }
}

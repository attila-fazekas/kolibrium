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

package dev.kolibrium.core.selenium.configuration

import dev.kolibrium.common.Browser
import dev.kolibrium.common.Browser.CHROME
import dev.kolibrium.common.Cookies
import dev.kolibrium.common.WebElements
import dev.kolibrium.core.selenium.WaitConfig
import dev.kolibrium.core.selenium.WaitConfig.Companion.DEFAULT
import dev.kolibrium.core.selenium.decorators.AbstractDecorator
import dev.kolibrium.core.selenium.isDisplayed
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.edge.EdgeDriver
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.safari.SafariDriver

internal const val ABOUT_BLANK = "about:blank"

internal object DefaultSeleniumProjectConfiguration : AbstractSeleniumProjectConfiguration() {
    override var baseUrl = ABOUT_BLANK

    override var cookies: Cookies? = emptySet()

    override var decorators: List<AbstractDecorator> = emptyList()

    override var defaultBrowser: Browser = CHROME

    override var elementReadyCondition: (WebElement.() -> Boolean) = { isDisplayed }

    override var elementsReadyCondition: (WebElements.() -> Boolean) = { isDisplayed }

    override var keepBrowserOpen: Boolean = false

    override var waitConfig: WaitConfig = DEFAULT

    override var chromeDriver: (() -> ChromeDriver)
        get() = { ChromeDriver() }
        set(_) {}

    override var safariDriver: (() -> SafariDriver)
        get() = { SafariDriver() }
        set(_) {}

    override var edgeDriver: (() -> EdgeDriver)
        get() = { EdgeDriver() }
        set(_) {}

    override var firefoxDriver: (() -> FirefoxDriver)
        get() = { FirefoxDriver() }
        set(_) {}
}

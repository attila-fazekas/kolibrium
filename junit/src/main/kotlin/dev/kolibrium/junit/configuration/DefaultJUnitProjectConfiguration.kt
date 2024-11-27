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

package dev.kolibrium.junit.configuration

import dev.kolibrium.core.Browser
import dev.kolibrium.core.Browser.CHROME
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.edge.EdgeDriver
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.safari.SafariDriver

@Suppress("UNUSED_PARAMETER")
internal object DefaultJUnitProjectConfiguration : AbstractJUnitProjectConfiguration() {
    override var baseUrl = "about:blank"

    override var defaultBrowser: Browser = CHROME

    override var keepBrowserOpen: Boolean = false

    override var chromeDriver: (() -> ChromeDriver)
        get() = { ChromeDriver() }
        set(value) {}

    override var safariDriver: (() -> SafariDriver)
        get() = { SafariDriver() }
        set(value) {}

    override var edgeDriver: (() -> EdgeDriver)
        get() = { EdgeDriver() }
        set(value) {}

    override var firefoxDriver: (() -> FirefoxDriver)
        get() = { FirefoxDriver() }
        set(value) {}
}

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

package dev.kolibrium.junit.config

import dev.kolibrium.core.Browser
import dev.kolibrium.core.Browser.CHROME
import dev.kolibrium.dsl.selenium.wait.WaitScope
import dev.kolibrium.dsl.selenium.wait.defaultWait
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.edge.EdgeDriver
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.safari.SafariDriver

internal class ProjectConfiguration : AbstractProjectConfiguration() {
    @Suppress("UNUSED_PARAMETER")
    companion object {
        internal var baseUrl = "https://www.google.com"

        internal var defaultBrowser: Browser = CHROME

        internal var keepBrowserOpen: Boolean = false

        internal var chromeDriver: (() -> ChromeDriver)
            get() = { ChromeDriver() }
            set(value) {}

        internal var safariDriver: (() -> SafariDriver)
            get() = { SafariDriver() }
            set(value) {}

        internal var edgeDriver: (() -> EdgeDriver)
            get() = { EdgeDriver() }
            set(value) {}

        internal var firefoxDriver: (() -> FirefoxDriver)
            get() = { FirefoxDriver() }
            set(value) {}

        internal var waitConfig: WaitScope = defaultWait
    }
}

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

import dev.kolibrium.common.Browser
import dev.kolibrium.common.InternalKolibriumApi
import dev.kolibrium.common.config.ProjectConfiguration
import dev.kolibrium.common.config.ProjectConfigurationLoader
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.edge.EdgeDriver
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.safari.SafariDriver

/**
 * Abstract base class for configuring Kolibrium's JUnit module project level settings.
 *
 * Provides customization points for WebDriver initialization, browser selection, and wait strategies.
 * To customize the configuration, create a class that extends this class and override the desired properties.
 */
@OptIn(InternalKolibriumApi::class)
public abstract class AbstractJUnitProjectConfiguration : ProjectConfiguration {
    /**
     * The base URL to navigate to when a WebDriver instance is created.
     */
    public open val baseUrl: String? = null

    /**
     * The preferred browser to use for tests when not explicitly specified.
     */
    public open val defaultBrowser: Browser? = null

    /**
     * Whether to keep the browser window open after test execution.
     */
    public open val keepBrowserOpen: Boolean? = null

    /**
     * Factory function for creating ChromeDriver instances.
     */
    public open val chromeDriver: (() -> ChromeDriver)? = null

    /**
     * Factory function for creating SafariDriver instances.
     */
    public open val safariDriver: (() -> SafariDriver)? = null

    /**
     * Factory function for creating EdgeDriver instances.
     */
    public open val edgeDriver: (() -> EdgeDriver)? = null

    /**
     * Factory function for creating FirefoxDriver instances.
     */
    public open val firefoxDriver: (() -> FirefoxDriver)? = null
}

internal object JUnitProjectConfiguration {
    @OptIn(InternalKolibriumApi::class)
    internal fun actualConfig(): AbstractJUnitProjectConfiguration =
        ProjectConfigurationLoader.loadConfiguration(AbstractJUnitProjectConfiguration::class)
            ?: DefaultJUnitProjectConfiguration
}

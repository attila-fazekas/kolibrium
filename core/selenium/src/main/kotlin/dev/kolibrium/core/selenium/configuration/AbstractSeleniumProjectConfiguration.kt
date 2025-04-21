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
import dev.kolibrium.common.InternalKolibriumApi
import dev.kolibrium.common.WebElements
import dev.kolibrium.common.config.ProjectConfiguration
import dev.kolibrium.common.config.ProjectConfigurationLoader
import dev.kolibrium.core.selenium.WaitConfig
import dev.kolibrium.core.selenium.decorators.AbstractDecorator
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.edge.EdgeDriver
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.safari.SafariDriver

/**
 * Abstract base class for configuring Kolibrium's Selenium module project level settings.
 * Provides customization points for wait strategies and element readiness checks.
 * To customize the configuration, create a class that extends this class and override the desired property.
 */
@OptIn(InternalKolibriumApi::class)
public abstract class AbstractSeleniumProjectConfiguration : ProjectConfiguration {
    /**
     * The base URL to navigate to when a WebDriver instance is created.
     */
    public open val baseUrl: String? = null

    /**
     * List of decorators to be applied to SearchContext objects (WebDriver or WebElement).
     * Decorators can add behavior like highlighting or slow motion to Selenium operations.
     */
    public open val decorators: List<AbstractDecorator> = emptyList()

    /**
     * The preferred browser to use for tests when not explicitly specified.
     */
    public open val defaultBrowser: Browser? = null

    /**
     * A predicate that determines when the found element is considered ready for use.
     */
    public open val elementReadyCondition: (WebElement.() -> Boolean)? = null

    /**
     * A predicate that determines when the found elements are considered ready for use.
     */
    public open val elementsReadyCondition: (WebElements.() -> Boolean)? = null

    /**
     * Whether to keep the browser window open after test execution.
     */
    public open val keepBrowserOpen: Boolean? = null

    /**
     * The wait configuration to use in synchronization operations.
     */
    public open val waitConfig: WaitConfig? = null

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

internal object SeleniumProjectConfiguration {
    @OptIn(InternalKolibriumApi::class)
    private val config by lazy {
        ProjectConfigurationLoader.loadConfiguration(AbstractSeleniumProjectConfiguration::class)
            ?: DefaultSeleniumProjectConfiguration
    }

    internal val actualConfig: AbstractSeleniumProjectConfiguration = config
}

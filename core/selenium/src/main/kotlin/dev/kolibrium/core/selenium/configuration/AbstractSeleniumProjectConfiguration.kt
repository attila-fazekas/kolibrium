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
import dev.kolibrium.common.Cookies
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
     * Allows configuring the base URL that will be used for all [dev.kolibrium.core.selenium.Page] instances and [dev.kolibrium.core.selenium.browserTest] functions unless overridden.
     */
    public open val baseUrl: String? = null

    /**
     * Allows configuring cookies that will be applied to all browser sessions when a [dev.kolibrium.core.selenium.Page] is instantiated.
     */
    public open val cookies: Cookies? = null

    /**
     * List of decorators to be applied to SearchContext objects (WebDriver or WebElement).
     * Decorators can add behavior like highlighting or slow motion to Selenium operations.
     */
    public open val decorators: List<AbstractDecorator> = emptyList()

    /**
     * The preferred browser to use for tests when not explicitly specified in [dev.kolibrium.core.selenium.browserTest] functions.
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
     * Controls whether browser sessions remain open after [dev.kolibrium.core.selenium.browserTest] functions complete.
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
    internal val actualConfig by lazy {
        ProjectConfigurationLoader.loadConfiguration(AbstractSeleniumProjectConfiguration::class)
            ?: DefaultSeleniumProjectConfiguration
    }
}

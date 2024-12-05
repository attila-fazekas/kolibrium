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

package dev.kolibrium.selenium.configuration

import dev.kolibrium.core.InternalKolibriumApi
import dev.kolibrium.core.WebElements
import dev.kolibrium.core.config.ProjectConfiguration
import dev.kolibrium.core.config.ProjectConfigurationLoader
import dev.kolibrium.selenium.Wait
import org.openqa.selenium.WebElement

/**
 * Abstract base class for configuring Kolibrium's Selenium module project level settings.
 *
 * Provides customization points for wait strategies.
 * To customize the configuration, create a class that extends this class and override the [wait] property.
 */
@OptIn(InternalKolibriumApi::class)
public abstract class AbstractSeleniumProjectConfiguration : ProjectConfiguration {
    /**
     * A predicate that determines when the found element is considered ready for use.
     */
    public open val elementReadyWhen: (WebElement.() -> Boolean)? = null

    /**
     * A predicate that determines when the found elements are considered ready for use.
     */
    public open val elementsReadyWhen: (WebElements.() -> Boolean)? = null

    /**
     * The wait configuration to use in synchronization operations.
     */
    public open val wait: Wait? = null
}

internal object SeleniumProjectConfiguration {
    @OptIn(InternalKolibriumApi::class)
    internal fun actualConfig(): AbstractSeleniumProjectConfiguration =
        ProjectConfigurationLoader.loadConfiguration(AbstractSeleniumProjectConfiguration::class)
            ?: DefaultSeleniumProjectConfiguration
}

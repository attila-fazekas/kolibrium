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

package dev.kolibrium.core.selenium

import dev.kolibrium.common.Cookies
import dev.kolibrium.common.WebElements
import dev.kolibrium.core.selenium.WaitConfig.Companion.Default
import dev.kolibrium.core.selenium.decorators.AbstractDecorator
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement

/**
 * Base configuration for a web site under test.
 *
 * A [Site] centralizes cross‑cutting configuration such as base URL, cookies that should be applied
 * to every session, element decorators, readiness conditions, and waiting behavior. Instances of [Page]
 * are parameterized by the specific [Site] type to keep navigation and interactions consistent.
 *
 * Example:
 * ```kotlin
 * class ShopSite : Site(baseUrl = "https://example.shop") {
 *     override val cookies = setOf(Cookie("ab", "test"))
 *     override fun configureDriver(driver: WebDriver) {
 *         driver.manage().window().maximize()
 *     }
 * }
 * ```
 *
 * @property baseUrl Base URL used by [Page] instances and by [dev.kolibrium.dsl.webTest]. Must be non‑blank.
 */
public abstract class Site(
    /**
     * Allows configuring the base URL that will be used for all [Page] instances and [dev.kolibrium.dsl.webTest] functions unless overridden.
     */
    public val baseUrl: String,
) {
    init {
        require(baseUrl.isNotBlank()) {
            "baseUrl is not set!"
        }
    }

    /**
     * Allows configuring cookies that will be applied to all browser sessions when a [Page] is instantiated.
     */
    public open val cookies: Cookies = emptySet()

    /**
     * List of decorators to be applied to SearchContext objects (WebDriver or WebElement).
     * Decorators can add behavior like highlighting or slow motion to Selenium operations.
     */
    public open val decorators: List<AbstractDecorator> = emptyList()

    /**
     * A predicate that determines when the found element is considered ready for use.
     */
    public open val elementReadyCondition: WebElement.() -> Boolean = { isDisplayed }

    /**
     * A predicate that determines when the found elements are considered ready for use.
     */
    public open val elementsReadyCondition: WebElements.() -> Boolean = { isDisplayed }

    /**
     * The wait configuration to use in synchronization operations.
     */
    public open val waitConfig: WaitConfig = Default

    /**
     * Perform site‑specific driver configuration.
     *
     * Called by the DSL when a driver is created and whenever a test switches to this [Site].
     * Typical use cases include window sizing, timeouts, or adding browser‑specific options.
     *
     * @param driver The active [WebDriver] for the current test session.
     */
    public open fun configureDriver(driver: WebDriver) {
    }
}

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

package dev.kolibrium.selenium.core

import org.openqa.selenium.By
import org.openqa.selenium.SearchContext
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement

/**
 * Base type for page objects bound to a [SeleniumSite].
 *
 * Page instances rely on a contextual [WebDriver] installed by Kolibrium's DSL.
 * Constructing and using pages outside those contexts will fail with a runtime
 * error. Delegated findElement(s) calls route through the contextual driver.
 */
public abstract class SeleniumPage<S : SeleniumSite> protected constructor() : SearchContext {
    /**
     * Relative path of this page within the [SeleniumSite]. Defaults to empty string (no navigation).
     * Any non-empty value causes the [dev.kolibrium.selenium.dsl.SiteScope.on] function to navigate to `baseUrl + path`.
     */
    public open val path: String = ""

    /** The current URL as reported by the driver. */
    protected val currentUrl: String
        get() = requireDriver().currentUrl!!

    /** The current page title as reported by the driver. */
    protected val pageTitle: String
        get() = requireDriver().title!!

    /**
     * Wait until the page is considered ready for interaction.
     * Default is a no-op; subclasses may override.
     */
    public open fun awaitReady() {}

    /**
     * Optional post-ready assertions to verify invariants.
     */
    public open fun assertReady() {}

    /**
     * Provides direct access to the underlying [WebDriver] for advanced operations.
     * Prefer using the DSL and locator delegates where possible.
     */
    protected val driver: WebDriver
        get() = requireDriver()

    override fun findElement(by: By): WebElement = requireDriver().findElement(by)

    override fun findElements(by: By): List<WebElement> = requireDriver().findElements(by)

    private fun requireDriver(): WebDriver =
        try {
            requireDriver("Page operation")
        } catch (_: IllegalStateException) {
            error(sessionNotAttachedMessage())
        }

    private fun sessionNotAttachedMessage(): String {
        val pageName = this::class.qualifiedName ?: this::class.simpleName ?: "<unknown page>"
        return (
            "Kolibrium runtime error: Page '$pageName' has no active WebDriver context.\n" +
                "You likely constructed this page directly or are calling it outside Kolibrium DSL.\n\n" +
                "How to fix:\n" +
                "- Run page interactions inside Kolibrium DSL (e.g., seleniumTest/on)\n"
        )
    }
}

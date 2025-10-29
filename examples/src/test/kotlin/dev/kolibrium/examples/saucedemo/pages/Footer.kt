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

package dev.kolibrium.examples.saucedemo.pages

import dev.kolibrium.core.selenium.Page
import dev.kolibrium.core.selenium.dataTest
import dev.kolibrium.examples.saucedemo.SauceDemo

/**
 * Marker interface for pages that include the shared footer component.
 *
 * Why a marker?
 * - We only want footer-related extension functions (e.g., visitTwitter) to be available
 *   on pages that actually have the footer. This improves compile-time safety and API
 *   discoverability, keeping invalid calls out of IntelliSense and preventing misuse.
 *
 * Usage:
 * - Implement this on any Page Object that renders the standard Sauce Demo footer.
 * - The footer navigation helpers will then be available as extensions on that page type.
 */
interface HasFooter

/**
 * Clicks the Twitter icon in the shared footer and returns the same page object.
 *
 * POM rationale:
 * - Methods on Page Objects return Page Objects. As this action does not navigate within the AUT
 *   (it opens an external link, typically in a new tab), we return the current page instance.
 *
 * Design details:
 * - Uses a generic receiver [P] constrained by [HasFooter] so the function is only available
 *   on pages that actually render the footer. This keeps call sites safe and expressive.
 * - Implemented with Kotlin's `apply`, which executes the block and returns the receiver (`this`),
 *   preserving the concrete page type through fluent chains.
 * - Relies on the context parameter `page: Page<SauceDemo>` to access locators (no need
 *   to pass WebDriver explicitly), per Kolibrium guidelines.
 *
 * Behavior notes:
 * - Does not switch the browser context to the external tab/window. Tests typically remain focused
 *   on the AUT. Provide alternative APIs if you want to switch windows or model the external page.
 *
 * Example:
 * ```kotlin
 * // Given InventoryPage implements HasFooter
 * inventoryPage
 *   .visitTwitter()  // remains on InventoryPage
 *   .someNextAction()
 * ```
 */
context(page: Page<SauceDemo>)
fun <P> P.visitTwitter(): P where P : Any, P : HasFooter =
    apply {
        val twitterLogo by page.dataTest("social-twitter")
        twitterLogo.click()
    }

/**
 * Clicks the Facebook icon in the shared footer and returns the same page object.
 *
 * Mirrors the rationale used by [visitTwitter]:
 * - Returns the current page (`P`) via `apply` to keep fluent chains and reflect no in-app navigation.
 * - Available only on pages implementing [HasFooter].
 * - Uses the `page` context parameter to resolve the data-test locator.
 *
 * If you ever decide to switch to the new window or model the external site, expose a
 * separate API that returns the appropriate type (e.g., a window handle or a Page Object).
 */
context(page: Page<SauceDemo>)
fun <P> P.visitFacebook(): P where P : Any, P : HasFooter =
    apply {
        val facebookLogo by page.dataTest("social-facebook")
        facebookLogo.click()
    }

/**
 * Clicks the LinkedIn icon in the shared footer and returns the same page object.
 *
 * Consistent with the Page Object guideline, this returns the current page (`P`) because
 * clicking a social icon navigates outside the AUT and we typically continue testing the AUT.
 */
context(page: Page<SauceDemo>)
fun <P> P.visitLinkedIn(): P where P : Any, P : HasFooter =
    apply {
        val linkedInLogo by page.dataTest("social-linkedin")
        linkedInLogo.click()
    }

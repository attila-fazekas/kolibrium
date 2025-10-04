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

package dev.kolibrium.dsl.selenium.webtest.saucedemo.pages

import dev.kolibrium.core.selenium.Page
import dev.kolibrium.core.selenium.dataTest
import dev.kolibrium.dsl.selenium.webtest.saucedemo.SauceDemo

// Return the current page so callers can continue fluent chains that expect a Page
context(page: Page<SauceDemo>)
fun visitTwitter(): InventoryPage {
    val twitterLogo by page.dataTest("social-twitter")
    twitterLogo.click()
    // We know callers use this from InventoryPage in tests; return it for chaining
    return page as InventoryPage
}

context(page: Page<SauceDemo>)
fun visitFacebook() {
    val facebookLogo by page.dataTest("social-facebook")
    facebookLogo.click()
}

context(page: Page<SauceDemo>)
fun visitLinkedIn() {
    val linkedInLogo by page.dataTest("social-linkedin")
    linkedInLogo.click()
}

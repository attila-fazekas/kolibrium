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

package dev.kolibrium.selenium.dsl.seleniumTest.saucedemo.pages

import dev.kolibrium.selenium.core.SeleniumPage
import dev.kolibrium.selenium.core.dataTest
import dev.kolibrium.selenium.dsl.seleniumTest.saucedemo.SauceDemo

// Return the current page so callers can continue fluent chains that expect a Page
context(page: SeleniumPage<SauceDemo>)
fun visitTwitter() {
    val twitterLogo by page.dataTest("social-twitter")
    twitterLogo.click()
}

context(page: SeleniumPage<SauceDemo>)
fun visitFacebook() {
    val facebookLogo by page.dataTest("social-facebook")
    facebookLogo.click()
}

context(page: SeleniumPage<SauceDemo>)
fun visitLinkedIn() {
    val linkedInLogo by page.dataTest("social-linkedin")
    linkedInLogo.click()
}

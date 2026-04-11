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

package dev.kolibrium.playwright

import com.microsoft.playwright.Locator
import com.microsoft.playwright.assertions.PlaywrightAssertions

/**
 * Asserts that the current locator is visible within the DOM.
 *
 * This method ensures the element matches the visible state, meaning it must
 * be present in the DOM and not hidden (e.g., via `display: none` or `visibility: hidden`).
 *
 * Throws an assertion error if the locator is not visible.
 */
public fun Locator.shouldBeVisible() {
    PlaywrightAssertions.assertThat(this).isVisible()
}

/**
 * Asserts that the locator contains the specified text.
 *
 * @param text The text that the locator is expected to have.
 */
public fun Locator.shouldHaveText(text: String) {
    PlaywrightAssertions.assertThat(this).hasText(text)
}

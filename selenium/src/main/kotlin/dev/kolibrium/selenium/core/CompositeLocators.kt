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

package dev.kolibrium.selenium.core

import org.openqa.selenium.By
import org.openqa.selenium.support.pagefactory.ByAll
import org.openqa.selenium.support.pagefactory.ByChained

/**
 * Creates a chained locator that finds elements by applying the given locators sequentially.
 *
 * Each locator in the chain is applied to the result of the previous locator, effectively
 * narrowing down the search with each step. This is useful for locating elements that are
 * nested within other elements.
 *
 * @param locators the locators to chain together
 * @return a [By] instance that represents the chained locator
 */
public fun chained(vararg locators: By): By = ByChained(*locators)

/**
 * Creates a composite locator that finds elements matching any of the given locators.
 *
 * The resulting locator will return elements that match at least one of the provided locators.
 * This is useful when an element can be located using different strategies depending on the
 * page state or context.
 *
 * @param locators the locators to try
 * @return a [By] instance that represents the any-of locator
 */
public fun anyOf(vararg locators: By): By = ByAll(*locators)

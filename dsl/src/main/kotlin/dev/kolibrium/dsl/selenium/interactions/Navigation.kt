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

package dev.kolibrium.dsl.selenium.interactions

import dev.kolibrium.dsl.selenium.creation.KolibriumDsl
import org.openqa.selenium.WebDriver

/**
 * Navigates to a relative path from the current URL.
 *
 * @receiver The [WebDriver] instance to perform navigation with.
 * @param relativePath The relative path to navigate to, which will be appended to the current URL.
 */
@KolibriumDsl
public fun WebDriver.navigateTo(relativePath: String): Unit = get("$currentUrl$relativePath")

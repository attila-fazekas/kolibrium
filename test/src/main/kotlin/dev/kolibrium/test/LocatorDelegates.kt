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

package dev.kolibrium.test

import dev.kolibrium.core.WebElements
import dev.kolibrium.dsl.selenium.wait.SyncConfig
import dev.kolibrium.selenium.xpath
import dev.kolibrium.selenium.xpaths
import org.openqa.selenium.SearchContext
import org.openqa.selenium.WebElement

fun SearchContext.dataTest(
    locator: String,
    cacheLookup: Boolean = true,
    syncConfig: (SyncConfig<WebElement>.() -> Unit) = {},
) = xpath("//*[@data-test='$locator']", cacheLookup, syncConfig)

fun SearchContext.dataTests(
    locator: String,
    cacheLookup: Boolean = true,
    syncConfig: (SyncConfig<WebElements>.() -> Unit) = {},
) = xpaths("//*[@data-test='$locator']", cacheLookup, syncConfig)
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

package dev.kolibrium.selenium

import dev.kolibrium.core.InternalKolibriumApi
import dev.kolibrium.core.WebElements
import dev.kolibrium.dsl.selenium.wait.WaitScope
import org.openqa.selenium.SearchContext
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ui.FluentWait
import kotlin.time.toJavaDuration

public val WebElement.clickable: Boolean
    get() = isDisplayed && isEnabled

public val WebElements.isDisplayed: Boolean
    get() = all { it.isDisplayed }

@OptIn(InternalKolibriumApi::class)
internal fun setUpWait(
    driver: SearchContext,
    waitScope: WaitScope,
) = FluentWait(driver).apply {
    with(waitScope) {
        timeout?.let { withTimeout(it.toJavaDuration()) }
        pollingInterval?.let { pollingEvery(it.toJavaDuration()) }
        message?.let { withMessage { it } }
        if (ignoringScope.exceptions.isNotEmpty()) {
            ignoreAll(ignoringScope.exceptions as Collection<Class<out Throwable>>)
        }
    }
}

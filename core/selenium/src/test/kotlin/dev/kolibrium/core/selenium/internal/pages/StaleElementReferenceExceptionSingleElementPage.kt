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

package dev.kolibrium.core.selenium.internal.pages

import dev.kolibrium.core.selenium.Page
import dev.kolibrium.core.selenium.className
import dev.kolibrium.core.selenium.id
import org.openqa.selenium.WebDriver

class StaleElementReferenceExceptionSingleElementPage(
    driver: WebDriver,
) : Page(driver) {
    val button by id("myButton") {
        isEnabled
    }

    val firework by className("firework")
}

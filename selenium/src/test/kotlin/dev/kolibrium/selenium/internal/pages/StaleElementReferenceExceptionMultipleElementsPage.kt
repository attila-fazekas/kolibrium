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

package dev.kolibrium.selenium.internal.pages

import dev.kolibrium.selenium.className
import dev.kolibrium.selenium.classNames
import dev.kolibrium.selenium.isEnabled
import org.openqa.selenium.WebDriver

context(WebDriver)
class StaleElementReferenceExceptionMultipleElementsPage {
    val buttons by classNames("grid-button") {
        isEnabled
    }

    val firework by className("firework")
}

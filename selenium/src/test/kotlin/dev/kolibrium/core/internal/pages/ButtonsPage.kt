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

package dev.kolibrium.core.internal.pages

import dev.kolibrium.core.KPage
import dev.kolibrium.core.id
import dev.kolibrium.core.isClickable
import org.openqa.selenium.WebDriver

class ButtonsPage(
    driver: WebDriver,
) : KPage(driver) {
    val button1 by id("button1") {
        isClickable
    }

    val button2 by id("button2") {
        isClickable
    }

    val button3 by id("button3") {
        isClickable
    }

    val button4 by id("button4") {
        isClickable
    }

    val result by id("result")
}

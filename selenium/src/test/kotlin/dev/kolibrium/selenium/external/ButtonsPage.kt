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

package dev.kolibrium.selenium.external

import dev.kolibrium.selenium.cssSelector
import dev.kolibrium.selenium.id
import org.openqa.selenium.WebDriver

context(WebDriver)
class ButtonsPage {
    val easyButton1 by cssSelector("#easy00")

    val easyButton2 by cssSelector("#easy01")

    val easyButton3 by cssSelector("#easy02")

    val easyButton4 by cssSelector("#easy03")

    val easyMessage by id("easybuttonmessage")

    val hardButton1 by cssSelector("#button00") {
        until = { isEnabled }
    }

    val hardButton2 by cssSelector("#button01") {
        until = { isEnabled }
    }

    val hardButton3 by cssSelector("#button02") {
        until = { isEnabled }
    }

    val hardButton4 by cssSelector("#button03") {
        until = { isEnabled }
    }

    val hardMessage by id("buttonmessage")
}

/*
 * Copyright 2023 Attila Fazekas
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

package io.kolibrium.selenium.external

import io.kolibrium.selenium.css
import io.kolibrium.selenium.id
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement

context(WebDriver)
class ButtonsPage {
    val easyButton1 by css<WebElement>("#easy00")
    val easyButton2 by css<WebElement>("#easy01")
    val easyButton3 by css<WebElement>("#easy02")
    val easyButton4 by css<WebElement>("#easy03")
    val easyMessage by id<WebElement>("easybuttonmessage")

    val hardButton1 by css<WebElement>("#button00") {
        it.isEnabled
    }

    val hardButton2 by css<WebElement>("#button01") {
        it.isEnabled
    }

    val hardButton3 by css<WebElement>("#button02") {
        it.isEnabled
    }

    val hardButton4 by css<WebElement>("#button03") {
        it.isEnabled
    }

    val hardMessage by id<WebElement>("buttonmessage")
}

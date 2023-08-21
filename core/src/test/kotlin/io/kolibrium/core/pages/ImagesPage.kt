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

package io.kolibrium.core.pages

import io.kolibrium.core.WebElements
import io.kolibrium.core.getValueOrFail
import io.kolibrium.core.name
import org.openqa.selenium.WebDriver
import org.openqa.selenium.support.ui.ExpectedConditions

context(WebDriver)
class ImagesPage {
    private val _images by name<WebElements>("kodee", 9, ExpectedConditions::numberOfElementsToBe)
    val images: WebElements
        get() = _images.getValueOrFail()
}

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

package dev.kolibrium.selenium.configuration

import dev.kolibrium.core.WebElements
import dev.kolibrium.selenium.Wait
import dev.kolibrium.selenium.Wait.Companion.DEFAULT
import dev.kolibrium.selenium.decorators.AbstractDecorator
import dev.kolibrium.selenium.isDisplayed
import org.openqa.selenium.WebElement

internal object DefaultSeleniumProjectConfiguration : AbstractSeleniumProjectConfiguration() {
    override var elementReadinessCondition: (WebElement.() -> Boolean) = { isDisplayed }

    override var elementsReadinessCondition: (WebElements.() -> Boolean) = { isDisplayed }

    override var wait: Wait = DEFAULT

    override var decorators: List<AbstractDecorator> = emptyList()
}

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

package dev.kolibrium.selenium.internal.pages

import dev.kolibrium.selenium.className
import dev.kolibrium.selenium.classNames
import dev.kolibrium.selenium.cssSelector
import dev.kolibrium.selenium.cssSelectors
import dev.kolibrium.selenium.id
import dev.kolibrium.selenium.idOrName
import dev.kolibrium.selenium.linkText
import dev.kolibrium.selenium.linkTexts
import dev.kolibrium.selenium.name
import dev.kolibrium.selenium.names
import dev.kolibrium.selenium.partialLinkText
import dev.kolibrium.selenium.partialLinkTexts
import dev.kolibrium.selenium.tagName
import dev.kolibrium.selenium.tagNames
import dev.kolibrium.selenium.xPath
import dev.kolibrium.selenium.xPaths
import org.openqa.selenium.WebDriver

context(WebDriver)
class TutorialPage {
    val className by className("by-class-name")
    val classNames by classNames("multiple")

    val cssSelector by cssSelector("[data-test='css-selector']")
    val cssSelectors by cssSelectors("[data-test='multiple']")

    val id by id("by-id")

    val idOrName by idOrName("by-name")

    val linkText by linkText("Locate by Link Text")
    val linkTexts by linkTexts("Multiple Locate by Link Text")

    val name by name("by-name")
    val names by names("multiple-by-name")

    val partialLinkText by partialLinkText("Partial Link Text Example")
    val partialLinkTexts by partialLinkTexts("Partial Link Example")

    val tagName by tagName("p")
    val tagNames by tagNames("span")

    val xPath by xPath("//div[@id='by-xpath']")
    val xPaths by xPaths("//div[@class='multiple-by-xpath']")

    val singleLocators by id("single-locators")
    val multipleLocators by id("multiple-locators")
}

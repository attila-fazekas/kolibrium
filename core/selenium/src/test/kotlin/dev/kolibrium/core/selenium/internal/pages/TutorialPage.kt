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

import dev.kolibrium.core.selenium.KPage
import dev.kolibrium.core.selenium.className
import dev.kolibrium.core.selenium.classNames
import dev.kolibrium.core.selenium.cssSelector
import dev.kolibrium.core.selenium.cssSelectors
import dev.kolibrium.core.selenium.id
import dev.kolibrium.core.selenium.idOrName
import dev.kolibrium.core.selenium.linkText
import dev.kolibrium.core.selenium.linkTexts
import dev.kolibrium.core.selenium.name
import dev.kolibrium.core.selenium.names
import dev.kolibrium.core.selenium.partialLinkText
import dev.kolibrium.core.selenium.partialLinkTexts
import dev.kolibrium.core.selenium.tagName
import dev.kolibrium.core.selenium.tagNames
import dev.kolibrium.core.selenium.xpath
import dev.kolibrium.core.selenium.xpaths
import org.openqa.selenium.WebDriver

class TutorialPage(
    driver: WebDriver,
) : KPage(driver) {
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

    val xpath by xpath("//div[@id='by-xpath']")
    val xpaths by xpaths("//div[@class='multiple-by-xpath']")

    val singleLocators by id("single-locators")
    val multipleLocators by id("multiple-locators")
}

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
import dev.kolibrium.core.selenium.xPath
import dev.kolibrium.core.selenium.xPaths
import org.openqa.selenium.WebDriver

context(WebDriver)
class HomePage {
    val header by className("header")

    val name by cssSelector("#name")

    val nameXPath by xPath("//*[@id='name']")

    val email by idOrName("email")

    val phone by id("phone")

    val phoneName by name("phone")

    val googleLink by xPath("//a[@class='link'][contains(text(),'Google')]")

    val fbLink by linkText("Facebook")

    val twitterLink by linkText("Twitter")

    val clickHereLink by partialLinkText("Click")

    val h1TagName by tagName("h1")

    val links by classNames("link")

    val linksTagName by tagNames("a")

    val linksCss by cssSelectors("a")

    val linksXPath by xPaths("//a[@class='link']")

    val fbLinks by linkTexts("Facebook")

    val fbPartialLinks by partialLinkTexts("Facebook")

    val selects by names("select")
}

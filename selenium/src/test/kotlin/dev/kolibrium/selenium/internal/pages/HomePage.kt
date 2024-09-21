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
import dev.kolibrium.selenium.xpath
import dev.kolibrium.selenium.xpaths
import org.openqa.selenium.WebDriver

context(WebDriver)
class HomePage {
    val header by className("header")

    val name by cssSelector("#name")

    val nameXpath by xpath("//*[@id='name']")

    val email by idOrName("email")

    val phone by id("phone")

    val phoneName by name("phone")

    val googleLink by xpath("//a[@class='link'][contains(text(),'Google')]")

    val fbLink by linkText("Facebook")

    val twitterLink by linkText("Twitter")

    val clickHereLink by partialLinkText("Click")

    val a1TagName by tagName("h1")

    val links by classNames("link")

    val linksTagName by tagNames("a")

    val linksCss by cssSelectors("a")

    val linksXpath by xpaths("//a[@class='link']")

    val fbLinks by linkTexts("Facebook")

    val fbPartialLinks by partialLinkTexts("Facebook")

    val selects by names("select")
}

/*
 * Copyright 2023-2026 Attila Fazekas & contributors
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

package dev.kolibrium.selenium.core.internal.pages

import dev.kolibrium.selenium.core.KPage
import dev.kolibrium.selenium.core.className
import dev.kolibrium.selenium.core.classNames
import dev.kolibrium.selenium.core.cssSelector
import dev.kolibrium.selenium.core.cssSelectors
import dev.kolibrium.selenium.core.id
import dev.kolibrium.selenium.core.idOrName
import dev.kolibrium.selenium.core.linkText
import dev.kolibrium.selenium.core.linkTexts
import dev.kolibrium.selenium.core.name
import dev.kolibrium.selenium.core.partialLinkText
import dev.kolibrium.selenium.core.partialLinkTexts
import dev.kolibrium.selenium.core.tagName
import dev.kolibrium.selenium.core.tagNames
import dev.kolibrium.selenium.core.xpath
import dev.kolibrium.selenium.core.xpaths
import org.openqa.selenium.WebDriver

class HomePage(
    driver: WebDriver,
) : dev.kolibrium.selenium.core.KPage(driver) {
    val header by className("header")

    val name by cssSelector("#name")

    val nameXPath by xpath("//*[@id='name']")

    val email by idOrName("email")

    val phone by id("phone")

    val phoneName by name("phone")

    val googleLink by xpath("//a[@class='link'][contains(text(),'Google')]")

    val fbLink by linkText("Facebook")

    val twitterLink by linkText("Twitter")

    val clickHereLink by partialLinkText("Click")

    val h1TagName by tagName("h1")

    val links by classNames("link")

    val linksTagName by tagNames("a")

    val linksCss by cssSelectors("a")

    val linksXPath by xpaths("//a[@class='link']")

    val fbLinks by linkTexts("Facebook")

    val fbPartialLinks by partialLinkTexts("Facebook")

    val selects by tagNames("select", readyWhen = { true })
}

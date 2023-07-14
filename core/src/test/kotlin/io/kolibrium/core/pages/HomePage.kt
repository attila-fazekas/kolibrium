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
import io.kolibrium.core.className
import io.kolibrium.core.css
import io.kolibrium.core.id
import io.kolibrium.core.idOrName
import io.kolibrium.core.linkText
import io.kolibrium.core.name
import io.kolibrium.core.partialLinkText
import io.kolibrium.core.tagName
import io.kolibrium.core.xpath
import org.openqa.selenium.WebElement

context(org.openqa.selenium.WebDriver)
class HomePage {
    val header by className<WebElement>("header")
    val name by css<WebElement>("#name")
    val nameXpath by xpath<WebElement>("//*[@id='name']")
    val email by idOrName<WebElement>("email")
    val phone by id<WebElement>("phone")
    val phoneName by name<WebElement>("phone")
    val googleLink by xpath<WebElement>("//a[@class='link'][contains(text(),'Google')]")
    val fbLink by linkText<WebElement>("Facebook")
    val twitterLink by linkText<WebElement>("Twitter")
    val clickHereLink by partialLinkText<WebElement>("Click")
    val a1TagName by tagName<WebElement>("h1")
    val links by className<WebElements>("link")
    val linksTagname by tagName<WebElements>("a")
    val linksCss by css<WebElements>("a")
    val linksXpath by xpath<WebElements>("//a[@class='link']")
    val fbLinks by linkText<WebElements>("Facebook")
    val fbPartialLinks by partialLinkText<WebElements>("Facebook")
    val selects by name<WebElements>("select")
}

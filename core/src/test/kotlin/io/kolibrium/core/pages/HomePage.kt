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
import io.kolibrium.core.getValueOrFail
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
    private val _header by className<WebElement>("header")
    val header: WebElement
        get() = getValueOrFail(_header)

    private val _name by css<WebElement>("#name")
    val name: WebElement
        get() = getValueOrFail(_name)

    private val _nameXpath by xpath<WebElement>("//*[@id='name']")
    val nameXpath: WebElement
        get() = getValueOrFail(_nameXpath)

    private val _email by idOrName<WebElement>("email")
    val email: WebElement
        get() = getValueOrFail(_email)

    private val _phone by id<WebElement>("phone")
    val phone: WebElement
        get() = getValueOrFail(_phone)

    private val _phoneName by name<WebElement>("phone")
    val phoneName: WebElement
        get() = getValueOrFail(_phoneName)

    private val _googleLink by xpath<WebElement>("//a[@class='link'][contains(text(),'Google')]")
    val googleLink: WebElement
        get() = getValueOrFail(_googleLink)

    private val _fbLink by linkText<WebElement>("Facebook")
    val fbLink: WebElement
        get() = getValueOrFail(_fbLink)

    private val _twitterLink by linkText<WebElement>("Twitter")
    val twitterLink: WebElement
        get() = getValueOrFail(_twitterLink)

    private val _clickHereLink by partialLinkText<WebElement>("Click")
    val clickHereLink: WebElement
        get() = getValueOrFail(_clickHereLink)

    private val _a1TagName by tagName<WebElement>("h1")
    val a1TagName: WebElement
        get() = getValueOrFail(_a1TagName)

    private val _links by className<WebElements>("link")
    val links: WebElements
        get() = getValueOrFail(_links)

    private val _linksTagName by tagName<WebElements>("a")
    val linksTagName: WebElements
        get() = getValueOrFail(_linksTagName)

    private val _linksCss by css<WebElements>("a")
    val linksCss: WebElements
        get() = getValueOrFail(_linksCss)

    private val _linksXpath by xpath<WebElements>("//a[@class='link']")
    val linksXpath: WebElements
        get() = getValueOrFail(_linksXpath)

    private val _fbLinks by linkText<WebElements>("Facebook")
    val fbLinks: WebElements
        get() = getValueOrFail(_fbLinks)

    private val _fbPartialLinks by partialLinkText<WebElements>("Facebook")
    val fbPartialLinks: WebElements
        get() = getValueOrFail(_fbPartialLinks)

    private val _selects by name<WebElements>("select")
    val selects: WebElements
        get() = getValueOrFail(_selects)
}

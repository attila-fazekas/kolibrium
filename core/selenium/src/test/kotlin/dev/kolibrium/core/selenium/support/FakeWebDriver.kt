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

package dev.kolibrium.core.selenium.support

import org.openqa.selenium.By
import org.openqa.selenium.OutputType
import org.openqa.selenium.SearchContext
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement

class FakeWebDriver : WebDriver {
    var currentUrlValue: String = "about:blank"
    var titleValue: String = ""

    var refreshCount: Int = 0
    var backCount: Int = 0
    var forwardCount: Int = 0
    val getCalls: MutableList<String> = mutableListOf()
    val findElementCalls: MutableList<By> = mutableListOf()

    private val navigation =
        object : WebDriver.Navigation {
            override fun back() {
                backCount++
            }

            override fun forward() {
                forwardCount++
            }

            override fun to(url: String) {
                get(url)
            }

            override fun to(url: java.net.URL) {
                get(url.toString())
            }

            override fun refresh() {
                refreshCount++
            }
        }

    override fun get(url: String) {
        currentUrlValue = url
        getCalls += url
    }

    override fun getCurrentUrl(): String = currentUrlValue

    override fun getTitle(): String = titleValue

    override fun findElements(by: By): MutableList<WebElement> = mutableListOf()

    override fun findElement(by: By): WebElement {
        findElementCalls += by
        return object : WebElement {
            override fun click() = Unit

            override fun submit() = Unit

            override fun sendKeys(vararg keysToSend: CharSequence) = Unit

            override fun clear() = Unit

            override fun getTagName(): String = "div"

            override fun getAttribute(name: String): String? = null

            override fun isSelected(): Boolean = false

            override fun isEnabled(): Boolean = true

            override fun getText(): String = ""

            override fun findElements(by: By): MutableList<WebElement> = mutableListOf()

            override fun findElement(by: By): WebElement = this

            override fun isDisplayed(): Boolean = true

            override fun getLocation(): org.openqa.selenium.Point = org.openqa.selenium.Point(0, 0)

            override fun getSize(): org.openqa.selenium.Dimension = org.openqa.selenium.Dimension(0, 0)

            override fun getRect(): org.openqa.selenium.Rectangle = org.openqa.selenium.Rectangle(0, 0, 0, 0)

            override fun getCssValue(propertyName: String): String = ""

            override fun getDomAttribute(name: String): String? = null

            override fun getDomProperty(name: String): String? = null

            override fun getAriaRole(): String? = null

            override fun getAccessibleName(): String = ""

            override fun getShadowRoot(): SearchContext = throw UnsupportedOperationException("not used in unit tests")

            override fun <X : Any> getScreenshotAs(target: OutputType<X>): X = throw UnsupportedOperationException("not used in unit tests")
        }
    }

    override fun getPageSource(): String = ""

    override fun close() {}

    override fun quit() {}

    override fun getWindowHandles(): MutableSet<String> = mutableSetOf("win")

    override fun getWindowHandle(): String = "win"

    override fun switchTo(): WebDriver.TargetLocator = throw UnsupportedOperationException("not used in unit tests")

    override fun navigate(): WebDriver.Navigation = navigation

    override fun manage(): WebDriver.Options = throw UnsupportedOperationException("not used in unit tests")
}

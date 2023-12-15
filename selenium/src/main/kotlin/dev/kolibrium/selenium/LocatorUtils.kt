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

package dev.kolibrium.selenium

import org.openqa.selenium.ElementNotInteractableException
import org.openqa.selenium.NoSuchElementException
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ui.FluentWait
import java.time.Duration

public typealias WebElements = List<WebElement>

public val WebElement.clickable: Boolean
    get() = isDisplayed && isEnabled

public val WebElements.isDisplayed: Boolean
    get() = all { it.isDisplayed }

private const val TIMEOUT: Long = 10
private const val POOLING_INTERVAL: Long = 1

internal fun setUpWait(driver: WebDriver) = FluentWait(driver)
    .withTimeout(Duration.ofSeconds(TIMEOUT))
    .pollingEvery(Duration.ofSeconds(POOLING_INTERVAL))
    .ignoreAll(
        listOf(
            NoSuchElementException::class.java,
            ElementNotInteractableException::class.java
        )
    )

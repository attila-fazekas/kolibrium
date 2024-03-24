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

package dev.kolibrium.dsl.selenium.wait

import dev.kolibrium.core.InternalKolibriumApi
import dev.kolibrium.core.WebElements
import dev.kolibrium.dsl.selenium.creation.KolibriumDsl
import dev.kolibrium.dsl.selenium.creation.KolibriumPropertyDsl
import org.openqa.selenium.NoSuchElementException
import org.openqa.selenium.StaleElementReferenceException
import org.openqa.selenium.WebElement
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@InternalKolibriumApi
public sealed class InternalSynchronization(
    @KolibriumPropertyDsl
    public var wait: WaitScope = defaultWait,
)

@KolibriumDsl
public class Synchronization : InternalSynchronization() {
    @KolibriumPropertyDsl
    public var until: (WebElement.() -> Boolean) = { isDisplayed }
}

@KolibriumDsl
public class Synchronizations : InternalSynchronization() {
    @KolibriumPropertyDsl
    public var until: (WebElements.() -> Boolean) = { all { it.isDisplayed } }
}

@KolibriumDsl
public class WaitScope {
    @InternalKolibriumApi
    public val ignoringScope: IgnoringScope by lazy { IgnoringScope() }

    @KolibriumPropertyDsl
    public var pollingInterval: Duration? = null

    @KolibriumPropertyDsl
    public var timeout: Duration? = null

    @KolibriumPropertyDsl
    public var message: String? = null

    @KolibriumDsl
    public fun ignoring(block: IgnoringScope.() -> Unit): IgnoringScope = ignoringScope.apply(block)
}

@InternalKolibriumApi
public val defaultWait: WaitScope =
    wait {
        timeout = 10.seconds
        pollingInterval = 200.milliseconds
        message = "Element could not be found"
        ignoring {
            +NoSuchElementException::class
            +StaleElementReferenceException::class
        }
    }

@KolibriumDsl
public fun wait(block: WaitScope.() -> Unit): WaitScope {
    return WaitScope().apply(block)
}

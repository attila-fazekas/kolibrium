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

public sealed class SyncConfig<T> {
    @KolibriumPropertyDsl
    @OptIn(InternalKolibriumApi::class)
    public var wait: WaitScope = defaultWait
    public abstract var until: T.() -> Boolean
}

@KolibriumDsl
public class WebElementSyncConfig : SyncConfig<WebElement>() {
    @KolibriumPropertyDsl
    override var until: WebElement.() -> Boolean = { isDisplayed }

    override fun toString(): String {
        return "WebElementSyncConfig(until=$until)"
    }
}

@KolibriumDsl
public class WebElementsSyncConfig : SyncConfig<WebElements>() {
    @KolibriumPropertyDsl
    override var until: WebElements.() -> Boolean = { all { it.isDisplayed } }

    override fun toString(): String {
        return "WebElementsSyncConfig(until=$until)"
    }
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

    override fun toString(): String {
        return "WaitScope(pollingInterval=$pollingInterval, timeout=$timeout, message=$message, " +
            "ignoringScope=$ignoringScope)"
    }
}

@InternalKolibriumApi
public val defaultWait: WaitScope =
    wait {
        pollingInterval = 200.milliseconds
        timeout = 10.seconds
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

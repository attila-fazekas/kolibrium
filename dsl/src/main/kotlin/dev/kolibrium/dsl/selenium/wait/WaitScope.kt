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

/**
 * Sealed class that provides a base configuration for synchronization.
 *
 * @param T The type of the object for which synchronization conditions are defined.
 */
public sealed class SyncConfig<T> {
    /**
     * The wait configuration for synchronization, defined using a [WaitScope].
     *
     * This property is initialized with a default wait configuration.
     */
    @KolibriumPropertyDsl
    @OptIn(InternalKolibriumApi::class)
    public var wait: WaitScope = defaultWait

    /**
     * An abstract property that defines the condition for synchronization.
     *
     * This lambda function receives the object of type [T] and returns a Boolean value indicating
     * whether the synchronization condition is met.
     */
    public abstract var until: T.() -> Boolean
}

/**
 * Synchronization configuration specifically for [WebElement].
 *
 * This class extends [SyncConfig] and provides a default condition for synchronization,
 * which is to wait until the [WebElement] is displayed.
 */
@KolibriumDsl
public class WebElementSyncConfig : SyncConfig<WebElement>() {
    /**
     * The condition that determines when the synchronization is complete.
     *
     * By default, the condition is that the [WebElement] is displayed.
     */
    @KolibriumPropertyDsl
    override var until: WebElement.() -> Boolean = { isDisplayed }

    /**
     * Returns a string representation of the [WebElementSyncConfig], primarily for debugging purposes.
     */
    override fun toString(): String = "WebElementSyncConfig(until=$until)"
}

/**
 * Synchronization configuration specifically for [WebElements].
 *
 * This class extends [SyncConfig] and provides a default condition for synchronization,
 * which is to wait until all the [WebElements] are displayed.
 */
@KolibriumDsl
public class WebElementsSyncConfig : SyncConfig<WebElements>() {
    /**
     * The condition that determines when the synchronization is complete.
     *
     * By default, the condition is that all the [WebElements] are displayed.
     */
    @KolibriumPropertyDsl
    override var until: WebElements.() -> Boolean = { all { it.isDisplayed } }

    /**
     * Returns a string representation of the [WebElementsSyncConfig], primarily for debugging purposes.
     */
    override fun toString(): String = "WebElementsSyncConfig(until=$until)"
}

/**
 * Scope class for configuring wait parameters in synchronization operations.
 */
@KolibriumDsl
public class WaitScope {
    /**
     * A lazy-initialized scope for defining exceptions to ignore during synchronization.
     */
    @InternalKolibriumApi
    public val ignoringScope: IgnoringScope by lazy { IgnoringScope() }

    /**
     * The duration between polling attempts when waiting for a condition.
     */
    @KolibriumPropertyDsl
    public var pollingInterval: Duration? = null

    /**
     * The maximum duration to wait for a condition to be met.
     */
    @KolibriumPropertyDsl
    public var timeout: Duration? = null

    /**
     * A custom message to be displayed if the wait condition is not met.
     */
    @KolibriumPropertyDsl
    public var message: String? = null

    /**
     * Configures which exceptions to ignore during waiting.
     *
     * @param block The configuration block that defines ignored exceptions.
     * @return The [IgnoringScope] used for configuring ignored exceptions.
     */
    @KolibriumDsl
    public fun ignoring(block: IgnoringScope.() -> Unit): IgnoringScope = ignoringScope.apply(block)

    /**
     * Returns a string representation of the [WaitScope], primarily for debugging purposes.
     */
    override fun toString(): String =
        "WaitScope(pollingInterval=$pollingInterval, timeout=$timeout, message=$message, " +
            "ignoringScope=$ignoringScope)"
}

/**
 * The default wait configuration for synchronization operations.
 *
 * This predefined [WaitScope] specifies default settings for polling interval, timeout,
 * message, and exceptions to ignore during synchronization.
 */
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

/**
 * Configures wait behaviors.
 *
 * @param block The configuration block for the [WaitScope].
 * @return The configured [WaitScope].
 */
@KolibriumDsl
public fun wait(block: WaitScope.() -> Unit): WaitScope = WaitScope().apply(block)

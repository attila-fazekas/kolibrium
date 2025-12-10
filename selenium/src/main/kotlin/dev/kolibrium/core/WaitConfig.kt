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

package dev.kolibrium.core

import org.openqa.selenium.support.ui.FluentWait
import java.time.Duration.ofMillis
import kotlin.reflect.KClass
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * General wait configuration used by synchronization helpers.
 *
 * Notes:
 * - Kolibrium’s locator delegates always ignore [org.openqa.selenium.NoSuchElementException] during waits
 *   (in addition to any classes you specify here) to avoid failing on the first miss.
 *
 * Examples:
 * - Start from a preset and tweak timeout
 *   val waitConfig = WaitConfig.Default.copy(timeout = 5.seconds)
 *
 * - Apply to a Selenium FluentWait
 *   org.openqa.selenium.support.ui.FluentWait(driver).configureWith(waitConfig)
 */
public class WaitConfig(
    /** The duration between polling attempts when waiting for a condition. */
    public var pollingInterval: Duration? = null,
    /** The maximum duration to wait for a condition to be met. */
    public var timeout: Duration? = null,
    /** A custom message to be displayed if the wait condition is not met. */
    public var message: String? = null,
    /** Exception classes that should be ignored while evaluating a condition. */
    public val ignoring: Set<KClass<out Throwable>> = emptySet(),
) {
    init {
        pollingInterval?.let {
            require(!it.isNegative()) { "pollingInterval must not be negative." }
            require(it >= 10.milliseconds) { "pollingInterval must be at least 10ms." }
        }
        timeout?.let {
            require(!it.isNegative()) { "timeout must not be negative." }
            require(it >= 100.milliseconds) { "timeout must be at least 100ms." }
        }
        if (pollingInterval != null && timeout != null) {
            require(pollingInterval!! <= timeout!!) {
                "pollingInterval ($pollingInterval) must not be greater than timeout ($timeout)."
            }
        }
    }

    /**
     * Create a modified copy of this configuration.
     *
     * Example:
     *   val longer = waitConfig.copy(timeout = 30.seconds)
     */
    public fun copy(
        pollingInterval: Duration? = this.pollingInterval,
        timeout: Duration? = this.timeout,
        message: String? = this.message,
        ignoring: Set<KClass<out Throwable>> = this.ignoring,
    ): WaitConfig = WaitConfig(pollingInterval, timeout, message, ignoring)

    /**
     * Preset configurations for common waiting scenarios.
     */
    public companion object {
        /**
         * Balanced preset suitable for most waits.
         */
        public val Default: WaitConfig =
            WaitConfig(
                pollingInterval = 200.milliseconds,
                timeout = 10.seconds,
                message = "Condition not met within timeout",
            )

        /**
         * Fast preset for short, eager waits.
         */
        public val Quick: WaitConfig =
            Default.copy(
                pollingInterval = 100.milliseconds,
                timeout = 2.seconds,
            )

        /**
         * Slower preset when operations may take longer (e.g., network-heavy flows).
         */
        public val Patient: WaitConfig =
            Default.copy(
                pollingInterval = 500.milliseconds,
                timeout = 30.seconds,
            )
    }
}

/**
 * Apply a common [WaitConfig] to a Selenium [FluentWait] instance.
 * Generic across receiver types so it can be reused for [org.openqa.selenium.WebDriver],
 * [org.openqa.selenium.WebElement] wrappers, etc.
 */
public fun <T> FluentWait<T>.configureWith(waitConfig: WaitConfig): FluentWait<T> =
    apply {
        waitConfig.timeout?.let { withTimeout(ofMillis(it.inWholeMilliseconds)) }
        waitConfig.pollingInterval?.let { pollingEvery(ofMillis(it.inWholeMilliseconds)) }
        waitConfig.message?.let { withMessage { it } }
        if (waitConfig.ignoring.isNotEmpty()) {
            ignoreAll(waitConfig.ignoring.map { it.java })
        }
    }

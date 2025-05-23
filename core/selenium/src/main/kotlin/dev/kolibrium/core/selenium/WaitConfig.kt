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

package dev.kolibrium.core.selenium

import org.openqa.selenium.NoSuchElementException
import kotlin.reflect.KClass
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Class for configuring wait parameters in synchronization operations.
 */
public class WaitConfig(
    /**
     * The duration between polling attempts when waiting for a condition.
     */
    public var pollingInterval: Duration? = null,
    /**
     * The maximum duration to wait for a condition to be met.
     */
    public var timeout: Duration? = null,
    /**
     * A custom message to be displayed if the wait condition is not met.
     */
    public var message: String? = null,
    /**
     * A list of exception classes that should be ignored during synchronization processes.
     */
    public val ignoring: List<KClass<out Throwable>> = emptyList(),
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

        pollingInterval?.let { polling ->
            timeout?.let { total ->
                require(polling <= total) {
                    "pollingInterval ($polling) must not be greater than timeout ($total)."
                }
            }
        }
    }

    /**
     * Creates a copy of this WaitConfig with modified parameters.
     *
     * @param pollingInterval The duration between consecutive condition checks.
     *        If null, uses the current configuration's polling interval.
     * @param timeout The maximum time to wait for the condition to be true.
     *        If null, uses the current configuration's timeout.
     * @param message The message to be included in the timeout exception if the condition is not met.
     *        If null, uses the current configuration's message.
     * @param ignoring A list of exception types to ignore during condition checks.
     *        Defaults to the current configuration's ignored exceptions.
     * @return A new WaitConfig instance with the specified parameters, using current values for any unspecified parameters.
     */
    public fun copy(
        pollingInterval: Duration? = this.pollingInterval,
        timeout: Duration? = this.timeout,
        message: String? = this.message,
        ignoring: List<KClass<out Throwable>> = this.ignoring,
    ): WaitConfig = WaitConfig(pollingInterval, timeout, message, ignoring)

    /**
     * Provides predefined wait configurations for common use cases.
     */
    public companion object {
        /**
         * Default wait configuration suitable for most web automation scenarios.
         */
        public val DEFAULT: WaitConfig =
            WaitConfig(
                pollingInterval = 200.milliseconds,
                timeout = 10.seconds,
                message = "Element could not be found",
                ignoring = listOf(NoSuchElementException::class),
            )

        /**
         * Fast wait configuration for responsive applications or when quick feedback is needed.
         * Inherits error handling from [DEFAULT] configuration with shorter intervals.
         */
        public val QUICK: WaitConfig =
            DEFAULT.copy(
                pollingInterval = 100.milliseconds,
                timeout = 2.seconds,
            )

        /**
         * Extended wait configuration for slower applications or operations that might take longer to complete.
         * Inherits error handling from [DEFAULT] configuration with longer intervals.
         */
        public val PATIENT: WaitConfig =
            DEFAULT.copy(
                pollingInterval = 500.milliseconds,
                timeout = 30.seconds,
            )
    }
}

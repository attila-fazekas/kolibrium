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

package dev.kolibrium.dsl.selenium.creation

import org.openqa.selenium.safari.SafariDriverService

/**
 * A scope for configuring Safari-specific driver service settings.
 *
 * This scope provides configuration options specific to SafariDriver, such as
 * logging and service port settings.
 *
 * @property builder The underlying SafariDriver service builder.
 */
@KolibriumDsl
public class SafariDriverServiceScope(
    override val builder: SafariDriverService.Builder,
) : DriverServiceScope() {
    /**
     * Enables or disables logging for the SafariDriver service.
     */
    @KolibriumPropertyDsl
    public var logging: Boolean? = null

    override fun configure() {
        super.configure()
        builder.apply {
            logging?.let { withLogging(it) }
        }
    }

    /**
     * Returns a string representation of the [SafariDriverServiceScope], primarily for debugging purposes.
     */
    override fun toString(): String =
        "SafariDriverServiceScope(environmentScope=$environmentScope, logging=$logging, port=$port, " +
            "timeout=$timeout)"
}

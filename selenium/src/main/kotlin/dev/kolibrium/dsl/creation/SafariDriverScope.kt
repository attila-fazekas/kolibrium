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

package dev.kolibrium.dsl.creation

import dev.kolibrium.dsl.KolibriumDsl
import org.openqa.selenium.safari.SafariDriverService
import org.openqa.selenium.safari.SafariOptions

/**
 * Scope class for configuring Safari-specific settings for Safari WebDriver.
 */
@KolibriumDsl
public class SafariDriverScope : DriverScope<SafariDriverServiceScope, SafariOptionsScope>() {
    override val driverServiceScope = SafariDriverServiceScope(SafariDriverService.Builder())
    override val optionsScope = SafariOptionsScope(SafariOptions())

    /**
     * Configures the Safari driver service.
     *
     * @param block The configuration block for Safari driver service.
     */
    @KolibriumDsl
    override fun driverService(block: SafariDriverServiceScope.() -> Unit) {
        driverServiceScope.apply {
            block()
            configure()
        }
    }

    /**
     * Configures the Safari-specific options.
     *
     * @param block The configuration block for Safari browser options.
     */
    @KolibriumDsl
    override fun options(block: SafariOptionsScope.() -> Unit) {
        optionsScope.apply {
            block()
            configure()
        }
    }

    /**
     * Returns a string representation of the [SafariDriverScope], primarily for debugging purposes.
     */
    override fun toString(): String = "SafariDriverScope(driverServiceScope=$driverServiceScope, optionsScope=$optionsScope)"
}

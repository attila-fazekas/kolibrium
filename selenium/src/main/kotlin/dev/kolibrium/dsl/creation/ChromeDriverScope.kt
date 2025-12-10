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
import org.openqa.selenium.chrome.ChromeDriverService
import org.openqa.selenium.chrome.ChromeOptions

/**
 * Scope class for configuring Chrome-specific settings for Chrome WebDriver.
 */
@KolibriumDsl
public class ChromeDriverScope : DriverScope<ChromeDriverServiceScope, ChromeOptionsScope>() {
    override val driverServiceScope = ChromeDriverServiceScope(ChromeDriverService.Builder())
    override val optionsScope = ChromeOptionsScope(ChromeOptions())

    /**
     * Configures the Chrome driver service.
     *
     * @param block The configuration block for Chrome driver service.
     */
    override fun driverService(block: ChromeDriverServiceScope.() -> Unit) {
        driverServiceScope.apply {
            block()
            configure()
        }
    }

    /**
     * Configures the Chrome-specific options.
     *
     * @param block The configuration block for Chrome browser options.
     */
    override fun options(block: ChromeOptionsScope.() -> Unit) {
        optionsScope.apply {
            block()
            configure()
        }
    }

    /**
     * Returns a string representation of the [ChromeDriverScope], primarily for debugging purposes.
     */
    override fun toString(): String = "ChromeDriverScope(driverServiceScope=$driverServiceScope, optionsScope=$optionsScope)"
}

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

package dev.kolibrium.dsl.selenium.creation

import dev.kolibrium.dsl.selenium.KolibriumDsl
import org.openqa.selenium.firefox.FirefoxOptions
import org.openqa.selenium.firefox.GeckoDriverService

/**
 * Scope class for configuring Firefox-specific settings for Firefox WebDriver.
 */
@KolibriumDsl
public class FirefoxDriverScope : DriverScope<GeckoDriverServiceScope, FirefoxOptionsScope>() {
    override val driverServiceScope = GeckoDriverServiceScope(GeckoDriverService.Builder())
    override val optionsScope = FirefoxOptionsScope(FirefoxOptions())

    /**
     * Configures the Gecko driver service.
     *
     * @param block The configuration block for Gecko driver service.
     */
    @KolibriumDsl
    override fun driverService(block: GeckoDriverServiceScope.() -> Unit) {
        driverServiceScope.apply {
            block()
            configure()
        }
    }

    /**
     * Configures the Firefox-specific options.
     *
     * @param block The configuration block for Firefox browser options.
     */
    @KolibriumDsl
    override fun options(block: FirefoxOptionsScope.() -> Unit) {
        optionsScope.apply {
            block()
            configure()
        }
    }

    /**
     * Returns a string representation of the [FirefoxDriverScope], primarily for debugging purposes.
     */
    override fun toString(): String = "FirefoxDriverScope(driverServiceScope=$driverServiceScope, optionsScope=$optionsScope)"
}

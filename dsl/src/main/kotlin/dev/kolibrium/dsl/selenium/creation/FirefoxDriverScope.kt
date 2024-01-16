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

package dev.kolibrium.dsl.selenium.creation

import org.openqa.selenium.firefox.FirefoxOptions
import org.openqa.selenium.firefox.GeckoDriverService

@KolibriumDsl
public class FirefoxDriverScope : DriverScope<GeckoDriverServiceScope, FirefoxOptionsScope>() {
    override val driverServiceScope = GeckoDriverServiceScope(GeckoDriverService.Builder())
    override val optionsScope = FirefoxOptionsScope(FirefoxOptions())

    @KolibriumDsl
    override fun driverService(block: GeckoDriverServiceScope.() -> Unit) {
        driverServiceScope.apply {
            block()
            configure()
        }
    }

    @KolibriumDsl
    override fun options(block: FirefoxOptionsScope.() -> Unit) {
        optionsScope.apply {
            block()
            configure()
        }
    }

    override fun toString(): String {
        return "FirefoxDriverScope(driverServiceScope=$driverServiceScope, optionsScope=$optionsScope)"
    }
}

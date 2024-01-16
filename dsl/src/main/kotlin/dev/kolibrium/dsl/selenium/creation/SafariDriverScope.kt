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

import org.openqa.selenium.safari.SafariDriverService
import org.openqa.selenium.safari.SafariOptions

@KolibriumDsl
public class SafariDriverScope : DriverScope<SafariDriverServiceScope, SafariOptionsScope>() {
    override val driverServiceScope = SafariDriverServiceScope(SafariDriverService.Builder())
    override val optionsScope = SafariOptionsScope(SafariOptions())

    @KolibriumDsl
    override fun driverService(block: SafariDriverServiceScope.() -> Unit) {
        driverServiceScope.apply {
            block()
            configure()
        }
    }

    @KolibriumDsl
    override fun options(block: SafariOptionsScope.() -> Unit) {
        optionsScope.apply {
            block()
            configure()
        }
    }

    override fun toString(): String {
        return "SafariDriverScope(driverServiceScope=$driverServiceScope, optionsScope=$optionsScope)"
    }
}

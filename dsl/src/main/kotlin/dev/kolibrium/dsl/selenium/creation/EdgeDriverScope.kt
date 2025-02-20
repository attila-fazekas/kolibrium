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
import org.openqa.selenium.edge.EdgeDriverService
import org.openqa.selenium.edge.EdgeOptions

/**
 * Scope class for configuring Edge-specific settings for Edge WebDriver.
 */
@KolibriumDsl
public class EdgeDriverScope : DriverScope<EdgeDriverServiceScope, EdgeOptionsScope>() {
    override val driverServiceScope = EdgeDriverServiceScope(EdgeDriverService.Builder())
    override val optionsScope = EdgeOptionsScope(EdgeOptions())

    /**
     * Configures the Edge driver service.
     *
     * @param block The configuration block for Edge driver service.
     */
    @KolibriumDsl
    override fun driverService(block: EdgeDriverServiceScope.() -> Unit) {
        driverServiceScope.apply {
            block()
            configure()
        }
    }

    /**
     * Configures the Edge-specific options.
     *
     * @param block The configuration block for Edge browser options.
     */
    @KolibriumDsl
    override fun options(block: EdgeOptionsScope.() -> Unit) {
        optionsScope.apply {
            block()
            configure()
        }
    }

    /**
     * Returns a string representation of the [EdgeDriverScope], primarily for debugging purposes.
     */
    override fun toString(): String = "EdgeDriverScope(driverServiceScope=$driverServiceScope, optionsScope=$optionsScope)"
}

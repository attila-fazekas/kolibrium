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

/**
 * Base scope class for WebDriver configuration providing driver service and options customization.
 *
 * @param DS The type of DriverServiceScope for the specific browser.
 * @param O The type of OptionsScope for the specific browser.
 */
public sealed class DriverScope<out DS : DriverServiceScope, out O : OptionsScope> {
    internal abstract val driverServiceScope: DS
    internal abstract val optionsScope: O

    /**
     * Configures the driver service.
     *
     * @param block The configuration block for driver service.
     */
    @KolibriumDsl
    public abstract fun driverService(block: DS.() -> Unit)

    /**
     * Configures the browser-specific options.
     *
     * @param block The configuration block for browser options.
     */
    @KolibriumDsl
    public abstract fun options(block: O.() -> Unit)
}

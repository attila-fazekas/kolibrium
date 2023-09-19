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

package io.kolibrium.dsl

import org.openqa.selenium.WebDriver
import org.openqa.selenium.remote.AbstractDriverOptions
import org.openqa.selenium.remote.service.DriverService

public class DriverScope<T : WebDriver>(
    private val builder: DriverService.Builder<*, *>,
    private val options: AbstractDriverOptions<*>
) {
    internal val driverServiceScope: DriverServiceScope by lazy { DriverServiceScope(builder) }

    internal val optionsScope: OptionsScope by lazy { OptionsScope(options) }

    @KolibriumDsl
    public fun driverService(block: DriverServiceScope.() -> Unit) {
        driverServiceScope.apply {
            block()
            configure()
        }
    }

    @KolibriumDsl
    public fun options(block: OptionsScope.() -> Unit) {
        optionsScope.apply {
            block()
            configure()
        }
    }

    public inner class DriverServiceScope(builder: DriverService.Builder<*, *>) : BaseDriverServiceScope(builder)

    public inner class OptionsScope(options: AbstractDriverOptions<*>) : BaseOptionsScope(options)
}

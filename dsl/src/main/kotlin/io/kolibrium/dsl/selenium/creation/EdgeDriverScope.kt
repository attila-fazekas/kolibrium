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

package io.kolibrium.dsl.selenium.creation

import org.openqa.selenium.edge.EdgeDriverService
import org.openqa.selenium.edge.EdgeOptions

@KolibriumDsl
public class EdgeDriverScope : DriverScope<EdgeDriverServiceScope, EdgeOptionsScope>() {

    override val driverServiceScope = EdgeDriverServiceScope(EdgeDriverService.Builder())
    override val optionsScope = EdgeOptionsScope(EdgeOptions())

    @KolibriumDsl
    override fun driverService(block: EdgeDriverServiceScope.() -> Unit) {
        driverServiceScope.apply {
            block()
            configure()
        }
    }

    @KolibriumDsl
    override fun options(block: EdgeOptionsScope.() -> Unit) {
        optionsScope.apply {
            block()
            configure()
        }
    }

    override fun toString(): String {
        return "EdgeDriverScope(driverServiceScope=$driverServiceScope, optionsScope=$optionsScope)"
    }
}

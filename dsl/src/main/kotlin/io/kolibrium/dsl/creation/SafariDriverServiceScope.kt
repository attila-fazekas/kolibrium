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

package io.kolibrium.dsl.creation

import org.openqa.selenium.safari.SafariDriverService

@KolibriumDsl
public class SafariDriverServiceScope(override val builder: SafariDriverService.Builder) :
    DriverServiceScope() {

    public var logging: Boolean? = null

    override fun configure() {
        super.configure()
        builder.apply {
            logging?.let { withLogging(it) }
        }
    }

    override fun toString(): String {
        return "SafariDriverServiceScope(environmentScope=$environmentScope, logging=$logging, port=$port, " +
            "timeout=$timeout)"
    }
}

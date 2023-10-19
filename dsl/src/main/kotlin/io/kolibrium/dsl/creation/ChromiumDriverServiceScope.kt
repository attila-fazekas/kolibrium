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

import org.apache.commons.validator.routines.InetAddressValidator
import org.openqa.selenium.chromium.ChromiumDriverLogLevel
import java.io.File

@KolibriumDsl
public abstract class ChromiumDriverServiceScope : DriverServiceScope() {

    protected val allowedIpsScope: AllowedIpsScope by lazy { AllowedIpsScope() }

    public var appendLog: Boolean? = null
    public var buildCheckDisabled: Boolean? = null
    public var executable: String? = null
    public var logFile: String? = null
    public var logLevel: ChromiumDriverLogLevel? = null
    public var readableTimestamp: Boolean? = null

    override fun configure() {
        super.configure()
        builder.apply {
            logFile?.let { withLogFile(File(it)) }
        }
    }

    internal open fun allowedIps(block: AllowedIpsScope.() -> Unit) {
        allowedIpsScope.apply(block)
        validateIps()
    }

    private fun validateIps() {
        with(allowedIpsScope.allowedIps) {
            if (isNotEmpty()) {
                val invalidIPAddresses = filter { !InetAddressValidator.getInstance().isValid(it) }
                check(invalidIPAddresses.isEmpty()) { "Following IP addresses are invalid: $invalidIPAddresses" }
            }
        }
    }
}

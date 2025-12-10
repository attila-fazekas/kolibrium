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

import org.apache.commons.validator.routines.InetAddressValidator
import org.openqa.selenium.chromium.ChromiumDriverLogLevel
import java.io.File

/**
 * Base scope class for configuring Chromium-based browser driver service settings.
 */
public abstract class ChromiumDriverServiceScope : DriverServiceScope() {
    protected val allowedIpsScope: AllowedIpsScope by lazy { AllowedIpsScope() }

    /**
     * Controls whether the driver's log output should be appended to an existing log file.
     */
    public var appendLog: Boolean? = null

    /**
     * Controls whether the driver executable can be used with potentially incompatible versions of the browser.
     */
    public var buildCheckDisabled: Boolean? = null

    /**
     * The path to the driver executable.
     */
    public var executable: String? = null

    /**
     * The path to the log file where the driver's output will be written.
     */
    public var logFile: String? = null

    /**
     * The logging level for the driver.
     */
    public var logLevel: ChromiumDriverLogLevel? = null

    /**
     * Controls whether timestamps in the driver's log should be human-readable.
     */
    public var readableTimestamp: Boolean? = null

    override fun configure() {
        super.configure()
        builder.apply {
            logFile?.let { withLogFile(File(it)) }
        }
    }

    internal open fun allowedIps(block: AllowedIpsScope.() -> Unit) {
        allowedIpsScope.apply(block)
        allowedIpsScope.allowedIps.validateIps()
    }

    private fun MutableSet<String>.validateIps() {
        if (isNotEmpty()) {
            val invalidIPAddresses = filter { !InetAddressValidator.getInstance().isValid(it) }
            check(invalidIPAddresses.isEmpty()) { "Following IP addresses are invalid: $invalidIPAddresses" }
        }
    }
}

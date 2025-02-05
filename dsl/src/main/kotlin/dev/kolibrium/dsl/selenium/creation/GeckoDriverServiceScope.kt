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

import org.openqa.selenium.firefox.FirefoxDriverLogLevel
import org.openqa.selenium.firefox.GeckoDriverService
import java.io.File

/**
 * Scope class for configuring Firefox-specific driver service settings.
 *
 * This class provides Firefox-specific configurations while inheriting driver service settings
 * from [DriverServiceScope].
 *
 * @property builder The underlying GeckoDriver service builder.
 */
@KolibriumDsl
public class GeckoDriverServiceScope(
    override val builder: GeckoDriverService.Builder,
) : DriverServiceScope() {
    private val allowedHostsScope: AllowedHostsScope by lazy { AllowedHostsScope() }

    /**
     * The path to the GeckoDriver executable.
     */
    @KolibriumPropertyDsl
    public var executable: String? = null

    /**
     * The path to the log file where GeckoDriver's output will be written.
     */
    @KolibriumPropertyDsl
    public var logFile: String? = null

    /**
     * The logging level for GeckoDriver.
     */
    @KolibriumPropertyDsl
    public var logLevel: FirefoxDriverLogLevel? = null

    /**
     * The directory containing the Firefox profile to be used.
     */
    @KolibriumPropertyDsl
    public var profileRoot: String? = null

    /**
     * Controls whether GeckoDriver's log output should be truncated.
     */
    @KolibriumPropertyDsl
    public var truncatedLogs: Boolean? = null

    override fun configure() {
        super.configure()
        builder.apply {
            executable?.let {
                ifExists(it).run {
                    usingDriverExecutable(File(it))
                }
            }
            logFile?.let { withLogFile(File(it)) }
            logLevel?.let { withLogLevel(it) }
            profileRoot?.let { withProfileRoot(File(it)) }
            truncatedLogs?.let { withTruncatedLogs(it) }
        }
    }

    /**
     * Configures the allowed host header values for incoming requests to GeckoDriver service.
     *
     * @param block The configuration block for specifying allowed hosts.
     */
    @KolibriumDsl
    public fun allowedHosts(block: AllowedHostsScope.() -> Unit) {
        allowedHostsScope.apply(block)
        builder.withAllowHosts(allowedHostsScope.allowedHosts.joinToString(separator = " "))
    }

    /**
     * Returns a string representation of the [GeckoDriverServiceScope], primarily for debugging purposes.
     */
    override fun toString(): String =
        "GeckoDriverServiceScope(allowedHostsScope=$allowedHostsScope, environmentScope=$environmentScope, " +
            "executable=$executable, logFile=$logFile, logLevel=$logLevel, port=$port, profileRoot=$profileRoot, " +
            "timeout=$timeout, truncatedLogs=$truncatedLogs)"
}

/*
 * Copyright 2023-2024 Attila Fazekas & contributors
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

import org.openqa.selenium.chrome.ChromeDriverService
import java.io.File

/**
 * A scope for configuring Chrome-specific driver service settings.
 *
 * This scope extends [ChromiumDriverServiceScope] and provides configuration options specific to ChromeDriver,
 * such as logging preferences, allowed IP addresses for incoming connections, and build check settings.
 *
 * @property builder The underlying ChromeDriver service builder.
 */
@KolibriumDsl
public class ChromeDriverServiceScope internal constructor(
    override val builder: ChromeDriverService.Builder,
) : ChromiumDriverServiceScope() {
    override fun configure() {
        super.configure()
        builder.apply {
            appendLog?.let { withAppendLog(it) }
            buildCheckDisabled?.let { withBuildCheckDisabled(it) }
            executable?.let {
                ifExists(it).run {
                    usingDriverExecutable(File(it))
                }
            }
            logLevel?.let { withLogLevel(it) }
            readableTimestamp?.let { withReadableTimestamp(it) }
        }
    }

    /**
     * Configures the allowed host header values for incoming requests to ChromeDriver service.
     *
     * @param block The configuration block for specifying allowed hosts.
     */
    @KolibriumDsl
    public override fun allowedIps(block: AllowedIpsScope.() -> Unit) {
        super.allowedIps(block)
        builder.withAllowedListIps(allowedIpsScope.allowedIps.joinToString(separator = ", "))
    }

    /**
     * Returns a string representation of the [ChromeDriverServiceScope], primarily for debugging purposes.
     */
    override fun toString(): String =
        "ChromiumDriverServiceScope(allowedIpsScope=$allowedIpsScope, appendLog=$appendLog, " +
            "buildCheckDisabled=$buildCheckDisabled, environmentScope=$environmentScope, executable=$executable, " +
            "logFile=$logFile, logLevel=$logLevel, port=$port, readableTimestamp=$readableTimestamp, " +
            "timeout=$timeout)"
}

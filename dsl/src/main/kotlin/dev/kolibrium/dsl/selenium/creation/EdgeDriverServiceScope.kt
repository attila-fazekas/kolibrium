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
import java.io.File

/**
 * Scope class for configuring Edge-specific driver service settings.
 *
 * This class provides Edge-specific configurations while inheriting driver service settings
 * from [ChromiumDriverServiceScope].
 *
 * @property builder The underlying EdgeDriver service builder.
 */
@KolibriumDsl
public class EdgeDriverServiceScope(
    override val builder: EdgeDriverService.Builder,
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
            logLevel?.let { withLoglevel(it) }
            readableTimestamp?.let { withReadableTimestamp(it) }
        }
    }

    /**
     * Configures the allowed host header values for incoming requests to EdgeDriver service.
     *
     * @param block The configuration block for specifying allowed hosts.
     */
    @KolibriumDsl
    override fun allowedIps(block: AllowedIpsScope.() -> Unit) {
        super.allowedIps(block)
        builder.withAllowedListIps(allowedIpsScope.allowedIps.joinToString(separator = ", "))
    }

    /**
     * Returns a string representation of the [EdgeDriverServiceScope], primarily for debugging purposes.
     */
    override fun toString(): String =
        "EdgeDriverServiceScope(allowedIpsScope=$allowedIpsScope, appendLog=$appendLog, " +
            "buildCheckDisabled=$buildCheckDisabled, environmentScope=$environmentScope, executable=$executable, " +
            "logFile=$logFile, logLevel=$logLevel, port=$port, readableTimestamp=$readableTimestamp, " +
            "timeout=$timeout)"
}

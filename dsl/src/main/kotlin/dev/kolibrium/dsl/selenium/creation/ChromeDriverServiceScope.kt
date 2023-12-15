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

package dev.kolibrium.dsl.selenium.creation

import org.openqa.selenium.chrome.ChromeDriverService
import java.io.File

@KolibriumDsl
public class ChromeDriverServiceScope internal constructor(
    override val builder: ChromeDriverService.Builder
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

    @KolibriumDsl
    public override fun allowedIps(block: dev.kolibrium.dsl.selenium.creation.AllowedIpsScope.() -> Unit) {
        super.allowedIps(block)
        builder.withAllowedListIps(allowedIpsScope.allowedIps.joinToString(separator = ", "))
    }

    override fun toString(): String {
        return "ChromiumDriverServiceScope(allowedIpsScope=$allowedIpsScope, appendLog=$appendLog, " +
            "buildCheckDisabled=$buildCheckDisabled, environmentScope=$environmentScope, executable=$executable, " +
            "logFile=$logFile, logLevel=$logLevel, port=$port, readableTimestamp=$readableTimestamp, " +
            "timeout=$timeout)"
    }
}

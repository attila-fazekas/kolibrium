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

import org.openqa.selenium.remote.service.DriverService
import java.io.File
import java.io.IOException
import java.net.ServerSocket
import kotlin.time.Duration
import kotlin.time.toJavaDuration

/**
 * A base scope for configuring Selenium driver service settings.
 *
 * This abstract scope provides common configuration options for all browser driver services,
 * such as port selection, environment variables, and timeout setting.
 */
@KolibriumDsl
public sealed class DriverServiceScope {
    internal abstract val builder: DriverService.Builder<*, *>

    protected val environmentScope: EnvironmentScope by lazy { EnvironmentScope() }

    /**
     * The port on which the driver service should listen for incoming connections.
     */
    @KolibriumPropertyDsl
    public var port: Int? = null

    /**
     * The maximum time to wait for the driver service to start.
     */
    @KolibriumPropertyDsl
    public var timeout: Duration? = null

    internal open fun configure() {
        with(builder) {
            port?.let {
                checkPort(it)
                usingPort(it)
            }
            timeout?.let { withTimeout(it.toJavaDuration()) }
        }
    }

    private fun checkPort(port: Int) {
        try {
            ServerSocket(port).use {}
        } catch (e: IOException) {
            throw DslConfigurationException(
                """
                    |DriverService is not set up properly:
                    |Port $port already in use
                """.trimMargin(),
            )
        }
    }

    @KolibriumDsl
    public fun environment(block: EnvironmentScope.() -> Unit) {
        environmentScope.apply(block)
        if (environmentScope.environmentVariables.isNotEmpty()) {
            builder.withEnvironment(environmentScope.environmentVariables)
        }
    }

    protected fun ifExists(file: String?): Boolean {
        file?.let {
            require(File(it).exists()) {
                """
                |DriverService is not set up properly:
                |The following file does not exist at the specified path: $file
                """.trimMargin()
            }
        }
        return true
    }

    /**
     * Returns a string representation of the [DriverServiceScope], primarily for debugging purposes.
     */
    override fun toString(): String = "DriverServiceScope(environmentScope=$environmentScope, port=$port, timeout=$timeout)"
}

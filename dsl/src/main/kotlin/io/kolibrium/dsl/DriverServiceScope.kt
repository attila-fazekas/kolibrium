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

import dev.drewhamilton.poko.Poko
import org.openqa.selenium.remote.service.DriverService
import java.io.IOException
import java.net.ServerSocket
import kotlin.time.Duration
import kotlin.time.toJavaDuration

@Poko
public sealed class BaseDriverServiceScope(internal val builder: DriverService.Builder<*, *>) {

    @KolibriumDsl
    public var port: Int? = null

    @KolibriumDsl
    public var timeout: Duration? = null

    @SuppressWarnings("SwallowedException")
    internal fun checkPort(): BaseDriverServiceScope {
        try {
            port?.let { ServerSocket(it).use {} }
        } catch (e: IOException) {
            throw KolibriumDslConfigurationException(
                """
                    |DriverService is not set up properly:
                    |Port $port already in use
                """.trimMargin()
            )
        }
        return this
    }

    internal fun configure(): BaseDriverServiceScope {
        builder.apply {
            port?.let { usingPort(it) }
            timeout?.let { withTimeout(it.toJavaDuration()) }
        }
        return this
    }

    @KolibriumDsl
    public fun environment(block: EnvironmentScope.() -> Unit) {
        val envScope = EnvironmentScope().apply(block)
        if (envScope.environmentVariables.isNotEmpty()) {
            builder.withEnvironment(envScope.environmentVariables)
        }
    }
}

public class DriverServiceScope<T : Browser>(builder: DriverService.Builder<*, *>) :
    BaseDriverServiceScope(builder)

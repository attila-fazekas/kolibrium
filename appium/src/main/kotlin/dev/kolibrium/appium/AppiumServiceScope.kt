/*
 * Copyright 2023-2026 Attila Fazekas & contributors
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

package dev.kolibrium.appium

import dev.kolibrium.webdriver.EnvironmentScope
import dev.kolibrium.webdriver.InternalKolibriumApi
import dev.kolibrium.webdriver.KolibriumDsl
import io.appium.java_client.service.local.AppiumDriverLocalService
import io.appium.java_client.service.local.AppiumServiceBuilder
import io.appium.java_client.service.local.flags.GeneralServerFlag
import io.appium.java_client.service.local.flags.ServerArgument
import org.openqa.selenium.remote.DesiredCapabilities
import java.io.File

/**
 * DSL scope for configuring and building an [AppiumDriverLocalService].
 *
 * This scope mirrors the common configuration options available on
 * [AppiumServiceBuilder] and exposes a concise, type-safe configuration style.
 * Use it via the top-level [appiumService] function.
 *
 * Example:
 *
 * ```kotlin
 * val service = appiumService {
 *     ipAddress = "127.0.0.1"
 *     port = 4723
 *     logLevel = "info"
 *     environments { environment("ANDROID_HOME", "/path/to/sdk") }
 * }
 * ```
 */
@KolibriumDsl
public class AppiumServiceScope
    @InternalKolibriumApi
    constructor() {
        internal val builder: AppiumServiceBuilder = AppiumServiceBuilder()

        /**
         * IP address the Appium server should bind to. Defaults to Appium's internal default
         * if not provided (usually 127.0.0.1).
         */
        public var ipAddress: String? = null

        /**
         * Fixed port for the Appium server to listen on.
         *
         * Mutually exclusive with [useAnyFreePort].
         */
        public var port: Int? = null

        /**
         * When true, let the operating system allocate a free port for the Appium server.
         *
         * Mutually exclusive with [port].
         */
        public var useAnyFreePort: Boolean = false

        /**
         * Path to a custom Node.js executable used to launch Appium.
         */
        public var nodeExecutable: File? = null

        /**
         * Path to the main Appium JS entry point (e.g., appium.js).
         */
        public var appiumJS: File? = null

        /**
         * File to which the Appium server log should be written.
         */
        public var logFile: File? = null

        /**
         * Base path prefix for all WebDriver routes (e.g., "/wd/hub").
         */
        public var basePath: String? = null

        /**
         * Appium server log level (e.g., "debug", "info", "warn", "error").
         */
        public var logLevel: String? = null

        /**
         * Enables Appium's relaxed security mode.
         */
        public var relaxedSecurity: Boolean = false

        /**
         * Comma-separated list of plugins to activate, or "all" to enable every available plugin.
         */
        public var plugins: String? = null

        /**
         * Comma-separated list of drivers to activate.
         */
        public var drivers: String? = null

        /**
         * Configures environment variables for the Appium server process.
         * Variables set here are inherited by any sessions the server spawns.
         */
        public fun environments(block: EnvironmentScope.() -> Unit) {
            val scope = EnvironmentScope().apply(block)
            if (scope.environmentVariables.isNotEmpty()) {
                builder.withEnvironment(scope.environmentVariables)
            }
        }

        /**
         * Provides server-side capabilities passed to the Appium server at startup.
         * These are not the same as session capabilities; they affect server behavior.
         */
        public fun capabilities(block: CapabilitiesScope.() -> Unit) {
            val scope = CapabilitiesScope().apply(block)
            if (scope.capabilities.isNotEmpty()) {
                builder.withCapabilities(DesiredCapabilities(scope.capabilities))
            }
        }

        /**
         * Low-level escape hatch to pass any [ServerArgument] not covered by first-class properties.
         *
         * @param flag The server flag to pass.
         * @param value Optional value for flags that require one.
         */
        public fun argument(
            flag: ServerArgument,
            value: String? = null,
        ) {
            if (value != null) {
                builder.withArgument(flag, value)
            } else {
                builder.withArgument(flag)
            }
        }

        /**
         * Builds a configured [AppiumDriverLocalService] instance.
         *
         * Note: this does not start the service; callers must invoke [AppiumDriverLocalService.start].
         */
        internal fun build(): AppiumDriverLocalService {
            require(port == null || !useAnyFreePort) {
                "port and useAnyFreePort are mutually exclusive"
            }

            ipAddress?.let { builder.withIPAddress(it) }
            port?.let { builder.usingPort(it) }
            if (useAnyFreePort) builder.usingAnyFreePort()
            nodeExecutable?.let { builder.usingDriverExecutable(it) }
            appiumJS?.let { builder.withAppiumJS(it) }
            logFile?.let { builder.withLogFile(it) }
            basePath?.let { builder.withArgument(GeneralServerFlag.BASEPATH, it) }
            logLevel?.let { builder.withArgument(GeneralServerFlag.LOG_LEVEL, it) }
            if (relaxedSecurity) builder.withArgument(GeneralServerFlag.RELAXED_SECURITY)
            plugins?.let { builder.withArgument(GeneralServerFlag.USE_PLUGINS, it) }
            drivers?.let { builder.withArgument(GeneralServerFlag.USE_DRIVERS, it) }
            return builder.build()
        }

        /**
         * Returns a string representation of the [AppiumServiceScope], primarily for debugging purposes.
         */
        override fun toString(): String = "AppiumServiceScope(builder=$builder)"
    }

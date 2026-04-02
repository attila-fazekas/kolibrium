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

import dev.kolibrium.webdriver.KolibriumDsl
import io.appium.java_client.service.local.AppiumDriverLocalService

/**
 * Builds an [AppiumDriverLocalService] using the [AppiumServiceScope] DSL.
 *
 * This function configures an [io.appium.java_client.service.local.AppiumServiceBuilder]
 * under the hood and returns a ready-to-start service instance. The caller owns the
 * lifecycle; call `start()`/`stop()` as needed.
 *
 * Example:
 *
 * ```kotlin
 * val service = appiumService {
 *     ipAddress = "127.0.0.1"
 *     useAnyFreePort = true
 *     logLevel = "info"
 * }
 * service.start()
 * try { /* run tests */ } finally { service.stop() }
 * ```
 */
@KolibriumDsl
public fun appiumService(block: AppiumServiceScope.() -> Unit): AppiumDriverLocalService = AppiumServiceScope().apply(block).build()

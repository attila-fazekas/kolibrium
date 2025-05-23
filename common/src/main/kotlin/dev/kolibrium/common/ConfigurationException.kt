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

package dev.kolibrium.common

/**
 * Used to throw a [RuntimeException] when there is a configuration error within Kolibrium.
 *
 * **Note:** This class is part of the internal API and should not be used outside of Kolibrium.
 *
 * @param message The detail message explaining the configuration error.
 * @param cause The underlying cause of the exception, or `null` if none.
 * @constructor Creates a [ConfigurationException] with the specified error message and optional cause.
 *
 * @see RuntimeException
 */
@InternalKolibriumApi
public open class ConfigurationException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)

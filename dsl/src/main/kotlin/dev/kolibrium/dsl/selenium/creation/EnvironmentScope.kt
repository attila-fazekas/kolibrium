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

/**
 * Scope class for configuring environment variables for the driver service.
 *
 * This scope allows setting environment variables that will be inherited by every browser session
 * launched by the server.
 */
@KolibriumDsl
public class EnvironmentScope {
    internal val environmentVariables: MutableMap<String, String> = mutableMapOf()

    /**
     * Adds an environment variable to the map of environment variables.
     *
     * @param name The name of the environment variable.
     * @param value The value of the environment variable.
     */
    @KolibriumDsl
    public fun environment(
        name: String,
        value: String,
    ) {
        environmentVariables[name] = value
    }

    /**
     * Returns a string representation of the [EnvironmentScope], primarily for debugging purposes.
     */
    override fun toString(): String = "EnvironmentScope(environmentVariables=$environmentVariables)"
}

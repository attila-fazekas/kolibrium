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
 * Scope class for configuring browser experimental flags.
 */
@KolibriumDsl
public class BrowserEnabledLabsExperiments {
    internal val experimentalFlags = mutableSetOf<ExperimentalFlag>()

    /**
     * Adds an [ExperimentalFlag] to the configuration.
     *
     * @param experimentalFlag The experimental flag instance to be added.
     */
    @KolibriumDsl
    public fun experimentalFlag(experimentalFlag: ExperimentalFlag) {
        experimentalFlags.add(experimentalFlag)
    }

    /**
     * Adds an experimental flag to the configuration.
     *
     * @param value The string representation of the experimental flag to be added.
     */
    @KolibriumDsl
    public fun experimentalFlag(value: String) {
        experimentalFlags.add(ExperimentalFlag(value))
    }

    /**
     * Returns a string representation of the [BrowserEnabledLabsExperiments], primarily for debugging purposes.
     */
    override fun toString(): String = "BrowserEnabledLabsExperiments(experimentalFlags=$experimentalFlags)"
}

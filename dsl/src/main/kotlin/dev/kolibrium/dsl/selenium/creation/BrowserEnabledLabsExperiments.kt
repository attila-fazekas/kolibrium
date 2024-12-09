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
     * This operator function allows adding experimental flags using the unary plus operator (+).
     */
    public operator fun ExperimentalFlag.unaryPlus() {
        experimentalFlags.add(this)
    }

    /**
     * Adds an experimental flag a string value to the configuration.
     *
     * This operator function allows adding experimental flags from string values using the unary plus operator (+).
     */
    public operator fun String.unaryPlus() {
        experimentalFlags.add(ExperimentalFlag(this))
    }

    /**
     * Returns a string representation of the [BrowserEnabledLabsExperiments], primarily for debugging purposes.
     */
    override fun toString(): String = "BrowserEnabledLabsExperiments(experimentalFlags=$experimentalFlags)"
}

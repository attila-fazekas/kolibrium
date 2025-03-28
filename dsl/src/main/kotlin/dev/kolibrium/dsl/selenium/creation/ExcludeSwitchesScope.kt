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

/**
 * Scope class for configuring browser switches to be excluded.
 */
@KolibriumDsl
public class ExcludeSwitchesScope {
    internal val switches = mutableSetOf<Switch>()

    /**
     * Adds a [Switch] to the list of excluded switches.
     *
     * This operator function allows adding switches to the exclusion list using the unary plus operator (`+`).
     */
    public operator fun Switch.unaryPlus() {
        switches.add(this)
    }

    /**
     * Adds a switch from a string value to the list of excluded switches.
     *
     * This operator function allows adding switches from string values to the exclusion list using the
     * unary plus operator (`+`).
     *
     * @throws IllegalArgumentException if the string is blank.
     */
    public operator fun String.unaryPlus() {
        switches.add(Switch(this))
    }

    /**
     * Returns a string representation of the [ExcludeSwitchesScope], primarily for debugging purposes.
     */
    override fun toString(): String = "ExcludeSwitchesScope(switches=$switches)"
}

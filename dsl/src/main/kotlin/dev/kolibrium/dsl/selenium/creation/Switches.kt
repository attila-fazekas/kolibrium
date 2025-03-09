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

import dev.kolibrium.dsl.selenium.KolibriumPropertyDsl

/**
 * Value class representing a browser feature switch to be excluded from the browser launch.
 *
 * @property value The string identifier of the switch.
 * @throws IllegalArgumentException if the value is blank.
 */
@JvmInline
public value class Switch(
    internal val value: String,
) {
    init {
        require(value.isNotBlank()) { "Switch value cannot be blank" }
    }
}

/**
 * Collection of predefined browser feature switches.
 */
public object Switches {
    /**
     * Enables automation mode.
     */
    @KolibriumPropertyDsl
    public val enable_automation: Switch = Switch("enable-automation")

    /**
     * Disables popup blocking.
     */
    @KolibriumPropertyDsl
    public val disable_popup_blocking: Switch = Switch("disable-popup-blocking")
}

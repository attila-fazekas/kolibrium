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
 * Scope class for configuring browser local state settings.
 */
@KolibriumDsl
public class LocalStateScope {
    internal val experiments by lazy { BrowserEnabledLabsExperiments() }

    /**
     * Configures browser lab experiments.
     *
     * @param block The configuration block for lab experiments.
     * @return The configured [BrowserEnabledLabsExperiments].
     */
    @KolibriumDsl
    public fun browserEnabledLabsExperiments(block: BrowserEnabledLabsExperiments.() -> Unit): BrowserEnabledLabsExperiments =
        experiments.apply(block)

    /**
     * Returns a string representation of the [LocalStateScope], primarily for debugging purposes.
     */
    override fun toString(): String = "LocalStateScope(browserEnabledLabsExperiments=$experiments)"
}

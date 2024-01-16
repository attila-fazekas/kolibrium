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

package dev.kolibrium.dsl.selenium.creation

@KolibriumDsl
public class LocalStateScope {
    internal val experiments by lazy { BrowserEnabledLabsExperiments() }

    @KolibriumDsl
    public fun browserEnabledLabsExperiments(
        block: BrowserEnabledLabsExperiments.() -> Unit,
    ): BrowserEnabledLabsExperiments = experiments.apply(block)

    override fun toString(): String {
        return "LocalStateScope(browserEnabledLabsExperiments=$experiments)"
    }
}

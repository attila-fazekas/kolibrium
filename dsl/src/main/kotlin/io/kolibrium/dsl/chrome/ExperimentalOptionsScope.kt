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

package io.kolibrium.dsl.chrome

import io.kolibrium.dsl.KolibriumDsl
import io.kolibrium.dsl.PreferencesScope

public class ExperimentalOptionsScope {
    public val preferencesScope: PreferencesScope by lazy { PreferencesScope() }
    public val switchesScope: SwitchesScope by lazy { SwitchesScope() }
    public val localStateScope: LocalStateScope by lazy { LocalStateScope() }

    @KolibriumDsl
    public fun preferences(block: PreferencesScope.() -> Unit): PreferencesScope = preferencesScope.apply(block)

    @KolibriumDsl
    public fun excludeSwitches(block: SwitchesScope.() -> Unit): SwitchesScope = switchesScope.apply(block)

    @KolibriumDsl
    public fun localState(block: LocalStateScope.() -> Unit): LocalStateScope = localStateScope.apply(block)
}

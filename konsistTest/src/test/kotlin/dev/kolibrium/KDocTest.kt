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

package dev.kolibrium

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.modifierprovider.withPublicModifier
import com.lemonappdev.konsist.api.ext.list.modifierprovider.withoutOperatorModifier
import com.lemonappdev.konsist.api.ext.list.modifierprovider.withoutOverrideModifier
import com.lemonappdev.konsist.api.ext.provider.hasValidKDocParamTags
import com.lemonappdev.konsist.api.ext.provider.hasValidKDocReceiverTag
import com.lemonappdev.konsist.api.ext.provider.hasValidKDocReturnTag
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class KDocTest {
    @Test
    @Disabled
    fun `every public function with parameter(s) has param tag(s)`() {
        Konsist
            .scopeFromProduction()
            .functions()
            .filter {
                it.resideInModule("core") || it.resideInModule("dsl") || it.resideInModule("selenium")
            }.withoutOverrideModifier()
            .withPublicModifier()
            .assertTrue {
                it.hasValidKDocParamTags()
            }
    }

    @Test
    @Disabled
    fun `every public function with return value has a return tag`() {
        Konsist
            .scopeFromProduction()
            .functions()
            .filter {
                it.resideInModule("core") || it.resideInModule("dsl") || it.resideInModule("selenium")
            }.withoutOverrideModifier()
            .withPublicModifier()
            .assertTrue {
                it.hasValidKDocReturnTag()
            }
    }

    @Test
    @Disabled
    fun `every public extension function has a receiver tag`() {
        Konsist
            .scopeFromProduction()
            .functions()
            .filter {
                it.resideInModule("core") || it.resideInModule("dsl") || it.resideInModule("selenium")
            }.withoutOverrideModifier()
            .withPublicModifier()
            .withoutOperatorModifier()
            .assertTrue {
                it.hasValidKDocReceiverTag()
            }
    }

    @Test
    @Disabled
    fun `every public extension property has a receiver tag`() {
        Konsist
            .scopeFromProduction()
            .properties()
            .filter {
                it.resideInModule("core") || it.resideInModule("dsl") || it.resideInModule("selenium")
            }.withoutOverrideModifier()
            .withPublicModifier()
            .assertTrue {
                it.hasValidKDocReceiverTag()
            }
    }
}

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

package dev.kolibrium

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.modifierprovider.withPublicModifier
import com.lemonappdev.konsist.api.ext.list.modifierprovider.withoutOperatorModifier
import com.lemonappdev.konsist.api.ext.list.modifierprovider.withoutOverrideModifier
import com.lemonappdev.konsist.api.ext.provider.hasValidKDocParamTags
import com.lemonappdev.konsist.api.ext.provider.hasValidKDocReceiverTag
import com.lemonappdev.konsist.api.ext.provider.hasValidKDocReturnTag
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.jupiter.api.Test

class KDocTest {
    private val functions =
        Konsist
            .scopeFromProduction()
            .functions()
            .filter {
                it.resideInModule("common") ||
                    it.resideInModule("dsl") ||
                    it.resideInModule("junit") ||
                    it.resideInModule("ksp") ||
                    it.resideInModule("selenium")
            }.withoutOverrideModifier()
            .withPublicModifier()

    @Test
    fun `every public function with parameter(s) has param tag(s)`() {
        functions
            .filterNot {
                it.name == "exception"
            }.assertTrue {
                it.hasValidKDocParamTags()
            }
    }

    @Test
    fun `every public function with return value has a return tag`() {
        functions
            .assertTrue {
                it.hasValidKDocReturnTag()
            }
    }

    @Test
    fun `every public extension function has a receiver tag`() {
        functions
            .withoutOperatorModifier()
            .assertTrue {
                it.hasValidKDocReceiverTag()
            }
    }

    @Test
    fun `every public extension property has a receiver tag`() {
        Konsist
            .scopeFromProduction()
            .properties()
            .filter {
                it.resideInModule("common") ||
                    it.resideInModule("dsl") ||
                    it.resideInModule("junit") ||
                    it.resideInModule("ksp") ||
                    it.resideInModule("selenium")
            }.withoutOverrideModifier()
            .withPublicModifier()
            .assertTrue {
                it.hasValidKDocReceiverTag()
            }
    }
}

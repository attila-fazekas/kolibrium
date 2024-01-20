/*
 * Copyright 2024 Attila Fazekas
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
import com.lemonappdev.konsist.api.ext.list.modifierprovider.withValueModifier
import com.lemonappdev.konsist.api.ext.list.properties
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.jupiter.api.Test

class KonsistTest {
    @Test
    fun `value classes should have 'value' property`() {
        Konsist
            .scopeFromPackage("dev.kolibrium.dsl..")
            .classes()
            .withValueModifier()
            .filterNot {
                it.name == "Extension"
            }
            .properties()
            .assertTrue {
                it.name == "value"
            }
    }
}

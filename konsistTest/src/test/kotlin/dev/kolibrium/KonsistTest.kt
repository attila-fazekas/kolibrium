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
import com.lemonappdev.konsist.api.declaration.KoFunctionDeclaration
import com.lemonappdev.konsist.api.declaration.KoPropertyDeclaration
import com.lemonappdev.konsist.api.ext.list.indexOfFirstInstance
import com.lemonappdev.konsist.api.ext.list.indexOfLastInstance
import com.lemonappdev.konsist.api.ext.list.modifierprovider.withValueModifier
import com.lemonappdev.konsist.api.ext.list.primaryConstructors
import com.lemonappdev.konsist.api.ext.list.withName
import com.lemonappdev.konsist.api.ext.list.withoutType
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.jupiter.api.Test

class KonsistTest {
    @Test
    fun `test classes should have 'Test' suffix`() {
        Konsist
            .scopeFromTest()
            .classes()
            .filter { it.functions().any { func -> func.hasAnnotationOf(Test::class) } }
            .assertTrue { it.hasNameEndingWith("Test") }
    }

    @Test
    fun `value classes should have 'value' property`() {
        Konsist
            .scopeFromProject()
            .classes()
            .withValueModifier()
            .filterNot {
                it.name == "Extension"
            }
            .primaryConstructors
            .assertTrue { it.hasParameterWithName("value") }
    }

    // as per https://github.com/oshai/kotlin-logging/issues/364#issuecomment-1763871697
    @Test
    fun `loggers should be declared outside of classes`() {
        Konsist
            .scopeFromProduction()
            .properties()
            .withName("logger")
            .withoutType {
                it.name == "KSPLogger"
            }
            .assertTrue {
                it.hasPrivateModifier && it.isTopLevel
            }
    }

    @Test
    fun `properties are declared before functions`() {
        Konsist
            .scopeFromProject()
            .classes()
            .assertTrue {
                val lastKoPropertyDeclarationIndex =
                    it
                        .declarations(includeNested = false, includeLocal = false)
                        .indexOfLastInstance<KoPropertyDeclaration>()

                val firstKoFunctionDeclarationIndex =
                    it
                        .declarations(includeNested = false, includeLocal = false)
                        .indexOfFirstInstance<KoFunctionDeclaration>()

                if (lastKoPropertyDeclarationIndex != -1 && firstKoFunctionDeclarationIndex != -1) {
                    lastKoPropertyDeclarationIndex < firstKoFunctionDeclarationIndex
                } else {
                    true
                }
            }
    }

    @Test
    fun `companion object is last declaration in class in 'main' source set`() {
        Konsist
            .scopeFromProduction()
            .classes()
            .assertTrue { koClassDeclaration ->
                val companionObject =
                    koClassDeclaration.objects(includeNested = false).lastOrNull { obj ->
                        obj.hasCompanionModifier
                    }

                companionObject?.let {
                    koClassDeclaration.declarations(includeNested = false, includeLocal = false).last() == companionObject
                } ?: true
            }
    }

    @Test
    fun `package name must match file path`() {
        Konsist
            .scopeFromProject()
            .packages
            .assertTrue { it.hasMatchingPath }
    }
}

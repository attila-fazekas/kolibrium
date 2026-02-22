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

@file:OptIn(ExperimentalCompilerApi::class)

package dev.kolibrium.api.ksp.processors

import com.tschuchort.compiletesting.KotlinCompilation.ExitCode.OK
import com.tschuchort.compiletesting.SourceFile.Companion.kotlin
import io.kotest.matchers.shouldBe
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Test

class ConcurrentProcessingTest : ApiBaseTest() {
    @Test
    fun `Multiple ApiSpec objects with overlapping scan packages generate separate clients`() {
        val spec1 =
            kotlin(
                "Spec1.kt",
                """
                package dev.kolibrium.api.ksp.test
                import dev.kolibrium.api.core.ApiSpec
                import dev.kolibrium.api.ksp.annotations.GenerateApi
                @GenerateApi
                object PetApiSpec : ApiSpec() {
                    override val baseUrl = "https://pet.api"
                }
                """.trimIndent(),
            )
        val spec2 =
            kotlin(
                "Spec2.kt",
                """
                package dev.kolibrium.api.ksp.test
                import dev.kolibrium.api.core.ApiSpec
                import dev.kolibrium.api.ksp.annotations.GenerateApi
                @GenerateApi
                object StoreApiSpec : ApiSpec() {
                    override val baseUrl = "https://store.api"
                }
                """.trimIndent(),
            )
        val request =
            kotlin(
                "Requests.kt",
                """
                package dev.kolibrium.api.ksp.test.models
                import dev.kolibrium.api.ksp.annotations.*
                import kotlinx.serialization.Serializable
                @Serializable
                data class ItemDto(val id: Int)
                @GET("/items")
                @Returns(success = ItemDto::class)
                @Serializable
                class GetItemsRequest
                """.trimIndent(),
            )
        val kotlinCompilation = getCompilation(spec1, spec2, request)
        val compilation = kotlinCompilation.compile()
        compilation.exitCode shouldBe OK
        kotlinCompilation.getGeneratedSource("PetClient.kt")
        kotlinCompilation.getGeneratedSource("StoreClient.kt")
    }
}

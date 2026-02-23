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
import io.kotest.matchers.string.shouldContain
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Test

class ByPrefixGroupingTest : ApiBaseTest() {
    private val byPrefixApiSpec =
        kotlin(
            "TestApiSpec.kt",
            """
            package dev.kolibrium.api.ksp.test

            import dev.kolibrium.api.core.ApiSpec
            import dev.kolibrium.api.ksp.annotations.ClientGrouping
            import dev.kolibrium.api.ksp.annotations.GenerateApi

            @GenerateApi(grouping = ClientGrouping.ByPrefix)
            object TestApiSpec : ApiSpec() {
                override val baseUrl = "https://test.api"
            }
            """.trimIndent(),
        )

    @Test
    fun `Paths without literal segments fall into root group`() {
        val request =
            kotlin(
                "Requests.kt",
                """
                package dev.kolibrium.api.ksp.test.models
                import dev.kolibrium.api.ksp.annotations.*
                import kotlinx.serialization.Serializable
                import kotlinx.serialization.Transient
                @Serializable
                data class ItemDto(val id: Int)
                @GET("/{id}")
                @Returns(success = ItemDto::class)
                @Serializable
                data class GetItemRequest(@Path @Transient val id: Int = 0)
                @GET("/users")
                @Returns(success = ItemDto::class)
                @Serializable
                object GetUsersRequest
                """.trimIndent(),
            )
        val kotlinCompilation = getCompilation(byPrefixApiSpec, request)
        val compilation = kotlinCompilation.compile()
        compilation.exitCode shouldBe OK
        val rootSource = kotlinCompilation.getGeneratedSource("RootClient.kt")
        rootSource shouldContain "fun getItem("
    }

    @Test
    fun `All endpoints in root group emits warning`() {
        val request =
            kotlin(
                "Requests.kt",
                """
                package dev.kolibrium.api.ksp.test.models
                import dev.kolibrium.api.ksp.annotations.*
                import kotlinx.serialization.Serializable
                import kotlinx.serialization.Transient
                @Serializable
                data class ItemDto(val id: Int)
                @GET("/{id}")
                @Returns(success = ItemDto::class)
                @Serializable
                data class GetItemRequest(@Path @Transient val id: Int = 0)
                @GET("/{a}/{b}")
                @Returns(success = ItemDto::class)
                @Serializable
                data class GetNestedRequest(
                    @Path @Transient val a: String = "",
                    @Path @Transient val b: String = ""
                )
                """.trimIndent(),
            )
        val compilation = getCompilation(byPrefixApiSpec, request).compile()
        compilation.exitCode shouldBe OK
        compilation.messages shouldContain "fall into the 'root' fallback group"
    }
}

/*
 * Copyright 2023-2026 Attila Fazekas & contributors
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

package dev.kolibrium.webdriver

import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test

class EnvironmentScopeTest {
    @Test
    fun `environment variables are stored correctly`() {
        val scope = EnvironmentScope()
        scope.environment("HEADLESS", "true")
        scope.environment("LANG", "en_US")

        scope.environmentVariables shouldContainExactly mapOf("HEADLESS" to "true", "LANG" to "en_US")
    }

    @Test
    fun `later value overwrites earlier for same key`() {
        val scope = EnvironmentScope()
        scope.environment("KEY", "first")
        scope.environment("KEY", "second")

        scope.environmentVariables["KEY"] shouldBe "second"
    }

    @Test
    fun `toString includes environment variables`() {
        val scope = EnvironmentScope().apply { environment("FOO", "bar") }
        scope.toString() shouldContain "FOO"
        scope.toString() shouldContain "bar"
    }

    @Test
    fun `empty scope has no variables`() {
        EnvironmentScope().environmentVariables.size shouldBe 0
    }
}

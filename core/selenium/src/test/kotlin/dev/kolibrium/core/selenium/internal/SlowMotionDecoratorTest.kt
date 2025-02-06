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

package dev.kolibrium.core.selenium.internal

import dev.kolibrium.core.selenium.decorators.DecoratorManager
import dev.kolibrium.core.selenium.decorators.SlowMotionDecorator
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.seconds

class SlowMotionDecoratorTest : BaseTest() {
    @BeforeEach
    fun setup() {
        DecoratorManager.addDecorators(
            SlowMotionDecorator(wait = 2.seconds),
        )
    }

    @AfterEach
    fun cleanup() {
        DecoratorManager.clearDecorators()
    }

    @Test
    fun `test with configured decorators`() =
        tutorial {
            idOrName.click()
        }
}

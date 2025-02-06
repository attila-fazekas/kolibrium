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

package dev.kolibrium.core.selenium.decorators

import org.openqa.selenium.SearchContext

/**
 * Manages the test-level decorators that can be applied to WebDriver and WebElement instances.
 * Decorators are applied at runtime and can modify the behavior of web element interactions.
 *
 * The manager maintains decorators on a per-thread basis, allowing for parallel test execution
 * without decorator interference between different test threads.
 *
 * Example usage:
 * ```kotlin
 * // Add decorators for specific test cases
 * DecoratorManager.addDecorators(
 *     SlowMotionDecorator(1.seconds),
 *     HighlighterDecorator(color = Color.BLUE)
 * )
 *
 * // Clear decorators after test completion
 * DecoratorManager.clearDecorators()
 * ```
 */
public object DecoratorManager {
    private val testLevelDecorators = ThreadLocal.withInitial { mutableListOf<AbstractDecorator>() }

    /**
     * Adds one or more decorators to the current test thread's decorator list.
     * Decorators are applied in the order they are provided.
     *
     * @param decorators The decorators to be added. Each decorator must extend [AbstractDecorator].
     * Multiple decorators can be provided as varargs.
     */
    public fun addDecorators(vararg decorators: AbstractDecorator) {
        testLevelDecorators.get().addAll(decorators)
    }

    /**
     * Removes all decorators from the current test thread's decorator list.
     * This should typically be called in test cleanup/teardown to ensure
     * decorators from one test don't affect subsequent tests.
     */
    public fun clearDecorators() {
        testLevelDecorators.get().clear()
    }

    internal fun getAllDecorators(): List<AbstractDecorator> = testLevelDecorators.get().toList()

    internal fun combine(decorators: List<AbstractDecorator>): (SearchContext) -> SearchContext =
        { context ->
            decorators.fold(context) { acc, decorator ->
                decorator.decorate(acc)
            }
        }
}

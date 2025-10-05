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
import org.openqa.selenium.WebDriver
import org.openqa.selenium.support.events.EventFiringDecorator

/**
 * Manages the test-level Kolibrium decorators.
 *
 * Decorators are stored in a thread-local list so parallel tests do not interfere with each other.
 * They are applied in insertion order to the root [SearchContext] (driver) and to all nested
 * elements discovered from it, preserving chaining across the object graph.
 *
 * Example
 * ```kotlin
 * // Add decorators for the current test
 * DecoratorManager.addDecorators(
 *     SlowMotionDecorator(1.seconds),
 *     HighlighterDecorator(color = Color.Blue)
 * )
 *
 * // Clear after the test
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

    /**
     * Runs the given [block] with the provided [decorators] installed for the current thread.
     * Decorators are cleared in a `finally` block, guaranteeing no leakage across tests.
     *
     * Example
     * ```kotlin
     * val result = DecoratorManager.withDecorators(HighlighterDecorator()) {
     *     // test body
     *     page.button.click()
     *     42
     * }
     * ```
     *
     * @param T The result type produced by [block].
     * @param decorators The decorators to install while running [block].
     * @param block The code to execute while the [decorators] are active.
     * @return The result returned by [block].
     */
    public inline fun <T> withDecorators(
        vararg decorators: AbstractDecorator,
        block: () -> T,
    ): T {
        try {
            addDecorators(*decorators)
            return block()
        } finally {
            clearDecorators()
        }
    }

    internal fun getAllDecorators(): List<AbstractDecorator> = testLevelDecorators.get().toList()

    internal fun combine(decorators: List<AbstractDecorator>): (SearchContext) -> SearchContext =
        { context ->
            val base: SearchContext =
                if (context is WebDriver) {
                    val listeners = decorators.mapNotNull { (it as? InteractionAware)?.interactionListener() }
                    if (listeners.isNotEmpty()) {
                        @Suppress("UNCHECKED_CAST")
                        EventFiringDecorator<WebDriver>(ListenerMultiplexer(listeners)).decorate(context)
                    } else {
                        context
                    }
                } else {
                    context
                }

            decorators.fold(base) { acc, decorator ->
                decorator.decorate(acc)
            }
        }
}

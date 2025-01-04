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

package dev.kolibrium.selenium.decorators

import org.openqa.selenium.SearchContext

/**
 * A manager for handling test-level decorators in a thread-safe manner.
 * This object allows adding, managing, and clearing decorators that enhance
 * or modify the behavior of Selenium's `SearchContext`.
 */
public object DecoratorManager {
    private val testLevelDecorators = ThreadLocal.withInitial { mutableListOf<AbstractDecorator>() }

    /**
     * Adds one or multiple decorators to the current thread's list of test-level decorators.
     *
     * @param decorators A vararg array of `AbstractDecorator` instances to be added.
     */
    public fun addDecorators(vararg decorators: AbstractDecorator) {
        testLevelDecorators.get().addAll(decorators)
    }

    /**
     * Clears all decorators from the current thread's list of test-level decorators.
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

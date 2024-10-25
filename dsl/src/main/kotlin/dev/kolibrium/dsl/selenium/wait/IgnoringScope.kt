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

package dev.kolibrium.dsl.selenium.wait

import dev.kolibrium.core.InternalKolibriumApi
import dev.kolibrium.dsl.selenium.creation.KolibriumDsl
import kotlin.reflect.KClass

/**
 * Scope class for configuring exceptions to ignore during synchronization operations.
 *
 * This class provides a way to specify which exceptions should be ignored when waiting
 * for synchronization conditions to be met.
 */
@KolibriumDsl
public class IgnoringScope {
    /**
     * A set of exception classes to be ignored during synchronization.
     */
    @InternalKolibriumApi
    public var exceptions: MutableSet<Class<out Throwable>> = mutableSetOf()
        private set

    /**
     * Adds an exception class to the set of ignored exceptions.
     *
     * @param T The type of the exception to be ignored.
     * @param exception The [KClass] of the exception type to be ignored.
     */
    @KolibriumDsl
    public fun <T : Throwable> exception(exception: KClass<T>) {
        exceptions.add(exception.java)
    }

    /**
     * Returns a string representation of the [IgnoringScope], primarily for debugging purposes.
     */
    override fun toString(): String = "IgnoringScope(exceptions=$exceptions)"
}

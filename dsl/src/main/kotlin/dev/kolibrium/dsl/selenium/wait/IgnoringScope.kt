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

@KolibriumDsl
public class IgnoringScope {
    @InternalKolibriumApi
    public var exceptions: MutableSet<Class<out Throwable>> = mutableSetOf()
        private set

    public operator fun <T : Throwable> KClass<T>.unaryPlus() {
        exceptions.add(this.java)
    }

    override fun toString(): String {
        return "IgnoringScope(exceptions=$exceptions)"
    }
}
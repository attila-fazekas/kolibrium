/*
 * Copyright 2023 Attila Fazekas
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

package io.kolibrium.dsl.internal

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

internal fun <T> threadLocalLazyDelegate(provider: (() -> T?)? = null) = ThreadLocalLazyDelegate(provider)

internal class ThreadLocalLazyDelegate<T>(val provider: (() -> T)?) : ReadWriteProperty<Any?, T> {

    private val threadLocal = object : ThreadLocal<T>() {
        override fun initialValue(): T? = provider?.invoke()
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        val value = threadLocal.get()
        threadLocal.remove()
        return value
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        threadLocal.set(value)
    }
}

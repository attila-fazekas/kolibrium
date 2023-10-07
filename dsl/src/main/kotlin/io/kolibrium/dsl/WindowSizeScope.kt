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

package io.kolibrium.dsl

import mu.KotlinLogging
import kotlin.properties.Delegates.vetoable

private const val WIDTH = 1280
private const val HEIGHT = 720

public class WindowSizeScope<T : Browser> {
    private val logger = KotlinLogging.logger { }

    public var width: Int by vetoable(WIDTH) { _, oldValue, newValue ->
        if (newValue < oldValue) {
            logger.debug("Requested window width $newValue < the minimum of $oldValue. Setting width to $oldValue")
            false
        } else {
            true
        }
    }
    public var height: Int by vetoable(HEIGHT) { _, oldValue, newValue ->
        if (newValue < oldValue) {
            logger.debug("Requested window height $newValue < the minimum of $oldValue. Setting height to $oldValue")
            false
        } else {
            true
        }
    }
}

context(ArgumentsScope<Chrome>)
@KolibriumDsl
@JvmName("windowSizeChrome")
public fun windowSize(block: WindowSizeScope<Chromium>.() -> Unit): Unit = setWindowSize(block)

context(ArgumentsScope<Firefox>)
@KolibriumDsl
@JvmName("windowSizeFirefox")
public fun windowSize(block: WindowSizeScope<Firefox>.() -> Unit): Unit = setWindowSize(block)

context(ArgumentsScope<Edge>)
@KolibriumDsl
@JvmName("windowSizeEdge")
public fun windowSize(block: WindowSizeScope<Chromium>.() -> Unit): Unit = setWindowSize(block)

context(ArgumentsScope<*>)
private inline fun <reified T : Browser> setWindowSize(block: WindowSizeScope<T>.() -> Unit) {
    val windowSizeScope = WindowSizeScope<T>().apply(block)
    when (T::class) {
        Chromium::class -> {
            this@ArgumentsScope.args.add(
                Argument<Chromium>("--window-size=${windowSizeScope.width},${windowSizeScope.height}")
            )
        }

        Firefox::class -> {
            this@ArgumentsScope.args.add(Argument<Firefox>("--height=${windowSizeScope.width}"))
            this@ArgumentsScope.args.add(Argument<Firefox>("--width=${windowSizeScope.height}"))
        }
    }
}

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

package io.kolibrium.dsl.selenium.creation

@KolibriumDsl
public class ArgumentsScope<T : Browser> : UnaryPlus<Argument<T>> {

    internal val windowSizeScope by lazy { WindowSizeScope() }

    internal val args = mutableSetOf<Argument<*>>()

    override operator fun Argument<T>.unaryPlus() {
        args.add(this)
    }

    override fun toString(): String {
        return "ArgumentsScope(args=$args)"
    }
}

@KolibriumDsl
@JvmName("windowSizeChrome")
public fun ArgumentsScope<Chrome>.windowSize(block: WindowSizeScope.() -> Unit): Unit = setWindowSize<Chrome>(block)

@KolibriumDsl
@JvmName("windowSizeEdge")
public fun ArgumentsScope<Edge>.windowSize(block: WindowSizeScope.() -> Unit): Unit = setWindowSize<Edge>(block)

@KolibriumDsl
@JvmName("windowSizeFirefox")
public fun ArgumentsScope<Firefox>.windowSize(block: WindowSizeScope.() -> Unit): Unit = setWindowSize<Firefox>(block)

private inline fun <reified T : Browser> ArgumentsScope<*>.setWindowSize(block: WindowSizeScope.() -> Unit) {
    windowSizeScope.apply(block)
    when (T::class) {
        Chrome::class, Edge::class -> {
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

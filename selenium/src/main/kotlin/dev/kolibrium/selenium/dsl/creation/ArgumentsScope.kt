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

package dev.kolibrium.selenium.dsl.creation

import dev.kolibrium.webdriver.InternalKolibriumApi
import dev.kolibrium.webdriver.KolibriumDsl

internal sealed interface ArgumentsScope<T : Argument> {
    operator fun T.unaryPlus()

    operator fun String.unaryPlus()

    fun windowSize(block: WindowSizeScope.() -> Unit)
}

/**
 * Base implementation for browser-specific argument scope classes.
 *
 * This class provides common functionality for collecting and managing browser command-line
 * arguments, including support for adding arguments via the unary plus operator.
 *
 * @param T The browser-specific [Argument] type this scope collects.
 * @param factory Constructor function that creates an instance of [T] from a raw string value.
 * @param scopeName Display name used in [toString] for debugging purposes.
 */
@InternalKolibriumApi
public abstract class AbstractArgumentsScope<T : Argument>(
    private val factory: (String) -> T,
    private val scopeName: String,
) : ArgumentsScope<T> {
    internal val args: MutableSet<T> = mutableSetOf()

    override operator fun T.unaryPlus() {
        args.add(this)
    }

    override operator fun String.unaryPlus() {
        args.add(factory(this))
    }

    protected fun createArg(value: String): T = factory(value)

    override fun toString(): String = "$scopeName(args=$args)"
}

/**
 * Scope class for adding additional Chrome-specific command line arguments.
 */
@KolibriumDsl
public class ChromeArgumentsScope internal constructor() :
    AbstractArgumentsScope<ChromeArgument>(::ChromeArgument, "ChromeArgumentsScope") {
        override fun windowSize(block: WindowSizeScope.() -> Unit) {
            val scope = WindowSizeScope().apply(block)
            args.add(createArg("--window-size=${scope.width},${scope.height}"))
        }
    }

/**
 * Scope class for adding additional Firefox-specific command line arguments.
 */
@KolibriumDsl
public class FirefoxArgumentsScope internal constructor() :
    AbstractArgumentsScope<FirefoxArgument>(::FirefoxArgument, "FirefoxArgumentsScope") {
        override fun windowSize(block: WindowSizeScope.() -> Unit) {
            val scope = WindowSizeScope().apply(block)
            +(createArg("--width=${scope.width}"))
            +(createArg("--height=${scope.height}"))
        }
    }

/**
 * Scope class for adding additional Edge-specific command line arguments.
 */
@KolibriumDsl
public class EdgeArgumentsScope internal constructor() : AbstractArgumentsScope<EdgeArgument>(::EdgeArgument, "EdgeArgumentsScope") {
    override fun windowSize(block: WindowSizeScope.() -> Unit) {
        val scope = WindowSizeScope().apply(block)
        args.add(createArg("--window-size=${scope.width},${scope.height}"))
    }
}

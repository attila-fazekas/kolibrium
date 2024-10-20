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

package dev.kolibrium.dsl.selenium.creation

/**
 * Base interface for adding additional command line arguments to be used when starting the browser.
 */
internal sealed interface ArgumentsScope {
    /**
     * Configures the browser window size.
     *
     * @param block The configuration block for window dimensions.
     */
    fun windowSize(block: WindowSizeScope.() -> Unit)

    fun argument(argument: Argument)
}

/**
 * Scope class for adding additional Chrome-specific command line arguments.
 */
@KolibriumDsl
public class ChromeArgumentsScope : ArgumentsScope {
    internal val args: MutableSet<Argument> = mutableSetOf()

    /**
     * Adds a Chrome-specific command line argument.
     */
    @KolibriumDsl
    override fun argument(argument: Argument) {
        args.add(argument)
    }

    /**
     * Configures the Chrome browser window size.
     *
     * @param block The configuration block for window dimensions.
     */
    @KolibriumDsl
    override fun windowSize(block: WindowSizeScope.() -> Unit) {
        val windowSizeScope = WindowSizeScope().apply(block)
        args.add(ChromeArgument.of("--window-size=${windowSizeScope.width},${windowSizeScope.height}"))
    }

    /**
     * Returns a string representation of the [ChromeArgumentsScope], primarily for debugging purposes.
     */
    override fun toString(): String = "ChromeArgumentsScope(args=$args)"
}

/**
 * Scope class for adding additional Firefox-specific command line arguments.
 */
@KolibriumDsl
public class FirefoxArgumentsScope : ArgumentsScope {
    internal val args: MutableSet<Argument> = mutableSetOf()

    /**
     * Adds a Firefox-specific command line argument.
     */
    @KolibriumDsl
    override fun argument(argument: Argument) {
        args.add(argument)
    }

    /**
     * Configures the Firefox browser window size.
     *
     * @param block The configuration block for window dimensions.
     */
    @KolibriumDsl
    override fun windowSize(block: WindowSizeScope.() -> Unit) {
        val windowSizeScope = WindowSizeScope().apply(block)
        argument(FirefoxArgument.of("--width=${windowSizeScope.width}"))
        argument(FirefoxArgument.of("--height=${windowSizeScope.height}"))
    }

    /**
     * Returns a string representation of the [FirefoxArgumentsScope], primarily for debugging purposes.
     */
    override fun toString(): String = "FirefoxArgumentsScope(args=$args)"
}

/**
 * Scope class for adding additional Edge-specific command line arguments.
 */
@KolibriumDsl
public class EdgeArgumentsScope : ArgumentsScope {
    internal val args: MutableSet<Argument> = mutableSetOf()

    /**
     * Adds an Edge-specific command line argument.
     */
    @KolibriumDsl
    override fun argument(argument: Argument) {
        args.add(argument)
    }

    /**
     * Configures the Edge browser window size.
     *
     * @param block The configuration block for window dimensions.
     */
    @KolibriumDsl
    override fun windowSize(block: WindowSizeScope.() -> Unit) {
        val windowSizeScope = WindowSizeScope().apply(block)
        argument(EdgeArgument.of("--window-size=${windowSizeScope.width},${windowSizeScope.height}"))
    }

    /**
     * Returns a string representation of the [EdgeArgumentsScope], primarily for debugging purposes.
     */
    override fun toString(): String = "EdgeArgumentsScope(args=$args)"
}

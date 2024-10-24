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
internal sealed interface ArgumentsScope<T : Argument> {
    fun windowSize(block: WindowSizeScope.() -> Unit)

    fun arg(argument: T)

    fun arg(value: String)
}

/**
 * Scope class for adding additional Chrome-specific command line arguments.
 */
@KolibriumDsl
public class ChromeArgumentsScope : ArgumentsScope<ChromeArgument> {
    internal val args: MutableSet<Argument> = mutableSetOf()

    /**
     * Adds a Chrome-specific command line [Argument].
     *
     * @param argument The Chrome argument to be added.
     */
    @KolibriumDsl
    override fun arg(argument: ChromeArgument) {
        args.add(argument)
    }

    /**
     * Adds a Chrome-specific command line argument from a string value.
     * The string must start with "--".
     *
     * @param value The argument string value.
     * @throws IllegalArgumentException if the value doesn't start with "--".
     */
    @KolibriumDsl
    override fun arg(value: String) {
        args.add(ChromeArgument(value))
    }

    /**
     * Configures the Chrome browser window size.
     *
     * @param block The configuration block for window dimensions.
     */
    @KolibriumDsl
    override fun windowSize(block: WindowSizeScope.() -> Unit) {
        val windowSizeScope = WindowSizeScope().apply(block)
        args.add(ChromeArgument("--window-size=${windowSizeScope.width},${windowSizeScope.height}"))
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
public class FirefoxArgumentsScope : ArgumentsScope<FirefoxArgument> {
    internal val args: MutableSet<Argument> = mutableSetOf()

    /**
     * Adds a Firefox-specific command line [Argument].
     *
     * @param argument The Chrome argument to be added.
     */
    @KolibriumDsl
    override fun arg(argument: FirefoxArgument) {
        args.add(argument)
    }

    /**
     * Adds a Firefox-specific command line argument from a string value.
     * The string must start with "--".
     *
     * @param value The argument string value.
     * @throws IllegalArgumentException if the value doesn't start with "--".
     */
    @KolibriumDsl
    override fun arg(value: String) {
        args.add(FirefoxArgument(value))
    }

    /**
     * Configures the Firefox browser window size.
     *
     * @param block The configuration block for window dimensions.
     */
    @KolibriumDsl
    override fun windowSize(block: WindowSizeScope.() -> Unit) {
        val windowSizeScope = WindowSizeScope().apply(block)
        arg(FirefoxArgument("--width=${windowSizeScope.width}"))
        arg(FirefoxArgument("--height=${windowSizeScope.height}"))
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
public class EdgeArgumentsScope : ArgumentsScope<EdgeArgument> {
    internal val args: MutableSet<Argument> = mutableSetOf()

    /**
     * Adds an Edge-specific command line [Argument].
     *
     * @param argument The Chrome argument to be added.
     */
    @KolibriumDsl
    override fun arg(argument: EdgeArgument) {
        args.add(argument)
    }

    /**
     * Adds an Edge-specific command line argument from a string value.
     * The string must start with "--".
     *
     * @param value The argument string value.
     * @throws IllegalArgumentException if the value doesn't start with "--".
     */
    @KolibriumDsl
    override fun arg(value: String) {
        args.add(FirefoxArgument(value))
    }

    /**
     * Configures the Edge browser window size.
     *
     * @param block The configuration block for window dimensions.
     */
    @KolibriumDsl
    override fun windowSize(block: WindowSizeScope.() -> Unit) {
        val windowSizeScope = WindowSizeScope().apply(block)
        arg(EdgeArgument("--window-size=${windowSizeScope.width},${windowSizeScope.height}"))
    }

    /**
     * Returns a string representation of the [EdgeArgumentsScope], primarily for debugging purposes.
     */
    override fun toString(): String = "EdgeArgumentsScope(args=$args)"
}

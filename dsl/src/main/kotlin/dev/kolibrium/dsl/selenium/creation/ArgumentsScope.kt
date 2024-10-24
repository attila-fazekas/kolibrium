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

internal sealed interface ArgumentsScope<T : Argument> {
    operator fun String.unaryPlus()

    operator fun T.unaryPlus()

    fun windowSize(block: WindowSizeScope.() -> Unit)
}

/**
 * Scope class for adding additional Chrome-specific command line arguments.
 */
@KolibriumDsl
public class ChromeArgumentsScope : ArgumentsScope<ChromeArgument> {
    internal val args: MutableSet<ChromeArgument> = mutableSetOf()

    /**
     * Adds a Chrome-specific command line [Argument].
     *
     * This operator function allows adding Chrome-specific command line arguments using the unary plus operator (+).
     */
    public override operator fun ChromeArgument.unaryPlus() {
        args.add(this)
    }

    /**
     * Adds a Chrome-specific command line argument from a string value.
     * The string must start with "--".
     *
     * This operator function allows adding Chrome-specific command line arguments from string values
     * using the unary plus operator (+).
     *
     * @throws IllegalArgumentException if the string doesn't start with "--".
     */
    public override operator fun String.unaryPlus() {
        args.add(ChromeArgument(this))
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
    internal val args: MutableSet<FirefoxArgument> = mutableSetOf()

    /**
     * Adds a Firefox-specific command line [Argument].
     *
     * This operator function allows adding Firefox-specific command line arguments using the unary plus operator (+).
     */
    public override operator fun FirefoxArgument.unaryPlus() {
        args.add(this)
    }

    /**
     * Adds a Firefox-specific command line argument from a string value.
     * The string must start with "--".
     *
     * This operator function allows adding Firefox-specific command line arguments from string values
     * using the unary plus operator (+).
     *
     * @throws IllegalArgumentException if the string doesn't start with "--".
     */
    public override operator fun String.unaryPlus() {
        args.add(FirefoxArgument(this))
    }

    /**
     * Configures the Firefox browser window size.
     *
     * @param block The configuration block for window dimensions.
     */
    @KolibriumDsl
    override fun windowSize(block: WindowSizeScope.() -> Unit) {
        val windowSizeScope = WindowSizeScope().apply(block)
        +(FirefoxArgument("--width=${windowSizeScope.width}"))
        +(FirefoxArgument("--height=${windowSizeScope.height}"))
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
    internal val args: MutableSet<EdgeArgument> = mutableSetOf()

    /**
     * Adds an Edge-specific command line [Argument].
     *
     * This operator function allows adding Edge-specific command line arguments using the unary plus operator (+).
     */
    public override operator fun EdgeArgument.unaryPlus() {
        args.add(this)
    }

    /**
     * Adds an Edge-specific command line argument from a string value.
     * The string must start with "--".
     *
     * This operator function allows adding Edge-specific command line arguments from string values
     * using the unary plus operator (+).
     *
     * @throws IllegalArgumentException +if the string doesn't start with "--".
     */
    public override operator fun String.unaryPlus() {
        args.add(EdgeArgument(this))
    }

    /**
     * Configures the Edge browser window size.
     *
     * @param block The configuration block for window dimensions.
     */
    @KolibriumDsl
    override fun windowSize(block: WindowSizeScope.() -> Unit) {
        val windowSizeScope = WindowSizeScope().apply(block)
        +(EdgeArgument("--window-size=${windowSizeScope.width},${windowSizeScope.height}"))
    }

    /**
     * Returns a string representation of the [EdgeArgumentsScope], primarily for debugging purposes.
     */
    override fun toString(): String = "EdgeArgumentsScope(args=$args)"
}

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

public sealed interface ArgumentsScope {
    public val args: MutableSet<Argument>

    @KolibriumDsl
    public fun windowSize(block: WindowSizeScope.() -> Unit)
}

@KolibriumDsl
public class ChromeArgumentsScope : ArgumentsScope {
    public override val args: MutableSet<Argument> = mutableSetOf()

    public operator fun ChromeArgument.unaryPlus() {
        args.add(this)
    }

    @KolibriumDsl
    public override fun windowSize(block: WindowSizeScope.() -> Unit) {
        val windowSizeScope = WindowSizeScope().apply(block)
        +ChromeArgument.of("--window-size=${windowSizeScope.width},${windowSizeScope.height}")
    }

    override fun toString(): String = "ChromeArgumentsScope(args=$args)"
}

@KolibriumDsl
public class FirefoxArgumentsScope : ArgumentsScope {
    override val args: MutableSet<Argument> = mutableSetOf()

    public operator fun FirefoxArgument.unaryPlus() {
        args.add(this)
    }

    @KolibriumDsl
    public override fun windowSize(block: WindowSizeScope.() -> Unit) {
        val windowSizeScope = WindowSizeScope().apply(block)
        +FirefoxArgument.of("--width=${windowSizeScope.width}")
        +FirefoxArgument.of("--height=${windowSizeScope.height}")
    }

    override fun toString(): String = "FirefoxArgumentsScope(args=$args)"
}

@KolibriumDsl
public class EdgeArgumentsScope : ArgumentsScope {
    public override val args: MutableSet<Argument> = mutableSetOf()

    public operator fun EdgeArgument.unaryPlus() {
        args.add(this)
    }

    @KolibriumDsl
    public override fun windowSize(block: WindowSizeScope.() -> Unit) {
        val windowSizeScope = WindowSizeScope().apply(block)
        +EdgeArgument.of("--window-size=${windowSizeScope.width},${windowSizeScope.height}")
    }

    override fun toString(): String = "EdgeArgumentsScope(args=$args)"
}

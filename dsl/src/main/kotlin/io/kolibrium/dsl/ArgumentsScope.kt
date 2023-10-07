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

import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.edge.EdgeOptions
import org.openqa.selenium.firefox.FirefoxOptions
import org.openqa.selenium.remote.AbstractDriverOptions

public class ArgumentsScope<T : Browser> : UnaryPlus<Argument<T>> {
    internal val args = mutableSetOf<Argument<*>>()

    override operator fun Argument<T>.unaryPlus() {
        args.add(this)
    }
}

@KolibriumDsl
@JvmName("argumentsChrome")
public fun OptionsScope<Chrome>.arguments(block: ArgumentsScope<Chrome>.() -> Unit):
    Unit = arguments(options, block)

@KolibriumDsl
@JvmName("argumentsChrome")
public fun DriverScope<Chrome>.OptionsScope.arguments(block: ArgumentsScope<Chrome>.() -> Unit):
    Unit = arguments(options, block)

@KolibriumDsl
@JvmName("argumentsFirefox")
public fun OptionsScope<Firefox>.arguments(block: ArgumentsScope<Firefox>.() -> Unit):
    Unit = arguments(options, block)

@KolibriumDsl
@JvmName("argumentsFirefox")
public fun DriverScope<Firefox>.OptionsScope.arguments(block: ArgumentsScope<Firefox>.() -> Unit):
    Unit = arguments(options, block)

@KolibriumDsl
@JvmName("argumentsEdge")
public fun OptionsScope<Edge>.arguments(block: ArgumentsScope<Edge>.() -> Unit):
    Unit = arguments(options, block)

@KolibriumDsl
@JvmName("argumentsEdge")
public fun DriverScope<Edge>.OptionsScope.arguments(block: ArgumentsScope<Edge>.() -> Unit):
    Unit = arguments(options, block)

private fun <T : Browser> arguments(options: AbstractDriverOptions<*>, block: ArgumentsScope<T>.() -> Unit) {
    val argsScope = ArgumentsScope<T>().apply(block)
    when (options) {
        is ChromeOptions -> options.addArguments(argsScope.args.map { it.name })

        is FirefoxOptions -> options.addArguments(argsScope.args.map { it.name })

        is EdgeOptions -> options.addArguments(argsScope.args.map { it.name })
    }
}

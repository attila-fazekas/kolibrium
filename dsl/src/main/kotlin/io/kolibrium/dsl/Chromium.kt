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

@file:Suppress("TooManyFunctions")

package io.kolibrium.dsl

import io.kolibrium.dsl.chrome.ExperimentalOptionsScope
import io.kolibrium.dsl.chrome.ExtensionsScope
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.edge.EdgeDriver
import org.openqa.selenium.edge.EdgeOptions
import org.openqa.selenium.remote.AbstractDriverOptions

context(OptionsScope<ChromeOptions>, ArgumentsScope)
@KolibriumDsl
public fun windowSize(block: WindowSizeScope.() -> Unit): Unit = setWindowSize(block)

context(DriverScope<ChromeDriver>, ArgumentsScope)
@KolibriumDsl
public fun windowSize(block: WindowSizeScope.() -> Unit): Unit = setWindowSize(block)

context(OptionsScope<EdgeOptions>, ArgumentsScope)
@KolibriumDsl
@JvmName("windowSizeEdge")
public fun windowSize(block: WindowSizeScope.() -> Unit): Unit = setWindowSize(block)

context(DriverScope<EdgeDriver>, ArgumentsScope)
@KolibriumDsl
@JvmName("windowSizeEdge")
public fun windowSize(block: WindowSizeScope.() -> Unit): Unit = setWindowSize(block)

context(ArgumentsScope)
private fun setWindowSize(block: WindowSizeScope.() -> Unit) {
    val windowSizeScope = WindowSizeScope().apply(block)
    this@ArgumentsScope.args.add(Argument("--window-size=${windowSizeScope.width},${windowSizeScope.height}"))
}

@KolibriumDsl
public fun OptionsScope<ChromeOptions>.experimentalOptions(block: ExperimentalOptionsScope.() -> Unit): Unit =
    experimentalOptions(options, block)

@KolibriumDsl
public fun DriverScope<ChromeDriver>.OptionsScope.experimentalOptions(block: ExperimentalOptionsScope.() -> Unit):
    Unit = experimentalOptions(options, block)

@KolibriumDsl
@JvmName("experimentalOptionsEdge")
public fun OptionsScope<EdgeOptions>.experimentalOptions(block: ExperimentalOptionsScope.() -> Unit): Unit =
    experimentalOptions(options, block)

@KolibriumDsl
@JvmName("experimentalOptionsEdge")
public fun DriverScope<EdgeDriver>.OptionsScope.experimentalOptions(block: ExperimentalOptionsScope.() -> Unit):
    Unit = experimentalOptions(options, block)

private fun experimentalOptions(options: AbstractDriverOptions<*>, block: ExperimentalOptionsScope.() -> Unit) {
    val expOptionsScope = ExperimentalOptionsScope().apply(block)
    with(expOptionsScope) {
        when (options) {
            is ChromeOptions -> {
                if (preferencesScope.preferences.isNotEmpty()) {
                    options.setExperimentalOption("prefs", preferencesScope.preferences)
                }
                if (switchesScope.switches.isNotEmpty()) {
                    options.setExperimentalOption("excludeSwitches", switchesScope.switches)
                }
                if (localStateScope.localStatePrefs.isNotEmpty()) {
                    options.setExperimentalOption("localState", localStateScope.localStatePrefs)
                }
            }

            is EdgeOptions -> {
                if (preferencesScope.preferences.isNotEmpty()) {
                    options.setExperimentalOption("prefs", preferencesScope.preferences)
                }
                if (switchesScope.switches.isNotEmpty()) {
                    options.setExperimentalOption("excludeSwitches", switchesScope.switches)
                }
                if (localStateScope.localStatePrefs.isNotEmpty()) {
                    options.setExperimentalOption("localState", localStateScope.localStatePrefs)
                }
            }
        }
    }
}

@KolibriumDsl
public fun OptionsScope<ChromeOptions>.extensions(block: ExtensionsScope.() -> Unit): Unit = extensions(options, block)

@KolibriumDsl
public fun DriverScope<ChromeDriver>.OptionsScope.extensions(block: ExtensionsScope.() -> Unit): Unit =
    extensions(options, block)

@KolibriumDsl
@JvmName("extensionsEdge")
public fun OptionsScope<EdgeOptions>.extensions(block: ExtensionsScope.() -> Unit): Unit = extensions(options, block)

@KolibriumDsl
@JvmName("extensionsEdge")
public fun DriverScope<EdgeDriver>.OptionsScope.extensions(block: ExtensionsScope.() -> Unit): Unit =
    extensions(options, block)

private fun extensions(options: AbstractDriverOptions<*>, block: ExtensionsScope.() -> Unit) {
    val extensionsScope = ExtensionsScope().apply(block)
    when (options) {
        is ChromeOptions -> options.addExtensions(extensionsScope.extensions.toList())

        is EdgeOptions -> options.addExtensions(extensionsScope.extensions.toList())
    }
}

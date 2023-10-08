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

package io.kolibrium.dsl.chromium

import io.kolibrium.dsl.Chrome
import io.kolibrium.dsl.DriverScope
import io.kolibrium.dsl.Edge
import io.kolibrium.dsl.KolibriumDsl
import io.kolibrium.dsl.OptionsScope
import io.kolibrium.dsl.UnaryPlus
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.edge.EdgeOptions
import org.openqa.selenium.remote.AbstractDriverOptions
import java.io.File

@JvmInline
public value class Extension(public val path: String)

@KolibriumDsl
public class ExtensionsScope : UnaryPlus<Extension> {
    internal val extensions = mutableSetOf<File>()

    override operator fun Extension.unaryPlus() {
        extensions.add(File(this.path))
    }
}

@KolibriumDsl
@JvmName("extensionsChrome")
public fun OptionsScope<Chrome>.extensions(block: ExtensionsScope.() -> Unit): Unit = extensions(options, block)

@KolibriumDsl
@JvmName("extensionsChrome")
public fun DriverScope<Chrome>.OptionsScope.extensions(block: ExtensionsScope.() -> Unit): Unit =
    extensions(options, block)

@KolibriumDsl
@JvmName("extensionsEdge")
public fun OptionsScope<Edge>.extensions(block: ExtensionsScope.() -> Unit): Unit = extensions(options, block)

@KolibriumDsl
@JvmName("extensionsEdge")
public fun DriverScope<Edge>.OptionsScope.extensions(block: ExtensionsScope.() -> Unit): Unit =
    extensions(options, block)

private fun extensions(options: AbstractDriverOptions<*>, block: ExtensionsScope.() -> Unit) {
    val extensionsScope = ExtensionsScope().apply(block)
    when (options) {
        is ChromeOptions -> options.addExtensions(extensionsScope.extensions.toList())

        is EdgeOptions -> options.addExtensions(extensionsScope.extensions.toList())
    }
}

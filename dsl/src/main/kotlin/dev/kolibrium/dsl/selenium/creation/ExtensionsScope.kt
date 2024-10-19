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

import java.io.File

/**
 * Represents a browser extension to be loaded.
 *
 * @property path The file system path to the extension.
 */
@JvmInline
public value class Extension(
    internal val path: String,
)

/**
 * Scope for configuring browser extensions.
 */
@KolibriumDsl
public class ExtensionsScope {
    internal val extensions = mutableSetOf<File>()

    /**
     * Adds an extension to be loaded by the browser.
     *
     * This operator function allows adding extensions using the unary plus operator (+).
     */
    public operator fun Extension.unaryPlus() {
        extensions.add(File(path))
    }

    /**
     * Returns a string representation of the [ExtensionsScope], primarily for debugging purposes.
     */
    override fun toString(): String = "ExtensionsScope(extensions=$extensions)"
}

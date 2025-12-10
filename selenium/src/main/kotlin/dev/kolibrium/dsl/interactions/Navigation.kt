/*
 * Copyright 2023-2025 Attila Fazekas & contributors
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

package dev.kolibrium.dsl.interactions

import dev.kolibrium.dsl.KolibriumDsl
import dev.kolibrium.dsl.internal.normalizePath
import org.openqa.selenium.WebDriver

/**
 * Navigates to a path resolved against the current origin (scheme + authority).
 *
 * Relative paths are normalized and resolved from the origin, ignoring the current page path.
 * Absolute URLs (http/https) are navigated to directly.
 *
 * @receiver The [WebDriver] instance to perform navigation with.
 * @param relativePath The path to navigate to; may be a relative path or an absolute URL.
 */
@KolibriumDsl
public fun WebDriver.navigateTo(relativePath: String) {
    val path = relativePath.trim()
    if (path.startsWith("http://") || path.startsWith("https://")) {
        get(path)
        return
    }

    val current = java.net.URI(currentUrl)
    val origin = java.net.URI("${current.scheme}://${current.authority}/")

    val normalizedPath = normalizePath(path)

    val resolved = origin.resolve(normalizedPath)
    get(resolved.toString())
}

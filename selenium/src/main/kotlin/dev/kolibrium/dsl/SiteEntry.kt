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

package dev.kolibrium.dsl

import dev.kolibrium.core.Page
import dev.kolibrium.core.Site
import dev.kolibrium.dsl.PageScope
import org.openqa.selenium.Cookie

/**
 * Site-scoped DSL receiver available inside `webTest { … }` blocks and within `switchTo<S>() { … }`.
 *
 * It represents the entry surface for flows on the active [Site], exposing operations like [open], [on],
 * cookie helpers, and site/window switching through higher-level DSL.
 *
 * Notes
 * - Implemented internally by Kolibrium; end users receive this as the receiver of [webTest] blocks.
 * - Backed by an internal implementation that binds to a live WebDriver session; the driver remains hidden.
 */
@KolibriumDsl
public interface SiteEntry<S : Site> {
    /** Add a cookie to the current browser session. */
    public fun addCookie(cookie: Cookie)

    /** Delete a cookie by name. */
    public fun deleteCookie(name: String)

    /** Delete all cookies. */
    public fun deleteAllCookies()

    /**
     * Navigate to the page created by [factory] and run [action] on it, returning the next page scope.
     */
    public fun <P : Page<S>, R : Page<S>> open(
        factory: () -> P,
        path: String? = null,
        action: P.() -> R,
    ): PageScope<R>

    /**
     * Bind a page created by [factory] to the current tab (no navigation) and run [action].
     */
    public fun <P : Page<S>, R : Page<S>> on(
        factory: () -> P,
        action: P.() -> R,
    ): PageScope<R>
}

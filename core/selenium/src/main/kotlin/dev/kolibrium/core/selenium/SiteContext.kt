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

package dev.kolibrium.core.selenium

import dev.kolibrium.common.InternalKolibriumApi

/**
 * Runtime holder for the currently active [Site].
 *
 * This uses a thread-local to isolate site configuration per test thread.
 */
@InternalKolibriumApi
public object SiteContext {
    private val current: ThreadLocal<Site?> = ThreadLocal.withInitial { null }

    /**
     * Returns the currently active Site for this thread, or null if none is set.
     *
     * Used by locator utilities and decorators to resolve defaults (waitConfig, readiness conditions, etc.).
     *
     * @return the Site bound to the current thread, or null when no site context is active
     */
    public fun get(): Site? = current.get()

    /**
     * Sets the current Site for this thread.
     *
     * Prefer using [withSite] to ensure the previous context is restored automatically.
     *
     * @param site the Site to bind to the current thread; pass null to clear
     */
    public fun set(site: Site?) {
        current.set(site)
    }

    /**
     * Executes [block] with [site] bound to the current thread's SiteContext.
     *
     * Restores the previously active Site (which may be null) after [block] completes,
     * even if an exception is thrown.
     *
     * This is intended for scoped, temporary context changes. For a persistent switch that
     * outlives a block, use [set].
     *
     * @param T The result type produced by [block].
     * @param site The Site to bind while executing [block].
     * @param block The code to run under the provided [site].
     * @return The result produced by [block].
     */
    public fun <T> withSite(
        site: Site,
        block: () -> T,
    ): T {
        val previous = current.get()
        current.set(site)
        return try {
            block()
        } finally {
            current.set(previous)
        }
    }
}

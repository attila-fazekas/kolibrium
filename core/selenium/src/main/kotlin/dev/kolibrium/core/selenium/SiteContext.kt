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

/**
 * Runtime holder for the currently active [Site].
 *
 * This uses a thread-local to isolate site configuration per test thread.
 */
public object SiteContext {
    private val current: ThreadLocal<Site?> = ThreadLocal.withInitial { null }

    /**
     * Returns the currently active [Site] for this thread, or null if none is set.
     */
    public fun get(): Site? = current.get()

    /**
     * Sets the current [Site] for this thread. Prefer using [withSite] when possible.
     */
    public fun set(site: Site?) {
        current.set(site)
    }

    /**
     * Executes [block] with [site] bound to the current thread's SiteContext.
     * Restores the previous value when finished (even on exception).
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

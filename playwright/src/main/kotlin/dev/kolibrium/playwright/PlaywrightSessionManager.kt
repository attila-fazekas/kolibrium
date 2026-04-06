/*
 * Copyright 2023-2026 Attila Fazekas & contributors
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

package dev.kolibrium.playwright

import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.Page

/**
 * Per-session runtime context for the Playwright module.
 *
 * Holds the Playwright [page], [context], and the [site] configuration the session operates under.
 * Each session is strictly confined to the thread that created it; call [assertThreadOrFail] before
 * every Playwright operation to enforce this.
 *
 * Thread-safety: not thread-safe. Each session is confined to the thread that created it.
 */
internal class PlaywrightSession internal constructor(
    val page: Page,
    val context: BrowserContext,
    val site: PlaywrightSite,
) {
    internal val owningThread: Thread = Thread.currentThread()

    /**
     * Asserts that the current call happens on the [owningThread] of this session.
     *
     * Playwright objects are strictly thread-confined: a session must only be used
     * from the thread that created it. Call this guard before performing operations
     * that rely on that confinement.
     *
     * @param op A short operation name used to enrich the failure message
     *           (e.g., "page.locator" or "ensureReady").
     * @throws IllegalStateException if invoked from a thread different from [owningThread].
     */
    internal fun assertThreadOrFail(op: String) {
        val current = Thread.currentThread()
        check(current === owningThread) {
            "Kolibrium thread confinement violation: '$op' was called from a different thread. " +
                "Owning thread='${owningThread.name}', current thread='${current.name}'. " +
                "Playwright sessions are single-threaded."
        }
    }
}

internal object PlaywrightSessionContext {
    private val tl: ThreadLocal<PlaywrightSession?> = ThreadLocal()

    internal fun get(): PlaywrightSession? = tl.get()

    internal fun <T> withSession(
        session: PlaywrightSession,
        block: () -> T,
    ): T {
        val prev = tl.get()
        tl.set(session)
        return try {
            block()
        } finally {
            if (prev == null) tl.remove() else tl.set(prev)
        }
    }
}

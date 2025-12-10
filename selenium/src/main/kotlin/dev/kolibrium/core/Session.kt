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

package dev.kolibrium.core

import org.openqa.selenium.WebDriver

/**
 * Per-driver runtime context. Holds the Selenium [driver] and the immutable [site]
 * configuration the session operates under.
 *
 * Thread-safety: not thread-safe. Each session is confined to the thread that created it.
 */
@InternalKolibriumApi
public class Session(
    public val driver: WebDriver,
    public val site: Site,
) {
    internal val owningThread: Thread = Thread.currentThread()

    /**
     * Asserts that the current call happens on the [owningThread] of this [Session].
     *
     * Kolibrium sessions are strictly thread-confined: a session must only be used
     * from the thread that created it. Call this guard before performing operations
     * that rely on that confinement (e.g., interacting with [driver] or page objects).
     *
     * @param op A short operation name used to enrich the failure message, making it
     * more obvious which action violated thread confinement (for example, "click"
     * or "findElement").
     *
     * @throws IllegalStateException if invoked from a thread different from [owningThread].
     */
    @InternalKolibriumApi
    public fun assertThreadOrFail(op: String) {
        val current = Thread.currentThread()
        check(current === owningThread) {
            "Kolibrium thread confinement violation: $op was called from a different thread. " +
                "Owning thread='${owningThread.name}', current thread='${current.name}'. One Session per thread."
        }
    }
}

/** Simple ThreadLocal holder for the current [Session]. */
@InternalKolibriumApi
public object SessionContext {
    private val tl: ThreadLocal<Session?> = ThreadLocal()

    /** Returns the current [Session] for this thread, or null if none is set. */
    @InternalKolibriumApi
    @JvmStatic
    public fun get(): Session? = tl.get()

    /** Sets the current [Session] for this thread. Prefer [withSession] where possible. */
    @InternalKolibriumApi
    @JvmStatic
    public fun set(session: Session?) {
        tl.set(session)
    }

    /** Clears the current [Session] for this thread. */
    @InternalKolibriumApi
    @JvmStatic
    public fun clear() {
        tl.remove()
    }

    /** Runs [block] with [session] bound to the current thread, restoring the previous value afterwards. */
    @InternalKolibriumApi
    @JvmStatic
    public fun <T> withSession(
        session: Session,
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

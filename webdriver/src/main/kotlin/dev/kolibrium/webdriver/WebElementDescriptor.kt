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

package dev.kolibrium.webdriver

import org.openqa.selenium.WebElement
import kotlin.properties.ReadOnlyProperty

/**
 * Descriptor for a single Selenium [WebElement] found by a locator delegate.
 *
 * Exposes the underlying [org.openqa.selenium.By] for debugging, optional wait configuration, and an optional readiness
 * predicate. It also acts as a Kotlin [ReadOnlyProperty] to enable `by ...` delegation in pages.
 *
 * Thread-safety: Descriptors may cache resolved elements for performance and are not thread-safe.
 * They are intended for single-threaded test usage (typical for page objects). Avoid sharing the same
 * descriptor instance across threads. If you must access from multiple threads, create separate owning
 * page instances or disable caching via `cacheLookup = false`.
 *
 * Waiting semantics
 * - During waits, Kolibrium always ignores [org.openqa.selenium.NoSuchElementException] as a minimal baseline,
 *   even if your [WaitConfig] does not include it in `ignoring`. This prevents failing fast while elements
 *   are still appearing in the DOM. You can still add more ignored exceptions via [WaitConfig.ignoring].
 *
 * toString() expectations
 * - Calling toString() on a descriptor yields a stable, human-friendly summary including:
 *   ctx, by, cacheLookup and waitConfig=(timeout=..., polling=...), plus a decorators field.
 *   Values not applicable are shown as "N/A". ctx shows the underlying, undecorated [org.openqa.selenium.SearchContext] type.
 *   decorators is always present: it's a class list like [HighlighterDecorator, SlowMotionDecorator] when
 *   any decorators are applied, or "N/A" when none are applied.
 * - Important: Calling toString() on the delegated [WebElement] (e.g., val e by id("..."); e.toString())
 *   will print Selenium's element string, not the descriptor summary. Keep a reference to the descriptor
 *   itself if you need its diagnostic string.
 */
public interface WebElementDescriptor :
    ReadOnlyProperty<Any?, WebElement>,
    HasBy {
    /** Resolve the WebElement immediately, applying wait and readiness checks. */
    public fun get(): WebElement
}

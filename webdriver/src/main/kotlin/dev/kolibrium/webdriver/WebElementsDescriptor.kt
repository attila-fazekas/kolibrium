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

import kotlin.properties.ReadOnlyProperty

/**
 * Descriptor for a list-like collection of [WebElements] found by a locator delegate.
 *
 * Provides the [org.openqa.selenium.By] locator for diagnostics, an optional wait configuration, and an optional
 * collection-level readiness predicate. Also acts as a [ReadOnlyProperty] for delegation.
 *
 * Thread-safety and caching
 * - Multi-element delegates always perform a fresh lookup and are not cached.
 * - They are intended for single-threaded test usage (typical for page objects). Avoid sharing the same
 *   descriptor instance across threads.
 *
 * Waiting semantics
 * - During waits, Kolibrium always ignores [org.openqa.selenium.NoSuchElementException] as a minimal baseline,
 *   even if your [WaitConfig] does not include it in `ignoring`. This prevents failing fast while elements
 *   are still appearing in the DOM. You can still add more ignored exceptions via [WaitConfig.ignoring].
 *
 * toString() expectations
 * - Calling toString() on a descriptor yields a stable, human-friendly summary including:
 *   ctx, by and waitConfig=(timeout=..., polling=...), plus a decorators field.
 *   Values not applicable are shown as "N/A". ctx shows the underlying, undecorated [org.openqa.selenium.SearchContext] type.
 *   decorators is always present: it's a class list like [HighlighterDecorator, SlowMotionDecorator] when
 *   any decorators are applied, or "N/A" when none are applied.
 * - Calling toString() on the delegated [WebElements] value prints Selenium's collection string, not
 *   the descriptor's summary. Keep a reference to the descriptor if you need its diagnostics.
 */
public interface WebElementsDescriptor :
    ReadOnlyProperty<Any?, WebElements>,
    HasBy {
    /** Resolve the collection immediately, applying wait and readiness checks. */
    public fun get(): WebElements
}

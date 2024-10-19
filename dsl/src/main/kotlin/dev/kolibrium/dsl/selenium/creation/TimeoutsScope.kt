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

import kotlin.time.Duration

/**
 * Scope for configuring timeout settings.
 */
@KolibriumDsl
public class TimeoutsScope {
    /**
     * Sets the time to wait for the implicit element location strategy when locating elements.
     */
    @KolibriumPropertyDsl
    public var implicitWait: Duration? = null

    /**
     * Sets the time interval within which a web page must be loaded in the current browsing context.
     */
    @KolibriumPropertyDsl
    public var pageLoad: Duration? = null

    /**
     * Sets the timeout for interrupting an executing script in the current browsing context.
     */
    @KolibriumPropertyDsl
    public var script: Duration? = null

    /**
     * Returns a string representation of the [TimeoutsScope], primarily for debugging purposes.
     */
    override fun toString(): String = "TimeoutsScope(implicitWait=$implicitWait, pageLoad=$pageLoad, script=$script)"
}

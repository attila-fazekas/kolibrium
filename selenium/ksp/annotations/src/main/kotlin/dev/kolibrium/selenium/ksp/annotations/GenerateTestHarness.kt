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

package dev.kolibrium.selenium.ksp.annotations

/**
 * Configures test harness generation for a [SeleniumSite][dev.kolibrium.selenium.core.SeleniumSite] subclass.
 *
 * This annotation is required on every `SeleniumSite` object to trigger test harness generation.
 * The processor generates two overloads of `<siteName>Test(…)` that delegate to `seleniumTest(…)`:
 * - A simple overload accepting only `driverFactory`, `keepBrowserOpen`, and a test `block`
 * - A setUp/tearDown overload adding `setUp` and `tearDown` lambdas for test fixture management
 *
 * The generated function name is derived from the annotated object's simple name with the first
 * character lowercased and `"Test"` appended (e.g., `BrowserStackDemo` → `browserStackDemoTest`).
 *
 * Requirements:
 * - Must be applied to an `object` declaration (not a `class`)
 * - The object must extend [SeleniumSite][dev.kolibrium.selenium.core.SeleniumSite]
 *
 * Example:
 * ```kotlin
 * @GenerateTestHarness
 * object BrowserStackDemo : SeleniumSite(baseUrl = "https://bstackdemo.com")
 *
 * // Generated: fun browserStackDemoTest(…) and fun <T> browserStackDemoTest(…)
 * ```
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
public annotation class GenerateTestHarness

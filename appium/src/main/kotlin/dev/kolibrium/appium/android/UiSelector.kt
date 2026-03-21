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

package dev.kolibrium.appium.android

/**
 * Scope for building a `UiSelector` expression string in a type-safe way.
 *
 * Used with [uiSelector] to construct Android UIAutomator selector expressions
 * without writing raw Java strings.
 *
 * Example:
 * ```
 * val addToCart by androidUIAutomator(uiSelector { text("Add To Cart") })
 * ```
 */
public class UiSelectorScope internal constructor() {
    private val clauses = mutableListOf<String>()

    /** Matches elements whose text equals [value] exactly. */
    public fun text(value: String) {
        clauses += """text("$value")"""
    }

    /** Matches elements whose text contains [value]. */
    public fun textContains(value: String) {
        clauses += """textContains("$value")"""
    }

    /** Matches elements whose text starts with [value]. */
    public fun textStartsWith(value: String) {
        clauses += """textStartsWith("$value")"""
    }

    /** Matches elements whose text matches the [regex] pattern. */
    public fun textMatches(regex: String) {
        clauses += """textMatches("$regex")"""
    }

    /** Matches elements whose content description equals [value] exactly. */
    public fun description(value: String) {
        clauses += """description("$value")"""
    }

    /** Matches elements whose content description contains [value]. */
    public fun descriptionContains(value: String) {
        clauses += """descriptionContains("$value")"""
    }

    /** Matches elements whose content description starts with [value]. */
    public fun descriptionStartsWith(value: String) {
        clauses += """descriptionStartsWith("$value")"""
    }

    /** Matches elements whose content description matches the [regex] pattern. */
    public fun descriptionMatches(regex: String) {
        clauses += """descriptionMatches("$regex")"""
    }

    /** Matches elements whose class name equals [value]. */
    public fun className(value: String) {
        clauses += """className("$value")"""
    }

    /** Matches elements whose resource name equals [value]. */
    public fun resourceId(value: String) {
        clauses += """resourceId("$value")"""
    }

    /** Matches elements that are clickable (or not, if [value] is false). */
    public fun clickable(value: Boolean = true) {
        clauses += "clickable($value)"
    }

    /** Matches elements that are enabled (or not, if [value] is false). */
    public fun enabled(value: Boolean = true) {
        clauses += "enabled($value)"
    }

    /** Matches elements that are focusable (or not, if [value] is false). */
    public fun focusable(value: Boolean = true) {
        clauses += "focusable($value)"
    }

    /** Matches elements that are scrollable (or not, if [value] is false). */
    public fun scrollable(value: Boolean = true) {
        clauses += "scrollable($value)"
    }

    /** Matches the element at the given [index] among siblings. */
    public fun index(index: Int) {
        clauses += "index($index)"
    }

    /** Matches the [n]-th instance of matching elements (zero-based). */
    public fun instance(n: Int) {
        clauses += "instance($n)"
    }

    internal fun build(): String = "new UiSelector().${clauses.joinToString(".")}"
}

/**
 * Builds a `UiSelector` expression string using a type-safe DSL.
 *
 * The returned string can be passed directly to [androidUIAutomator] or [androidUIAutomators].
 *
 * Example:
 * ```
 * val loginButton by androidUIAutomator(uiSelector { text("Login") })
 * val listItem by androidUIAutomator(uiSelector {
 *     className("android.widget.TextView")
 *     textContains("Product")
 *     enabled()
 * })
 * ```
 *
 * @param block Builder block that configures the selector clauses.
 * @return The raw UIAutomator selector expression string.
 */
public fun uiSelector(block: UiSelectorScope.() -> Unit): String = UiSelectorScope().apply(block).build()

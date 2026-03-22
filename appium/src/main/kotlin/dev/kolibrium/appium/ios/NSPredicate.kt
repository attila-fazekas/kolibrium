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

package dev.kolibrium.appium.ios

/**
 * Modifier for string comparison operators that controls case and diacritic sensitivity.
 *
 * By default, NSPredicate string comparisons are case and diacritic sensitive.
 * Use these modifiers to change that behavior.
 *
 * Example:
 * ```
 * name.beginsWith("bar", CaseAndDiacriticInsensitive)
 * // Produces: name BEGINSWITH[cd] 'bar'
 * ```
 */
public enum class StringModifier(
    internal val value: String,
) {
    /** Case-insensitive comparison. */
    CaseInsensitive("c"),

    /** Diacritic-insensitive comparison. */
    DiacriticInsensitive("d"),

    /** Both case- and diacritic-insensitive comparison. */
    CaseAndDiacriticInsensitive("cd"),
}

/**
 * Scope for building an NSPredicate expression string in a type-safe way.
 *
 * Multiple clauses at the top level are joined with `AND`. Use [anyOf] for `OR` grouping
 * and [not] for negation.
 *
 * Example:
 * ```
 * nsPredicate {
 *     type equalTo XCUIElementType.BUTTON
 *     label.beginsWith("Add")
 *     isEnabled equalTo true
 * }
 * // Produces: type == 'XCUIElementTypeButton' AND label BEGINSWITH 'Add' AND enabled == true
 * ```
 *
 * @see nsPredicate
 */
public class NSPredicateScope internal constructor() {
    private val clauses = mutableListOf<String>()

    // --- String attributes ---

    /** The element's type (e.g. `XCUIElementTypeButton`). */
    public val type: StringAttribute get() = StringAttribute("type")

    /** The element's name (accessibility identifier). */
    public val name: StringAttribute get() = StringAttribute("name")

    /** The element's label. */
    public val label: StringAttribute get() = StringAttribute("label")

    /** The element's value. */
    public val value: StringAttribute get() = StringAttribute("value")

    // --- Boolean attributes ---

    /** Whether the element is enabled. Maps to the `enabled` NSPredicate attribute. */
    public val isEnabled: BooleanAttribute get() = BooleanAttribute("enabled")

    /** Whether the element is visible. Maps to the `visible` NSPredicate attribute. */
    public val isVisible: BooleanAttribute get() = BooleanAttribute("visible")

    /** Whether the element is accessible. */
    public val accessible: BooleanAttribute get() = BooleanAttribute("accessible")

    /** Whether the element is selected. */
    public val selected: BooleanAttribute get() = BooleanAttribute("selected")

    /** Whether the element is focused. */
    public val focused: BooleanAttribute get() = BooleanAttribute("focused")

    /** Whether the element is hittable (can receive tap events). */
    public val hittable: BooleanAttribute get() = BooleanAttribute("hittable")

    // --- Numeric / rect attributes ---

    /** Accessor for the element's bounding rectangle sub-attributes. */
    public val rect: RectAccessor get() = RectAccessor()

    /**
     * Provides access to the numeric sub-attributes of an element's bounding rectangle:
     * [x], [y], [width], and [height].
     */
    public inner class RectAccessor {
        /** The x-coordinate of the element's origin. */
        public val x: NumericAttribute get() = NumericAttribute("rect.x")

        /** The y-coordinate of the element's origin. */
        public val y: NumericAttribute get() = NumericAttribute("rect.y")

        /** The width of the element. */
        public val width: NumericAttribute get() = NumericAttribute("rect.width")

        /** The height of the element. */
        public val height: NumericAttribute get() = NumericAttribute("rect.height")
    }

    /**
     * An attribute that supports string comparison operations.
     *
     * All string comparisons accept an optional [StringModifier] to control
     * case and diacritic sensitivity.
     */
    public inner class StringAttribute(
        private val attr: String,
    ) {
        /** Matches when the attribute equals [value] exactly. */
        public infix fun equalTo(value: String): Unit = addClause("$attr == '$value'")

        /** Matches when the attribute does not equal [value]. */
        public infix fun notEqualTo(value: String): Unit = addClause("$attr != '$value'")

        /** Matches when the attribute contains [value]. */
        public fun contains(
            value: String,
            modifier: StringModifier? = null,
        ): Unit = addClause("$attr CONTAINS${modifier.suffix()} '$value'")

        /** Matches when the attribute begins with [value]. */
        public fun beginsWith(
            value: String,
            modifier: StringModifier? = null,
        ): Unit = addClause("$attr BEGINSWITH${modifier.suffix()} '$value'")

        /** Matches when the attribute ends with [value]. */
        public fun endsWith(
            value: String,
            modifier: StringModifier? = null,
        ): Unit = addClause("$attr ENDSWITH${modifier.suffix()} '$value'")

        /**
         * Matches when the attribute matches the [pattern] using wildcard characters.
         * `?` matches 1 character, `*` matches 0 or more characters.
         */
        public fun like(
            pattern: String,
            modifier: StringModifier? = null,
        ): Unit = addClause("$attr LIKE${modifier.suffix()} '$pattern'")

        /**
         * Matches when the attribute matches the [regex] pattern
         * (ICU v3 regular expression syntax).
         */
        public fun matches(
            regex: String,
            modifier: StringModifier? = null,
        ): Unit = addClause("$attr MATCHES${modifier.suffix()} '$regex'")

        /**
         * Matches when the attribute's value is contained in [values].
         * Equivalent to SQL `IN`.
         */
        public infix fun isIn(values: List<String>) {
            val set = values.joinToString("', '", "{'", "'}")
            addClause("$attr IN $set")
        }
    }

    /**
     * An attribute that supports boolean equality comparison.
     */
    public inner class BooleanAttribute(
        private val attr: String,
    ) {
        /** Matches when the attribute equals [value]. */
        public infix fun equalTo(value: Boolean): Unit = addClause("$attr == $value")
    }

    /**
     * An attribute that supports numeric comparison operations including
     * ordering and range checks.
     */
    public inner class NumericAttribute(
        private val attr: String,
    ) {
        /** Matches when the attribute equals [value]. */
        public infix fun equalTo(value: Number): Unit = addClause("$attr == $value")

        /** Matches when the attribute does not equal [value]. */
        public infix fun notEqualTo(value: Number): Unit = addClause("$attr != $value")

        /** Matches when the attribute is greater than [value]. */
        public infix fun greaterThan(value: Number): Unit = addClause("$attr > $value")

        /** Matches when the attribute is greater than or equal to [value]. */
        public infix fun greaterThanOrEqualTo(value: Number): Unit = addClause("$attr >= $value")

        /** Matches when the attribute is less than [value]. */
        public infix fun lessThan(value: Number): Unit = addClause("$attr < $value")

        /** Matches when the attribute is less than or equal to [value]. */
        public infix fun lessThanOrEqualTo(value: Number): Unit = addClause("$attr <= $value")

        /**
         * Matches when the attribute's value falls between [lower] and [upper] (inclusive).
         */
        public fun between(
            lower: Number,
            upper: Number,
        ): Unit = addClause("$attr BETWEEN { $lower, $upper }")
    }

    // --- Compound predicates ---

    /**
     * Groups clauses with `OR`. All clauses defined inside the [block] are joined with `OR`
     * and the resulting group is added to the outer scope as a single clause.
     *
     * Example:
     * ```
     * nsPredicate {
     *     anyOf {
     *         name equalTo "done"
     *         value equalTo "done"
     *     }
     *     type equalTo XCUIElementType.BUTTON
     * }
     * // Produces: (name == 'done' OR value == 'done') AND type == 'XCUIElementTypeButton'
     * ```
     */
    public fun anyOf(block: NSPredicateScope.() -> Unit) {
        val inner = NSPredicateScope().apply(block)
        addClause("(${inner.clauses.joinToString(" OR ")})")
    }

    /**
     * Groups clauses with `AND`. Useful for explicit grouping inside an [anyOf] block.
     *
     * Example:
     * ```
     * nsPredicate {
     *     anyOf {
     *         allOf {
     *             name equalTo "done"
     *             isEnabled equalTo true
     *         }
     *         label equalTo "cancel"
     *     }
     * }
     * // Produces: ((name == 'done' AND enabled == true) OR label == 'cancel')
     * ```
     */
    public fun allOf(block: NSPredicateScope.() -> Unit) {
        val inner = NSPredicateScope().apply(block)
        addClause("(${inner.clauses.joinToString(" AND ")})")
    }

    /**
     * Negates the single clause produced by [block].
     *
     * Example:
     * ```
     * nsPredicate {
     *     not { name equalTo "done" }
     * }
     * // Produces: NOT (name == 'done')
     * ```
     */
    public fun not(block: NSPredicateScope.() -> Unit) {
        val inner = NSPredicateScope().apply(block)
        addClause("NOT (${inner.clauses.joinToString(" AND ")})")
    }

    private fun addClause(expression: String) {
        clauses += expression
    }

    private fun StringModifier?.suffix(): String = if (this != null) "[$value]" else ""

    internal fun build(): String = clauses.joinToString(" AND ")
}

/**
 * Builds an NSPredicate expression string using a type-safe DSL.
 *
 * The returned string can be passed directly to [iOSNSPredicate] or [iOSNSPredicates].
 *
 * Example:
 * ```
 * private val loginButton by iOSNSPredicate(nsPredicate {
 *     label equalTo "Login"
 *     isEnabled equalTo true
 * })
 *
 * private val searchField by iOSNSPredicate(nsPredicate {
 *     type equalTo XCUIElementType.TEXT_FIELD
 *     name.contains("search", StringModifier.CaseInsensitive)
 * })
 *
 * private val done by iOSNSPredicate(nsPredicate {
 *     anyOf {
 *         name equalTo "done"
 *         value equalTo "done"
 *     }
 *     type.isIn(listOf(XCUIElementType.BUTTON, XCUIElementType.KEY))
 * })
 *
 * private val offscreen by iOSNSPredicate(nsPredicate {
 *     rect.x.between(1, 100)
 *     not { isVisible equalTo true }
 * })
 * ```
 *
 * @param block Builder block that configures the predicate clauses.
 * @return The raw NSPredicate expression string.
 */
public fun nsPredicate(block: NSPredicateScope.() -> Unit): String = NSPredicateScope().apply(block).build()

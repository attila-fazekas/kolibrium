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

package dev.kolibrium.ksp.annotations

/**
 * Instructs kolibrium-codegen to generate source code for locating a single element
 * using the "className" locator strategy.
 *
 * This annotation allows you to specify a class attribute for locating an element in the
 * generated code. If [locator] is empty, the name of the enum entry will automatically be used
 * as the property name in the generated code.
 *
 * @property locator The "class" attribute value to search for. If left empty, the enum entry
 *                   name will be used as the property name in the generated code.
 * @property cacheLookup If true (default), the element will be looked up only once and cached for
 *                       subsequent accesses, similarly to Page Factory's @CacheLookup annotation.
 *                       If false, a new lookup will be performed each time the element is accessed.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
public annotation class ClassName(
    val locator: String = "",
    val cacheLookup: Boolean = true,
)

/**
 * Instructs kolibrium-codegen to generate source code for locating multiple elements
 * using the "className" locator strategy.
 *
 * This annotation allows you to specify a class attribute for locating elements in the
 * generated code. If [locator] is empty, the name of the enum entry will automatically be used
 * as the property name in the generated code.
 *
 * @property locator The "class" attribute value to search for. If left empty, the enum entry
 *                   name will be used as the property name in the generated code.
 * @property cacheLookup If true (default), the elements will be looked up only once and cached for
 *                       subsequent accesses, similarly to Page Factory's @CacheLookup annotation.
 *                       If false, a new lookup will be performed each time the elements are accessed.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
public annotation class ClassNames(
    val locator: String = "",
    val cacheLookup: Boolean = true,
)

/**
 * Instructs kolibrium-codegen to generate source code for locating a single element
 * using the "cssSelector" locator strategy.
 *
 * This annotation allows you to specify a CSS selector for locating an element in the
 * generated code. If [locator] is empty, the name of the enum entry will automatically be used
 * as the property name in the generated code.
 *
 * @property locator The CSS selector expression to search for. If left empty, the enum entry
 *                   name will be used as the property name in the generated code.
 * @property cacheLookup If true (default), the element will be looked up only once and cached for
 *                       subsequent accesses, similarly to Page Factory's @CacheLookup annotation.
 *                       If false, a new lookup will be performed each time the element is accessed.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
public annotation class CssSelector(
    val locator: String = "",
    val cacheLookup: Boolean = true,
)

/**
 * Instructs kolibrium-codegen to generate source code for locating multiple elements
 * using the "cssSelector" locator strategy.
 *
 * This annotation allows you to specify a CSS selector for locating elements in the
 * generated code. If [locator] is empty, the name of the enum entry will automatically be used
 * as the property name in the generated code.
 *
 * @property locator The CSS selector expression to search for. If left empty, the enum entry
 *                   name will be used as the property name in the generated code.
 * @property cacheLookup If true (default), the elements will be looked up only once and cached for
 *                       subsequent accesses, similarly to Page Factory's @CacheLookup annotation.
 *                       If false, a new lookup will be performed each time the elements are accessed.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
public annotation class CssSelectors(
    val locator: String = "",
    val cacheLookup: Boolean = true,
)

/**
 * Instructs kolibrium-codegen to generate source code for locating a single element
 * using the "id" locator strategy.
 *
 * This annotation allows you to specify an ID attribute for locating an element in the
 * generated code. If [locator] is empty, the name of the enum entry will automatically be used
 * as the property name in the generated code.
 *
 * @property locator The "id" attribute value to search for. If left empty, the enum entry
 *                   name will be used as the property name in the generated code.
 * @property cacheLookup If true (default), the element will be looked up only once and cached for
 *                       subsequent accesses, similarly to Page Factory's @CacheLookup annotation.
 *                       If false, a new lookup will be performed each time the element is accessed.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
public annotation class Id(
    val locator: String = "",
    val cacheLookup: Boolean = true,
)

/**
 * Instructs kolibrium-codegen to generate source code for locating multiple elements
 * using the "id" locator strategy.
 *
 * This annotation allows you to specify an ID attribute for locating elements in the
 * generated code. If [locator] is empty, the name of the enum entry will automatically be used
 * as the property name in the generated code.
 *
 * @property locator The "id" attribute value to search for. If left empty, the enum entry
 *                   name will be used as the property name in the generated code.
 * @property cacheLookup If true (default), the elements will be looked up only once and cached for
 *                       subsequent accesses, similarly to Page Factory's @CacheLookup annotation.
 *                       If false, a new lookup will be performed each time the elements are accessed.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
public annotation class Ids(
    val locator: String = "",
    val cacheLookup: Boolean = true,
)

/**
 * Instructs kolibrium-codegen to generate source code for locating a single element
 * using the "linkText" locator strategy.
 *
 * This annotation allows you to specify link text for locating an element in the
 * generated code. If [locator] is empty, the name of the enum entry will automatically be used
 * as the property name in the generated code.
 *
 * @property locator The exact link text to match against. If left empty, the enum entry
 *                   name will be used as the property name in the generated code.
 * @property cacheLookup If true (default), the element will be looked up only once and cached for
 *                       subsequent accesses, similarly to Page Factory's @CacheLookup annotation.
 *                       If false, a new lookup will be performed each time the element is accessed.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
public annotation class LinkText(
    val locator: String = "",
    val cacheLookup: Boolean = true,
)

/**
 * Instructs kolibrium-codegen to generate source code for locating multiple elements
 * using the "linkText" locator strategy.
 *
 * This annotation allows you to specify link text for locating elements in the
 * generated code. If [locator] is empty, the name of the enum entry will automatically be used
 * as the property name in the generated code.
 *
 * @property locator The exact link text to match against. If left empty, the enum entry
 *                   name will be used as the property name in the generated code.
 * @property cacheLookup If true (default), the elements will be looked up only once and cached for
 *                       subsequent accesses, similarly to Page Factory's @CacheLookup annotation.
 *                       If false, a new lookup will be performed each time the elements are accessed.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
public annotation class LinkTexts(
    val locator: String = "",
    val cacheLookup: Boolean = true,
)

/**
 * Instructs kolibrium-codegen to generate source code for locating a single element
 * using the "name" locator strategy.
 *
 * This annotation allows you to specify a name attribute for locating an element in the
 * generated code. If [locator] is empty, the name of the enum entry will automatically be used
 * as the property name in the generated code.
 *
 * @property locator The "name" attribute value to search for. If left empty, the enum entry
 *                   name will be used as the property name in the generated code.
 * @property cacheLookup If true (default), the element will be looked up only once and cached for
 *                       subsequent accesses, similarly to Page Factory's @CacheLookup annotation.
 *                       If false, a new lookup will be performed each time the element is accessed.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
public annotation class Name(
    val locator: String = "",
    val cacheLookup: Boolean = true,
)

/**
 * Instructs kolibrium-codegen to generate source code for locating multiple elements
 * using the "name" locator strategy.
 *
 * This annotation allows you to specify a name attribute for locating elements in the
 * generated code. If [locator] is empty, the name of the enum entry will automatically be used
 * as the property name in the generated code.
 *
 * @property locator The "name" attribute value to search for. If left empty, the enum entry
 *                   name will be used as the property name in the generated code.
 * @property cacheLookup If true (default), the elements will be looked up only once and cached for
 *                       subsequent accesses, similarly to Page Factory's @CacheLookup annotation.
 *                       If false, a new lookup will be performed each time the elements are accessed.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
public annotation class Names(
    val locator: String = "",
    val cacheLookup: Boolean = true,
)

/**
 * Instructs kolibrium-codegen to generate source code for locating a single element
 * using the "partialLinkText" locator strategy.
 *
 * This annotation allows you to specify partial link text for locating an element in the
 * generated code. If [locator] is empty, the name of the enum entry will automatically be used
 * as the property name in the generated code.
 *
 * @property locator The partial link text to match against. If left empty, the enum entry
 *                   name will be used as the property name in the generated code.
 * @property cacheLookup If true (default), the element will be looked up only once and cached for
 *                       subsequent accesses, similarly to Page Factory's @CacheLookup annotation.
 *                       If false, a new lookup will be performed each time the element is accessed.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
public annotation class PartialLinkText(
    val locator: String = "",
    val cacheLookup: Boolean = true,
)

/**
 * Instructs kolibrium-codegen to generate source code for locating multiple elements
 * using the "partialLinkText" locator strategy.
 *
 * This annotation allows you to specify partial link text for locating elements in the
 * generated code. If [locator] is empty, the name of the enum entry will automatically be used
 * as the property name in the generated code.
 *
 * @property locator The partial link text to match against. If left empty, the enum entry
 *                   name will be used as the property name in the generated code.
 * @property cacheLookup If true (default), the elements will be looked up only once and cached for
 *                       subsequent accesses, similarly to Page Factory's @CacheLookup annotation.
 *                       If false, a new lookup will be performed each time the elements are accessed.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
public annotation class PartialLinkTexts(
    val locator: String = "",
    val cacheLookup: Boolean = true,
)

/**
 * Instructs kolibrium-codegen to generate source code for locating a single element
 * using the "tagName" locator strategy.
 *
 * This annotation allows you to specify an element's tag name for locating an element in the
 * generated code. If [locator] is empty, the name of the enum entry will automatically be used
 * as the property name in the generated code.
 *
 * @property locator The element's tag name to search for. If left empty, the enum entry
 *                   name will be used as the property name in the generated code.
 * @property cacheLookup If true (default), the element will be looked up only once and cached for
 *                       subsequent accesses, similarly to Page Factory's @CacheLookup annotation.
 *                       If false, a new lookup will be performed each time the element is accessed.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
public annotation class TagName(
    val locator: String = "",
    val cacheLookup: Boolean = true,
)

/**
 * Instructs kolibrium-codegen to generate source code for locating multiple elements
 * using the "tagName" locator strategy.
 *
 * This annotation allows you to specify an element's tag name for locating elements in the
 * generated code. If [locator] is empty, the name of the enum entry will automatically be used
 * as the property name in the generated code.
 *
 * @property locator The element's tag name to search for. If left empty, the enum entry
 *                   name will be used as the property name in the generated code.
 * @property cacheLookup If true (default), the elements will be looked up only once and cached for
 *                       subsequent accesses, similarly to Page Factory's @CacheLookup annotation.
 *                       If false, a new lookup will be performed each time the elements are accessed.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
public annotation class TagNames(
    val locator: String = "",
    val cacheLookup: Boolean = true,
)

/**
 * Instructs kolibrium-codegen to generate source code for locating a single element
 * using the "xpath" locator strategy.
 *
 * This annotation allows you to specify an XPath expression for locating an element in the
 * generated code. If [locator] is empty, the name of the enum entry will automatically be used
 * as the property name in the generated code.
 *
 * @property locator The XPath expression to use for locating the element. If left empty, the enum entry
 *                   name will be used as the property name in the generated code.
 * @property cacheLookup If true (default), the element will be looked up only once and cached for
 *                       subsequent accesses, similarly to Page Factory's @CacheLookup annotation.
 *                       If false, a new lookup will be performed each time the element is accessed.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
public annotation class XPath(
    val locator: String = "",
    val cacheLookup: Boolean = true,
)

/**
 * Instructs kolibrium-codegen to generate source code for locating multiple elements
 * using the "xpath" locator strategy.
 *
 * This annotation allows you to specify an XPath expression for locating elements in the
 * generated code. If [locator] is empty, the name of the enum entry will automatically be used
 * as the property name in the generated code.
 *
 * @property locator The XPath expression to use for locating the elements. If left empty, the enum entry
 *                   name will be used as the property name in the generated code.
 * @property cacheLookup If true (default), the elements will be looked up only once and cached for
 *                       subsequent accesses, similarly to Page Factory's @CacheLookup annotation.
 *                       If false, a new lookup will be performed each time the elements are accessed.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
public annotation class XPaths(
    val locator: String = "",
    val cacheLookup: Boolean = true,
)

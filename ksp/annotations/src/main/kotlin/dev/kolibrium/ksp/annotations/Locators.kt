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

// WebElement locator annotations

/**
 * Instructs kolibrium-codegen to generate source code that finds a single element using the
 * "className" locator strategy.
 *
 * @property locator The value of the "class" attribute to search for.
 * If empty, the enum entry name will be used as the property name in the generated source code.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
public annotation class ClassName(
    val locator: String = "",
)

/**
 * Instructs kolibrium-codegen to generate source code that finds a single element using the
 * "cssSelector" locator strategy.
 *
 * @property locator The value of the CSS expression to search for.
 * If empty, the enum entry name will be used as the property name in the generated source code.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
public annotation class CssSelector(
    val locator: String = "",
)

/**
 * Instructs kolibrium-codegen to generate source code that finds a single element using the
 * "id" locator strategy.
 *
 * @property locator The value of the "id" attribute to search for.
 * If empty, the enum entry name will be used as the property name in the generated source code.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
public annotation class Id(
    val locator: String = "",
)

/**
 * Instructs kolibrium-codegen to generate source code that finds a single element using the
 * "linkText" locator strategy.
 *
 * @property locator The exact text to match against.
 * If empty, the enum entry name will be used as the property name in the generated source code.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
public annotation class LinkText(
    val locator: String = "",
)

/**
 * Instructs kolibrium-codegen to generate source code that finds a single element using the
 * "name" locator strategy.
 *
 * @property locator The value of the "name" attribute to search for.
 * If empty, the enum entry name will be used as the property name in the generated source code.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
public annotation class Name(
    val locator: String = "",
)

/**
 * Instructs kolibrium-codegen to generate source code that finds a single element using the
 * "partialLinkText" locator strategy.
 *
 * @property locator The partial text in the link to match against.
 * If empty, the enum entry name will be used as the property name in the generated source code.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
public annotation class PartialLinkText(
    val locator: String = "",
)

/**
 * Instructs kolibrium-codegen to generate source code that finds a single element using the
 * "tagName" locator strategy.
 *
 * @property locator The element's tag name.
 * If empty, the enum entry name will be used as the property name in the generated source code.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
public annotation class TagName(
    val locator: String = "",
)

/**
 * Instructs kolibrium-codegen to generate source code that finds a single element using the
 * "xpath" locator strategy.
 *
 * @property locator The XPath expression to use.
 * If empty, the enum entry name will be used as the property name in the generated source code.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
public annotation class XPath(
    val locator: String = "",
)

// WebElements locator annotations

/**
 * Instructs kolibrium-codegen to generate source code that finds multiple elements using the
 * "className" locator strategy.
 *
 * @property locator The value of the "class" attribute to search for.
 * If empty, the enum entry name will be used as the property name in the generated source code.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
public annotation class ClassNames(
    val locator: String = "",
)

/**
 * Instructs kolibrium-codegen to generate source code that finds multiple elements using the
 * "cssSelector" locator strategy.
 *
 * @property locator The CSS expression to search for.
 * If empty, the enum entry name will be used as the property name in the generated source code.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
public annotation class CssSelectors(
    val locator: String = "",
)

/**
 * Instructs kolibrium-codegen to generate source code that finds multiple elements using the
 * "id" locator strategy.
 *
 * @property locator The value of the "id" attribute to search for.
 * If empty, the enum entry name will be used as the property name in the generated source code.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
public annotation class Ids(
    val locator: String = "",
)

/**
 * Instructs kolibrium-codegen to generate source code that finds multiple elements using the
 * "linkText" locator strategy.
 *
 * @property locator The exact text to match against.
 * If empty, the enum entry name will be used as the property name in the generated source code.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
public annotation class LinkTexts(
    val locator: String = "",
)

/**
 * Instructs kolibrium-codegen to generate source code that finds multiple elements using the
 * "name" locator strategy.
 *
 * @property locator The value of the "name" attribute to search for.
 * If empty, the enum entry name will be used as the property name in the generated source code.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
public annotation class Names(
    val locator: String = "",
)

/**
 * Instructs kolibrium-codegen to generate source code that finds multiple elements using the
 * "partialLinkText" locator strategy.
 *
 * @property locator The partial text in the link to match against.
 * If empty, the enum entry name will be used as the property name in the generated source code.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
public annotation class PartialLinkTexts(
    val locator: String = "",
)

/**
 * Instructs kolibrium-codegen to generate source code that finds multiple elements using the
 * "tagName" locator strategy.
 *
 * @property locator The element's tag name.
 * If empty, the enum entry name will be used as the property name in the generated source code.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
public annotation class TagNames(
    val locator: String = "",
)

/**
 * Instructs kolibrium-codegen to generate source code that finds multiple elements using the
 * "xpath" locator strategy.
 *
 * @property locator The XPath expression to use.
 * If empty, the enum entry name will be used as the property name in the generated source code.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
public annotation class XPaths(
    val locator: String = "",
)

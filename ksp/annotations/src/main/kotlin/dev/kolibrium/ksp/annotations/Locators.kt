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

// for WebElement

/**
 * Instructs kolibrium-codegen to generate source code that finds a single element using "className" locator strategy.
 * [locator] is the value of the "class" attribute to search for.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
public annotation class ClassName(val locator: String = "")

/**
 * Instructs kolibrium-codegen to generate source code that finds a single element using "cssSelector" locator strategy.
 * [locator] is the CSS expression to search for.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
public annotation class CssSelector(val locator: String = "")

/**
 * Instructs kolibrium-codegen to generate source code that finds a single element using "id" locator strategy.
 * [locator] is the value of the "id" attribute to search for.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
public annotation class Id(val locator: String = "")

/**
 * Instructs kolibrium-codegen to generate source code that finds a single element using "linkText" locator strategy.
 * [locator] is the exact text to match against.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
public annotation class LinkText(val locator: String = "")

/**
 * Instructs kolibrium-codegen to generate source code that finds a single element using "name" locator strategy.
 * [locator] is the value of the "name" attribute to search for.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
public annotation class Name(val locator: String = "")

/**
 * Instructs kolibrium-codegen to generate source code that finds a single element using "partialLinkText" locator strategy.
 * [locator] is the partial text in link to match against.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
public annotation class PartialLinkText(val locator: String = "")

/**
 * Instructs kolibrium-codegen to generate source code that finds a single element using "tagName" locator strategy.
 * [locator] is the element's tag name.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
public annotation class TagName(val locator: String = "")

/**
 * Instructs kolibrium-codegen to generate source code that finds a single element using "xpath" locator strategy.
 * [locator] is the XPath to use.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
public annotation class Xpath(val locator: String = "")

// for WebElements

/**
 * Instructs kolibrium-codegen to generate source code that finds multiple elements using "className" locator strategy.
 * [locator] is the value of the "class" attribute to search for.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
public annotation class ClassNames(val locator: String = "")

/**
 * Instructs kolibrium-codegen to generate source code that finds multiple elements using "cssSelector" locator strategy.
 * [locator] is the CSS expression to search for.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
public annotation class CssSelectors(val locator: String = "")

/**
 * Instructs kolibrium-codegen to generate source code that finds multiple elements using "id" locator strategy.
 * [locator] is the value of the "id" attribute to search for.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
public annotation class Ids(val locator: String = "")

/**
 * Instructs kolibrium-codegen to generate source code that finds multiple elements using "linkText" locator strategy.
 * [locator] is the exact text to match against.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
public annotation class LinkTexts(val locator: String = "")

/**
 * Instructs kolibrium-codegen to generate source code that finds multiple elements using "name" locator strategy.
 * [locator] is the value of the "name" attribute to search for.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
public annotation class Names(val locator: String = "")

/**
 * Instructs kolibrium-codegen to generate source code that finds multiple elements using "partialLinkText" locator strategy.
 * [locator] is the partial text in link to match against.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
public annotation class PartialLinkTexts(val locator: String = "")

/**
 * Instructs kolibrium-codegen to generate source code that finds multiple elements using "tagName" locator strategy.
 * [locator] is the element's tag name.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
public annotation class TagNames(val locator: String = "")

/**
 * Instructs kolibrium-codegen to generate source code that finds multiple elements using "xpath" locator strategy.
 * [locator] is the XPath to use.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
public annotation class Xpaths(val locator: String = "")

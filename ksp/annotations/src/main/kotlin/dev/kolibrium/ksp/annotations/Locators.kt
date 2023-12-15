/*
 * Copyright 2023 Attila Fazekas
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
 * Instructs kolibrium-codegen to use "className" locator strategy when generating source code.
 * [locator] is the value of the "class" attribute to search for.
 * If [collectToList] is true, the generated source code will look for list of elements.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
public annotation class ClassName(val locator: String = "", val collectToList: Boolean = false)

/**
 * Instructs kolibrium-codegen to use "css" locator strategy when generating source code.
 * [locator] is the CSS expression to search for.
 * If [collectToList] is true, the generated source code will look for list of elements.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
public annotation class Css(val locator: String = "", val collectToList: Boolean = false)

/**
 * Instructs kolibrium-codegen to use "id" locator strategy when generating source code.
 * [locator] is the value of the "id" attribute to search for.
 * If [collectToList] is true, the generated source code will look for list of elements.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
public annotation class Id(val locator: String = "", val collectToList: Boolean = false)

/**
 * Instructs kolibrium-codegen to use "linkText" locator strategy when generating source code.
 * [locator] is the exact text to match against.
 * If [collectToList] is true, the generated source code will look for list of elements.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
public annotation class LinkText(val locator: String = "", val collectToList: Boolean = false)

/**
 * Instructs kolibrium-codegen to use "name" locator strategy when generating source code.
 * [locator] is the value of the "name" attribute to search for.
 * If [collectToList] is true, the generated source code will look for list of elements.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
public annotation class Name(val locator: String = "", val collectToList: Boolean = false)

/**
 * Instructs kolibrium-codegen to use "partialLinkText" locator strategy when generating source code.
 * [locator] is the partial text in link to match against.
 * If [collectToList] is true, the generated source code will look for list of elements.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
public annotation class PartialLinkText(val locator: String = "", val collectToList: Boolean = false)

/**
 * Instructs kolibrium-codegen to use "tagName" locator strategy when generating source code.
 * [locator] is the element's tag name.
 * If [collectToList] is true, the generated source code will look for list of elements.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
public annotation class TagName(val locator: String = "", val collectToList: Boolean = false)

/**
 * Instructs kolibrium-codegen to use "xpath" locator strategy when generating source code.
 * [locator] is the XPath to use.
 * If [collectToList] is true, the generated source code will look for list of elements.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
public annotation class Xpath(val locator: String = "", val collectToList: Boolean = false)

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

package io.kolibrium.ksp.annotations

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
public annotation class ClassName(val locator: String = "", val collectToList: Boolean = false)

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
public annotation class Css(val locator: String = "", val collectToList: Boolean = false)

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
public annotation class Id(val locator: String = "", val collectToList: Boolean = false)

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
public annotation class LinkText(val locator: String = "", val collectToList: Boolean = false)

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
public annotation class Name(val locator: String = "", val collectToList: Boolean = false)

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
public annotation class PartialLinkText(val locator: String = "", val collectToList: Boolean = false)

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
public annotation class TagName(val locator: String = "", val collectToList: Boolean = false)

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
public annotation class Xpath(val locator: String = "", val collectToList: Boolean = false)

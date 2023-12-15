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
 * Enum classes annotated with [Page] will instruct kolibrium-codegen to generate Page Object classes.
 * If the class name ends with "Locators" that part will be removed from the generated class name. For example,
 * the LoginPageLocators enum class will be named as LoginPage.
 * If [generatedClassName] is defined, it will be used as the generated class name.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
public annotation class Page(val generatedClassName: String = "")

/**
 *
 * If [baseUrl] is defined, an init block will be generated to open the URL upon page initialization.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
public annotation class Url(val baseUrl: String = "")

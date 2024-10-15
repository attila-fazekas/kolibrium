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
 * Marks an enum class as a source for generating a Page Object class using kolibrium-codegen.
 *
 * The generated class will have the same name as the enum class, with the optional "Locators" suffix removed.
 * For example, `LoginPageLocators` will generate a class named `LoginPage`.
 *
 * @property generatedClassName If provided, this name will be used for the generated class instead of the derived name.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
public annotation class Page(
    val generatedClassName: String = "",
)

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
public annotation class Url(
    val value: String = "",
)

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
public annotation class Path(
    val value: String = "",
)

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
public annotation class Resource(
    val value: String = "",
)

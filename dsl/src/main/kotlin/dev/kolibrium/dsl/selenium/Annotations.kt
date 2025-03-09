/*
 * Copyright 2023-2025 Attila Fazekas & contributors
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

package dev.kolibrium.dsl.selenium

/**
 * Marker annotation for Kolibrium DSL functions and classes.
 */
@DslMarker
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
internal annotation class KolibriumDsl

/**
 * Marker annotation for Kolibrium DSL properties.
 *
 * Use this annotation to extend the Kolibrium DSL with your own properties, such as command-line arguments,
 * experimental flags, browser preferences, or browser feature switches.
 */
@DslMarker
@Target(AnnotationTarget.PROPERTY)
public annotation class KolibriumPropertyDsl

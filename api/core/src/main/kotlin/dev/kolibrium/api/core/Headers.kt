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

package dev.kolibrium.api.core

import io.ktor.http.HeadersBuilder

/**
 * Appends an HTTP header with the given [key] and [value].
 *
 * The [value] is converted to its string representation before being appended.
 *
 * @receiver The [HeadersBuilder] to append the header to.
 * @param key The name of the header.
 * @param value The value of the header.
 */
public fun HeadersBuilder.header(
    key: String,
    value: Any,
): Unit = append(key, value.toString())

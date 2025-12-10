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

package dev.kolibrium.dsl.internal

/**
 * Normalize a relative path to ensure it begins with a leading slash and is trimmed.
 *
 * Absolute URLs (http/https) should be handled by the caller before invoking this.
 */
internal fun normalizePath(path: String): String {
    val trimmed = path.trim()
    return if (trimmed.startsWith('/')) trimmed else "/$trimmed"
}

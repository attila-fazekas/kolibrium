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

package dev.kolibrium.common

/**
 * Marks the Kolibrium internal API, which is not intended for public use.
 *
 * This annotation is used to signal that the annotated element is part of the internal API of Kolibrium
 * and should not be used by external consumers. Any usage of this API will result in a compilation error
 * due to the opt-in requirement.
 *
 * @see RequiresOptIn.Level.ERROR
 */
@RequiresOptIn(
    level = RequiresOptIn.Level.ERROR,
    message = "This is an internal API of Kolibrium, please do not use it.",
)
public annotation class InternalKolibriumApi

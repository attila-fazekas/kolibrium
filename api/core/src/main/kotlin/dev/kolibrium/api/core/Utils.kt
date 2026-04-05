/*
 * Copyright 2023-2026 Attila Fazekas & contributors
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

/**
 * Asserts properties of this [ApiResponse] using the given [block].
 *
 * Intended for use in the assertion phase of an API test, providing a readable boundary
 * between the act and assert stages.
 *
 * Example:
 * ```
 * val response = updateUser(userId) {
 *     isActive = false
 * }
 *
 * response.verify {
 *     status shouldBe HttpStatusCode.OK
 *     body.isActive shouldBe false
 * }
 * ```
 *
 * @param T the type of the response body
 * @param block assertions to run with this [ApiResponse] as the receiver
 */
public inline fun <T> ApiResponse<T>.verify(block: ApiResponse<T>.() -> Unit) {
    block()
}

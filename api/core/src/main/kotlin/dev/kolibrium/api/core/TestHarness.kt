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

import kotlinx.coroutines.runBlocking

/**
 * Executes an API test block within a coroutine scope.
 *
 * This function provides a simple way to run suspend API test code synchronously.
 * It blocks the current thread until the test completes.
 *
 * @param C The type of the API client or context
 * @param api The API client instance to use for the test
 * @param block The suspend test block to execute with the API client as a receiver
 */
public fun <C> apiTest(
    api: C,
    block: suspend C.() -> Unit,
): Unit =
    runBlocking {
        api.block()
    }

/**
 * Executes an API operation and returns its result.
 *
 * This function allows you to run suspend API operations synchronously and
 * retrieve their return value. It blocks the current thread until the operation completes.
 *
 * @param C The type of the API client or context
 * @param T The return type of the operation
 * @param api The API client instance to use
 * @param block The suspend operation to execute with the API client as a receiver
 * @return The result of the operation
 */
public fun <C, T> runWithApi(
    api: C,
    block: suspend C.() -> T,
): T =
    runBlocking {
        api.block()
    }

/**
 * Executes an API test with setup and teardown phases.
 *
 * This function provides a structured way to run API tests with proper resource management:
 * 1. Executes the [setUp] block to prepare test context
 * 2. Runs the test [block] with the prepared context
 * 3. Always executes the [tearDown] block to clean up resources, even if the test fails
 *
 * Teardown failures are caught and suppressed to avoid hiding the original test failure.
 *
 * @param C The type of the API client or context
 * @param T The type of the test context returned by setUp
 * @param api The API client instance to use for the test
 * @param setUp The suspend block that prepares the test context
 * @param tearDown The suspend block that cleans up the test context (optional, defaults to no-op)
 * @param block The suspend test block to execute with the test context
 */
public fun <C, T> apiTest(
    api: C,
    setUp: suspend C.() -> T,
    tearDown: suspend C.(T) -> Unit = {},
    block: suspend C.(T) -> Unit,
): Unit =
    runBlocking {
        val ctx = api.setUp()
        try {
            api.block(ctx)
        } finally {
            // Teardown failures should not hide the original test failure.
            runCatching { api.tearDown(ctx) }
        }
    }

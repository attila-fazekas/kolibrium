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

package dev.kolibrium.selenium

import kotlin.reflect.KClass
import kotlin.time.Duration

/**
 * Class for configuring wait parameters in synchronization operations.
 */
public class Wait(
    /**
     * The duration between polling attempts when waiting for a condition.
     */
    public var pollingInterval: Duration? = null,
    /**
     * The maximum duration to wait for a condition to be met.
     */
    public var timeout: Duration? = null,
    /**
     * A custom message to be displayed if the wait condition is not met.
     */
    public var message: String? = null,
    /**
     * A list of exception classes that should be ignored during synchronization processes.
     */
    public val ignoring: List<KClass<out Throwable>> = emptyList(),
)

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

/**
 * Base specification class for API testing.
 *
 * This abstract class serves as a foundation for defining API specifications with a base URL.
 * Extend this class to create specific API clients or test specifications.
 *
 * @property baseUrl The base URL for the API endpoint (e.g., "https://api.example.com")
 */
public abstract class ApiSpec(
    public open val baseUrl: String,
)

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

package dev.kolibrium.dsl.selenium.creation

/**
 * Value class representing an experimental feature flag.
 *
 * @property value The string value of the experimental flag.
 */
@JvmInline
public value class ExperimentalFlag(
    internal val value: String,
)

/**
 * Object containing predefined experimental flags.
 */
public object ExperimentalFlags {
    /**
     * Requires secure attribute for cookies without SameSite.
     */
    @KolibriumPropertyDsl
    public val cookies_without_same_site_must_be_secure: ExperimentalFlag =
        ExperimentalFlag("cookies-without-same-site-must-be-secure@2")

    /**
     * Enables SameSite by default for cookies.
     */
    @KolibriumPropertyDsl
    public val same_site_by_default_cookies: ExperimentalFlag = ExperimentalFlag("same-site-by-default-cookies@2")

    /**
     * Controls whether Chrome should use the built-in automation extension that Selenium typically installs.
     */
    @KolibriumPropertyDsl
    public val use_automation_extension: ExperimentalFlag = ExperimentalFlag("useAutomationExtension")
}

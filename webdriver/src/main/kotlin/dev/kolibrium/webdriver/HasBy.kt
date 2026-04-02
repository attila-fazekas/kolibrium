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

package dev.kolibrium.webdriver

import org.openqa.selenium.By

/**
 * Interface exposing the Selenium [org.openqa.selenium.By] locator used to find element(s).
 * Useful for debugging and to integrate with custom find/wait utilities.
 */
@InternalKolibriumApi
public interface HasBy {
    /** The Selenium [org.openqa.selenium.By] locator associated with this descriptor. */
    @InternalKolibriumApi
    public val by: By
}

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

package dev.kolibrium.appium

import io.appium.java_client.AppiumDriver
import org.openqa.selenium.By
import org.openqa.selenium.SearchContext
import org.openqa.selenium.WebElement

/**
 * Base type for screen objects bound to an [App].
 *
 * Screen instances rely on a contextual [AppiumDriver] installed by Kolibrium's DSL
 * entry points ([androidTest], [iosTest], [appiumTest]). Constructing and using screens
 * outside those contexts will fail with a runtime error. Delegated `findElement(s)` calls
 * route through the contextual driver to satisfy [org.openqa.selenium.SearchContext].
 */
public abstract class Screen<A : App> : SearchContext {
    /**
     * Wait until the screen is considered ready for interaction.
     * Default is a no-op; subclasses may override.
     */
    public open fun awaitReady() {}

    /**
     * Optional post-ready assertions to verify invariants.
     */
    public open fun assertReady() {}

    override fun findElement(by: By): WebElement = requireDriver().findElement(by)

    override fun findElements(by: By): List<WebElement> = requireDriver().findElements(by)

    private fun requireDriver(): AppiumDriver =
        AppiumDriverContextHolder.get() ?: error(
            "Kolibrium runtime error: Screen '${this::class.simpleName ?: "<unknown>"}' " +
                "has no active AppiumDriver context.\n" +
                "Run screen interactions inside Kolibrium DSL (e.g., androidTest/iosTest/appiumTest → on).",
        )
}

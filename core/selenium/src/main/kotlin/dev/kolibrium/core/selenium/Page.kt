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

package dev.kolibrium.core.selenium

import org.openqa.selenium.By
import org.openqa.selenium.SearchContext
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement

/**
 * Base type for page objects bound to a [Site].
 * Library code may subclass this to add helpers and waiting primitives.
 *
 * Page instances rely on a contextual [org.openqa.selenium.WebDriver] installed by Kolibrium’s DSL
 * or via [withDriver]. Constructing and using pages outside those contexts will fail with a runtime
 * error. Delegated findElement(s) calls route through the contextual driver.
 */
public abstract class Page<S : Site> : SearchContext {
    /**
     * Relative path of this page within the [Site]. Defaults to "/".
     */
    public open val path: String = "/"

    /** The current URL as reported by the driver. */
    protected val currentUrl: String
        get() = requireDriver().currentUrl!!

    /** The current page title as reported by the driver. */
    protected val pageTitle: String
        get() = requireDriver().title!!

    /**
     * Wait until the page is considered ready for interaction.
     * Default is a no-op; libraries may override.
     */
    public open fun awaitReady() {}

    /**
     * Optional post-ready assertions to verify invariants.
     */
    public open fun assertReady() {}

    /** Reloads the current page. */
    protected fun refresh() {
        requireDriver().navigate().refresh()
    }

    /** Navigates back in the browser history. */
    protected fun back() {
        requireDriver().navigate().back()
    }

    /** Navigates forward in the browser history. */
    protected fun forward() {
        requireDriver().navigate().forward()
    }

    override fun findElement(by: By): WebElement = requireDriver().findElement(by)

    override fun findElements(by: By): List<WebElement> = requireDriver().findElements(by)

    private fun requireDriver(): WebDriver =
        DriverContextHolder.get()
            ?: error(sessionNotAttachedMessage())

    private fun sessionNotAttachedMessage(): String {
        val pageName = this::class.qualifiedName ?: this::class.simpleName ?: "<unknown page>"
        return (
            "Kolibrium runtime error: Page '$pageName' has no active WebDriver context.\n" +
                "You likely constructed this page directly or are calling it outside Kolibrium DSL.\n\n" +
                "How to fix:\n" +
                "- Run page interactions inside Kolibrium DSL (e.g., webTest/open/on)\n" +
                "- Or wrap code with withDriver(driver) { ... } so a contextual driver is available\n"
        )
    }
}

@PublishedApi
internal object DriverContextHolder {
    private val tl: ThreadLocal<WebDriver?> = ThreadLocal()

    @PublishedApi
    internal fun get(): WebDriver? = tl.get()

    @PublishedApi
    internal fun set(driver: WebDriver) {
        tl.set(driver)
    }

    @PublishedApi
    internal fun clear() {
        tl.remove()
    }
}

/**
 * Provide a contextual WebDriver for the duration of [block]. Also makes the driver available to
 * Java-override-based APIs (SearchContext) via a thread-local bridge.
 */
public inline fun <T> withDriver(
    driver: WebDriver,
    block: () -> T,
): T {
    DriverContextHolder.set(driver)
    return try {
        block()
    } finally {
        DriverContextHolder.clear()
    }
}

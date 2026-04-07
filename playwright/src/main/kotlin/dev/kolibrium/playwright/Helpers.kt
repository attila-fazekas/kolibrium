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

package dev.kolibrium.playwright

import com.microsoft.playwright.Download
import com.microsoft.playwright.Page
import com.microsoft.playwright.Response
import kotlin.time.Duration

/**
 * Waits for a popup [Page] triggered by [trigger], passes it to [use], and closes it afterwards.
 *
 * The popup is automatically closed in a `finally` block, even if [use] throws.
 *
 * @param T The return type of the [use] block.
 * @param trigger The action that causes the popup to open (e.g., clicking a link with `target="_blank"`).
 * @param use The block that receives the popup [Page] and returns a result.
 * @return The result of [use].
 *
 * Example:
 * ```kotlin
 * val title = page.withPopup(
 *     trigger = { page.click("a[target='_blank']") },
 *     use = { popup -> popup.title() },
 * )
 * ```
 */
public inline fun <T> Page.withPopup(
    crossinline trigger: () -> Unit,
    crossinline use: (Page) -> T,
): T {
    val popup = waitForPopup { trigger() }
    return try {
        use(popup)
    } finally {
        try {
            popup.close()
        } catch (_: Throwable) {
        }
    }
}

/**
 * Waits for a [Download] triggered by [trigger] and passes it to [use].
 *
 * @param T The return type of the [use] block.
 * @param trigger The action that initiates the download (e.g., clicking a download link).
 * @param use The block that receives the [Download] and returns a result.
 * @return The result of [use].
 *
 * Example:
 * ```kotlin
 * val path = page.withDownload(
 *     trigger = { page.click("#download-btn") },
 *     use = { download -> download.path() },
 * )
 * ```
 */
public inline fun <T> Page.withDownload(
    crossinline trigger: () -> Unit,
    crossinline use: (Download) -> T,
): T {
    val download = waitForDownload { trigger() }
    return use(download)
}

/**
 * Waits for a network [Response] whose URL contains [substring] while executing [trigger].
 *
 * @param substring The substring to match against response URLs.
 * @param timeout Optional timeout as a [Duration]. Uses Playwright's default if `null`.
 * @param trigger The action that causes the network request (e.g., clicking a submit button).
 * @return The first matching [Response].
 *
 * Example:
 * ```kotlin
 * // With Playwright's default timeout:
 * val response = page.waitForResponseContaining("/api/products") {
 *     page.click("#load-more")
 * }
 *
 * // With a custom timeout:
 * val response = page.waitForResponseContaining("/api/products", timeout = 10.seconds) {
 *     page.click("#load-more")
 * }
 * ```
 */
public fun Page.waitForResponseContaining(
    substring: String,
    timeout: Duration? = null,
    trigger: () -> Unit,
): Response =
    if (timeout != null) {
        waitForResponse(
            { response -> response.url().contains(substring) },
            Page.WaitForResponseOptions().setTimeout(timeout.inWholeMilliseconds.toDouble()),
        ) { trigger() }
    } else {
        waitForResponse({ response -> response.url().contains(substring) }) {
            trigger()
        }
    }

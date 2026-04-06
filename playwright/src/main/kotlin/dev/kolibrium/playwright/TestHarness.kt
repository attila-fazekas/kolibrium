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

import com.microsoft.playwright.Browser
import com.microsoft.playwright.Browser.NewContextOptions
import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.BrowserType.LaunchOptions
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import com.microsoft.playwright.Tracing
import java.nio.file.Paths

/**
 * Kolibrium-specific configuration for the Playwright harness.
 *
 * This is intentionally separate from Playwright's own [LaunchOptions] and [NewContextOptions],
 * which should be passed directly to [playwrightTest]. Only Kolibrium-level concerns belong here.
 *
 * @property recordTrace Whether to capture a Playwright trace. When `true`, traces are always
 *           started; on test failure the trace is saved to [traceDir], otherwise it is discarded.
 * @property traceDir Directory where failure traces are written. Relative paths are resolved
 *           against the project working directory.
 * @property testName Optional name used in the trace file on failure. When `null`, the name is
 *           inferred from the call stack. KSP-generated harness functions can supply this automatically.
 */
public class KolibriumConfig(
    public val recordTrace: Boolean = false,
    public val traceDir: String = "build/traces",
    public val testName: String? = null,
)

/**
 * Runs a Playwright test against [site] with optional [setUp]/[tearDown] lifecycle hooks.
 *
 * The prepared value [T] produced by [setUp] is passed to both [block] and [tearDown].
 * If the test body throws, [tearDown] still runs; its exception (if any) is added as suppressed.
 *
 * @param S The concrete [PlaywrightSite] type.
 * @param T The type of the value produced by [setUp] and consumed by [block]/[tearDown].
 * @param site The site under test.
 * @param browserType Which browser engine to launch.
 * @param launchOptions Playwright [LaunchOptions] passed directly — controls headless, slowMo, etc.
 * @param contextOptions Playwright [NewContextOptions] passed directly — controls viewport, locale, etc.
 * @param config Kolibrium-specific configuration (tracing, etc.).
 * @param setUp Produces a value before the browser is launched. Useful for test data creation.
 * @param tearDown Cleans up after the test, receiving the value from [setUp]. Runs even on failure.
 * @param block The test body, executed with a [SiteScope] receiver.
 */
public fun <S : PlaywrightSite, T> playwrightTest(
    site: S,
    browserType: BrowserType = BrowserType.Chromium,
    launchOptions: LaunchOptions? = null,
    contextOptions: NewContextOptions? = null,
    config: KolibriumConfig = KolibriumConfig(),
    setUp: () -> T,
    tearDown: (T) -> Unit = {},
    block: SiteScope<S>.(T) -> Unit,
) {
    playwrightTestImpl(site, browserType, launchOptions, contextOptions, config, setUp, tearDown, block)
}

/**
 * Convenience overload of [playwrightTest] when no setUp/tearDown lifecycle is needed.
 *
 * @param S The concrete [PlaywrightSite] type.
 * @param site The site under test.
 * @param browserType Which browser engine to launch.
 * @param launchOptions Playwright [LaunchOptions] passed directly.
 * @param contextOptions Playwright [NewContextOptions] passed directly.
 * @param config Kolibrium-specific configuration (tracing, etc.).
 * @param block The test body, executed with a [SiteScope] receiver.
 */
public fun <S : PlaywrightSite> playwrightTest(
    site: S,
    browserType: BrowserType = BrowserType.Chromium,
    launchOptions: LaunchOptions? = null,
    contextOptions: NewContextOptions? = null,
    config: KolibriumConfig = KolibriumConfig(),
    block: SiteScope<S>.(Unit) -> Unit,
) {
    playwrightTest(
        site = site,
        browserType = browserType,
        launchOptions = launchOptions,
        contextOptions = contextOptions,
        config = config,
        setUp = { },
        block = block,
    )
}

internal fun <S : PlaywrightSite, T> playwrightTestImpl(
    site: S,
    browserType: BrowserType,
    launchOptions: LaunchOptions?,
    contextOptions: NewContextOptions?,
    config: KolibriumConfig,
    setUp: () -> T,
    tearDown: (T) -> Unit,
    block: SiteScope<S>.(T) -> Unit,
) {
    val prepared: T = setUp()
    var testError: Throwable? = null

    try {
        Playwright.create().use { playwright ->
            val browser = launchBrowser(playwright, browserType, launchOptions)
            browser.use {
                val context = it.newContext(contextOptions ?: NewContextOptions())
                if (config.recordTrace) startTracing(context)

                var failed = false
                try {
                    val page = initializePage(context, site)
                    runTestBlock(page, context, site, prepared, block)
                } catch (e: Throwable) {
                    failed = true
                    throw e
                } finally {
                    if (config.recordTrace) stopTracing(context, failed, config)
                }
            }
        }
    } catch (e: Throwable) {
        testError = e
        throw e
    } finally {
        safeTearDown(prepared, testError, tearDown)
    }
}

private fun launchBrowser(
    playwright: Playwright,
    browserType: BrowserType,
    launchOptions: LaunchOptions?,
): Browser {
    val bt =
        when (browserType) {
            BrowserType.Chromium -> playwright.chromium()
            BrowserType.Firefox -> playwright.firefox()
            BrowserType.WebKit -> playwright.webkit()
        }
    return bt.launch(launchOptions ?: LaunchOptions())
}

private fun <S : PlaywrightSite> initializePage(
    context: BrowserContext,
    site: S,
): Page {
    val page = context.newPage()
    page.navigate(site.baseUrl)

    if (site.cookies.isNotEmpty()) {
        context.addCookies(site.cookies)
        page.reload()
    }

    site.onSessionReady(page)
    return page
}

private fun <S : PlaywrightSite, T> runTestBlock(
    page: Page,
    context: BrowserContext,
    site: S,
    prepared: T,
    block: SiteScope<S>.(T) -> Unit,
) {
    val session = PlaywrightSession(page, context, site)
    PlaywrightSessionContext.withSession(session) {
        PlaywrightPageContextHolder.set(page)
        try {
            val scope = SiteScope<S>(page, context)
            scope.block(prepared)
        } finally {
            PlaywrightPageContextHolder.clear()
        }
    }
}

private fun startTracing(context: BrowserContext) {
    context.tracing().start(
        Tracing
            .StartOptions()
            .setScreenshots(true)
            .setSnapshots(true),
    )
}

private fun stopTracing(
    context: BrowserContext,
    failed: Boolean,
    config: KolibriumConfig,
) {
    if (failed) {
        val name = config.testName ?: inferTestName()
        val timestamp = System.currentTimeMillis()
        val tracePath = Paths.get("${config.traceDir}/${sanitizeFileName(name)}_$timestamp.zip")
        tracePath.parent?.toFile()?.mkdirs()
        context.tracing().stop(Tracing.StopOptions().setPath(tracePath))
    } else {
        context.tracing().stop()
    }
}

private fun <T> safeTearDown(
    prepared: T,
    testError: Throwable?,
    tearDown: (T) -> Unit,
) {
    try {
        tearDown(prepared)
    } catch (teardownError: Throwable) {
        if (testError != null) {
            testError.addSuppressed(teardownError)
        } else {
            throw teardownError
        }
    }
}

/**
 * Infers the current test name by walking the call stack for a method annotated with `@Test`.
 *
 * Returns the method name (e.g., "add item to cart") or a timestamp fallback if detection fails.
 * This is intended for trace file naming, not for correctness-critical logic.
 */
internal fun inferTestName(): String {
    val stackTrace = Thread.currentThread().stackTrace
    for (frame in stackTrace) {
        try {
            val clazz = Class.forName(frame.className)
            val method = clazz.declaredMethods.firstOrNull { it.name == frame.methodName }
            if (method != null && method.annotations.any { it.annotationClass.simpleName == "Test" }) {
                return frame.methodName
            }
        } catch (_: Throwable) {
            // Class not loadable, or method not found — skip
        }
    }
    return "trace_${System.currentTimeMillis()}"
}

private fun sanitizeFileName(name: String): String = name.replace(Regex("[^a-zA-Z0-9._-]"), "_")

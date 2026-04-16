# Kolibrium Playwright User Guide

## Table of Contents
1. [Overview](#overview)
2. [Setup](#setup)
3. [Defining your site](#defining-your-site)
    - [Basic site](#basic-site)
    - [Declarative cookies](#declarative-cookies)
    - [Session hook](#session-hook)
4. [Writing tests](#writing-tests)
    - [Simple test](#simple-test)
    - [Chaining pages](#chaining-pages)
    - [Staying on the same page](#staying-on-the-same-page)
    - [setUp / tearDown](#setup--teardown)
5. [Pages and navigation](#pages-and-navigation)
6. [Browser and context configuration](#browser-and-context-configuration)
    - [Browser type](#browser-type)
    - [Launch options](#launch-options)
    - [Context options](#context-options)
7. [Tracing](#tracing)
8. [Cookie management](#cookie-management)
9. [Assertions](#assertions)
10. [Helpers](#helpers)
11. [Thread safety](#thread-safety)
12. [KSP-generated test harness](#ksp-generated-test-harness)
13. [Customization](#customization)
14. [Troubleshooting](#troubleshooting)

---

## Overview

Kolibrium Playwright provides a Kotlin‑first test harness for running browser UI tests with [Microsoft Playwright](https://playwright.dev/java/). It removes ceremony around Playwright lifecycle management, offers a fluent page object DSL, and integrates with Kolibrium's unified harness pattern for readable, structured test flows.

---

## Setup

Add the Playwright module to your Gradle build:

```kotlin
dependencies {
    testImplementation("dev.kolibrium:kolibrium-playwright:<version>")
}
```

---

## Defining your site

Model your application under test as a singleton extending `PlaywrightSite`.

### Basic site

```kotlin
object SauceDemo : PlaywrightSite(baseUrl = "https://www.saucedemo.com")
```

### Declarative cookies

For stable, site‑wide cookies (e.g., locale, A/B test flags), override the `cookies` property. Cookies are applied to the browser context before the first navigation, so the server sees them on the very first request.

```kotlin
object MyApp : PlaywrightSite(baseUrl = "https://example.com") {
    override val cookies: List<Cookie> = listOf(
        Cookie("locale", "en-US").setUrl("https://example.com"),
        Cookie("ab_test", "variant_b").setUrl("https://example.com"),
    )
}
```

### Session hook

For dynamic or environment‑specific setup that requires a live session, override `onSessionReady`:

```kotlin
object MyApp : PlaywrightSite(baseUrl = "https://example.com") {
    override fun onSessionReady(page: Page) {
        val token = System.getenv("AUTH_TOKEN") ?: return
        page.context().addCookies(listOf(
            Cookie("auth", token).setUrl("https://example.com"),
        ))
    }
}
```

> **Prefer `cookies` for static defaults.** Use `onSessionReady` only when values are truly dynamic.

---

## Writing tests

Use `playwrightTest` (or a KSP‑generated site‑specific wrapper) as the entry point. The harness creates a Playwright instance, launches a browser, creates a context and page, navigates to `baseUrl`, and guarantees cleanup.

The single navigation verb is `on` — use it for every page interaction, whether it's the first page after launch or a page reached via navigation.

### Simple test

```kotlin
@Test
fun `login and verify products`() = playwrightTest(site = SauceDemo) {
    on(::LoginPage) {
        login()
    }.on(::InventoryPage) {
        titleText() shouldBe "Products"
    }
}
```

### Chaining pages

```kotlin
@Test
fun `add item to cart`() = sauceDemoTest {
    on(::LoginPage) {
        login()
    }.on(::InventoryPage) {
        addToCart("sauce-labs-backpack")
        openCart()
    }.on(::CartPage) {
        itemCount() shouldBe 1
    }
}
```

### Staying on the same page

Use `then` to perform additional actions or assertions without switching pages:

```kotlin
@Test
fun `verify cart badge updates`() = sauceDemoTest {
    on(::LoginPage) {
        login()
    }.on(::InventoryPage) {
        addToCart("sauce-labs-backpack")
    }.then {
        cartBadgeCount() shouldBe "1"
    }
}
```

### setUp / tearDown

`playwrightTest` supports computing a fixture before the browser launches and tearing it down afterward, even on failure:

```kotlin
@Test
fun `with test data`() = playwrightTest(
    site = SauceDemo,
    setUp = { createTestUser() },
    tearDown = { user -> deleteTestUser(user) },
) { user ->
    on(::LoginPage) {
        login(user.username, user.password)
    }.on(::InventoryPage) {
        titleText() shouldBe "Products"
    }
}
```

---

## Pages and navigation

Define pages by extending `PlaywrightPage<YourSite>`. The underlying Playwright `Page` is available via the `protected` `page` property — tests interact through page object methods, not raw locators:

```kotlin
class LoginPage : PlaywrightPage<SauceDemo>() {
    fun login(username: String = "standard_user", password: String = "secret_sauce") {
        page.fill("[data-test='username']", username)
        page.fill("[data-test='password']", password)
        page.click("[data-test='login-button']")
    }
}

class InventoryPage : PlaywrightPage<SauceDemo>() {
    fun titleText(): String = page.locator(".title").textContent()

    fun addToCart(item: String) {
        page.click("[data-test='add-to-cart-$item']")
    }

    fun openCart() {
        page.click(".shopping_cart_link")
    }

    fun cartBadgeCount(): String = page.locator(".shopping_cart_badge").textContent()
}

class CartPage : PlaywrightPage<SauceDemo>() {
    fun itemCount(): Int = page.locator(".cart_item").count()
}
```

Override `awaitReady()` and `assertReady()` for pages that need readiness checks before interactions:

```kotlin
class InventoryPage : PlaywrightPage<SauceDemo>() {
    override fun awaitReady() {
        page.waitForSelector(".inventory_list")
    }

    override fun assertReady() {
        check(page.url().contains("/inventory")) { "Not on inventory page" }
    }

    // ...
}
```

---

## Browser and context configuration

Playwright's own configuration types are passed through directly — no Kolibrium wrapper, no abstraction drift.

### Browser type

```kotlin
sauceDemoTest(browserType = BrowserType.Firefox) {
    // ...
}
```

Supported values: `Chromium`, `Firefox`, `WebKit`.

### Launch options

```kotlin
sauceDemoTest(
    launchOptions = LaunchOptions().apply {
        headless = false
        slowMo = 1000.0
    },
) {
    // ...
}
```

### Context options

```kotlin
sauceDemoTest(
    contextOptions = NewContextOptions().apply {
        setViewportSize(1280, 720)
        setLocale("en-US")
        setTimezoneId("Europe/Berlin")
    },
) {
    // ...
}
```

---

## Tracing

Kolibrium can capture Playwright traces on test failure for post‑mortem debugging. Enable tracing via `KolibriumConfig`:

```kotlin
@Test
fun `login and verify products`() = sauceDemoTest(
    config = KolibriumConfig(recordTrace = true),
) {
    on(::LoginPage) {
        login()
    }.on(::InventoryPage) {
        titleText() shouldBe "Products"
    }
}
```

| Outcome       | Behavior                                                                      |
|---------------|-------------------------------------------------------------------------------|
| Test passes   | Trace is discarded                                                            |
| Test fails    | Trace is saved to `build/traces/<testName>_<timestamp>.zip`                   |

The test name is inferred automatically from the call stack. You can also set it explicitly:

```kotlin
KolibriumConfig(recordTrace = true, testName = "my-test")
```

Or change the output directory:

```kotlin
KolibriumConfig(recordTrace = true, traceDir = "build/playwright-traces")
```

View traces with [Playwright Trace Viewer](https://trace.playwright.dev/).

---

## Cookie management

### Declarative (on the site)

Override `cookies` on your `PlaywrightSite` for cookies that should be present for every test. See [Declarative cookies](#declarative-cookies).

### Runtime (in the test body)

`SiteScope` provides cookie management methods for the active browser context:

```kotlin
sauceDemoTest {
    addCookie(Cookie("session-id", "abc123").setUrl("https://www.saucedemo.com"))
    clearCookie("session-id")
    clearCookies()

    val all = cookies()
    val forUrl = cookies("https://www.saucedemo.com")

    on(::InventoryPage) {
        // ...
    }
}
```

---

## Assertions

Kolibrium provides Kotlin‑idiomatic assertion extensions for Playwright `Locator`:

```kotlin
page.locator(".title").shouldBeVisible()
page.locator(".title").shouldHaveText("Products")
```

These delegate to Playwright's built‑in `PlaywrightAssertions` with auto‑waiting.

---

## Helpers

Utility extensions for common Playwright patterns:

### Popup handling

```kotlin
page.withPopup(
    trigger = { page.click("a[target='_blank']") },
    use = { popup -> popup.title() },
)
```

The popup is automatically closed afterward.

### Download handling

```kotlin
page.withDownload(
    trigger = { page.click("#download-btn") },
    use = { download -> download.path() },
)
```

### Waiting for network responses

```kotlin
page.waitForResponseContaining("/api/products") {
    page.click("#load-more")
}
```

---

## Thread safety

Playwright objects are single‑threaded. Kolibrium enforces this with a thread confinement check on every page access, cookie operation, and readiness check. If a page object or scope method is called from a thread different from the one that created the session, an `IllegalStateException` is thrown with a clear error message identifying the violation.

---

## KSP-generated test harness

The `playwrightTest` function is generic — it requires the `site` parameter on every call. For convenience, Kolibrium's KSP processor *(planned)* generates a site‑specific wrapper:

```kotlin
// Generated by KSP — do not edit
fun SauceDemoTest.sauceDemoTest(
    browserType: BrowserType = BrowserType.Chromium,
    launchOptions: LaunchOptions? = null,
    contextOptions: NewContextOptions? = null,
    config: KolibriumConfig = KolibriumConfig(),
    block: SiteScope<SauceDemo>.(Unit) -> Unit,
) = playwrightTest(
    site = SauceDemo,
    browserType = browserType,
    launchOptions = launchOptions,
    contextOptions = contextOptions,
    config = config,
    block = block,
)
```

Until KSP generation is available, define this wrapper manually in your test class:

```kotlin
fun sauceDemoTest(
    browserType: BrowserType = BrowserType.Chromium,
    launchOptions: LaunchOptions? = null,
    contextOptions: NewContextOptions? = null,
    config: KolibriumConfig = KolibriumConfig(),
    block: SiteScope<SauceDemo>.(Unit) -> Unit,
) = playwrightTest(
    site = SauceDemo,
    browserType = browserType,
    launchOptions = launchOptions,
    contextOptions = contextOptions,
    config = config,
    block = block,
)
```

---

## Customization

Override properties and hooks on your `PlaywrightSite` to adjust default behavior:

- **`cookies`** — declarative cookies applied before the first real navigation.
- **`onSessionReady(page)`** — hook invoked after the page is created and navigated to `baseUrl`; useful for dynamic/session‑specific setup.

Override hooks on your `PlaywrightPage` for readiness control:

- **`awaitReady()`** — wait for the page to be ready for interaction (e.g., wait for a selector).
- **`assertReady()`** — assert page invariants after readiness (e.g., check the URL).

---

## Troubleshooting

- **"has no active Playwright Page context"** — You're using a page object outside the Kolibrium DSL. Ensure all page interactions happen inside `playwrightTest { on(...) { } }`.

- **"thread confinement violation"** — A page object or scope method was called from a different thread than the one that created the session. Playwright is single‑threaded; don't share page objects across threads or coroutine dispatchers.

- **Trace file not created on failure** — Ensure `KolibriumConfig(recordTrace = true)` is passed to `playwrightTest` or your generated wrapper. Check that the test actually fails (traces are only saved on failure).

- **Cookies not visible to the server** — Declarative cookies require the origin to be established first. The harness navigates to `baseUrl`, then applies cookies, then reloads. If you add cookies via `onSessionReady`, you may need to reload manually.

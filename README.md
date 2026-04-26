<div align="center">
<p><img src="https://raw.githubusercontent.com/attila-fazekas/kolibrium/main/assets/kolibrium_logo.png" alt="kolibrium_logo.png"></p>
<h1>Kotlin library for Selenium/Appium tests</h1>
<p><a href="https://opensource.org/licenses/Apache-2.0"><img src="https://img.shields.io/badge/License-Apache_2.0-blue.svg" alt="License"></a>
<a href="https://github.com/attila-fazekas/kolibrium/actions/workflows/gradle.yml"><img src="https://github.com/attila-fazekas/kolibrium/actions/workflows/gradle.yml/badge.svg" alt="Build"></a>
<a href="https://github.com/attila-fazekas/kolibrium/actions/workflows/codeql.yml"><img src="https://github.com/attila-fazekas/kolibrium/actions/workflows/codeql.yml/badge.svg" alt="CodeQL"></a>
<a href="https://central.sonatype.com/search?namespace=dev.kolibrium"><img src="https://img.shields.io/maven-central/v/dev.kolibrium/kolibrium-selenium.svg" alt="Maven Central"></a>
<a href="https://javadoc.io/doc/dev.kolibrium/kolibrium-selenium"><img src="https://img.shields.io/badge/API_reference-KDoc-blue" alt="KDoc"></a>
</div>

Kolibrium is a Kotlin-first library for browser and mobile test automation. It wraps Selenium and Appium with idiomatic Kotlin APIs - type-safe element location, structured Page/Screen Objects, smart waits, and a unified test harness - so you write less boilerplate and catch more errors at compile time.

# Design Philosophy

**Compile-time safety over runtime discovery.** Type errors and invalid configurations are caught before a test runs. Sealed class hierarchies and strong typing throughout make this possible without boilerplate.

**A single mental model across modules.** Configuration, harness, lifecycle, and extension points work the same way whether you're testing a browser or a mobile app. An engineer who knows one module knows most of the other.

**Kotlin-first means actually using Kotlin.** Not Java with Kotlin syntax. Kolibrium uses context parameters, DSLs, and modern language features where they reduce friction and improve correctness.

**Escape hatches are not afterthoughts.** Opinionated defaults cover the common case. When they don't fit, the underlying Selenium `WebDriver` and Appium driver are directly accessible.

# Modules

## Selenium

Fluent type-safe element location, smart waits, Page Object patterns, thread-safe session management, and an extensible decorator framework.

- `kolibrium-selenium`: core WebDriver functionality including Page Object base class, `seleniumTest` harness, element locator delegates, and extensible decorator framework

See [Kolibrium website](https://kolibrium.dev/docs/category/selenium) for detailed Selenium module documentation.

## Appium

`AndroidApp`, `IosApp`, and `CrossPlatformApp` cover platform-specific and shared automation scenarios with a consistent API surface.

- `kolibrium-appium`: Screen Object base class, driver factories, locator delegates, type-safe `UiSelector` DSL (Android) and `NSPredicate` DSL (iOS), and test harness functions

See [appium/README.md](appium/README.md) for detailed Appium module documentation.

# Unified Architecture

## Configuration

Every module uses the same pattern to define the application under test.

```kotlin
// Selenium
object BrowserStackDemo : Site(baseUrl = "https://bstackdemo.com") {
    override val elementReadyCondition: WebElement.() -> Boolean = { isClickable }
    override val waitConfig: WaitConfig = Quick
}

// Appium
object SauceDemoApp : AndroidApp(
    appPackage = "com.saucelabs.mydemoapp.android",
    appActivity = ".view.activities.SplashActivity",
)

// Ios
object MyDemoIosApp : IosApp(
    bundleId = "com.saucelabs.mydemo.app.ios",
)
```

## Test Harness

Both modules follow a unified three-phase lifecycle: `setUp` → `block` → `tearDown`. The harness handles session creation, cleanup, and error propagation.

```kotlin
// Selenium
seleniumTest(site = BrowserStackDemo, driverFactory = ::ChromeDriver) {
    on(::ProductsPage) {
        titleText() shouldBe "Products"
    }
}

// Appium
androidTest(app = SauceDemoApp) {
    on(::ProductsScreen) {
        titleText() shouldBe "Products"
    }
}
```

# Concurrency Model

Thread-confined sessions for browser and mobile. Each test gets its own browser or driver session. No shared mutable state across tests - parallel execution is safe by default.

# Error Handling

The failure contract is consistent across all modules:

- **`setUp` failure** → the test body does not run, `tearDown` does not run, the exception propagates immediately
- **Test body failure** → `tearDown` still runs, teardown exceptions are suppressed against the original failure
- **`tearDown` failure with no prior failure** → teardown exception propagates normally

# Documentation

Full documentation is available at [https://kolibrium.dev](https://kolibrium.dev).

# Contributing

Please read [CONTRIBUTING](docs/CONTRIBUTING.md) before submitting pull requests.

# Project Status

Kolibrium is targeting a production-ready 1.0.0 release once its own APIs are stable and Kotlin's [Context parameters](https://github.com/Kotlin/KEEP/blob/context-parameters/proposals/context-parameters.md) and [Rich Errors](https://github.com/Kotlin/KEEP/blob/main/proposals/KEEP-0441-rich-errors-motivation.md) are out of preview.
<div align="center">
<p><img src="https://raw.githubusercontent.com/attila-fazekas/kolibrium/main/assets/kolibrium_logo.png" alt="kolibrium_logo.png"></p>
<h1>Modern testing toolkit for Kotlin</h1>
<p><a href="https://opensource.org/licenses/Apache-2.0"><img src="https://img.shields.io/badge/License-Apache_2.0-blue.svg" alt="License"></a>
<a href="https://github.com/attila-fazekas/kolibrium/actions/workflows/gradle.yml"><img src="https://github.com/attila-fazekas/kolibrium/actions/workflows/gradle.yml/badge.svg" alt="Build"></a>
<a href="https://github.com/attila-fazekas/kolibrium/actions/workflows/codeql.yml"><img src="https://github.com/attila-fazekas/kolibrium/actions/workflows/codeql.yml/badge.svg" alt="CodeQL"></a>
<a href="https://central.sonatype.com/search?namespace=dev.kolibrium"><img src="https://img.shields.io/maven-central/v/dev.kolibrium/kolibrium-selenium.svg" alt="Maven Central"></a>
<a href="https://javadoc.io/doc/dev.kolibrium/kolibrium-selenium"><img src="https://img.shields.io/badge/API_reference-KDoc-blue" alt="KDoc"></a>
</div>

Kolibrium is a unified, Kotlin-first testing ecosystem where API, browser, and mobile automation share the same configuration patterns, harness lifecycle, error contract, and design principles. The modules can be used independently or composed into integrated test suites with type safety across layers.

# Design Philosophy

**Compile-time safety over runtime discovery.** Type errors, missing endpoints, and invalid configurations are caught before a test runs. KSP code generation, sealed class hierarchies, and strong typing throughout make this possible without boilerplate.

**A single mental model across modules.** Configuration, harness, lifecycle, and extension points work the same way whether you're testing a REST API, a browser, or a mobile app. An engineer who knows one module knows 80% of any other module.

**Kotlin-first means actually using Kotlin.** Not Java with Kotlin syntax. Kolibrium uses coroutines, context parameters, DSLs, and modern language features where they reduce friction and improve correctness.

**Escape hatches are not afterthoughts.** Opinionated defaults cover the common case. When they don't fit, the underlying Selenium `WebDriver`, Appium driver, and Ktor client are directly accessible.

# Modules

Kolibrium is divided into modules that can be used independently or together as an integrated stack.

## API

Compile-time type-safe REST clients via KSP code generation. Define endpoints declaratively; the processor generates type-safe client methods, handles serialization and deserialization via kotlinx.serialization, manages authentication, and generates test harness functions and request body DSL builders.

- `kolibrium-api-core`: core functionality and test harness
- `kolibrium-api-ksp-annotations`: annotation definitions (`@GET`, `@POST`, `@Path`, etc.)
- `kolibrium-api-ksp-processors`: KSP processor for code generation

See [api/README.md](api/README.md) for detailed API module documentation.

## Selenium

Fluent type-safe element location, smart waits, Page Object patterns, thread-safe session management, and an extensible decorator framework.

- `kolibrium-selenium`: core WebDriver functionality including Page object base class, `seleniumTest` harness, element locator delegates, and extensible decorator framework
- `kolibrium-selenium-ksp` *(planned)*: KSP processor for test harness code generation

## Appium

`AndroidApp`, `IosApp`, and `CrossPlatformApp` cover platform-specific and shared automation scenarios with a consistent API surface.

- `kolibrium-appium`: Screen object base class, driver factories, locator delegates, and test harness functions
- `kolibrium-appium-ksp` *(planned)*: KSP processor for test harness code generation

See [appium/README.md](appium/README.md) for detailed Appium module documentation.

## Playwright *(planned)*

- `kolibrium-playwright` *(planned)*: lightweight test harness, Site/PageObject base classes, and Playwright lifecycle management
- `kolibrium-playwright-ksp` *(planned)*: KSP processor for test harness code generation

# Unified Architecture

## Configuration

Every module uses the same pattern to define the application under test.

```kotlin
// API
@GenerateApi
object VinylStoreApiSpec : ApiSpec(baseUrl = "http://localhost:8080")

// Selenium
object BrowserstackDemo : Site(baseUrl = "https://bstackdemo.com") {
    override val elementReadyCondition: (WebElement.() -> Boolean) = { isClickable }
    override val waitConfig: WaitConfig = Quick
}

// Appium
object SauceDemoApp : AndroidApp(
    driverFactory = androidDriverByPackage(
        appPackage = "com.saucelabs.mydemoapp.android",
        appActivity = ".view.activities.SplashActivity",
    )
)

// Playwright (planned)
object SauceDemo : Site(baseUrl = "https://www.saucedemo.com")
```

## Test Harness

All modules follow a unified three-phase lifecycle: `setUp` → `block` → `tearDown`. The harness handles session creation, cleanup, and error propagation.

```kotlin
// API
vinylStoreApiTest {
    getProducts()
}

// Selenium
browserStackDemoTest {
    open(::ProductsPage) {
        titleText() shouldBe "Products"
    }
}

// Appium
sauceDemoAppTest {
    open(::ProductsScreen) {
        titleText() shouldBe "Products"
    }
}

// Playwright (planned)
sauceDemoTest {
    open(::LoginPage) {
        login("standard_user", "secret_sauce")
    }
}
```

## Cross-Module Integration

The modules compose. API calls handle test data setup and teardown; browser or mobile automation handles behavioral verification. Type safety holds across the entire test.

# Concurrency Model

Thread-confined sessions for browser and mobile, coroutine-scoped for API, with the harness bridging between the two. Each test gets its own browser or driver session and its own coroutine scope for API calls. No shared mutable state across tests - parallel execution is safe by default.

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
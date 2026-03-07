<div align="center">
<p><img src="https://raw.githubusercontent.com/attila-fazekas/kolibrium/main/assets/kolibrium_logo.png" alt="kolibrium_logo.png"></p>
<h1>Modern testing toolkit for Kotlin</h1>
<p><a href="https://opensource.org/licenses/Apache-2.0"><img src="https://img.shields.io/badge/License-Apache_2.0-blue.svg" alt="License"></a>
<a href="https://github.com/attila-fazekas/kolibrium/actions/workflows/gradle.yml"><img src="https://github.com/attila-fazekas/kolibrium/actions/workflows/gradle.yml/badge.svg" alt="Build"></a>
<a href="https://github.com/attila-fazekas/kolibrium/actions/workflows/codeql.yml"><img src="https://github.com/attila-fazekas/kolibrium/actions/workflows/codeql.yml/badge.svg" alt="CodeQL"></a>
<a href="https://central.sonatype.com/search?namespace=dev.kolibrium"><img src="https://img.shields.io/maven-central/v/dev.kolibrium/kolibrium-selenium.svg" alt="Maven Central"></a>
<a href="https://javadoc.io/doc/dev.kolibrium/kolibrium-selenium"><img src="https://img.shields.io/badge/API_reference-KDoc-blue" alt="KDoc"></a>  
</div>

Kolibrium is a unified, Kotlin-first testing ecosystem that seamlessly integrates API testing with browser and mobile automation. 
Write idiomatic Kotlin tests with minimal boilerplate and compile-time safety.

# Core Concepts & Design Philosophy

Kolibrium APIs are designed to be simple, intuitive, consistent, and predictable with reasonable defaults. 
The goal is minimal cognitive load: Test engineers should be able to build muscle memory across modules without constantly context-switching. 
Everything fits together into a coherent developer experience. 
Many library consumers will think with a Java mindset, so familiar and simple APIs at call sites that feel natural to Java-background users are always preferred.

- **Kotlin-First**: Not Java-with-Kotlin. Kolibrium embraces coroutines, type safety, DSLs, and modern language features such as Context parameters and Rich errors *(planned)* where they add value, but never forces them.
- **Type Safety at Compile Time**: Catch errors before runtime. KSP code generation, sealed class hierarchies, and strong typing throughout.
- **Minimal Boilerplate**: Write what matters, the test logic. Let the framework handle ceremony. Generated code and DSLs eliminate repetition and verbosity.
- **Escape Hatches Everywhere**: Opinionated defaults for the 80% case. Full control for the 20% edge cases. Selenium WebDriver, Appium drivers, Playwright *(planned)*, and Ktor Client are always accessible for advanced users.

# Modules

Kolibrium is divided into several modules, each of which can be used either independently or in conjunction with others.

## Core

Shared foundation for the entire ecosystem.

- `kolibrium-core`: common annotations, shared utilities, and core abstractions used across modules
- `kolibrium-ksp-core` *(planned)*: shared foundation for codegen and test harness generation

## API

Compile-time type-safe REST clients via KSP code generation. Provides a declarative way to define REST API endpoints and generates type-safe client methods, automatic serialization/deserialization via kotlinx.serialization, authentication handling, test harness functions, and request body DSL builders.

- `kolibrium-api-core`: core functionality and test harness
- `kolibrium-api-ksp-annotations`: annotation definitions (`@GET`, `@POST`, `@Path`, etc.)
- `kolibrium-api-ksp-processors`: KSP processor for code generation

## Selenium

Enhanced Selenium WebDriver with Kotlin ergonomics. Solves common Selenium pain points with idiomatic Kotlin patterns: fluent type-safe element location, smart waits and synchronization, Page Object patterns, thread-safe session management, and enhanced failure handling with automatic screenshots *(planned)*.

- `kolibrium-selenium`: core WebDriver functionality including Page object base class, `seleniumTest` harness, element locator delegates, and extensible decorator framework
- `kolibrium-selenium-ksp` *(planned)*: KSP processor for test harness code generation

## Appium

Mobile testing for Android and iOS via Appium. Provides a sealed `App` hierarchy (`AndroidApp`, `IosApp`, `CrossPlatformApp`) with platform-specific driver factories, Screen objects with locator delegates (`accessibilityId`, `resourceId`), and dedicated test harnesses (`androidTest`, `iosTest`, `appiumTest`).

- `kolibrium-appium`: Screen object base class, driver factories, locator delegates, and test harness functions
- `kolibrium-appium-ksp` *(planned)*: KSP processor for test harness code generation

## Playwright *(planned)*

Lightweight abstractions and test harness for Playwright.

- `kolibrium-playwright` *(planned)*: lightweight test harness, Site/PageObject base classes, and Playwright lifecycle management
- `kolibrium-playwright-ksp` *(planned)*: KSP processor for test harness code generation

# Unified Architecture

## Similar Configuration

All modules follow the same pattern for defining the application under test:

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

## Similar Test Harness

All modules follow a unified three-phase lifecycle: `setUp` → `block` → `tearDown`.

```kotlin
// API
vinylStoreApiTest { 
    getProducts() 
}

// Selenium
browserstackDemoTest { 
    open(::ProductsPage) {
        titleText() shouldBe "Products" 
    } 
}

// Appium
androidTest(app = SauceDemoApp) { 
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

## Integration

Kolibrium's modules work together for complete E2E workflows: set up test data via API or database (fast, reliable), verify UI behavior via browser or mobile automation, and clean up via API or database in teardown. Type-safe across the entire test, one ecosystem, consistent patterns.

# Concurrency Model

Thread-confined sessions for browser and mobile modules, coroutine-scoped for API, with the harness functions handling the bridge. Each test gets its own browser/driver session and its own coroutine scope for API calls. No shared mutable state across tests.

# Error Handling

Across all modules, the failure contract is:

- **`setUp` failure** → no test body runs, no `tearDown` runs, exception propagates immediately
- **Test body failure** → `tearDown` still runs, teardown exceptions are suppressed on the original
- **`tearDown` failure (no prior failure)** → teardown exception propagates normally

# Documentation

The documentation is available at [https://kolibrium.dev](https://kolibrium.dev).

# Contributing

Please read [CONTRIBUTING](docs/CONTRIBUTING.md) before submitting your pull requests.

# Project status

Kolibrium is built to demonstrate what becomes possible when Kotlin's language features are applied thoughtfully to test automation.
The goal is a production-ready 1.0.0 release once the project's APIs and Kotlin's [Context parameters](https://github.com/Kotlin/KEEP/blob/context-parameters/proposals/context-parameters.md) and [Rich Errors](https://github.com/Kotlin/KEEP/blob/main/proposals/KEEP-0441-rich-errors-motivation.md) are stabilized.  

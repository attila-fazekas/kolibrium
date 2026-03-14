# Kolibrium Appium User Guide

## Table of Contents
1. [Overview](#overview)
2. [Setup](#setup)
3. [Defining your app](#defining-your-app)
4. [Writing tests](#writing-tests)
5. [Screens and navigation](#screens-and-navigation)
6. [Local Appium server management](#local-appium-server-management)
7. [Cloud and parallel execution](#cloud-and-parallel-execution)
8. [Customization](#customization)
9. [Troubleshooting](#troubleshooting)

---

## Overview

Kolibrium Appium provides a Kotlin‑first test harness for running mobile UI tests with Appium. It removes ceremony around driver and session management, offers optional local Appium server handling, and integrates with Kolibrium's screen DSL for readable, structured test flows.

---

## Setup

Add the Appium module to your Gradle build:

```kotlin
dependencies {
    testImplementation("dev.kolibrium:kolibrium-appium:<version>")
}
```

---

## Defining your app

Model your application under test as a singleton extending `AndroidApp`, `IosApp`, or `CrossPlatformApp`. The only required argument is a driver factory — a function that creates an `AppiumDriver`.

Kolibrium provides convenience factories for common setups, or you can supply your own lambda for full control.

### Android — preinstalled app

```kotlin
object MyApp : AndroidApp(
    driverFactory = androidDriverByPackage(
        appPackage = "com.example.app",
        appActivity = ".MainActivity",
    ),
)
```

### Android — install from APK

```kotlin
object MyApp : AndroidApp(
    driverFactory = androidDriverByApp(
        appPath = "/path/to/app.apk",
    ),
)
```

### iOS — preinstalled app

```kotlin
object MyApp : IosApp(
    driverFactory = iosDriverByBundleId(
        bundleId = "com.example.ios",
    ),
)
```

### iOS — install from IPA/.app

```kotlin
object MyApp : IosApp(
    driverFactory = iosDriverByApp(
        appPath = "/path/to/app.ipa",
    ),
)
```

### Cross‑platform

```kotlin
object MyApp : CrossPlatformApp(
    androidDriverFactory = androidDriverByPackage(
        appPackage = "com.example.app",
        appActivity = ".MainActivity",
    ),
    iosDriverFactory = iosDriverByBundleId(
        bundleId = "com.example.ios",
    ),
)
```

### Custom driver factory

Since the driver factory is just a `() -> AppiumDriver` function, you can configure the driver however you need:

```kotlin
object MyApp : AndroidApp(
    driverFactory = {
        val options = UiAutomator2Options().apply {
            setAppPackage("com.example.app")
            setAppActivity(".MainActivity")
            setNewCommandTimeout(Duration.ofSeconds(60))
        }
        AndroidDriver(URI("http://127.0.0.1:4723").toURL(), options)
    },
)
```

---

## Writing tests

Use `androidTest`, `iosTest`, or `appiumTest` as the entry point. Each creates a driver session, runs the test body, and guarantees cleanup.

### Android

```kotlin
@Test
fun checkout() = androidTest(app = MyApp) {
    open(::ProductsScreen) {
        titleText() shouldBe "Products"
        Product.Backpack.openProductDetails()
    }.on(::ProductDetailsScreen) {
        addToCart()
    }
}
```

### iOS

```kotlin
@Test
fun checkout() = iosTest(app = MyApp) {
    open(::ProductsScreen) {
        titleText() shouldBe "Products"
    }
}
```

### Cross‑platform

Pass the platform‑specific factory explicitly:

```kotlin
@Test
fun checkout_android() = appiumTest(
    app = MyApp,
    driverFactory = MyApp.androidDriverFactory,
) {
    // test body
}

@Test
fun checkout_ios() = appiumTest(
    app = MyApp,
    driverFactory = MyApp.iosDriverFactory,
) {
    // test body
}
```

### setUp / tearDown

`appiumTest` supports computing a fixture before the session and tearing it down afterward, even on failure:

```kotlin
data class Fixture(val user: User)

@Test
fun purchase_flow() = appiumTest(
    app = MyApp,
    driverFactory = MyApp.driverFactory,
    setUp = { Fixture(seedUser()) },
    tearDown = { fixture -> deleteUser(fixture.user) },
) { fixture ->
    // use fixture.user in the test body
}
```

---

## Screens and navigation

Define screens by extending `Screen<YourApp>` and declaring elements with locator delegates:

```kotlin
class ProductsScreen : Screen<MyApp>() {
    val title by accessibilityId("title")

    private val products by xpaths(
        """//*[@resource-id="com.example.app:id/productList"]/android.view.ViewGroup""",
    )

    fun titleText(): String = title.text
}

class ProductDetailsScreen : Screen<MyApp>() {
    private val cartButton by resourceId("cartBt")

    fun addToCart() {
        cartButton.click()
    }
}
```

Navigate between screens using `open` and `on`:

```kotlin
open(::ProductsScreen) {
    titleText() shouldBe "Products"
}.on(::ProductDetailsScreen) {
    addToCart()
}
```

---

## Local Appium server management

If you don't run an external Appium server, Kolibrium can manage a local one for you. Pass a `service` when defining your app:

```kotlin
object MyApp : AndroidApp(
    driverFactory = androidDriverByPackage(
        appPackage = "com.example.app",
        appActivity = ".MainActivity",
    ),
    service = appiumService {
        port = 4723
        logLevel = "info"
    },
)
```

When `service` is provided, the harness starts it before creating the driver session and stops it during teardown. A JVM shutdown hook prevents orphaned Appium processes on abnormal exits.

When `service` is `null` (the default), no local server is managed — tests connect to whatever Appium server is already running.

---

## Cloud and parallel execution

For parallel or multi‑device execution, use a cloud provider such as BrowserStack, Sauce Labs, or LambdaTest. The integration point is the driver factory — point it at the cloud hub URL with the appropriate capabilities:

```kotlin
object MyApp : AndroidApp(
    driverFactory = {
        AndroidDriver(
            URI("https://hub-cloud.browserstack.com/wd/hub").toURL(),
            UiAutomator2Options().apply {
                setCapability("bstack:options", mapOf(
                    "userName" to System.getenv("BROWSERSTACK_USERNAME"),
                    "accessKey" to System.getenv("BROWSERSTACK_ACCESS_KEY"),
                    "deviceName" to "Samsung Galaxy S22 Ultra",
                    "platformVersion" to "12.0",
                ))
            },
        )
    },
)
```

Everything else — test harness, screens, navigation — works identically regardless of whether the driver connects to a local or remote server.

---

## Customization

Override properties on your `App` to adjust default behavior:

- **`elementReadyCondition`** — predicate applied by locator delegates to determine when an element is ready for interaction (defaults to `isDisplayed`).
- **`waitConfig`** — default wait timing used by locator delegates.
- **`onSessionReady(driver)`** — hook invoked after the driver session is created and before any screen interactions; useful for setting e.g., orientation or geolocation, or performing pre‑navigation setup.

```kotlin
object MyApp : AndroidApp(
    driverFactory = androidDriverByPackage(
        appPackage = "com.example.app",
        appActivity = ".MainActivity",
    ),
) {
    override val elementReadyCondition: WebElement.() -> Boolean
        get() = { isEnabled }

    override fun onSessionReady(driver: AppiumDriver) {
        (driver as AndroidDriver).apply {
            rotate(ScreenOrientation.PORTRAIT)
            location = Location(59.332700, 18.065600, 0.0) // Stockholm
        }
    }
}
```

---

## Troubleshooting

- **Port already in use when starting local Appium** — Ensure no orphaned Appium processes are running. The managed service stops on normal teardown and on JVM shutdown; if a previous run crashed, kill the process manually or change the port.

- **Tests can't find elements intermittently** — Override `elementReadyCondition` or `waitConfig` on your app to better match your UI's state transitions.

- **Using a remote Appium server** — Leave `service` as `null` (the default). The harness will not start or stop any local server.

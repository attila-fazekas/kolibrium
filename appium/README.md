
# Kolibrium Appium User Guide

## Table of Contents
1. [Overview](#overview)
2. [Setup](#setup)
3. [Defining your app](#defining-your-app)
    - [Android](#android)
    - [iOS](#ios)
    - [Cross‑platform](#cross-platform)
    - [Custom driver factory](#custom-driver-factory)
4. [Appium launch modes](#appium-launch-modes)
5. [Writing tests](#writing-tests)
6. [Screens and navigation](#screens-and-navigation)
7. [Local Appium server management](#local-appium-server-management)
8. [Cloud and parallel execution](#cloud-and-parallel-execution)
9. [Customization](#customization)
10. [Troubleshooting](#troubleshooting)

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

Model your application under test as a singleton extending `AndroidApp`, `IosApp`, or `CrossPlatformApp`.

Each class supports two launch modes — **by identifier** (preinstalled app) and **by app path** (fresh install) — plus an escape hatch via a custom `driverFactory` lambda. See [Appium launch modes](#appium-launch-modes) for details on how these map to Appium capabilities.

### Android

#### Preinstalled app (launch by package)

The most common case. Provide `appPackage` and `appActivity`; the driver factory is derived automatically.

```kotlin
object MyAndroidApp : AndroidApp(
    appPackage = "com.example.app",
    appActivity = ".MainActivity",
)
```

`appPackage` is stored as a property and can be referenced in screen locators:

```kotlin
private val products by xpaths(
    """//*[@resource-id="${MyAndroidApp.appPackage}:id/productRV"]/android.view.ViewGroup""",
)
```

#### Install from APK/AAB

Provide `appPath`; Appium installs the binary and launches it automatically. You can optionally include `appPackage` if your screens need it for locators or deep links — but **do not** include `appActivity`, since Appium determines the launch activity from the APK manifest.

```kotlin
object MyAndroidApp : AndroidApp(
    appPath = "/path/to/app.apk",
)
```

With `appPackage` for locators:

```kotlin
object MyAndroidApp : AndroidApp(
    appPath = "/path/to/app.apk",
    appPackage = "com.example.app",
)
```

### iOS

#### Preinstalled app (launch by bundle ID)

```kotlin
object MyIosApp : IosApp(
    bundleId = "com.example.ios",
)
```

`bundleId` is stored as a property and can be referenced in screen locators.

#### Install from .app/IPA

Provide `appPath`; Appium installs the binary and derives the bundle ID from it automatically. **Do not** include `bundleId` when using `appPath` — Appium ignores it in install mode.

```kotlin
object MyIosApp : IosApp(
    appPath = "/path/to/app.ipa",
)
```

### Cross-platform

`CrossPlatformApp` composes an `AndroidApp` and an `IosApp`:

```kotlin
object MyApp : CrossPlatformApp(
    android = MyAndroidApp,
    ios = MyIosApp,
)
```

Or define the platform apps inline:

```kotlin
object MyApp : CrossPlatformApp(
    android = object : AndroidApp(
        appPackage = "com.example.app",
        appActivity = ".MainActivity",
    ) {},
    ios = object : IosApp(
        bundleId = "com.example.ios",
    ) {},
)
```

Properties like `appPackage` and `bundleId` are available through delegation:

```kotlin
MyApp.appPackage   // delegates to MyApp.android.appPackage
MyApp.bundleId     // delegates to MyApp.ios.bundleId
```

### Custom driver factory

Since the driver factory is just a `() -> AppiumDriver` function, you can configure the driver however you need. Pass it as the `driverFactory` parameter alongside any metadata properties your screens require:

```kotlin
object MyAndroidApp : AndroidApp(
    appPackage = "com.example.app",
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

## Appium launch modes

Appium treats **launch by identifier** and **install from path** as two distinct execution modes. Kolibrium enforces this distinction at construction time.

### Android

| Parameter                    | Launch mode | What Appium does                                                                                   |
|------------------------------|-------------|----------------------------------------------------------------------------------------------------|
| `appPackage` + `appActivity` | **Attach**  | Launches an already-installed app by package and activity                                          |
| `appPath`                    | **Install** | Installs the APK/AAB and launches it; determines activity from the manifest                        |
| `appPath` + `appPackage`     | **Install** | Same as above; `appPackage` is stored for locators/deep links but not sent to Appium for launching |

> **⚠️ `appPath` + `appActivity` is rejected.** When installing from an APK, Appium ignores `appActivity` — providing it suggests a misconfiguration. If you need `appPackage` for locators, provide it without `appActivity`.

### iOS

| Parameter  | Launch mode | What Appium does                                                             |
|------------|-------------|------------------------------------------------------------------------------|
| `bundleId` | **Attach**  | Launches an already-installed app by bundle identifier                       |
| `appPath`  | **Install** | Installs the .app/IPA and launches it; derives the bundle ID from the binary |

> **⚠️ `appPath` + `bundleId` is rejected.** When installing from an app path, Appium derives the bundle ID itself — providing both suggests a misconfiguration.

### Rule of thumb

- **Fresh install (CI, new build)** → use `appPath`
- **Reuse installed app (faster local runs)** → use `appPackage`/`appActivity` or `bundleId`
- **Never mix both modes** — they represent fundamentally different Appium execution paths

---

## Writing tests

Use `androidTest`, `iosTest`, or `appiumTest` as the entry point. Each creates a driver session, runs the test body, and guarantees cleanup.

### Android

```kotlin
@Test
fun checkout() = androidTest(app = MyAndroidApp) {
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
fun checkout() = iosTest(app = MyIosApp) {
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
    app = MyAndroidApp,
    driverFactory = MyAndroidApp.driverFactory,
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
class ProductsScreen : Screen<MyAndroidApp>() {
    val title by accessibilityId("title")

    private val products by xpaths(
        """//*[@resource-id="${MyAndroidApp.appPackage}:id/productList"]/android.view.ViewGroup""",
    )

    fun titleText(): String = title.text
}

class ProductDetailsScreen : Screen<MyAndroidApp>() {
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
object MyAndroidApp : AndroidApp(
    appPackage = "com.example.app",
    appActivity = ".MainActivity",
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
object MyCloudApp : AndroidApp(
    appPackage = "com.example.app",
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
object MyAndroidApp : AndroidApp(
    appPackage = "com.example.app",
    appActivity = ".MainActivity",
) {
    override val elementReadyCondition: WebElement.() -> Boolean
        get() = { isEnabled }

    override fun onSessionReady(driver: AndroidDriver) {
        driver.apply {
            rotate(ScreenOrientation.LANDSCAPE)
            location = Location(39.4666667, -0.3666667, 0.0) // Valencia, Spain
        }
    }
}
```

---

## Troubleshooting

- **Port already in use when starting local Appium** — Ensure no orphaned Appium processes are running. The managed service stops on normal teardown and on JVM shutdown; if a previous run crashed, kill the process manually or change the port.

- **Tests can't find elements intermittently** — Override `elementReadyCondition` or `waitConfig` on your app to better match your UI's state transitions.

- **Using a remote Appium server** — Leave `service` as `null` (the default). The harness will not start or stop any local server.


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
6. [Deep links](#deep-links)
7. [Screens and navigation](#screens-and-navigation)
8. [UiSelector DSL](#uiselector-dsl)
9. [NSPredicate DSL](#nspredicate-dsl)
10. [Local Appium server management](#local-appium-server-management)
11. [Settings DSL](#settings-dsl)
12. [Cloud and parallel execution](#cloud-and-parallel-execution)
13. [Customization](#customization)
14. [Troubleshooting](#troubleshooting)

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

Each class supports two launch modes - **by identifier** (preinstalled app) and **by app path** (fresh install) - plus an escape hatch via a custom `driverFactory` lambda. See [Appium launch modes](#appium-launch-modes) for details on how these map to Appium capabilities.

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

Provide `appPath`; Appium installs the binary and launches it automatically. You can optionally include `appPackage` if your screens need it for locators or deep links - but **do not** include `appActivity`, since Appium determines the launch activity from the APK manifest.

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

Provide `appPath`; Appium installs the binary and derives the bundle ID from it automatically. **Do not** include `bundleId` when using `appPath` - Appium ignores it in install mode.

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

> **⚠️ `appPath` + `appActivity` is rejected.** When installing from an APK, Appium ignores `appActivity` - providing it suggests a misconfiguration. If you need `appPackage` for locators, provide it without `appActivity`.

### iOS

| Parameter  | Launch mode | What Appium does                                                             |
|------------|-------------|------------------------------------------------------------------------------|
| `bundleId` | **Attach**  | Launches an already-installed app by bundle identifier                       |
| `appPath`  | **Install** | Installs the .app/IPA and launches it; derives the bundle ID from the binary |

> **⚠️ `appPath` + `bundleId` is rejected.** When installing from an app path, Appium derives the bundle ID itself - providing both suggests a misconfiguration.

### Rule of thumb

- **Fresh install (CI, new build)** → use `appPath`
- **Reuse installed app (faster local runs)** → use `appPackage`/`appActivity` or `bundleId`
- **Never mix both modes** - they represent fundamentally different Appium execution paths

---


## Writing tests

Use `androidTest`, `iosTest`, or `appiumTest` as the entry point. Each creates a driver session, runs the test body, and guarantees cleanup.

The single navigation verb is `on` - use it for every screen interaction, whether it's the first screen after launch, a screen reached via navigation, or a screen landed on via deep link.

### Android

```kotlin
@Test
fun checkout() = androidTest(app = MyAndroidApp) {
        on(::ProductsScreen) {
            titleText() shouldBe "Products"
            selectProduct("Backpack")
        }.on(::ProductDetailsScreen) {
            addToCart()
        }
    }
```

### iOS

```kotlin
@Test
fun checkout() = iosTest(app = MyIosApp) {
        on(::ProductsScreen) {
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
        on(::ProductsScreen) {
            // test body
        }
    }

@Test
fun checkout_ios() = appiumTest(
    app = MyApp,
    driverFactory = MyApp.iosDriverFactory,
) {
    on(::ProductsScreen) {
        // test body
    }
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

## Deep links

Deep links let you skip the normal navigation flow and land directly on a specific screen - useful for focused tests that don't need to traverse the entire UI.

Pass the `deepLink` parameter to `androidTest`, `iosTest`, or the generic `appiumTest`. The harness creates the driver session, executes the Appium `mobile: deepLink` command with the correct platform parameters, and then runs the test body.

### Android

```kotlin
@Test
fun product_details() =
    androidTest(app = MyAndroidApp, deepLink = "myapp://product-details/1") {
        on(::ProductDetailsScreen) {
            titleText() shouldBe "Sauce Labs Backpack"
        }
    }
```

The harness sends `mobile: deepLink` with `url` and `package` (from `AndroidApp.appPackage`).

### iOS

```kotlin
@Test
@Test
fun product_details() =
    iosTest(app = MyIosApp, deepLink = "myapp://product-details/1") {
        on(::ProductDetailsScreen) {
            titleText() shouldBe "Sauce Labs Backpack"
        }
    }
```

The harness sends `mobile: deepLink` with `url` and `bundleId` (from `IosApp.bundleId`).

### Cross‑platform

```kotlin
@Test
fun product_details_android() = appiumTest(
        app = MyApp,
        driverFactory = MyApp.androidDriverFactory,
        deepLink = "myapp://product-details/1",
    ) {
        on(::ProductDetailsScreen) {
            // test body
        }
    }

@Test
fun product_details_ios() = appiumTest(
    app = MyApp,
    driverFactory = MyApp.iosDriverFactory,
    deepLink = "myapp://product-details/1",
) {
    on(::ProductDetailsScreen) {
        // test body
    }
}
```

For `CrossPlatformApp`, the platform is resolved at runtime from the driver type - Android drivers send `package`, iOS drivers send `bundleId`.

### Prerequisites

- The app must be configured to handle the deep link scheme (Android intent filters / iOS universal links or custom URL schemes).
- `appPackage` (Android) or `bundleId` (iOS) must be set on the app definition - the deep link command needs it to target the correct app.
- The Appium server (or cloud provider) must support the `mobile: deepLink` extension.

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

Navigate between screens using `on`:

```kotlin
on(::ProductsScreen) {
    titleText() shouldBe "Products"
    selectProduct("Backpack")
}.on(::ProductDetailsScreen) {
    addToCart()
}
```

`on` is the single verb for all screen interactions. The first `on` call (on `AppEntry`) creates the screen and returns a `ScreenScope`; subsequent `.on(...)` calls chain from `ScreenScope`. Use `then` to perform additional actions on the current screen and `verify` to run assertions, both without switching screens:

```kotlin
on(::ProductsScreen) {
    selectProduct("Backpack")
}.on(::ProductDetailsScreen) {
    addToCart()
}.then {
    cartBadgeCount() shouldBe 1
    proceedToCheckout()
}.on(::CheckoutScreen) {
    placeOrder()
}
```

---

## UiSelector DSL
For Android screens, Kolibrium provides a type-safe DSL for building UIAutomator `UiSelector` expressions - no raw Java strings required. Use `uiSelector { }` with `androidUIAutomator` or `androidUIAutomators`.

```kotlin
val loginButton by androidUIAutomator(uiSelector { text("Login") })

val listItem by androidUIAutomator(uiSelector {
    className("android.widget.TextView")
    textContains("Product")
    enabled()
})
```

### Available matchers

#### Text

| Function                | Matches when…                        |
|-------------------------|--------------------------------------|
| `text(value)`           | element text equals `value` exactly  |
| `textContains(value)`   | element text contains `value`        |
| `textStartsWith(value)` | element text starts with `value`     |
| `textMatches(regex)`    | element text matches `regex` pattern |

#### Content description

| Function                       | Matches when…                               |
|--------------------------------|---------------------------------------------|
| `description(value)`           | content description equals `value` exactly  |
| `descriptionContains(value)`   | content description contains `value`        |
| `descriptionStartsWith(value)` | content description starts with `value`     |
| `descriptionMatches(regex)`    | content description matches `regex` pattern |

#### Identity & state

| Function                   | Matches when…                        |
|----------------------------|--------------------------------------|
| `className(value)`         | element class name equals `value`    |
| `resourceId(value)`        | element resource name equals `value` |
| `clickable(value = true)`  | element is (or isn't) clickable      |
| `enabled(value = true)`    | element is (or isn't) enabled        |
| `focusable(value = true)`  | element is (or isn't) focusable      |
| `scrollable(value = true)` | element is (or isn't) scrollable     |

#### Position

| Function       | Matches when…                            |
|----------------|------------------------------------------|
| `index(index)` | element is at `index` among siblings     |
| `instance(n)`  | element is the `n`-th match (zero-based) |

### Combining clauses
All clauses in a single `uiSelector { }` block are chained as a single `UiSelector` expression:

```kotlin
val addToCart by androidUIAutomator(uiSelector {
    className("android.widget.Button")
    textContains("Add To Cart")
    enabled()
    instance(0)
})
```

---

## NSPredicate DSL

For iOS screens, Kolibrium provides a type-safe DSL for building `NSPredicate` expressions — no raw predicate strings required. Use `nsPredicate { }` with `iOSNSPredicate` or `iOSNSPredicates`.

```kotlin
val loginButton by iOSNSPredicate(nsPredicate {
    label equalTo "Login"
    isEnabled equalTo true
})
```

Multiple top-level clauses are joined with `AND`.

### Attributes

#### String

| Attribute | NSPredicate key |
|-----------|-----------------|
| `type`    | `type`          |
| `name`    | `name`          |
| `label`   | `label`         |
| `value`   | `value`         |

#### Boolean

| Attribute    | NSPredicate key |
|--------------|-----------------|
| `isEnabled`  | `enabled`       |
| `isVisible`  | `visible`       |
| `accessible` | `accessible`    |
| `selected`   | `selected`      |
| `focused`    | `focused`       |
| `hittable`   | `hittable`      |

#### Rect (numeric)

Access via `rect.x`, `rect.y`, `rect.width`, `rect.height`.

### String matchers

All string matchers accept an optional `StringModifier` (`CaseInsensitive`, `DiacriticInsensitive`, or both).

| Function                           | Produces                            |
|------------------------------------|-------------------------------------|
| `attr equalTo "value"`             | `attr == 'value'`                   |
| `attr notEqualTo "value"`          | `attr != 'value'`                   |
| `attr.contains("v", modifier?)`    | `attr CONTAINS[modifier] 'v'`       |
| `attr.beginsWith("v", modifier?)`  | `attr BEGINSWITH[modifier] 'v'`     |
| `attr.endsWith("v", modifier?)`    | `attr ENDSWITH[modifier] 'v'`       |
| `attr.like("v*", modifier?)`       | `attr LIKE[modifier] 'v*'`          |
| `attr.matches("regex", modifier?)` | `attr MATCHES[modifier] 'regex'`    |
| `attr isIn listOf("a", "b")`       | `attr IN {'a', 'b'}`                |

### Numeric matchers

| Function                       | Produces                        |
|--------------------------------|---------------------------------|
| `attr equalTo 42`              | `attr == 42`                    |
| `attr notEqualTo 42`           | `attr != 42`                    |
| `attr greaterThan 42`          | `attr > 42`                     |
| `attr greaterThanOrEqualTo 42` | `attr >= 42`                    |
| `attr lessThan 42`             | `attr < 42`                     |
| `attr lessThanOrEqualTo 42`    | `attr <= 42`                    |
| `attr.between(lower, upper)`   | `attr BETWEEN { lower, upper }` |

### Compound predicates

| Function       | Joins clauses with | Notes                                        |
|----------------|--------------------|----------------------------------------------|
| `anyOf { }`    | `OR`               | Result is grouped in parentheses             |
| `allOf { }`    | `AND`              | Useful for explicit grouping inside `anyOf`  |
| `not { }`      | `NOT (...)`        | Negates the clause(s) inside the block       |

```kotlin
// OR grouping
val done by iOSNSPredicate(nsPredicate {
    anyOf {
        name equalTo "done"
        value equalTo "done"
    }
    type isIn listOf(XCUIElementType.BUTTON, XCUIElementType.KEY)
})
// Produces: (name == 'done' OR value == 'done') AND type IN {'XCUIElementTypeButton', 'XCUIElementTypeKey'}

// Negation + rect
val offscreen by iOSNSPredicate(nsPredicate {
    rect.x.between(1, 100)
    not { isVisible equalTo true }
})
// Produces: rect.x BETWEEN { 1, 100 } AND NOT (visible == true)

// Nested allOf inside anyOf
val submit by iOSNSPredicate(nsPredicate {
    anyOf {
        allOf {
            name equalTo "done"
            isEnabled equalTo true
        }
        label equalTo "cancel"
    }
})
// Produces: ((name == 'done' AND enabled == true) OR label == 'cancel')
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

When `service` is `null` (the default), no local server is managed - tests connect to whatever Appium server is already running.

---

## Settings DSL

Appium settings let you adjust driver behavior at runtime — unlike capabilities, they can be changed multiple times during a test.

### In the test body

Use `settings { }` directly inside the test block to apply settings before any screen interaction:
```
kotlin
androidTest(app = MyAndroidApp) {
    settings {
        ignoreUnimportantViews = true
    }
    on(::ProductsScreen) {
        // ...
    }
}
```
### In a screen chain

`settings { }` is also available on `ScreenScope`, returning the same scope for fluent chaining:
```
kotlin
on(::ProductsScreen) {
    // ...
}.settings {
    ignoreUnimportantViews = true
}.on(::ProductDetailsScreen) {
    // ...
}
```
### At server startup (via `capabilities`)

Settings can also be forwarded to the Appium server at startup as the `appium:settings` capability, using the `settings { }` block inside `capabilities { }`:
```
kotlin
object MyAndroidApp : AndroidApp(
    appPackage = "com.example.app",
    appActivity = ".MainActivity",
    service = appiumService {
        port = 4723
        capabilities {
            settings {
                ignoreUnimportantViews = true
            }
        }
    },
)
```
> **Note:** Settings applied via `capabilities { settings { } }` influence server-side behavior at startup and are **not** the same as the runtime `settings { }` DSL available in the test body or screen chain.

### Available settings

All settings map directly to the [UiAutomator2 driver settings](https://github.com/appium/appium-uiautomator2-driver#settings-api). Only set what you need — unset properties are omitted from the request.

| Property                               | Type      | Default  | Description                                                           |
|----------------------------------------|-----------|----------|-----------------------------------------------------------------------|
| `actionAcknowledgmentTimeout`          | `Long`    | 3000 ms  | Wait for acknowledgment of UiAutomator actions (click, setText, etc.) |
| `allowInvisibleElements`               | `Boolean` | false    | Include invisible elements in the XML source tree                     |
| `ignoreUnimportantViews`               | `Boolean` | false    | Enable layout hierarchy compression                                   |
| `elementResponseAttributes`            | `String`  | —        | Comma-separated attribute names to include in `findElement` response  |
| `enableMultiWindows`                   | `Boolean` | false    | Include all interactive windows in the page source                    |
| `enableTopmostWindowFromActivePackage` | `Boolean` | false    | Limit interactions to the topmost window of the active package        |
| `enableNotificationListener`           | `Boolean` | true     | Listen for new toast notifications                                    |
| `keyInjectionDelay`                    | `Long`    | 0 ms     | Delay between key presses during text injection                       |
| `scrollAcknowledgmentTimeout`          | `Long`    | 200 ms   | Wait for acknowledgment of scroll swipe actions                       |
| `shouldUseCompactResponses`            | `Boolean` | true     | Used with `elementResponseAttributes`                                 |
| `waitForIdleTimeout`                   | `Long`    | 10000 ms | Wait for the UI to become idle                                        |
| `waitForSelectorTimeout`               | `Long`    | 10000 ms | Wait for a widget to become visible (UIAutomator strategy only)       |
| `normalizeTagNames`                    | `Boolean` | false    | Normalize element class names in page source XML tag names            |
| `shutdownOnPowerDisconnect`            | `Boolean` | true     | Shut down the server if the device is disconnected from power         |
| `simpleBoundsCalculation`              | `Boolean` | false    | Use absolute element bounds without occlusion checking                |
| `trackScrollEvents`                    | `Boolean` | true     | Apply scroll event tracking                                           |
| `wakeLockTimeout`                      | `Long`    | 24 h     | UiAutomator2 server wake lock timeout                                 |
| `serverPort`                           | `Int`     | 6790     | Port for the UiAutomator2 server on the device                        |
| `mjpegServerPort`                      | `Int`     | 7810     | Port for the MJPEG screenshot broadcaster                             |
| `mjpegServerFramerate`                 | `Int`     | 10       | Max screenshots per second for MJPEG broadcaster                      |
| `mjpegScalingFactor`                   | `Int`     | 50       | Downscaling percentage for MJPEG screenshots                          |
| `mjpegServerScreenshotQuality`         | `Int`     | 50       | JPEG compression quality for MJPEG screenshots                        |
| `mjpegBilinearFiltering`               | `Boolean` | false    | Use bilinear filtering for MJPEG resize                               |
| `useResourcesForOrientationDetection`  | `Boolean` | false    | Strategy for detecting device orientation                             |
| `enforceXPath1`                        | `Boolean` | false    | Use XPath 1 instead of XPath 2                                        |
| `limitXPathContextScope`               | `Boolean` | true     | Limit context-based XPath searches to the parent element              |
| `disableIdLocatorAutocompletion`       | `Boolean` | false    | Disable resource ID locator autocompletion                            |
| `alwaysTraversableViewClasses`         | `String`  | —        | Classes to traverse even when invisible                               |
| `includeExtrasInPageSource`            | `Boolean` | —        | Include `extras` attribute in XML page source                         |
| `includeA11yActionsInPageSource`       | `Boolean` | —        | Include `actions` attribute in XML page source                        |
| `snapshotMaxDepth`                     | `Int`     | 70       | Maximum depth for the source tree snapshot                            |
| `currentDisplayId`                     | `Int`     | 0        | Display ID for element finding and screenshots                        |

---

## Cloud and parallel execution

For parallel or multi‑device execution, use a cloud provider such as BrowserStack, Sauce Labs, or LambdaTest. The integration point is the driver factory - point it at the cloud hub URL with the appropriate capabilities:

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

Everything else - test harness, screens, navigation - works identically regardless of whether the driver connects to a local or remote server.

---

## Customization

Override properties on your `App` to adjust default behavior:

- **`elementReadyCondition`** - predicate applied by locator delegates to determine when an element is ready for interaction (defaults to `isDisplayed`).
- **`waitConfig`** - default wait timing used by locator delegates.
- **`onSessionReady(driver)`** - hook invoked after the driver session is created and before any screen interactions; useful for setting e.g., orientation or geolocation, or performing pre‑navigation setup.

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

- **Port already in use when starting local Appium** - Ensure no orphaned Appium processes are running. The managed service stops on normal teardown and on JVM shutdown; if a previous run crashed, kill the process manually or change the port.

- **Tests can't find elements intermittently** - Override `elementReadyCondition` or `waitConfig` on your app to better match your UI's state transitions.

- **Using a remote Appium server** - Leave `service` as `null` (the default). The harness will not start or stop any local server.

<div align="center">
<p><img src="https://raw.githubusercontent.com/attila-fazekas/kolibrium/main/assets/kolibrium_logo.png" alt="kolibrium_logo.png"></p>
<h1>Kotlin library for Selenium tests</h1>
<p><a href="https://opensource.org/licenses/Apache-2.0"><img src="https://img.shields.io/badge/License-Apache_2.0-blue.svg" alt="License"></a>
<a href="https://github.com/attila-fazekas/kolibrium/actions/workflows/gradle.yml"><img src="https://github.com/attila-fazekas/kolibrium/actions/workflows/gradle.yml/badge.svg" alt="Build"></a>
<a href="https://github.com/attila-fazekas/kolibrium/actions/workflows/codeql.yml"><img src="https://github.com/attila-fazekas/kolibrium/actions/workflows/codeql.yml/badge.svg" alt="CodeQL"></a>
<a href="https://central.sonatype.com/search?namespace=dev.kolibrium"><img src="https://img.shields.io/maven-central/v/dev.kolibrium/kolibrium-selenium.svg" alt="Maven Central"></a>
<a href="https://javadoc.io/doc/dev.kolibrium/kolibrium-selenium"><img src="https://javadoc.io/badge2/dev.kolibrium/kolibrium-selenium/javadoc.svg" alt="Javadocs"></a>
<a href="https://img.shields.io/badge/Project%20status-Experimental-red.svg"><img src="https://img.shields.io/badge/Project%20status-Experimental-red.svg" alt="Project status"></a></p>
</div>

Build simple and maintainable automation faster with Kolibrium.

Kolibrium is a declarative Kotlin library designed to reduce boilerplate code and find better abstractions to express Selenium tests in a compact way.  

Quickly bring your test automation efforts to life with less code and easy-to-read APIs.  

# Table of content

- [Get started](#get-started)
  - [Write your first test with locator delegates](#Write-your-first-test-with-locator-delegates)
  - [Generate repository classes for locators with Kolibrium code generation](#Generate-repository-classes-for-locators-with-Kolibrium-code-generation)
  - [Use DSL functions for creating and configuring `WebDriver` instances](#Use-DSL-functions-for-creating-and-configuring-WebDriver-instances)
  - [Inject driver instances into your tests with the JUnit module](#Inject-driver-instances-into-your-tests-with-the-JUnit-module)
- [Project status](#project-status)

# Get Started

Kolibrium is divided into several subprojects (modules), each of which can be used either independently or in conjunction with others.

- `selenium`: offers a range of delegate functions for locating elements
- `ksp`: offers code generation with [Kotlin Symbol Processing (KSP)](https://kotlinlang.org/docs/ksp-overview.html) for part of the Page Object classes
- `dsl`: offers [Domain-Specific Languages (DSLs)](https://kotlinlang.org/docs/type-safe-builders.html) functions for creating, configuring, and interacting with driver instances
- `junit`: offers an extension to write JUnit tests without boilerplate

You can decide to go all-in on Kolibrium by opting for all four modules or choosing just one or two. For example, you could use the Selenium library in conjunction with DSL and JUnit, or you could use Selenium with KSP.  

## Write your first test with locator delegates

In this tutorial, we’ll cover the basics of Kolibrium and explore several project configurations to help you get started quickly. 

We'll be writing tests for the login functionality on the Sauce Labs demo e-commerce website: https://www.saucedemo.com.  

### 1. Add the Selenium module to your project

To get started, add the following dependency to your Gradle project build file (`build.gradle.kts`):
```kotlin  
dependencies { 
    implementation("dev.kolibrium:kolibrium-selenium:0.2.0") 
    // other dependencies
}  
```  

### 2. Use locator delegate functions from the Selenium module

Create a test class, such as a JUnit test class, and use the locator delegate functions from the `selenium` module to locate elements:  
```kotlin  
@Test  
fun loginTest() {  
    with(driver) {  
        val username by name("user-name")  
        val password by id("password")  
        val button by name("login-button")  
  
        username.sendKeys("standard_user")  
        password.sendKeys("secret_sauce")  
        button.click()  
  
        val shoppingCart by className("shopping_cart_link")  
  
        shoppingCart.isDisplayed shouldBe true  
    }  
}
```  

> **Note**: `shouldBe` is an assertion function from [kotest](https://kotest.io/), but you may use any assertion library you prefer.

Kolibrium utilizes the [Delegation pattern](https://en.wikipedia.org/wiki/Delegation_pattern), natively supported in Kotlin, to locate elements lazily with locator strategies.    

For instance, the following code locates the `WebElement` with the `"user-name"` attribute when it’s accessed (in this case, when the `sendKeys` command is issued):    

```kotlin  
val username by name("user-name")  
username.sendKeys("standard_user")
```  

### 3. Implement Page Object models

To introduce a layer of abstraction, let's create a Page Object class for the login page:
```kotlin  
class LoginPage(driver: WebDriver) {  
    private val username: WebElement by driver.name("user-name")  
  
    private val password: WebElement by driver.idOrName("password")  
  
    private val button: WebElement by driver.name("login-button")  
  
    fun login(username: String, password: String) {  
        this.username.sendKeys(username)  
        this.password.sendKeys(password)  
        button.click()  
    }  
}
```

Similarly, for the inventory page:  
```kotlin  
class InventoryPage(driver: WebDriver) { 
    private val shoppingCart by driver.className("shopping_cart_link")
  
    fun isShoppingCartDisplayed() = shoppingCart.isDisplayed
}
```

Now, let's utilize these page objects in our test:  
```kotlin
@Test  
fun loginTest() {  
    LoginPage(driver).login(  
        username = "standard_user",  
        password = "secret_sauce"  
    )
  
    InventoryPage(driver).isShoppingCartDisplayed() shouldBe true 
}
```

### 4. Use Context Receivers to inject the driver instance into the Page Objects

Context receivers offer a streamlined way to provide context (such as a `WebDriver` instance) to functions without passing it explicitly as an argument. In this example, we'll use context receivers to simplify access to the `WebDriver` instance in our Page Objects.  

> ⚠️ **Note**: Context receivers, also known as [context parameters](https://github.com/Kotlin/KEEP/blob/context-parameters/proposals/context-parameters.md), are still an experimental feature in Kotlin and are not enabled by default. 
 
To enable them, add the following configuration to your `build.gradle.kts` file:
```kotlin
tasks.withType<KotlinCompile> { 
    compilerOptions.freeCompilerArgs = listOf(
        "-Xcontext-receivers",
    )
}
```  

After reloading the Gradle configuration, we can add `context(WebDriver)` to our Page Objects and remove the constructor and driver instance when calling the delegate functions:  
```kotlin  
context(WebDriver)  
class LoginPage {  
    private val username: WebElement by name("user-name")  
  
    private val password: WebElement by idOrName("password")  
  
    private val button: WebElement by name("login-button")  
  
    fun login(username: String, password: String) {  
        this.username.sendKeys(username)  
        this.password.sendKeys(password)  
        button.click()  
    }  
}

context(WebDriver)
class InventoryPage { 
    private val shoppingCart by className("shopping_cart_link")
  
    fun isShoppingCartDisplayed() = shoppingCart.isDisplayed
}
```

To make the test compile after introducing context receivers, we need to provide a driver context by using the `with` scope function:  
```kotlin  
@Test  
fun loginTest() {  
    with(driver) {  
        LoginPage().login(  
            username = "standard_user",  
            password = "secret_sauce"  
        )

        InventoryPage().isShoppingCartDisplayed() shouldBe true 
    }  
}
```

## Generate repository classes for locators with Kolibrium code generation

In this section, we will begin using the `ksp` module to generate part of our Page Objects.

### 1. Add KSP module to the build file

First, update your Gradle project build file by adding the following configuration:
```kotlin
plugins {  
    kotlin("jvm") version "2.0.21"  
    id("com.google.devtools.ksp") version "2.0.21-1.0.26"  
}

dependencies {  
    implementation("dev.kolibrium:kolibrium-annotations:0.2.0")  
    implementation("dev.kolibrium:kolibrium-selenium:0.2.0")  
    ksp("dev.kolibrium:kolibrium-ksp:0.2.0")
    ksp("dev.zacsweers.autoservice:auto-service-ksp:1.2.0") 
    // other dependencies
}
```

### 2. Store locators in a single file

Create a file named `Locators.kt` and add the following enum classes to the `dev.kolibrium.demo.ksp._01.locators` package:    
```kotlin
package dev.kolibrium.demo.ksp._01.locators

@Locators  
enum class LoginPageLocators {  
    @Id("user-name")  
    username,  
      
    password,  
      
    @Name("login-button")  
    loginButton  
}  
  
@Locators  
enum class InventoryPageLocators {  
    @ClassName("shopping_cart_link")  
    shoppingCart  
}
```

### 3. Generate files

Now, build the project and navigate to the `build/generated/ksp/main/kotlin/dev/kolibrium/demo/ksp/_01/locators/generated/` directory. You will notice that two files have been created:  

`LoginPageLocators.kt`:
```kotlin
package dev.kolibrium.demo.ksp._01.locators.generated  
  
context(WebDriver)  
public class LoginPageLocators {  
  public val username: WebElement by id("user-name")  
  
  public val password: WebElement by idOrName("password")  
  
  public val loginButton: WebElement by name("login-button")  
}
```

`InventoryPageLocators`:
```kotlin
package dev.kolibrium.demo.ksp._01.locators.generated  
    
context(WebDriver)  
public class InventoryPageLocators {  
  public val shoppingCart: WebElement by className("shopping_cart_link")  
}
```

### 4. Create Page Objects and use locators from the generated files

Now, let's create Page Object classes that incorporate a `locators` property.

`LoginPage.kt`:
```kotlin
context(WebDriver)  
class LoginPage {  
    private val locators = LoginPageLocators()  
  
    fun login(username: String = "standard_user", password: String = "secret_sauce") = with(locators) {  
        this.username.sendKeys(username)  
        this.password.sendKeys(password)  
        loginButton.submit()  
    }  
}
```

`InventoryPage`:
```kotlin
context(WebDriver)  
class InventoryPage {  
    private val locators = InventoryPageLocators()  
  
    fun isShoppingCartDisplayed() = locators.shoppingCart.isDisplayed  
}
```

Next, update the test accordingly:
```kotlin
@Test  
fun loginTest() {  
    with(driver) {  
        LoginPage().login()  
  
        InventoryPage().isShoppingCartDisplayed() shouldBe true  
    }  
}
```

## Use DSL functions for creating and configuring `WebDriver` instances

In this section, we will leverage DSLs to create WebDriver instances in our tests. 

### 1. Add the DSL module to the build file

As always, begin by updating your Gradle configuration:
```kotlin  
dependencies {  
    implementation("dev.kolibrium:kolibrium-dsl:0.2.0")  
	// other dependencies
}
```

### 2. Create driver instance using the DSL module

Let's create a Chrome driver instance with the following configuration:  
```kotlin
@BeforeEach  
fun setUp() {  
    driver = chromeDriver {  
        driverService {  
            appendLog = true  
            readableTimestamp = true  
        }  
        options {  
            arguments {  
                +incognito  
                windowSize {  
                    width = 1920
                    height = 1080
                }  
            }        
        }    
    }    
    
    driver["https://www.saucedemo.com/"]  
}
```

While it's intuitive to understand what the `chromeDriver` function call does, let's break it down further:  
- It creates a `DriverService` (specifically, a `ChromeDriverService`) with `appendLog` and `readableTimestamp` enabled.  
- It sets up an `Options` object (in this case, a `ChromeOptions`) with `incognito` mode enabled and a window size of 1920 x 1080 pixels.
- Finally, it utilizes both objects to instantiate the actual driver.

## Inject driver instances into your tests with the JUnit module

It's time to unlock the full potential of Kolibrium by enabling it to inject drivers into your JUnit 5 tests.  

### 1. Add the JUnit module to the build file

As you may expect, we begin with the Gradle configurations:
```kotlin
plugins {  
    kotlin("jvm") version "2.0.21"  
    id("com.google.devtools.ksp") version "2.0.21-1.0.26"  
}

dependencies {  
    implementation("dev.kolibrium:kolibrium-annotations:0.2.0")  
    implementation("dev.kolibrium:kolibrium-junit:0.2.0")  
    implementation("dev.kolibrium:kolibrium-selenium:0.2.0")  
    ksp("dev.kolibrium:kolibrium-ksp:0.2.0")  
    ksp("dev.zacsweers.autoservice:auto-service-ksp:1.2.0")  
    testImplementation("io.kotest:kotest-assertions-core-jvm:5.9.1")  
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.3")  
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.11.3")  
}
```

### 2. Annotate your test with `@Kolibrium`

Next, let's enhance the test class by adding `context(WebDriver)` and the `@Kolibrium` annotation at the top. This allows us to simplify the test code:  
```kotlin  
context(WebDriver)  
@Kolibrium  
class JUnitTest {  
    @BeforeEach  
    fun setUp() {  
        this@WebDriver["https://www.saucedemo.com"]  
    }  
  
    @Test  
    fun loginTest() {  
        LoginPage().login(  
            username = "standard_user",  
            password = "secret_sauce"  
        )

        InventoryPage().isShoppingCartDisplayed() shouldBe true  
    }  
}
```

### 3. Customize the injected driver

You can tailor the injected driver by creating a custom Kolibrium configuration.

First, add the AutoService dependency:
```kotlin  
dependencies { 
    ksp("dev.zacsweers.autoservice:auto-service-ksp:1.2.0") 
    // other dependencies
}
```  

Next, create a project configuration by extending `AbstractProjectConfiguration` and overriding the `baseUrl` and `chromeDriver` properties:  
```kotlin
@AutoService(AbstractProjectConfiguration::class)  
class KolibriumConfiguration : AbstractProjectConfiguration() {  
    override val baseUrl = "https://www.saucedemo.com"  
  
    override val chromeDriver = {  
        chromeDriver {  
            options {  
                arguments {  
                    +incognito  
                    +start_maximized  
                }  
                experimentalOptions {  
                    excludeSwitches {  
                        +enable_automation  
                    }  
                    localState {  
                        browserEnabledLabsExperiments {  
                            +same_site_by_default_cookies  
                            +cookies_without_same_site_must_be_secure  
                        }  
                    }                
                }     
            }
        } 
    }
}
```

After that, remove the `@BeforeEach` block from the test:
```kotlin  
context(WebDriver)  
@Kolibrium  
class JUnitTest {  
    @Test  
    fun loginTest() {  
        LoginPage().login(  
            username = "standard_user",  
            password = "secret_sauce"  
        )

        InventoryPage().isShoppingCartDisplayed() shouldBe true 
    }  
}
```

With this configuration, the `@Kolibrium` annotation will instruct JUnit to inject a `ChromeDriver` with the specified experimental options into the tests. It will also navigate to https://www.saucedemo.com before executing the tests.  

# Project status

The library is currently in development and not yet ready for production. It is expected to be released as version 1.0.0 once Kotlin's [context parameters](https://github.com/Kotlin/KEEP/blob/context-parameters/proposals/context-parameters.md) are stabilized.  

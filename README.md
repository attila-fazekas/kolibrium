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
  - [Write your first test](#write-your-first-test)
  - [Generate Page Object classes with kolibrium-codegen](#generate-page-object-classes-with-kolibrium-codegen)
  - [Use DSL functions for creating driver instances](#use-dsl-functions-for-creating-driver-instances)
  - [Inject driver instances to your tests with JUNit module](#inject-driver-instances-to-your-tests-with-junit-module)
- [Project status](#project-status)

# Get Started

Kolibrium is divided into several subprojects (modules), each of which can be used either independently or in conjunction with others.

- `dsl`: offers DSL functions for creating driver instances
- `ksp`: offers code generation for Page Object classes
- `junit`: offers an extension to write JUnit tests without boilerplate
- `selenium`: offers a range of delegate functions for locating elements 

You can decide to go all-in on Kolibrium by opting for all four modules or choosing just one or two. For example, you could use the Selenium library in conjunction with DSL and JUnit, or you could use Selenium with KSP.

In this tutorial, we will cover the basics of Kolibrium and explore some project combinations to quickly get you started.

## Write your first test

We will be writing tests for the login functionality on SauceLabs demo e-commerce website (https://www.saucedemo.com/)

### 1. Add the Selenium module to your project

First add the following configuration to your Gradle project build file:
```kotlin
dependencies {  
    implementation("dev.kolibrium:kolibrium-selenium:0.1.0")  
}

repositories {  
	mavenCentral()  
}

tasks.withType<KotlinCompile> {
    compilerOptions.freeCompilerArgs = listOf(  
        "-Xcontext-receivers",  
    )  
}
```

_Note: the above configuration uses Kotlin DSL._

See the full file [here](https://gist.github.com/attila-fazekas/3e8b3460b16f1005e954d5142246a10d)

### 2. Use delegates from the Selenium module

Create a test class, such as a JUnit class, and begin using the delegate functions from the `selenium`  module:
```kotlin
@Test  
fun loginTest() {  
    with(driver) {  
        val username by name<WebElement>("user-name")  
        val password by id<WebElement>("password")  
        val button by name<WebElement>("login-button")  
  
        username.sendKeys("standard_user")  
        password.sendKeys("secret_sauce")  
        button.click()  
  
        val logo by className<WebElement>("app_logo")  
  
        logo.text shouldBe "Swag Labs"  
    }  
}
```

_Note: `shouldBe` is an assertion function from [kotest](https://kotest.io/) but you can use any assertion library you like._

See the full file [here](https://gist.github.com/attila-fazekas/261ac3d6191cef1d6390527ccd05ce56)

### 3. Introduce Page Objects

Now, let's introduce some abstraction by creating a Page Object class for the login page:
```kotlin
context(WebDriver)  
class LoginPage {  
    val username: WebElement by name<WebElement>("user-name")  
  
    val password: WebElement by idOrName<WebElement>("password")  
  
    val button: WebElement by name<WebElement>("login-button")  
  
    fun login(username: String, password: String) {  
        this.username.sendKeys(username)  
        this.password.sendKeys(password)  
        button.click()  
    }  
}
```

and the for the landing page:
```kotlin
context(WebDriver)  
class LandingPage { 
    val logo: WebElement by className<WebElement>("app_logo")  
}
```

Then let's use them in the test:
```kotlin
@Test  
fun loginTest() {  
    with(driver) {  
        LoginPage().login(  
            username = "standard_user",  
            password = "secret_sauce"  
        )  
  
        LandingPage().logo.text shouldBe "Swag Labs"  
    }  
}
```

See the full files [here](https://gist.github.com/attila-fazekas/fafc62a009bb267edd4473767afbe1ec)

## Generate Page Object classes with kolibrium-codegen

In this section, we will start using the `ksp` module to generate part of our Page Objects.

### 1. Add KSP module to the build file

First, modify your Gradle project build file by adding the following:
```kotlin
plugins {  
    kotlin("jvm") version "1.9.21"  
    id("com.google.devtools.ksp") version "1.9.21-1.0.15"  
}

dependencies {  
    implementation("dev.kolibrium:kolibrium-annotations:0.1.0")
    ksp("dev.kolibrium:kolibrium-ksp:0.1.0")
    implementation("dev.kolibrium:kolibrium-selenium:0.1.0")
}
```

See the full file [here](https://gist.github.com/attila-fazekas/b89e08eaaba4ae0dc88952b22f345c37)

### 2. Store locators in one file

Create a file named `Locators.kt` and add the following enum classes in `com.saucedemo` package:
```kotlin
@Page  
enum class LoginPageLocators {  
  
    @Name("user-name")  
    username,  
  
    password,  
  
    @Name("login-button")  
    button  
}  
  
@Page  
enum class LandingPageLocators {  
  
    @ClassName("app_logo")  
    logo  
}
```

See the full file [here](https://gist.github.com/attila-fazekas/f6c041ef46e8a11b6205210f5b32cc22)

### 3. Generate files

Now, build the project and check the `/build/generated/ksp/main/kotlin/com/saucedemo/generated/` directory. You will see that two files have been created:

`LoginPage.kt`:
```kotlin
context(WebDriver)  
public class LoginPage {  
  public val username: WebElement by name<WebElement>("user-name")  
  
  public val password: WebElement by idOrName<WebElement>("password")  
  
  public val button: WebElement by name<WebElement>("login-button")  
}
```

`LandingPage.kt`:
```kotlin
context(WebDriver)  
public class LandingPage {  
  public val logo: WebElement by className<WebElement>("app_logo")  
}
```

### 4. Create an extension function for login

Create a file e.g. `Pages.kt` in `com.saucedemo` package and write an extension function for the generated `com.saucedemo.generated.LoginPage`:
```kotlin
fun LoginPage.login(username: String, password: String) {
    this.username.sendKeys(username)
    this.password.sendKeys(password)
    button.click()
}
```

See the full file [here](https://gist.github.com/attila-fazekas/bafc4b9eb755d4a858d24feaa4058c3d)

### 5. Use the login function in your test

Import the generated Page Objects in your test and use the extension function to log in:
```kotlin
import com.saucedemo.generated.LandingPage  
import com.saucedemo.generated.LoginPage

@Test  
fun loginTest() {  
    with(driver) {  
        LoginPage().login(  
            username = "standard_user",  
            password = "secret_sauce"  
        )  
  
        LandingPage().logo.text shouldBe "Swag Labs"  
    }  
}
```

See the full file [here](https://gist.github.com/attila-fazekas/7a7f4ec02f6a8ecc1e30f24a0104d523)

## Use DSL functions for creating driver instances

Now, we will be creating WebDriver instances in our tests with the power of DSL:s.

### 1. Add the DSL module to the build file

As usual, we will start with Gradle configurations:
```kotlin
dependencies {
    implementation("dev.kolibrium:kolibrium-annotations:0.1.0")
    implementation("dev.kolibrium:kolibrium-dsl:0.1.0")
    implementation("dev.kolibrium:kolibrium-selenium:0.1.0")
    ksp("dev.kolibrium:kolibrium-ksp:0.1.0")
}
```
See the full file [here](https://gist.github.com/attila-fazekas/e79181ec33cb17ef29a3cfef6888f059)

### 2. Create driver instance with help of the DSL module

Let's create a driver instance with the following configuration:
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
                    height = 800
                    width = 600
                }
            }
        }
    }
}
```

It's easy to guess what the `chromeDriver` function call does, but let's dissect it: 
 - it creates a `DriverService` (a `ChromeDriverService` to be specific) with enabled `appendLog` and `readableTimestamp`.
 - it creates an `Options` (in this case a `ChromeOptions`) with enabled `incognito` mode and window size of 800 x 600 pixels.
 - then it uses both objects to create the actual driver.

See the full file [here](https://gist.github.com/attila-fazekas/0bf7aafa985914d9b4929333e979bfc6)

## Inject driver instances to your tests with JUNit module

It's time to unlock the full potential of Kolibrium by letting it inject drivers into your JUnit 5 tests.

### 1. Add the JUnit module to the build file

I'm sure you know the drill by now: we start with Gradle configurations:
```kotlin
dependencies {
    implementation("dev.kolibrium:kolibrium-annotations:0.1.0")
    implementation("dev.kolibrium:kolibrium-dsl:0.1.0")
    implementation("dev.kolibrium:kolibrium-junit:0.1.0")
    implementation("dev.kolibrium:kolibrium-selenium:0.1.0")
    ksp("dev.kolibrium:kolibrium-ksp:0.1.0")
}
```

See the full file [here](https://gist.github.com/attila-fazekas/13930ba924035ad1b4eeef801925516d)

### 2. Annotate your test with @Kolibrium

Let's put `context(WebDriver)` and `@Kolibrium` at the top of the test class so that the test shrinks to a more concise form:
```kotlin
context(WebDriver)
@Kolibrium
class SauceDemoTest {

    @Test
    fun loginTest() {
        LoginPage().login(
            username = "standard_user",
            password = "secret_sauce"
        )

        LandingPage().logo.text shouldBe "Swag Labs"
    }
}
```

At runtime a default `ChromeDriver` instance will be injected into the test.

See the full file [here](https://gist.github.com/attila-fazekas/d6dbe02862279339f6c49388b4393adf)

### 2. Customize the injected driver

We can tailor the injected driver by creating a custom Kolibrium configuration.

First, let's add the AutoService dependency:
```kotlin
ksp("dev.zacsweers.autoservice:auto-service-ksp:1.1.0")
```

Then let's create a project level configuration by extending `AbstractProjectConfiguration` with the following content:
```kotlin
@AutoService(AbstractProjectConfiguration::class)
class KolibriumConfiguration : AbstractProjectConfiguration() {

    override val chromeDriver = {
        chromeDriver {
            driverService {
                logLevel = DEBUG
                port = 7899
                readableTimestamp = true
            }
            options {
                arguments {
                    +start_maximized
                    +incognito
                }
                experimentalOptions {
                    excludeSwitches {
                        +Switches.enable_automation
                    }
                    localState {
                        browserEnabledLabsExperiments {
                            +ExperimentalFlags.same_site_by_default_cookies
                            +ExperimentalFlags.cookies_without_same_site_must_be_secure
                        }
                    }
                }
            }
        }
    }
}
```

When initiating tests, a ChromeDriver with some experimental options will be injected into the tests.

See the full files [here](https://gist.github.com/attila-fazekas/edc6d919a75122d29d956b8119170c0c)


# Project status

The library is not yet ready for production but is expected to be released as version 1.0.0 once Kotlin's context receivers are stabilized.
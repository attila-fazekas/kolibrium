# The Kolibrium Ecosystem

> **A comprehensive, type-safe testing toolkit for Kotlin developers**
> 
> From API client generation to browser automation - idiomatic, integrated, and built for modern Kotlin testing.

---

## 🎯 Vision

Kolibrium provides a unified, Kotlin-first testing ecosystem that seamlessly integrates API testing with browser automation. Write type-safe tests with minimal boilerplate, leveraging Kotlin's strengths at compile-time and runtime.

---

## 📦 Modules

### `kolibrium-core`
**Shared foundation for the entire ecosystem**

- Common annotations (`@InternalKolibriumApi`, `@KolibriumDsl`)
- Shared utilities (URL joining, file sanitization, etc.)
- Core abstractions used across modules
- Build configuration shared across modules

**Keep it minimal** - only truly shared code lives here.

---

### `kolibrium-api` 🚀
**Compile-time type-safe REST clients via KSP code generation**

#### The Problem
Current Kotlin API testing is verbose and error-prone:
```kotlin
// Manual, repetitive, fragile
val response = client.post("/users") {
    contentType(ContentType.Application.Json)
    setBody("""{"name":"John","email":"john@example.com"}""")
}
val user = Json.decodeFromString<User>(response.bodyAsText())
```

#### The Solution
Define your API once with `@Serializable` data classes. KSP generates type-safe clients:

```kotlin
@Serializable
data class CreateUserRequest(val name: String, val email: String)

@Serializable  
data class User(val id: String, val name: String, val email: String)

@ApiClient(baseUrl = "https://api.example.com")
interface UserApi {
    @POST("/users")
    suspend fun createUser(@Body request: CreateUserRequest): User
    
    @GET("/users/{id}")
    suspend fun getUser(@Path("id") userId: String): User
    
    @DELETE("/users/{id}")
    suspend fun deleteUser(@Path("id") userId: String)
}

// Usage in tests - fully type-safe!
val api = UserApi.create()
val user = api.createUser(CreateUserRequest("John", "john@example.com"))
assertEquals("John", user.name)
```

#### Key Features
- ✅ **Type-safe at compile time** - catch errors before runtime
- ✅ **Fast KSP code generation** - faster than annotation processing
- ✅ **kotlinx.serialization** - native Kotlin serialization
- ✅ **Testing-focused DSL** - built-in assertions and helpers
- ✅ **Zero reflection overhead** - all generated at compile time
- ✅ **Built on Ktor Client** - modern, coroutine-based HTTP

#### Sub-modules
- `kolibrium-api-annotations` - Annotation definitions (`@GET`, `@POST`, `@Path`, etc.)
- `kolibrium-api-ksp` - KSP processor for code generation

---

### `kolibrium-selenium`
**Enhanced Selenium WebDriver with Kotlin ergonomics**

#### Value Proposition
Solves common Selenium pain points with idiomatic Kotlin patterns:

- **Fluent, type-safe element location** - better than raw By locators
- **Smart waits and synchronization** - no more flaky tests
- **Page Object patterns** - structured, maintainable test code
- **Thread-safe session management** - safe parallel test execution
- **Enhanced failure handling** - automatic screenshots, better diagnostics

#### Key Features
- Wrapper around Selenium Java with better ergonomics
- Reduces boilerplate while maintaining Selenium's full power
- Battle-tested patterns for robust browser automation
- Seamless integration with kolibrium-api for setup/teardown

---

### `kolibrium-playwright`
**Lightweight test harness for Playwright**

#### Philosophy
**Not a wrapper** - Playwright's API is already excellent. Instead, we provide:

- **Structure** - Site, PageObject, and Session abstractions
- **Lifecycle management** - traces, videos, screenshots on failure
- **Thread-safety guarantees** - safe concurrent test execution
- **Test framework integration** - JUnit 5, trace-on-failure, etc.
- **Sensible defaults** - opinionated config for common cases
- **Escape hatches** - full Playwright API access when needed

#### Core Concepts

**Sites** - Base abstraction for web applications:
```kotlin
object SauceDemo : Site(baseUrl = "https://www.saucedemo.com") {
    override val cookies = listOf(/* ... */)
    override fun configureSite() { /* optional setup */ }
}
```

**PageObjects** - Represent pages with type-safe navigation:
```kotlin
class LoginPage : PageObject<SauceDemo>() {
    override val path = "/login"
    
    fun login(username: String, password: String): DashboardPage {
        page.fill("#username", username)
        page.fill("#password", password)
        page.click("#login-button")
        return DashboardPage()
    }
}
```

**Test Harness** - Clean DSL for test execution:
```kotlin
playwrightTest(
    site = SauceDemo,
    config = Config(headless = false, recordTrace = true)
) {
    open(::LoginPage) { login("user", "pass") }
        .then { verifyDashboard() }
        .then { logout() }
}
```

#### Key Features
- Thread-confined sessions (safe parallel execution)
- Automatic trace recording on test failure
- Video recording support
- Viewport and browser configuration
- Integration with JUnit 5 lifecycle

---

## 🔗 Integration: The Killer Feature

Kolibrium's modules work together seamlessly for complete E2E workflows:

```kotlin
@Test
fun `full E2E test with API setup`() = playwrightTest(
    site = MyApp,
    prepare = {
        // Use kolibrium-api to set up test data
        val api = UserApi.create(baseUrl = "https://api.myapp.com")
        api.createUser(CreateUserRequest(
            name = "Test Admin",
            email = "admin@test.com",
            role = "ADMIN"
        ))
    }
) {
    // Now do UI testing with prepared data
    open(::LoginPage) { login("admin@test.com", "password") }
        .then { verifyAdminDashboard() }
        .then { createNewUser("john@example.com") }
}
```

**The workflow:**
1. Generate type-safe API client (kolibrium-api)
2. Set up test data via API (fast, reliable)
3. Verify UI behavior (kolibrium-playwright/selenium)
4. Clean up via API in teardown

**Benefits:**
- Faster test execution (API setup > UI setup)
- More reliable (avoid UI flakiness in setup)
- Type-safe across the entire test
- One ecosystem, consistent patterns

---

## 🎨 Design Philosophy

### 1. **Kotlin-First**
Not Java-with-Kotlin. Embrace coroutines, type safety, DSLs, and modern language features.

### 2. **Type Safety at Compile Time**
Catch errors before runtime. Use KSP, sealed classes, and strong typing throughout.

### 3. **Minimal Boilerplate**
Write what matters - the test logic. Let the framework handle ceremony.

### 4. **Don't Wrap Good APIs**
Playwright's API is excellent - provide structure, not wrappers. Ktor Client is solid - generate on top of it, don't replace it.

### 5. **Escape Hatches Everywhere**
Opinionated defaults for the 80% case. Full control for the 20% edge cases.

### 6. **Testing-Focused**
Built for testing workflows, not production code. Optimized for debuggability and failure analysis.


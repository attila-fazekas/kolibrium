# Kolibrium API User Guide

## Table of Contents
1. [Overview](#overview)
2. [Setup](#setup)
3. [Defining API specification](#defining-api-specification)
4. [Defining request models](#defining-request-models)
    - [Path parameters](#path-parameters)
    - [Query parameters](#query-parameters)
    - [Body parameters](#body-parameters)
5. [Defining response models](#defining-response-models)
6. [Generate code](#generate-code)
7. [Customizing code generation](#customizing-code-generation)
    - [Client grouping](#client-grouping)
8. [HTTP method annotations](#http-method-annotations)
9. [Authentication](#authentication)
10. [Return types](#return-types)
11. [Validation rules](#validation-rules)
12. [Complete examples](#complete-examples)
13. [Best practices](#best-practices)
14. [Troubleshooting](#troubleshooting)

---

## Overview

The Kolibrium API module is a code generation tool that uses [Kotlin Symbol Processing (KSP)](https://kotlinlang.org/docs/ksp-overview.html) to automatically generate type-safe HTTP client code from annotated request models. It provides a declarative way to define REST API endpoints and generates:

- **Type-safe client methods** - Compile-time safety for API calls
- **Automatic serialization/deserialization** - Using kotlinx.serialization
- **Authentication handling** - Support for Bearer, Basic, API Key, and Custom auth
- **Test harness functions** - Built-in testing utilities
- **Path parameter substitution** - Automatic URL construction
- **Query parameter handling** - Optional parameters with null safety
- **Request body DSL builders** - Fluent API for building requests

> Note: Context parameters require Kotlin’s context parameters feature to be enabled in your project.

---

## Setup

### If you're starting from scratch

The easiest way to get started is to create a repository from the [kolibrium-api-starter](https://github.com/attila-fazekas/kolibrium-api-starter) template.
It includes all necessary dependencies so that you can start coding immediately.

### If you already have an existing project

#### 1. Add KSP plugin

Add the KSP plugin and Kotlin serialization to your `build.gradle.kts`:

```kotlin
plugins {
    kotlin("jvm") version "2.3.10"
    kotlin("plugin.serialization") version "2.3.10"
    id("com.google.devtools.ksp") version "2.3.5"
}
```

#### 2. Add dependencies

```kotlin
dependencies {
    // Kolibrium API core
    implementation("dev.kolibrium:kolibrium-api-core:<version>")
    
    // KSP annotations
    implementation("dev.kolibrium:kolibrium-api-ksp-annotations:<version>")
    
    // KSP processor
    ksp("dev.kolibrium:kolibrium-api-ksp-processors:<version>")
    
    // Ktor client (required)
    implementation("io.ktor:ktor-client-core:<ktor-version>")
    implementation("io.ktor:ktor-client-cio:<ktor-version>")
    implementation("io.ktor:ktor-client-content-negotiation:<ktor-version>")
    implementation("io.ktor:ktor-serialization-kotlinx-json:<ktor-version>")

    // Testing libraries, such as JUnit, Kotest, etc.
}
```

---

## Defining API specification

An API specification is the entry point for code generation. Create a class or object that:
1. Extends `ApiSpec`. Class name should follow the pattern `<Name>ApiSpec` (e.g., `VinylStoreApiSpec`)
2. Is annotated with `@GenerateApi`
3. Implements `baseUrl`

### Basic API specification example

```kotlin
import dev.kolibrium.api.core.ApiSpec
import dev.kolibrium.api.ksp.annotations.GenerateApi

@GenerateApi
object MyApiSpec : ApiSpec(
    baseUrl = "https://api.example.com"
)
```

## Defining request models

Request models define API endpoints and can have the following properties:
- Path variable in the URL path
- Query parameter in the URL query string
- Body parameter

**Requirements:**
- Must be annotated with `@Serializable` and, by default, placed under `<api-package>.models` (scan packages can be customized on the API spec)
- Must have exactly one HTTP method annotation (`@GET`, `@POST`, `@PUT`, `@PATCH`, `@DELETE`)
- Must have a `@Returns` annotation specifying the response type
- Class name must end with the `Request` suffix (e.g., `GetUserRequest`, `CreateOrderRequest`)
- If the class has properties, it must be a `data class`
- Classes without properties (marker classes) don't need to be data classes
- Cannot be `abstract` or `sealed`

### Basic request model example

```kotlin
import dev.kolibrium.api.ksp.annotations.GET
import dev.kolibrium.api.ksp.annotations.Returns
import kotlinx.serialization.Serializable

@GET("/users")
@Returns(UsersResponse::class)
@Serializable
class ListUsersRequest
```

### Path parameters

Path variables are defined using curly braces `{variableName}` and substituted into the URL path.

```kotlin
@GET("/users/{id}/posts/{postId}")
@Returns(Post::class)
@Serializable
data class GetUserPostRequest(
    @Path @Transient val id: Int = 0,
    @Path @Transient val postId: Int = 0
)
```

**Requirements:**
- Must be annotated with both `@Path` and `@Transient`
- Must be one of: `String`, `Int`, `Long`, `Short`, `Float`, `Double`, `Boolean`
- Must have a matching path variable in the URL

### Query parameters

Query parameters are appended to the URL as query strings.

```kotlin
@GET("/users")
@Returns(UserList::class)
@Serializable
data class ListUsersRequest(
    @Query @Transient val page: Int? = null,
    @Query @Transient val limit: Int? = null,
    @Query @Transient val search: String? = null
)
```

**Requirements:**
- Must be annotated with both `@Query` and `@Transient`
- Must be nullable (query parameters are optional)
- Only allowed on `GET` and `DELETE` requests
- Must be one of: `String?`, `Int?`, `Long?`, `Boolean?`, `Short?`, `Float?`, `Double?`, or `List<T>?` where T is one of these types

### Body parameters

Body parameters are serialized as the request body (JSON).

```kotlin
@POST("/users")
@Returns(User::class)
@Serializable
data class CreateUserRequest(
    var name: String? = null,
    var email: String? = null,
    var age: Int? = null
)
```

**Requirements:**
- Only allowed on `POST`, `PUT`, and `PATCH` requests
- Should be `var` to support the DSL builder pattern
- Must be nullable or have a default value
- Not annotated with `@Path` or `@Query`

---

## Defining response models

The response type must be annotated with `@Serializable`.

### Basic response model example

```kotlin
@Serializable
data class UsersResponse(
    val users: List<UserResponse>,
    val total: Int,
)

@Serializable
data class UserResponse( 
    val id: Int,
    val email: Email,
    val role: Role,
    val isActive: Boolean,
    val createdAt: String,
    val updatedAt: String,
)
```

---

## Generate code

After building the project, the following files are generated in `<api-package>.generated` package:

### Client Class

`<ApiName>Client.kt` - Client class with all API methods

### Test Harness

`<ApiName>TestHarness.kt` - Test utility functions

#### Simple Test Harness

```kotlin
fun myApiTest(
    baseUrl: String = MyApiSpec.baseUrl,
    client: HttpClient = defaultHttpClient,
    block: suspend MyClient.() -> Unit
)
```

Usage:

```kotlin
@Test
fun `get users`() = myApiTest {
    val response = getUsers()
    response.requireSuccess()
}
```

#### Test Harness with Setup/Teardown

```kotlin
fun <T> myApiTest(
    baseUrl: String = MyApiSpec.baseUrl,
    client: HttpClient = defaultHttpClient,
    setUp: suspend MyClient.() -> T,
    tearDown: suspend MyClient.(T) -> Unit = {},
    block: suspend MyClient.(T) -> Unit
)
```

Usage:

```kotlin
@Test
fun `update user`() = myApiTest(
    setUp = {
        // Create test user, return its ID
        createUser { name = "Test" }.body.id
    },
    tearDown = { userId ->
        // Clean up
        deleteUser(userId)
    }
) { userId ->
    // Test with the created user
    val response = updateUser(userId) { name = "Updated" }
    assertEquals("Updated", response.body.name)
}
```

---

## Customizing code generation

### Scan custom packages

By default, the processor scans `<api-package>.models` for request classes. You can also specify custom packages:

```kotlin
@GenerateApi(scanPackages = ["com.example.api.requests", "com.example.api.queries"])
object MyApiSpec : ApiSpec(
    baseUrl = "https://api.example.com"
)
```

### Client grouping

The processor supports two client organization modes:

#### SingleClient (Default)

All endpoints are generated in a single client class:

```kotlin
@GenerateApi(grouping = ClientGrouping.SingleClient)
object MyApiSpec : ApiSpec(baseUrl = "https://api.example.com")
```

Generates:

```kotlin
class MyClient(client: HttpClient, baseUrl: String) {
    suspend fun getUser(id: Int): ApiResponse<User>
    suspend fun listUsers(): ApiResponse<UserList>
    suspend fun createVinyl(block: CreateVinylRequest.() -> Unit): ApiResponse<Vinyl>
    // ... all methods in one class
}
```

#### ByPrefix

This feature automatically organizes API client methods into separate client classes based on the first path segment of their endpoints. 
This helps with:
- Code Organization: Large APIs with many endpoints get split into logical, resource-based client classes
- Discoverability: Developers can navigate client.users.getUser() vs client.vinyls.createVinyl() more intuitively
- Separation of Concerns: Each resource domain gets its own client class

```kotlin
@GenerateApi(grouping = ClientGrouping.ByPrefix)
object MyApiSpec : ApiSpec(baseUrl = "https://api.example.com")
```

Given these requests:
- `GET /users/{id}` → `UsersClient`
- `GET /users` → `UsersClient`
- `POST /vinyls` → `VinylsClient`
- `GET /vinyls/{id}` → `VinylsClient`

It groups related endpoints by their first path segment and generates:

```kotlin
// Group clients
class UsersClient(client: HttpClient, baseUrl: String) {
    suspend fun getUser(id: Int): ApiResponse<User>
    suspend fun listUsers(): ApiResponse<UserList>
}

class VinylsClient(client: HttpClient, baseUrl: String) {
    suspend fun getVinyl(id: Int): ApiResponse<Vinyl>
    suspend fun createVinyl(block: CreateVinylRequest.() -> Unit): ApiResponse<Vinyl>
}

// Root aggregator client that contains all group clients as properties
class MyClient(client: HttpClient, baseUrl: String) {
    val users = UsersClient(client, baseUrl)
    val vinyls = VinylsClient(client, baseUrl)
}
```

Usage:

```kotlin
val client = MyClient(httpClient, "https://api.example.com")
client.users.getUser(1)
client.vinyls.createVinyl { artist = "Pink Floyd" }
```

> Note: If all paths start with `/api/...`, everything groups under a single ApiClient, defeating the purpose of grouping. The solution is to put `/api` in `baseUrl`.

---

## HTTP method annotations

The following HTTP method annotations are available:

| Annotation | HTTP Method | Typical Use Case     |
|------------|-------------|----------------------|
| `@GET`     | GET         | Retrieving resources |
| `@POST`    | POST        | Creating resources   |
| `@PUT`     | PUT         | Replacing resources  |
| `@PATCH`   | PATCH       | Partial updates      |
| `@DELETE`  | DELETE      | Removing resources   |

Each annotation requires a `path` parameter:

```kotlin
@GET("/users")           // List users
@GET("/users/{id}")      // Get user by ID
@POST("/users")          // Create user
@PUT("/users/{id}")      // Replace user
@PATCH("/users/{id}")    // Update user
@DELETE("/users/{id}")   // Delete user
```

---

## Authentication

The `@Auth` annotation configures authentication for requests.

### No authentication (default)

```kotlin
@GET("/public/data")
@Returns(Data::class)
@Serializable
class GetPublicDataRequest
```

### Bearer token authentication

```kotlin
@GET("/secure/data")
@Auth(type = AuthType.BEARER)
@Returns(Data::class)
@Serializable
class GetSecureDataRequest
```

Generated method requires a `token` context parameter:

```kotlin
context(token: String)
suspend fun getSecureData(): ApiResponse<Data>
```

Usage:

```kotlin
context("my-bearer-token") {
    client.getSecureData()
}
```

### Basic Authentication

```kotlin
@GET("/protected/resource")
@Auth(type = AuthType.BASIC)
@Returns(Resource::class)
@Serializable
class GetProtectedResourceRequest
```

Generated method requires `username` and `password` context parameters:

```kotlin
context(username: String, password: String)
suspend fun getProtectedResource(): ApiResponse<Resource>
```

Usage:

```kotlin
context("admin", "secret") {
    client.getProtectedResource()
}
```

### API key authentication

```kotlin
@GET("/api/data")
@Auth(type = AuthType.API_KEY)
@Returns(Data::class)
@Serializable
class GetApiDataRequest
```

By default, the processor uses the `X-API-Key` header name, but you can customize it:

```kotlin
@GET("/api/data")
@Auth(type = AuthType.API_KEY, headerName = "X-Custom-API-Key")
@Returns(Data::class)
@Serializable
class GetApiDataRequest
```

Generated method requires an `apiKey` context parameter:

```kotlin
context(apiKey: String)
suspend fun getApiData(): ApiResponse<Data>
```

Usage:
```kotlin
context("my-api-key") {
    client.getApiData()
}
```

### Custom authentication

```kotlin
@GET("/custom/auth")
@Auth(type = AuthType.CUSTOM)
@Returns(Data::class)
@Serializable
class GetCustomAuthDataRequest
```

Requires implementing an `AuthContext.Custom` class in your API package:

```kotlin
package your.api.package

import io.ktor.client.request.HttpRequestBuilder

sealed interface AuthContext {
    object Custom : AuthContext {
        fun configure(request: HttpRequestBuilder) {
            // Add custom authentication logic
            request.headers.append("X-Custom-Header", "value")
        }
    }
}
```

---

## Return types

### Typed responses

Specify the response type using `@Returns`:

```kotlin
@GET("/users/{id}")
@Returns(User::class)
@Serializable
data class GetUserRequest(@Path @Transient val id: Int = 1)
```

The response type must be `@Serializable`:

```kotlin
@Serializable
data class User(
    val id: Int,
    val name: String,
    val email: String
)
```

Generated method returns `ApiResponse<User>`:

```kotlin
suspend fun getUser(id: Int): ApiResponse<User>
```

### Empty responses

For endpoints that return no body (e.g., DELETE):

```kotlin
@DELETE("/users/{id}")
@Returns(Unit::class)
@Serializable
data class DeleteUserRequest(@Path @Transient val id: Int = 1)
```

Generated method returns `EmptyResponse` (typealias for `ApiResponse<Unit>`):

```kotlin
suspend fun deleteUser(id: Int): EmptyResponse
```

### ApiResponse class

All generated methods return `ApiResponse<T>` which provides:

```kotlin
class ApiResponse<T>(
    val status: HttpStatusCode,    // HTTP status code
    val headers: Headers,          // Response headers
    val contentType: ContentType?, // Content type
    val body: T                    // Deserialized body
) {
    val isSuccess: Boolean         // true if 2xx
    val isClientError: Boolean     // true if 4xx
    val isServerError: Boolean     // true if 5xx
    
    fun header(name: String): String?  // Get specific header
    fun requireSuccess(): ApiResponse<T>  // Throws if not 2xx
}
```
---

## Validation rules

The processor validates your code at compile time and reports errors for:

### API specification errors

| Error                               | Cause                                                     |
|-------------------------------------|-----------------------------------------------------------|
| Must be an object or concrete class | `@GenerateApi` on interface or abstract class             |
| Must extend ApiSpec                 | Missing `ApiSpec` base class                              |
| Duplicate API name                  | Multiple `@GenerateApi` classes with same name in package |

### Request class errors

| Error                            | Cause                                  |
|----------------------------------|----------------------------------------|
| Must end with 'Request' suffix   | Class name doesn't end with `Request`  |
| Must be a data class             | Has properties but isn't a data class  |
| Cannot be abstract or sealed     | Request class is abstract or sealed    |
| Multiple HTTP method annotations | More than one of `@GET`, `@POST`, etc. |
| Must specify a path              | HTTP annotation has empty path         |
| Must have @Returns annotation    | Missing `@Returns` annotation          |

### Parameter errors

| Error                                                             | Cause                                    |
|-------------------------------------------------------------------|------------------------------------------|
| @Path must be annotated with @Transient                           | Path parameter missing `@Transient`      |
| @Path must be String, Int, Long, Short, Float, Double, or Boolean | Invalid path parameter type              |
| Path variable has no matching @Path parameter                     | URL has `{var}` but no matching property |
| @Query must be annotated with @Transient                          | Query parameter missing `@Transient`     |
| @Query must be nullable                                           | Non-nullable query parameter             |
| @Query not allowed on POST/PUT/PATCH                              | Query params on body requests            |
| Body parameters not allowed on GET/DELETE                         | Body params on non-body requests         |
| Body parameter must be nullable or have default                   | Non-nullable body param without default  |

### Return type errors

| Error                             | Cause                                 |
|-----------------------------------|---------------------------------------|
| Return type not @Serializable     | Response type missing `@Serializable` |
| Return type could not be resolved | Invalid or missing type in `@Returns` |

---

## Complete examples

### Full API definition

```kotlin
// ApiSpec.kt
package dev.kolibrium.api.example

import dev.kolibrium.api.core.ApiSpec
import dev.kolibrium.api.ksp.annotations.GenerateApi

@GenerateApi
object VinylStoreApiSpec : ApiSpec(
    baseUrl = "http://localhost:8080"
)
```

```kotlin
// models/Vinyl.kt
package dev.kolibrium.api.example.models

import dev.kolibrium.api.ksp.annotations.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

// Request models
@POST("/api/vinyls")
@Returns(Vinyl::class)
@Serializable
data class CreateVinylRequest(
    var artist: String? = null,
    var album: String? = null,
    var year: Int? = null,
    var genre: String? = null,
    var price: Double? = null,
    var stock: Int? = null
)

@GET("/api/vinyls/{id}")
@Returns(Vinyl::class)
@Serializable
data class GetVinylRequest(
    @Path @Transient val id: Int = 0
)

@GET("/api/vinyls")
@Returns(VinylList::class)
@Serializable
data class ListVinylsRequest(
    @Query @Transient val genre: String? = null,
    @Query @Transient val artist: String? = null
)

@PUT("/api/vinyls/{id}")
@Returns(Vinyl::class)
@Serializable
data class UpdateVinylRequest(
    @Path @Transient val id: Int = 0,
    var artist: String? = null,
    var album: String? = null,
    var year: Int? = null,
    var genre: String? = null,
    var price: Double? = null,
    var stock: Int? = null
)

@DELETE("/api/vinyls/{id}")
@Returns(Unit::class)
@Serializable
data class DeleteVinylRequest(
    @Path @Transient val id: Int = 0
)

// Response models
@Serializable
data class Vinyl(
    val id: Int,
    val artist: String,
    val album: String,
    val year: Int,
    val genre: String,
    val price: Double,
    val stock: Int
)

@Serializable
data class VinylList(
    val vinyls: List<Vinyl>,
    val total: Int
)
```

### Using the generated client

```kotlin
import dev.kolibrium.api.example.generated.VinylStoreClient
import dev.kolibrium.api.example.generated.vinylStoreApiTest
import dev.kolibrium.api.core.defaultHttpClient

// Direct client usage
suspend fun main() {
    val client = VinylStoreClient(defaultHttpClient, "http://localhost:8080")
    
    // Create a vinyl using DSL
    val created = client.createVinyl {
        artist = "Pink Floyd"
        album = "The Dark Side of the Moon"
        year = 1973
        genre = "Progressive Rock"
        price = 29.99
        stock = 10
    }
    println("Created: ${created.body}")
    
    // Get by ID
    val vinyl = client.getVinyl(created.body.id)
    println("Retrieved: ${vinyl.body}")
    
    // List with filters
    val rockVinyls = client.listVinyls(genre = "Progressive Rock")
    println("Found ${rockVinyls.body.total} rock vinyls")
    
    // Update
    val updated = client.updateVinyl(created.body.id) {
        price = 24.99
    }
    println("Updated price: ${updated.body.price}")
    
    // Delete
    client.deleteVinyl(created.body.id)
}

// Test usage
class VinylStoreTest {
    @Test
    fun `create and retrieve vinyl`() = vinylStoreApiTest {
        val created = createVinyl {
            artist = "Test Artist"
            album = "Test Album"
            year = 2024
            genre = "Test"
            price = 19.99
            stock = 5
        }
        
        assertTrue(created.isSuccess)
        
        val retrieved = getVinyl(created.body.id)
        assertEquals(created.body, retrieved.body)
        
        // Cleanup
        deleteVinyl(created.body.id)
    }
    
    @Test
    fun `test with setup and teardown`() = vinylStoreApiTest(
        setUp = {
            createVinyl {
                artist = "Setup Artist"
                album = "Setup Album"
                year = 2024
                genre = "Test"
                price = 9.99
                stock = 1
            }.body.id
        },
        tearDown = { vinylId ->
            deleteVinyl(vinylId)
        }
    ) { vinylId ->
        val response = getVinyl(vinylId)
        assertEquals("Setup Artist", response.body.artist)
    }
}
```

---

## Best practices

### Organize request models

Keep request and response models in the default `models` subpackage:

```
dev.kolibrium.api.example/
├── MyApiSpec.kt
└── models/
    ├── User.kt          # User requests and response
    ├── Order.kt         # Order requests and response
    └── Product.kt       # Product requests and response
```

### Use descriptive names

```kotlin
// Good
data class GetUserByIdRequest(...)
data class ListActiveOrdersRequest(...)
data class CreateProductRequest(...)

// Avoid
data class UserRequest(...)      // Ambiguous
data class GetRequest(...)       // Too generic
```

### Handle errors gracefully

```kotlin
val response = client.getUser(id)

when {
    response.isSuccess -> handleUser(response.body)
    response.isClientError -> handleNotFound()
    response.isServerError -> handleServerError()
}

// Or use requireSuccess() for fail-fast
val user = client.getUser(id).requireSuccess().body
```

### Use test harness for integration tests

```kotlin
@Test
fun `integration test`() = myApiTest(
    setUp = { /* create test data */ },
    tearDown = { /* cleanup */ }
) { testData ->
    // Test with guaranteed cleanup
}
```

---

## Troubleshooting

### No code generated

**Symptoms:** Build succeeds but no client classes are generated.

**Solutions:**
1. Ensure `@GenerateApi` class extends `ApiSpec`
2. Check that request classes are in the scan packages (default: `<api-package>.models`)
3. Verify request classes have `@Serializable` and HTTP method annotations
4. Check KSP is properly configured in `build.gradle.kts`

### Compilation errors in generated code

**Symptoms:** Generated code has import or type errors.

**Solutions:**
1. Ensure all response types are `@Serializable`
2. Add required Ktor dependencies
3. Check that kotlinx.serialization plugin is applied

### Path variable mismatch

**Symptoms:** Error about path variable not matching.

**Solutions:**
1. Ensure path variable names match property names exactly
2. Check for typos in `{variableName}` syntax
3. Verify `@Path` annotation is present on the property

### Query parameters not working

**Symptoms:** Query parameters ignored or cause errors.

**Solutions:**
1. Add `@Transient` annotation alongside `@Query`
2. Make query parameters nullable
3. Only use on `GET` or `DELETE` requests

### Authentication not applied

**Symptoms:** Requests fail with 401 Unauthorized.

**Solutions:**
1. Verify `@Auth` annotation is on the request class
2. Provide credentials via context parameters
3. For `CUSTOM` auth, ensure `AuthContext.Custom` is implemented in the correct package

### Build performance

**Symptoms:** Slow incremental builds.

**Solutions:**
1. Use KSP incremental processing (enabled by default)
2. Minimize changes to API spec files
3. Consider splitting large APIs into multiple specs

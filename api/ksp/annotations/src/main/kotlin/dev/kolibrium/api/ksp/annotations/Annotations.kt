/*
 * Copyright 2023-2025 Attila Fazekas & contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.kolibrium.api.ksp.annotations

import kotlin.reflect.KClass

/**
 * Marks a class as an HTTP GET request specification.
 *
 * Classes annotated with @GET will have a corresponding client method generated
 * that performs an HTTP GET request to the specified path.
 *
 * @property path The URL path for this GET request (e.g., "/users/{id}")
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
public annotation class GET(
    val path: String,
)

/**
 * Marks a class as an HTTP POST request specification.
 *
 * Classes annotated with @POST will have a corresponding client method generated
 * that performs an HTTP POST request to the specified path. Typically used for
 * creating resources or submitting data.
 *
 * @property path The URL path for this POST request (e.g., "/users")
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
public annotation class POST(
    val path: String,
)

/**
 * Marks a class as an HTTP PATCH request specification.
 *
 * Classes annotated with @PATCH will have a corresponding client method generated
 * that performs an HTTP PATCH request to the specified path. Typically used for
 * partial updates to existing resources.
 *
 * @property path The URL path for this PATCH request (e.g., "/users/{id}")
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
public annotation class PATCH(
    val path: String,
)

/**
 * Marks a class as an HTTP PUT request specification.
 *
 * Classes annotated with @PUT will have a corresponding client method generated
 * that performs an HTTP PUT request to the specified path. Typically used for
 * complete updates or replacements of existing resources.
 *
 * @property path The URL path for this PUT request (e.g., "/users/{id}")
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
public annotation class PUT(
    val path: String,
)

/**
 * Marks a class as an HTTP DELETE request specification.
 *
 * Classes annotated with @DELETE will have a corresponding client method generated
 * that performs an HTTP DELETE request to the specified path. Typically used for
 * removing resources.
 *
 * @property path The URL path for this DELETE request (e.g., "/users/{id}")
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
public annotation class DELETE(
    val path: String,
)

/**
 * Marks a property as a path parameter in an API request.
 *
 * Properties annotated with @Path will be substituted into the URL path template.
 * For example, given a path "/users/{id}" and a property `val id: String` annotated
 * with @Path, the property value will replace the "{id}" placeholder in the URL.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.PROPERTY)
public annotation class Path

/**
 * Marks a property as a query parameter in an API request.
 *
 * Properties annotated with @Query will be appended to the URL as query string parameters.
 * For example, a property `val limit: Int` annotated with @Query will be added as
 * "?limit=value" to the request URL.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.PROPERTY)
public annotation class Query

/**
 * Specifies the return types for an API request.
 *
 * Use this annotation on request classes to indicate what types of data the API
 * endpoint returns. The generated client method will deserialize the response
 * body into the specified types.
 *
 * When only [success] is specified, the generated method returns `ApiResponse<SuccessType>`.
 *
 * When both [success] and [error] are specified, the generated method returns a sealed
 * result type with Success and Error variants, allowing type-safe handling of both cases:
 * ```kotlin
 * when (val result = client.login { ... }) {
 *     is LoginResult.Success -> println("Token: ${result.data.token}")
 *     is LoginResult.Error -> println("Error: ${result.data.message}")
 * }
 * ```
 *
 * @property success The KClass of the expected success response type
 * @property error The optional KClass of the expected error response type. When specified,
 *   the generated method returns a sealed result type instead of [ApiResponse].
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
public annotation class Returns(
    val success: KClass<*>,
    val error: KClass<*> = Nothing::class,
)

/**
 * Defines how API client classes are organized.
 */
public enum class ClientGrouping {
    /**
     * Generates a single client class containing all API methods.
     * This is the default behavior.
     */
    SingleClient,

    /**
     * Groups API methods by their path prefix into separate client classes.
     * For example, `/vinyls/...` endpoints go into `VinylsClient`,
     * `/users/...` endpoints go into `UsersClient`, etc.
     * A root aggregator client is generated that contains properties for each group.
     */
    ByPrefix,
}

/**
 * Configures authentication for an API request.
 *
 * This annotation specifies the authentication mechanism to be used when making
 * API calls. It can be applied to request classes to indicate how credentials
 * should be provided.
 *
 * @property type The type of authentication to use. Defaults to [AuthType.NONE].
 * @property headerName The name of the header used for authentication credentials.
 *   Only relevant for [AuthType.API_KEY]. Defaults to "X-API-Key".
 *
 * Example usage:
 * ```kotlin
 * @GET("/secure/data")
 * @Auth(type = AuthType.BEARER)
 * data class GetSecureData(...)
 *
 * @GET("/protected/resource")
 * @Auth(type = AuthType.API_KEY, headerName = "X-Custom-API-Key")
 * data class GetProtectedResource(...)
 * ```
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
public annotation class Auth(
    val type: AuthType = AuthType.NONE,
    val headerName: String = "X-API-Key",
)

/**
 * Marks a class or object as an API specification.
 *
 * Classes annotated with @GenerateApi serve as the entry point for API client generation.
 * The processor will generate a client class with methods for all request classes
 * found in the scan packages.
 *
 * @property scanPackages The packages to scan for request classes. If empty (default),
 *   the processor will scan `<api-package>.models` where `<api-package>` is the package
 *   containing the @GenerateApi class.
 * @property grouping Defines how the generated client classes are organized.
 *   Use [ClientGrouping.SingleClient] (default) for a single client with all methods,
 *   or [ClientGrouping.ByPrefix] to group methods by their API path prefix.
 *
 * Example usage:
 * ```kotlin
 * // Uses default scan package: dev.example.api.models
 * @GenerateApi
 * object MyApi : Api("https://api.example.com")
 *
 * // Explicit scan packages
 * @GenerateApi(scanPackages = ["dev.example.api.requests", "dev.example.api.queries"])
 * object MyApi : Api("https://api.example.com")
 *
 * // Grouped client by path prefix
 * @GenerateApi(grouping = ClientGrouping.ByPrefix)
 * object MyApi : Api("https://api.example.com")
 * ```
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
public annotation class GenerateApi(
    val scanPackages: Array<String> = [],
    val grouping: ClientGrouping = ClientGrouping.SingleClient,
)

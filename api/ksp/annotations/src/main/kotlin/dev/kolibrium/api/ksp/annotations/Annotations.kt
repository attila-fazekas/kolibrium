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
 * Configures code generation for an [ApiSpec][dev.kolibrium.api.core.ApiSpec] subclass.
 *
 * This annotation is required on every `ApiSpec` subclass to trigger code generation.
 * When used without explicit arguments, all properties use their defaults.
 *
 * @property scanPackages Packages to scan for request classes. An empty array (the default) means
 *   "use the convention default": the `<api-package>.models` subpackage of the annotated class's
 *   package.
 * @property grouping Controls how generated client classes are organized. Defaults to
 *   [ClientGrouping.SingleClient], which places all endpoint methods in a single client class.
 *   Use [ClientGrouping.ByPrefix] to group methods by their first path segment into separate
 *   client classes.
 * @property generateTestHarness Whether to generate a test harness file for this API specification.
 *   Defaults to `true`. Set to `false` to skip generating test harness functions.
 * @property displayName Human-readable name for the API, used only in generated KDoc comments. Has
 *   no effect on generated class names or function names — those are derived from the annotated
 *   class's simple name. When empty (the default), the display name is derived by stripping the
 *   first matching suffix from `ApiSpec`, `Spec`, `Api` (in that order) from the class name. Set
 *   this explicitly when the derived name is awkward or incorrect (e.g., for class names like
 *   `ApiSpec` or `Spec` that produce redundant KDoc).
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
public annotation class GenerateApi(
    val scanPackages: Array<String> = [],
    val grouping: ClientGrouping = ClientGrouping.SingleClient,
    val generateTestHarness: Boolean = true,
    val displayName: String = "",
)

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
 * Marks a property as an HTTP header in an API request.
 *
 * Properties annotated with @Header will be sent as HTTP headers in the request.
 * The header name defaults to the property name but can be overridden via the [name]
 * parameter — useful when the HTTP header name contains characters that aren't valid
 * in Kotlin identifiers (e.g., `X-Correlation-ID`).
 *
 * Header properties must be `String?`, annotated with `@Transient`, and nullable
 * (a null value means the header is not sent).
 *
 * Example usage:
 * ```kotlin
 * @GET("/users")
 * @Returns(success = UserList::class)
 * @Serializable
 * data class ListUsersRequest(
 *     @Header(name = "X-Correlation-ID") @Transient val correlationId: String? = null,
 *     @Header @Transient val accept: String? = null,
 * )
 * ```
 *
 * @property name The HTTP header name. Defaults to the property name when empty.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.PROPERTY)
public annotation class Header(
    val name: String = "",
)

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

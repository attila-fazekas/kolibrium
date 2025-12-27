import kotlin.reflect.KClass

/**
 * Collects unique HTTP method names used across requests for import generation.
 *
 * @param requests List of request class information
 * @return List of Ktor function names (e.g., "get", "post") to import
 */
/**
 * Generates the root aggregator client that contains all group clients.
 *
 * The root client provides access to group clients as properties:
 * ```kotlin
 * val client = MyApiClient(httpClient, baseUrl)
 * client.vinyls.getVinyl(id = "123") // Access vinyls group
 * client.users.getUser(id = "456")   // Access users group
 * ```
 *
 * @param rootClientClassName Name of the root client class
 * @param clientPackage Package for the generated client
 * @param groupClientClassNames Map of group names to their client class names
 * @param sourceFiles Source files for dependency tracking
 */
/**
 * Generates a client class for a specific endpoint group.
 *
 * @param groupClientClassName Name of the client class to generate
 * @param clientPackage Package for the generated client
 * @param requests List of request class information in this group
 * @param sourceFiles Source files for dependency tracking
 */
/**
 * Generates multiple client classes grouped by API path prefix.
 *
 * Creates:
 * - Individual client classes for each group (e.g., VinylsClient, UsersClient)
 * - A root aggregator client with properties for each group client
 *
 * @param apiInfo The API specification information
 * @param requests List of all request class information for this API
 */
/**
 * Generates a single client class containing all API endpoints.
 *
 * The generated client class:
 * - Takes HttpClient and baseUrl as constructor parameters
 * - Contains a suspend function for each request class
 * - Returns typed ApiResponse objects
 *
 * @param apiInfo The API specification information
 * @param requests List of all request class information for this API
 */
/**
 * Validates the return type of a request class.
 *
 * Requirements:
 * - Return type must be resolvable
 * - If not Unit, must be a concrete class type
 * - If not Unit, must be annotated with @Serializable
 *
 * @param info The request class information
 * @param errors Mutable list to collect validation errors
 */
/**
 * Validates body parameters of a request class.
 *
 * Requirements:
 * - Not allowed on GET or DELETE requests
 * - Should be var for DSL builder pattern (warning)
 * - Must be nullable or have default values for DSL builder pattern (error)
 *
 * @param info The request class information
 * @param bodyProperties List of properties that are body parameters (not @Path or @Query)
 * @param errors Mutable list to collect validation errors
 * @param warnings Mutable list to collect validation warnings
 */
/**
 * Checks if a type is valid for a query parameter.
 *
 * Valid types:
 * - String, Int, Long, Boolean
 * - List<String>, List<Int>, List<Long>, List<Boolean>
 *
 * @param type The type to validate
 * @return true if the type is valid for query parameters
 */
/**
 * Validates query parameters of a request class.
 *
 * Requirements:
 * - Only allowed on GET and DELETE requests
 * - Must NOT be annotated with @Transient
 * - Must be nullable (optional by nature in REST APIs)
 * - Must be String, Int, Long, Boolean, or List of these types
 *
 * @param info The request class information
 * @param queryProperties List of properties annotated with @Query
 * @param errors Mutable list to collect validation errors
 */
/**
 * Validates path parameters of a request class.
 *
 * Requirements:
 * - Path variables in the URL must be valid Kotlin identifiers
 * - Each path variable must have a matching @Path parameter
 * - Each @Path parameter must correspond to a path variable in the URL
 * - @Path parameters must be annotated with @Transient
 * - @Path parameters must be String, Int, Long, or Boolean
 *
 * @param info The request class information
 * @param pathProperties List of properties annotated with @Path
 * @param errors Mutable list to collect validation errors
 */
/**
 * Validates all parameters (path, query, and body) of a request class.
 *
 * Ensures:
 * - No property is annotated with both @Path and @Query
 * - Path parameters meet their specific requirements
 * - Query parameters meet their specific requirements
 * - Body parameters meet their specific requirements
 *
 * @param info The request class information
 * @param errors Mutable list to collect validation errors
 * @param warnings Mutable list to collect validation warnings
 */
/**
 * Validates a request class and extracts request information.
 *
 * Validation rules:
 * - Name must end with "Request" suffix and be descriptive
 * - Generated function name must be a valid Kotlin identifier
 * - Cannot be abstract or sealed
 * - Must have exactly one HTTP method annotation
 * - Must specify a non-blank path in the HTTP method annotation
 * - Must have @Returns annotation with valid return type
 * - Must be a data class if it has any declared properties
 *
 * @param requestClass The request class to validate
 * @param apiInfo The API specification this request belongs to
 * @param errors Mutable list to collect validation errors
 * @return RequestClassInfo if validation passes, null otherwise
 */
/**
 * Discovers request model classes for each API specification.
 *
 * A class is considered a request model if:
 * - It's in one of the configured scan packages (or subpackages)
 * - It's annotated with @Serializable
 * - It has at least one HTTP method annotation (GET, POST, PUT, DELETE, PATCH)
 *
 * @param apiSpecInfos List of validated API specifications
 * @param resolver The resolver for accessing symbols
 * @return Map of API specifications to their discovered request classes
 */
/**
 * Extracts the client grouping configuration from the @GenerateApi annotation.
 *
 * @param apiSpec The API specification class
 * @return The grouping mode (defaults to SingleClient if not specified)
 */
/**
 * Determines the packages to scan for request classes.
 *
 * If the @GenerateApi annotation specifies scanPackages, those are used.
 * Otherwise, defaults to `<api-package>.models`.
 *
 * @param apiSpec The API specification class
 * @param apiPackage The package containing the API specification
 * @return List of package names to scan for request classes
 */
/**
 * Validates an API specification class and extracts configuration.
 *
 * Validation rules:
 * - Must be an object declaration or concrete class (not abstract/sealed)
 * - Must extend dev.kolibrium.api.core.ApiSpec
 * - Must have a valid package name
 * - Must have a descriptive name (not just "ApiSpec")
 *
 * @param apiSpecClass The API specification class to validate
 * @param errors Mutable list to collect validation errors
 * @return ApiSpecInfo if validation passes, null otherwise
 */
/**
 * Processes symbols to generate API client code.
 *
 * The processing workflow:
 * 1. Discovers @GenerateApi annotated classes
 * 2. Validates API specification classes
 * 3. Discovers request classes in configured scan packages
 * 4. Validates request classes and their parameters
 * 5. Checks for naming collisions and duplicate APIs
 * 6. Generates client classes and test harnesses if validation passes
 *
 * @param resolver The resolver for accessing symbols in the current compilation
 * @return Empty list (no symbols need to be deferred)
 */
/**
 * Symbol processor that generates API client code from annotated request classes.
 *
 * This processor discovers classes annotated with [@GenerateApi][dev.kolibrium.api.ksp.annotations.GenerateApi]
 * and generates corresponding HTTP client implementations. It supports two client generation modes:
 * - **SingleClient**: All endpoints in one client class
 * - **ByPrefix**: Endpoints grouped by API path prefix into separate client classes
 *
 * The processor validates:
 * - API specification classes (must extend ApiSpec)
 * - Request classes (must be @Serializable data classes with HTTP method annotations)
 * - Path parameters (must match path variables and be annotated with @Path and @Transient)
 * - Query parameters (must be nullable and annotated with @Query)
 * - Body parameters (must be nullable or have defaults for DSL builder pattern)
 * - Return types (must be @Serializable or Unit)
 *
 * Generated code includes:
 * - Typed HTTP client classes with suspend functions
 * - Test harness functions for API testing
 * - Automatic serialization/deserialization using Ktor's content negotiation
 *
 * @param environment The symbol processor environment providing access to logging and code generation
 */
public class ApiCodegenProcessor(
    environment: SymbolProcessorEnvironment,
) : SymbolProcessor {

/**
 * Reports collected diagnostics to the logger.
 *
 * @param warnings List of warning diagnostics
 * @param errors List of error diagnostics
 */
/**
 * Gets a specific annotation from a symbol.
 *
 * @param annotationClass The annotation class to retrieve
 * @return The annotation if present, null otherwise
 */
/**
 * Checks if a symbol has a specific annotation.
 *
 * @param annotationClass The annotation class to check for
 * @return true if the annotation is present
 */
/**
 * Extracts path variable names from a URL path.
 *
 * Example: "/vinyls/{id}/tracks/{trackId}" returns ["id", "trackId"]
 *
 * @param path The URL path
 * @return PathVariables containing valid and invalid variable names
 */
/**
 * Checks for function name collisions in the generated client.
 *
 * For SingleClient mode, checks across all requests.
 * For ByPrefix mode, checks within each group separately.
 *
 * @param apiInfo The API specification information
 * @param requestInfos List of request class information
 * @param errors Mutable list to collect validation errors
 */
/**
 * Converts a KSType to a KotlinPoet TypeName, handling generics and nullability.
 *
 * @return The converted TypeName
 */
/**
 * Builds a URL path with path parameters substituted with string templates.
 *
 * Example: "/vinyls/{id}" becomes "/vinyls/$id"
 *
 * @param info The request class information
 * @return The URL path with Kotlin string template substitutions
 */
/**
 * Gets the return type name for a client method.
 *
 * @param info The request class information
 * @return EmptyResponse for Unit returns, ApiResponse<T> otherwise
 */
/**
 * Generates test harness functions for API testing.
 *
 * Creates two overloaded functions:
 * 1. Simple version: just API test block
 * 2. setUp/tearDown version: with test lifecycle hooks
 *
 * Example usage:
 * ```kotlin
 * myApiTest {
 *     val response = getVinyl(id = "123")
 *     response.requireSuccess()
 * }
 * ```
 *
 * @param apiInfo The API specification information
 */
/**
 * Generates the function body code for a client method.
 *
 * The generated code:
 * - Creates request object with DSL builder if needed
 * - Builds URL with path parameters
 * - Makes HTTP request with appropriate method
 * - Sets content type and body for requests with bodies
 * - Adds query parameters for GET/DELETE requests
 * - Returns typed ApiResponse
 *
 * @param info The request class information
 * @param requestClassName The fully qualified request class name
 * @param hasBody Whether the request has body parameters
 * @return The generated method body code
 */
/**
 * Generates a suspend function for a specific request class.
 *
 * The generated function:
 * - Has parameters for path variables
 * - Has optional parameters for query parameters
 * - Has a DSL builder lambda for body parameters
 * - Returns ApiResponse<T> or EmptyResponse
 *
 * @param info The request class information
 * @return The generated function specification
 */
/**
 * Validates ByPrefix grouping mode specific constraints.
 *
 * Checks:
 * - Group names are valid Kotlin identifiers
 * - Warns if a group contains only one endpoint (inefficient grouping)
 *
 * @param apiInfo The API specification information
 * @param requests List of request class information
 * @param errors Mutable list to collect validation errors
 * @param warnings Mutable list to collect validation warnings
 */
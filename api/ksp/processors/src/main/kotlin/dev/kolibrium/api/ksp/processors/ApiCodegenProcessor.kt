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

package dev.kolibrium.api.ksp.processors

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.symbol.Nullability
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.ExperimentalKotlinPoetApi
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asTypeName
import dev.kolibrium.api.ksp.annotations.Auth
import dev.kolibrium.api.ksp.annotations.AuthType
import dev.kolibrium.api.ksp.annotations.ClientGrouping
import dev.kolibrium.api.ksp.annotations.DELETE
import dev.kolibrium.api.ksp.annotations.GET
import dev.kolibrium.api.ksp.annotations.GenerateApi
import dev.kolibrium.api.ksp.annotations.PATCH
import dev.kolibrium.api.ksp.annotations.POST
import dev.kolibrium.api.ksp.annotations.PUT
import dev.kolibrium.api.ksp.annotations.Path
import dev.kolibrium.api.ksp.annotations.Query
import dev.kolibrium.api.ksp.annotations.Returns
import io.ktor.http.HttpMethod
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

/**
 * Symbol processor that generates API client code from annotated request classes.
 *
 * This processor discovers classes annotated with [@GenerateApi][GenerateApi]
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
    private val logger: KSPLogger = environment.logger
    private val codeGenerator: CodeGenerator = environment.codeGenerator

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val warnings = mutableListOf<Diagnostic>()
        val errors = mutableListOf<Diagnostic>()

        // Discover @GenerateApi annotated classes directly via annotation
        val generateApiSymbols =
            resolver
                .getSymbolsWithAnnotation(GenerateApi::class.qualifiedName!!)
                .filterIsInstance<KSClassDeclaration>()
                .toList()

        if (generateApiSymbols.isEmpty()) {
            logger.logging("No @GenerateApi classes found, skipping code generation")
            return emptyList()
        }

        logger.logging("Discovered ${generateApiSymbols.size} @GenerateApi class(es)")

        // Validate API spec classes and collect info
        val apiSpecInfos =
            generateApiSymbols.mapNotNull { apiClass ->
                validateApiSpecClass(apiClass, errors)
            }

        // Log discovered APIs with their scan packages
        apiSpecInfos.forEach { apiInfo ->
            val scanPackagesStr = apiInfo.scanPackages.joinToString(", ")
            logger.logging("API '${apiInfo.apiName}' will scan packages: [$scanPackagesStr]")
        }

        // Check for duplicate API names in the same package
        val apiNamesByPackage = apiSpecInfos.groupBy { it.packageName }
        apiNamesByPackage.forEach { (packageName, apis) ->
            val duplicates = apis.groupBy { it.apiName }.filter { it.value.size > 1 }
            duplicates.forEach { (apiName, duplicateApis) ->
                duplicateApis.forEach { api ->
                    errors +=
                        Diagnostic(
                            "Duplicate API name '$apiName' in package '$packageName'",
                            api.apiSpec,
                        )
                }
            }
        }

        // Discover request models per API based on scan packages
        val requestsByApi: Map<ApiSpecInfo, List<KSClassDeclaration>> =
            discoverRequestModels(apiSpecInfos, resolver)

        // Log request class counts per API
        requestsByApi.forEach { (apiInfo, requestClasses) ->
            logger.logging("API '${apiInfo.apiName}' found ${requestClasses.size} request class(es)")
        }

        // Check if any APIs have request classes
        val totalRequestClasses = requestsByApi.values.sumOf { it.size }
        if (totalRequestClasses == 0 && apiSpecInfos.isNotEmpty()) {
            apiSpecInfos.forEach { apiInfo ->
                val scanPackages = apiInfo.scanPackages.joinToString(", ")
                warnings +=
                    Diagnostic(
                        "No request classes found in scan packages [$scanPackages] for API '${apiInfo.apiName}'",
                        apiInfo.apiSpec,
                    )
            }
        }

        // Validate and collect request info per API
        val requestInfosByApi: Map<ApiSpecInfo, List<RequestClassInfo>> =
            requestsByApi.mapValues { (apiInfo, requestClasses) ->
                requestClasses.mapNotNull { requestClass ->
                    validateRequestClass(requestClass, apiInfo, errors)
                }
            }

        // Parameter and Return Type Validation
        requestInfosByApi.values.flatten().forEach { info ->
            validateRequestParameters(info, errors, warnings)
            validateReturnType(info, errors)
        }

        // Check for function name collisions per API
        requestInfosByApi.forEach { (apiInfo, requests) ->
            checkFunctionNameCollisions(apiInfo, requests, errors)
        }

        // Validate grouped mode specific constraints
        requestInfosByApi.forEach { (apiInfo, requests) ->
            if (apiInfo.grouping == ClientGrouping.ByPrefix) {
                validateGroupedMode(apiInfo, requests, errors, warnings)
            }
        }

        reportDiagnostics(warnings, errors)

        // If there are errors, don't generate code
        if (errors.isNotEmpty()) {
            return emptyList()
        }

        // Code Generation per API
        apiSpecInfos.forEach { apiInfo ->
            val requests = requestInfosByApi[apiInfo] ?: emptyList()
            if (requests.isNotEmpty()) {
                generateClientClass(apiInfo, requests)
                generateTestHarness(apiInfo)
            }
        }

        return emptyList()
    }

    private fun validateApiSpecClass(
        apiSpecClass: KSClassDeclaration,
        errors: MutableList<Diagnostic>,
    ): ApiSpecInfo? {
        val className = apiSpecClass.getClassName()

        // Must be an object or concrete class
        val isObject = apiSpecClass.classKind == ClassKind.OBJECT
        val isClass = apiSpecClass.classKind == ClassKind.CLASS
        val isAbstract = apiSpecClass.modifiers.contains(Modifier.ABSTRACT)

        if (!isObject && !(isClass && !isAbstract)) {
            errors +=
                Diagnostic(
                    "@GenerateApi class $className must be an object declaration or a concrete class",
                    apiSpecClass,
                )
            return null
        }

        // Must extend dev.kolibrium.api.core.ApiSpec
        val extendsApiSpec =
            apiSpecClass.superTypes.any { superType ->
                val resolvedType = superType.resolve()
                resolvedType.declaration.qualifiedName?.asString() == API_SPEC_BASE_CLASS
            }

        if (!extendsApiSpec) {
            errors +=
                Diagnostic(
                    "@GenerateApi class $className must extend $API_SPEC_BASE_CLASS",
                    apiSpecClass,
                )
            return null
        }

        // Must be in a package with a valid name
        val packageName = apiSpecClass.packageName.asString()

        if (!packageName.isValidKotlinPackage()) {
            errors +=
                Diagnostic(
                    "@GenerateApi class $className has invalid package name: $packageName",
                    apiSpecClass,
                )
            return null
        }

        // Extract API name from class simple name (remove "ApiSpec" suffix, convert to camelCase)
        val simpleName = apiSpecClass.simpleName.asString()
        val apiName = simpleName.removeSuffix("ApiSpec").replaceFirstChar { it.lowercase() }

        if (apiName.isBlank()) {
            errors +=
                Diagnostic(
                    "@GenerateApi class must not be named exactly 'ApiSpec'. Use a descriptive name like '<Name>ApiSpec'.",
                    apiSpecClass,
                )
            return null
        }

        // Determine scan packages from annotation or use default
        val scanPackages = determineScanPackages(apiSpecClass, packageName)

        // Extract grouping configuration from annotation
        val grouping = extractGroupingConfiguration(apiSpecClass)

        logger.info("Grouping for $apiName: $grouping")

        return ApiSpecInfo(
            apiSpec = apiSpecClass,
            apiName = apiName,
            packageName = packageName,
            scanPackages = scanPackages,
            grouping = grouping,
        )
    }

    private fun extractAuthType(requestClass: KSClassDeclaration): AuthType {
        val annotation = requestClass.getAnnotation(Auth::class) ?: return AuthType.NONE

        return when (val authArg = annotation.getArgumentValue("type")) {
            is KSClassDeclaration -> {
                val enumName = authArg.simpleName.asString()
                AuthType.entries.find { it.name == enumName } ?: AuthType.NONE
            }

            else -> {
                AuthType.NONE
            }
        }
    }

    private fun extractApiKeyHeader(requestClass: KSClassDeclaration): String {
        val annotation = requestClass.getAnnotation(Auth::class) ?: return "X-API-Key"
        return annotation.getArgumentValue("headerName") as? String ?: "X-API-Key"
    }

    private fun String.isValidHttpHeaderName(): Boolean {
        // RFC 7230: token = 1*tchar
        // tchar = "!" / "#" / "$" / "%" / "&" / "'" / "*" / "+" / "-" / "." /
        //         "0"-"9" / "A"-"Z" / "^" / "_" / "`" / "a"-"z" / "|" / "~"
        val regex = Regex("^[!#$%&'*+\\-.0-9A-Z^_`a-z|~]+$")
        return isNotEmpty() && regex.matches(this)
    }

    private fun determineScanPackages(
        apiSpec: KSClassDeclaration,
        apiPackage: String,
    ): List<String> {
        val defaultPackage = listOf("$apiPackage.models")

        val generateApiAnnotation = apiSpec.getAnnotation(GenerateApi::class) ?: return defaultPackage

        val scanPackages =
            when (val scanPackagesArg = generateApiAnnotation.getArgumentValue("scanPackages")) {
                is List<*> -> scanPackagesArg.filterIsInstance<String>()
                else -> emptyList()
            }

        return scanPackages.ifEmpty {
            // Default to <api-package>.models
            defaultPackage
        }
    }

    private fun extractGroupingConfiguration(apiSpec: KSClassDeclaration): ClientGrouping {
        val generateApiAnnotation =
            apiSpec.getAnnotation(GenerateApi::class)
                ?: return ClientGrouping.SingleClient

        return when (val groupingArg = generateApiAnnotation.getArgumentValue("grouping")) {
            is KSClassDeclaration -> {
                val enumEntryName = groupingArg.simpleName.asString()
                ClientGrouping.entries.find { it.name == enumEntryName } ?: ClientGrouping.SingleClient
            }

            else -> {
                ClientGrouping.SingleClient
            }
        }
    }

    private fun discoverRequestModels(
        apiSpecInfos: List<ApiSpecInfo>,
        resolver: Resolver,
    ): Map<ApiSpecInfo, List<KSClassDeclaration>> {
        val allFiles = resolver.getAllFiles().toList()

        return apiSpecInfos.associateWith { apiInfo ->
            // Find files in scan packages (using startsWith for subpackage support)
            val filesInScanPackages =
                allFiles.filter { file ->
                    val filePackage = file.packageName.asString()
                    apiInfo.scanPackages.any { scanPackage ->
                        filePackage == scanPackage || filePackage.startsWith("$scanPackage.")
                    }
                }

            // Get all class declarations from those files
            val classesInScanPackages =
                filesInScanPackages
                    .flatMap { it.declarations.toList() }
                    .filterIsInstance<KSClassDeclaration>()

            // Filter to request classes: must have @Serializable and at least one HTTP method annotation
            classesInScanPackages.filter { classDeclaration ->
                classDeclaration.hasAnnotation(Serializable::class) &&
                    classDeclaration.getHttpMethodAnnotations().isNotEmpty()
            }
        }
    }

    private fun validateRequestClass(
        requestClass: KSClassDeclaration,
        apiInfo: ApiSpecInfo,
        errors: MutableList<Diagnostic>,
    ): RequestClassInfo? {
        val className = requestClass.getClassName()

        val simpleName = requestClass.simpleName.asString()
        val functionName = deriveFunctionName(simpleName)

        if (!simpleName.endsWith("Request") || simpleName == "Request") {
            errors +=
                Diagnostic(
                    "Request class '$simpleName' must end with 'Request' suffix and have a descriptive name (e.g., 'GetUserRequest', 'CreateOrderRequest')",
                    requestClass,
                )
            return null
        }

        if (!functionName.isValidKotlinIdentifier()) {
            errors +=
                Diagnostic(
                    "Request class name '$className' generates invalid function name '$functionName'. " +
                        "Function name must be a valid Kotlin identifier.",
                    requestClass,
                )
            return null
        }

        if (requestClass.modifiers.contains(Modifier.ABSTRACT) || requestClass.modifiers.contains(Modifier.SEALED)) {
            errors += Diagnostic("Request class $className cannot be abstract or sealed", requestClass)
            return null
        }

        val httpAnnotations = requestClass.getHttpMethodAnnotations()
        if (httpAnnotations.size != 1) {
            errors += Diagnostic("Class $className has multiple HTTP method annotations", requestClass)
            return null
        }

        val methodAnnotation = httpAnnotations.single()
        val httpMethod =
            methodAnnotation.toHttpMethod()
                ?: run {
                    errors += Diagnostic("Class $className has an unsupported HTTP method annotation", requestClass)
                    return null
                }

        val path = methodAnnotation.getArgumentValue("path")?.let { it as? String }

        if (path.isNullOrBlank()) {
            errors += Diagnostic("HTTP method annotation on $className must specify a path", requestClass)
            return null
        }

        val returnsAnnotation = requestClass.getAnnotation(Returns::class)
        if (returnsAnnotation == null) {
            errors +=
                Diagnostic(
                    "Request class $className with HTTP method annotation must have @Returns annotation",
                    requestClass,
                )
            return null
        }

        val returnType = returnsAnnotation.getKClassTypeArgument("success")
        if (returnType == null || returnType.isError) {
            errors += Diagnostic("Success type for $className could not be resolved", requestClass)
            return null
        }

        val isEmptyResponse = returnType.declaration.qualifiedName?.asString() == KOTLIN_UNIT

        // Extract optional error type
        val errorType = returnsAnnotation.getKClassTypeArgument("error")
        val errorQualifiedName = errorType?.declaration?.qualifiedName?.asString()
        val resolvedErrorType =
            if (errorType != null &&
                !errorType.isError &&
                errorQualifiedName != KOTLIN_NOTHING &&
                errorQualifiedName != JAVA_VOID
            ) {
                errorType
            } else {
                null
            }

        // Collect parameters
        val properties = requestClass.getAllProperties().toList()
        val ctorDefaults =
            requestClass.primaryConstructor
                ?.parameters
                ?.associateBy(
                    keySelector = { it.name?.asString().orEmpty() },
                    valueTransform = { it.hasDefault },
                ).orEmpty()

        val pathProperties = mutableListOf<KSPropertyDeclaration>()
        val queryProperties = mutableListOf<KSPropertyDeclaration>()
        val bodyProperties = mutableListOf<KSPropertyDeclaration>()

        properties.forEach { property ->
            val isPath = property.hasAnnotation(Path::class)
            val isQuery = property.hasAnnotation(Query::class)
            when {
                isPath -> pathProperties += property
                isQuery -> queryProperties += property
                else -> bodyProperties += property
            }
        }

        // Only require data class if there are any declared properties.
        // This allows marker request classes for endpoints with no path/query/body params.
        if (properties.isNotEmpty() && !requestClass.modifiers.contains(Modifier.DATA)) {
            errors += Diagnostic("Request class $className must be a data class", requestClass)
            return null
        }

        val authType = extractAuthType(requestClass)
        val apiKeyHeader = extractApiKeyHeader(requestClass)

        if (authType == AuthType.API_KEY && !apiKeyHeader.isValidHttpHeaderName()) {
            errors +=
                Diagnostic(
                    "Invalid API key header name: '$apiKeyHeader' in request class $className",
                    requestClass,
                )
        }

        // Validate that headerName is only used with API_KEY auth type
        val authAnnotation = requestClass.getAnnotation(Auth::class)
        if (authAnnotation != null && authType != AuthType.API_KEY) {
            val headerNameValue = authAnnotation.getArgumentValue("headerName") as? String
            // Only error if headerName was explicitly provided (not the default)
            if (headerNameValue != null && headerNameValue != "X-API-Key") {
                errors +=
                    Diagnostic(
                        "headerName parameter can only be used with AuthType.API_KEY, but request uses AuthType.$authType",
                        requestClass,
                    )
            }
        }

        return RequestClassInfo(
            requestClass = requestClass,
            httpMethod = httpMethod,
            path = path,
            group = extractGroupByApiPrefix(path),
            returnType = returnType,
            errorType = resolvedErrorType,
            isEmptyResponse = isEmptyResponse,
            pathProperties = pathProperties,
            queryProperties = queryProperties,
            bodyProperties = bodyProperties,
            ctorDefaults = ctorDefaults,
            apiPackage = apiInfo.packageName,
            authType = authType,
            apiKeyHeader = apiKeyHeader,
        )
    }

    private fun validateRequestParameters(
        info: RequestClassInfo,
        errors: MutableList<Diagnostic>,
        warnings: MutableList<Diagnostic>,
    ) {
        val requestClass = info.requestClass
        val className = requestClass.getClassName()

        val properties = requestClass.getAllProperties().toList()

        val pathProperties = mutableListOf<KSPropertyDeclaration>()
        val queryProperties = mutableListOf<KSPropertyDeclaration>()
        val bodyProperties = mutableListOf<KSPropertyDeclaration>()

        properties.forEach { property ->
            val isPath = property.hasAnnotation(Path::class)
            val isQuery = property.hasAnnotation(Query::class)
            if (isPath && isQuery) {
                errors +=
                    Diagnostic(
                        "Property '${property.simpleName.asString()}' in $className cannot be annotated with both @Path and @Query",
                        property,
                    )
                return@forEach
            }

            when {
                isPath -> pathProperties += property
                isQuery -> queryProperties += property
                else -> bodyProperties += property
            }
        }

        validatePathParameters(info, pathProperties, errors)
        validateQueryParameters(info, queryProperties, errors)
        validateBodyParameters(info, bodyProperties, errors, warnings)
    }

    private fun validatePathParameters(
        info: RequestClassInfo,
        pathProperties: List<KSPropertyDeclaration>,
        errors: MutableList<Diagnostic>,
    ) {
        val requestClass = info.requestClass

        val pathVariables = extractPathVariables(info.path)
        pathVariables.invalidNames.forEach { invalid ->
            errors +=
                Diagnostic(
                    "Path variable '{$invalid}' in ${info.path} is not a valid Kotlin identifier",
                    requestClass,
                )
        }

        pathVariables.names.forEach { variableName ->
            val hasMatch = pathProperties.any { it.simpleName.asString() == variableName }
            if (!hasMatch) {
                errors +=
                    Diagnostic(
                        "Path variable '{$variableName}' in ${info.path} has no matching @Path parameter",
                        requestClass,
                    )
            }
        }

        pathProperties.forEach { property ->
            val propertyName = property.simpleName.asString()
            if (!pathVariables.names.contains(propertyName)) {
                errors +=
                    Diagnostic(
                        "@Path parameter '$propertyName' not found in path: ${info.path}",
                        property,
                    )
            }

            if (!property.hasAnnotation(Transient::class)) {
                errors +=
                    Diagnostic(
                        "@Path parameter '$propertyName' must be annotated with @Transient",
                        property,
                    )
            }

            val typeQualifiedName =
                property.type
                    .resolve()
                    .declaration.qualifiedName
                    ?.asString()
            if (typeQualifiedName !in ALLOWED_PATH_AND_QUERY_PARAMETER_TYPES) {
                errors +=
                    Diagnostic(
                        "@Path parameter '$propertyName' must be String, Int, Long, Short, Float, Double, or Boolean",
                        property,
                    )
            }
        }
    }

    private fun validateQueryParameters(
        info: RequestClassInfo,
        queryProperties: List<KSPropertyDeclaration>,
        errors: MutableList<Diagnostic>,
    ) {
        if (queryProperties.isEmpty()) return

        if (info.httpMethod != HttpMethod.Get && info.httpMethod != HttpMethod.Delete) {
            errors +=
                Diagnostic(
                    "@Query parameters not allowed on ${info.httpMethod} requests",
                    info.requestClass,
                )
            return
        }

        queryProperties.forEach { property ->
            val propertyName = property.simpleName.asString()

            // Query parameters must be annotated with @Transient
            if (!property.hasAnnotation(Transient::class)) {
                errors +=
                    Diagnostic(
                        "@Query parameter '$propertyName' must be annotated with @Transient",
                        property,
                    )
            }

            val resolvedType = property.type.resolve()
            val isNullable = resolvedType.nullability == Nullability.NULLABLE

            // Query parameters must be nullable (they're optional by nature in REST APIs)
            if (!isNullable) {
                errors +=
                    Diagnostic(
                        "@Query parameter '$propertyName' must be nullable (e.g., String?, Int?)",
                        property,
                    )
            }

            // Validate type (allow String, Int, Long, Boolean, and List<T> where T is one of those)
            if (!isValidQueryParameterType(resolvedType)) {
                errors +=
                    Diagnostic(
                        "@Query parameter '$propertyName' must be String, Int, Long, Short, Float, Double, Boolean, or List of these types",
                        property,
                    )
            }
        }
    }

    private fun isValidQueryParameterType(type: KSType): Boolean {
        val declaration = type.declaration
        val qualifiedName = declaration.qualifiedName?.asString() ?: return false

        // Check if it's a simple type
        if (qualifiedName in ALLOWED_PATH_AND_QUERY_PARAMETER_TYPES) {
            return true
        }

        // Check if it's a List<T> where T is an allowed type
        if (qualifiedName == "kotlin.collections.List") {
            val typeArg =
                type.arguments
                    .firstOrNull()
                    ?.type
                    ?.resolve()
            if (typeArg != null) {
                val typeArgQualifiedName = typeArg.declaration.qualifiedName?.asString()
                return typeArgQualifiedName in ALLOWED_PATH_AND_QUERY_PARAMETER_TYPES
            }
        }

        return false
    }

    private fun validateBodyParameters(
        info: RequestClassInfo,
        bodyProperties: List<KSPropertyDeclaration>,
        errors: MutableList<Diagnostic>,
        warnings: MutableList<Diagnostic>,
    ) {
        if (bodyProperties.isEmpty()) return

        val requestClass = info.requestClass
        val className = requestClass.getClassName()
        val ctorDefaults = info.ctorDefaults

        if (info.httpMethod == HttpMethod.Get || info.httpMethod == HttpMethod.Delete) {
            errors +=
                Diagnostic(
                    "Request body parameters not allowed on GET/DELETE",
                    requestClass,
                )
            return
        }

        bodyProperties.forEach { property ->
            val propertyName = property.simpleName.asString()

            // Check mutability (warning only)
            if (!property.isMutable) {
                warnings +=
                    Diagnostic(
                        "Body parameter '$propertyName' should be var for DSL builder pattern",
                        property,
                    )
            }

            // Check nullability or default value (error)
            val isNullable = property.type.resolve().nullability == Nullability.NULLABLE
            val hasDefault = ctorDefaults[propertyName] == true

            if (!isNullable && !hasDefault) {
                errors +=
                    Diagnostic(
                        "Body parameter '$propertyName' in $className must be nullable or have a default value for DSL builder pattern",
                        property,
                    )
            }
        }
    }

    private fun validateReturnType(
        info: RequestClassInfo,
        errors: MutableList<Diagnostic>,
    ) {
        val requestClass = info.requestClass
        val className = requestClass.getClassName()

        // Validate success type
        val returnType = info.returnType
        if (returnType.isError) {
            errors += Diagnostic("Success type for $className could not be resolved", requestClass)
            return
        }

        val returnQualifiedName = returnType.declaration.qualifiedName?.asString()
        if (returnQualifiedName != KOTLIN_UNIT) {
            val returnClass = returnType.declaration as? KSClassDeclaration
            if (returnClass == null) {
                errors +=
                    Diagnostic(
                        "Success type '${returnQualifiedName ?: returnType}' for $className must be a concrete class type",
                        requestClass,
                    )
                return
            }

            if (!returnClass.hasAnnotation(Serializable::class)) {
                errors +=
                    Diagnostic(
                        "Success type ${returnClass.qualifiedName?.asString() ?: returnClass.simpleName.asString()} for $className is not @Serializable",
                        requestClass,
                    )
            }
        }

        // Validate error type if specified
        val errorType = info.errorType
        if (errorType != null) {
            if (errorType.isError) {
                errors += Diagnostic("Error type for $className could not be resolved", requestClass)
                return
            }

            val errorClass = errorType.declaration as? KSClassDeclaration
            if (errorClass == null) {
                errors +=
                    Diagnostic(
                        "Error type '${errorType.declaration.qualifiedName?.asString() ?: errorType}' for $className must be a concrete class type",
                        requestClass,
                    )
                return
            }

            if (!errorClass.hasAnnotation(Serializable::class)) {
                errors +=
                    Diagnostic(
                        "Error type ${errorClass.qualifiedName?.asString() ?: errorClass.simpleName.asString()} for $className is not @Serializable",
                        requestClass,
                    )
            }
        }
    }

    /**
     * Extracts the group name from an API path.
     *
     * The group is determined by the first path segment after the leading slash.
     * For example:
     * - `/vinyls` -> "vinyls"
     * - `/vinyls/{id}` -> "vinyls"
     * - `/users/profile` -> "users"
     * - `/` -> "root"
     * - `` (empty) -> "root"
     */
    private fun extractGroupByApiPrefix(path: String): String {
        val trimmedPath = path.trimStart('/')
        if (trimmedPath.isEmpty()) return "root"

        val firstSegment = trimmedPath.substringBefore('/')
        return if (firstSegment.startsWith("{")) {
            // Path starts with a path variable, use "root" as a group
            "root"
        } else {
            firstSegment
        }
    }

    // Groups request classes by their API path prefix.
    private fun groupRequestsByPrefix(requests: List<RequestClassInfo>): Map<String, List<RequestClassInfo>> =
        requests.groupBy { extractGroupByApiPrefix(it.path) }

    private fun generateClientClass(
        apiInfo: ApiSpecInfo,
        requests: List<RequestClassInfo>,
    ) {
        when (apiInfo.grouping) {
            ClientGrouping.SingleClient -> generateSingleClientClass(apiInfo, requests)
            ClientGrouping.ByPrefix -> generateGroupedClientClasses(apiInfo, requests)
        }
    }

    private fun generateSingleClientClass(
        apiInfo: ApiSpecInfo,
        requests: List<RequestClassInfo>,
    ) {
        val clientClassName = "${apiInfo.apiSpec.simpleName.asString().removeSuffix("ApiSpec")}Client"
        val clientPackage = "${apiInfo.packageName}.generated"

        val sourceFiles =
            requests.mapNotNull { it.requestClass.containingFile } +
                listOfNotNull(apiInfo.apiSpec.containingFile)

        generateClientClassFile(
            clientClassName = clientClassName,
            clientPackage = clientPackage,
            apiInfo = apiInfo,
            requests = requests,
            sourceFiles = sourceFiles,
        )
    }

    private fun generateGroupedClientClasses(
        apiInfo: ApiSpecInfo,
        requests: List<RequestClassInfo>,
    ) {
        val clientPackage = "${apiInfo.packageName}.generated"
        val rootClientClassName = "${apiInfo.apiSpec.simpleName.asString().removeSuffix("ApiSpec")}Client"
        val groupedRequests = groupRequestsByPrefix(requests)

        // Collect all source files for dependencies
        val sourceFiles =
            requests.mapNotNull { it.requestClass.containingFile } +
                listOfNotNull(apiInfo.apiSpec.containingFile)

        // Generate individual group client classes
        val groupClientClassNames = mutableMapOf<String, ClassName>()
        groupedRequests.forEach { (groupName, groupRequests) ->
            val groupClientClassName = "${groupName.replaceFirstChar { it.uppercase() }}Client"
            val groupClientClass = ClassName(clientPackage, groupClientClassName)
            groupClientClassNames[groupName] = groupClientClass

            generateClientClassFile(
                clientClassName = groupClientClassName,
                clientPackage = clientPackage,
                apiInfo = apiInfo,
                requests = groupRequests,
                sourceFiles = sourceFiles,
            )
        }

        // Generate root aggregator client
        generateRootAggregatorClient(
            rootClientClassName = rootClientClassName,
            clientPackage = clientPackage,
            groupClientClassNames = groupClientClassNames,
            sourceFiles = sourceFiles,
        )
    }

    private fun generateClientClassFile(
        clientClassName: String,
        clientPackage: String,
        apiInfo: ApiSpecInfo,
        requests: List<RequestClassInfo>,
        sourceFiles: List<KSFile>,
    ) {
        val classBuilder =
            TypeSpec
                .classBuilder(clientClassName)
                .primaryConstructor(
                    FunSpec
                        .constructorBuilder()
                        .addParameter("client", HTTP_CLIENT_CLASS)
                        .addParameter("baseUrl", String::class)
                        .build(),
                ).addProperty(
                    PropertySpec
                        .builder("client", HTTP_CLIENT_CLASS)
                        .initializer("client")
                        .addModifiers(KModifier.PRIVATE)
                        .build(),
                ).addProperty(
                    PropertySpec
                        .builder("baseUrl", String::class)
                        .initializer("baseUrl")
                        .addModifiers(KModifier.PRIVATE)
                        .build(),
                )

        // Generate result types for requests that have error types
        val resultTypes =
            requests
                .filter { it.errorType != null }
                .map { generateResultType(it, clientPackage) }

        // Generate methods for each request
        requests.forEach { info ->
            val funSpec = generateClientMethod(apiInfo, info, clientPackage)
            classBuilder.addFunction(funSpec)
        }

        // Collect HTTP methods used for imports
        val httpMethodNames = collectHttpMethodNames(requests)

        val fileSpecBuilder =
            FileSpec
                .builder(clientPackage, clientClassName)
                .addFileComment("Code generated by kolibrium-codegen. Do not edit.")

        // Add result types before the client class
        resultTypes.forEach { resultType ->
            fileSpecBuilder.addType(resultType)
        }

        fileSpecBuilder.addType(classBuilder.build())

        // Add HTTP method imports
        httpMethodNames.forEach { methodName ->
            fileSpecBuilder.addImport("io.ktor.client.request", methodName)
        }

        // Add auth-related imports if any request uses auth
        val usesAuth = requests.any { it.authType != AuthType.NONE }
        if (usesAuth) {
            val authTypes = requests.map { it.authType }.toSet()

            if (AuthType.BEARER in authTypes) {
                fileSpecBuilder.addImport("io.ktor.client.request", "bearerAuth")
            }
            if (AuthType.BASIC in authTypes) {
                fileSpecBuilder.addImport("io.ktor.client.request", "basicAuth")
            }
            if (AuthType.API_KEY in authTypes) {
                fileSpecBuilder.addImport("io.ktor.client.request", "header")
            }
            if (AuthType.CUSTOM in authTypes) {
                fileSpecBuilder.addImport(apiInfo.packageName, "AuthContext")
            }
        }

        // Add isSuccess import if any request has error type (for sealed result handling)
        val hasErrorTypes = requests.any { it.errorType != null }
        if (hasErrorTypes) {
            fileSpecBuilder.addImport("io.ktor.http", "isSuccess")
        }

        val fileSpec = fileSpecBuilder.build()

        codeGenerator
            .createNewFile(
                Dependencies(false, *sourceFiles.toTypedArray()),
                fileSpec.packageName,
                fileSpec.name,
            ).writer()
            .use { writer -> fileSpec.writeTo(writer) }
    }

    private fun generateRootAggregatorClient(
        rootClientClassName: String,
        clientPackage: String,
        groupClientClassNames: Map<String, ClassName>,
        sourceFiles: List<KSFile>,
    ) {
        val constructorBuilder =
            FunSpec
                .constructorBuilder()
                .addParameter("client", HTTP_CLIENT_CLASS)
                .addParameter("baseUrl", String::class)

        val classBuilder =
            TypeSpec
                .classBuilder(rootClientClassName)
                .primaryConstructor(constructorBuilder.build())
                .addProperty(
                    PropertySpec
                        .builder("client", HTTP_CLIENT_CLASS)
                        .initializer("client")
                        .addModifiers(KModifier.INTERNAL)
                        .build(),
                ).addProperty(
                    PropertySpec
                        .builder("baseUrl", String::class)
                        .initializer("baseUrl")
                        .addModifiers(KModifier.INTERNAL)
                        .build(),
                )

        // Add group client properties
        groupClientClassNames.forEach { (groupName, groupClientClass) ->
            classBuilder.addProperty(
                PropertySpec
                    .builder(groupName, groupClientClass)
                    .initializer("%T(client, baseUrl)", groupClientClass)
                    .build(),
            )
        }

        val fileSpecBuilder =
            FileSpec
                .builder(clientPackage, rootClientClassName)
                .addFileComment("Code generated by kolibrium-codegen. Do not edit.")
                .addType(classBuilder.build())

        val fileSpec = fileSpecBuilder.build()

        codeGenerator
            .createNewFile(
                Dependencies(false, *sourceFiles.toTypedArray()),
                fileSpec.packageName,
                fileSpec.name,
            ).writer()
            .use { writer -> fileSpec.writeTo(writer) }
    }

    private fun collectHttpMethodNames(requests: List<RequestClassInfo>): List<String> =
        requests
            .map { info ->
                when (info.httpMethod) {
                    HttpMethod.Get -> "get"
                    HttpMethod.Post -> "post"
                    HttpMethod.Put -> "put"
                    HttpMethod.Delete -> "delete"
                    HttpMethod.Patch -> "patch"
                    else -> "get"
                }
            }.distinct()

    private fun validateGroupedMode(
        apiInfo: ApiSpecInfo,
        requests: List<RequestClassInfo>,
        errors: MutableList<Diagnostic>,
        warnings: MutableList<Diagnostic>,
    ) {
        val groupedRequests = groupRequestsByPrefix(requests)

        // Check for empty groups (defensive - shouldn't happen)
        if (groupedRequests.isEmpty()) {
            warnings +=
                Diagnostic(
                    "API '${apiInfo.apiName}' with ByPrefix grouping has no groupable endpoints",
                    apiInfo.apiSpec,
                )
            return
        }

        groupedRequests.forEach { (groupName, groupRequests) ->
            // Validate group name is a valid Kotlin identifier
            if (!groupName.isValidKotlinIdentifier()) {
                errors +=
                    Diagnostic(
                        "Group name '$groupName' in API '${apiInfo.apiName}' is not a valid Kotlin identifier",
                        apiInfo.apiSpec,
                    )
            }

            // Warn if a group contains only one endpoint
            if (groupRequests.size == 1) {
                warnings +=
                    Diagnostic(
                        "Group '$groupName' in API '${apiInfo.apiName}' contains only one endpoint; consider using SingleClient mode",
                        groupRequests.first().requestClass,
                    )
            }
        }
    }

    @OptIn(ExperimentalKotlinPoetApi::class)
    private fun generateClientMethod(
        apiInfo: ApiSpecInfo,
        info: RequestClassInfo,
        clientPackage: String,
    ): FunSpec {
        val functionName = info.requestClass.toFunctionName()
        val modelsPackage = info.requestClass.packageName.asString()
        val requestClassName = ClassName(modelsPackage, info.requestClass.simpleName.asString())
        val returnTypeName = getReturnTypeName(info, clientPackage)

        val funBuilder =
            FunSpec
                .builder(functionName)
                .addModifiers(KModifier.SUSPEND)
                .returns(returnTypeName)

        // Add context parameters based on resolved auth type
        when (info.authType) {
            AuthType.BEARER -> {
                funBuilder.contextParameter("token", String::class)
            }

            AuthType.BASIC -> {
                funBuilder
                    .contextParameter("username", String::class)
                    .contextParameter("password", String::class)
            }

            AuthType.API_KEY -> {
                funBuilder.contextParameter("apiKey", String::class)
            }

            AuthType.CUSTOM -> {
                val authContextClass = ClassName(apiInfo.packageName, "AuthContext", "Custom")
                funBuilder.contextParameter("auth", authContextClass)
            }

            AuthType.NONE -> {
                // No context parameters
            }
        }

        // Add path parameters as function parameters
        info.pathProperties.forEach { property ->
            val paramName = property.simpleName.asString()
            val paramType = property.type.resolve().toTypeName()
            funBuilder.addParameter(paramName, paramType)
        }

        // Add query parameters as optional function parameters
        info.queryProperties.forEach { property ->
            val paramName = property.simpleName.asString()
            val paramType = property.type.resolve().toTypeName()
            funBuilder.addParameter(
                ParameterSpec
                    .builder(paramName, paramType)
                    .defaultValue("null")
                    .build(),
            )
        }

        // Add DSL builder parameter for body requests
        val hasBody = info.bodyProperties.isNotEmpty()
        if (hasBody) {
            val lambdaType =
                LambdaTypeName.get(
                    receiver = requestClassName,
                    returnType = Unit::class.asTypeName(),
                )
            funBuilder.addParameter("block", lambdaType)
        }

        // Generate function body
        val codeBlock = generateClientMethodBody(info, requestClassName, hasBody, clientPackage)
        funBuilder.addCode(codeBlock)

        return funBuilder.build()
    }

    private fun generateClientMethodBody(
        info: RequestClassInfo,
        requestClassName: ClassName,
        hasBody: Boolean,
        clientPackage: String,
    ): CodeBlock {
        val builder = CodeBlock.builder()

        // For requests with body, create a request object
        if (hasBody) {
            if (info.pathProperties.isNotEmpty()) {
                // Pass path params to constructor
                val pathParams =
                    info.pathProperties.joinToString(", ") {
                        "${it.simpleName.asString()} = ${it.simpleName.asString()}"
                    }
                builder.addStatement("val request = %T($pathParams).apply(block)", requestClassName)
            } else {
                builder.addStatement("val request = %T().apply(block)", requestClassName)
            }
            builder.add("\n")
        }

        // Build URL with path parameters
        val urlPath = buildUrlPath(info)

        // Get the HTTP method name
        val httpMethodName =
            when (info.httpMethod) {
                HttpMethod.Get -> "get"
                HttpMethod.Post -> "post"
                HttpMethod.Put -> "put"
                HttpMethod.Delete -> "delete"
                HttpMethod.Patch -> "patch"
                else -> "get"
            }

        val hasQueryParams = info.queryProperties.isNotEmpty()
        val needsAuth = info.authType != AuthType.NONE

        // Generate HTTP request
        if (hasBody || hasQueryParams || needsAuth) {
            builder.addStatement($$"val httpResponse = client.%L(\"$baseUrl%L\") {", httpMethodName, urlPath)
            builder.indent()

            // Apply authentication based on resolved type
            when (info.authType) {
                AuthType.BEARER -> {
                    builder.addStatement("%M(token)", BEARER_AUTH_MEMBER)
                }

                AuthType.BASIC -> {
                    builder.addStatement("%M(username, password)", BASIC_AUTH_MEMBER)
                }

                AuthType.API_KEY -> {
                    builder.addStatement("header(%S, apiKey)", info.apiKeyHeader)
                }

                AuthType.CUSTOM -> {
                    builder.addStatement("auth.configure(this)")
                }

                AuthType.NONE -> {
                    // No authentication
                }
            }

            // Content type and body for non-GET/DELETE requests
            if (hasBody) {
                builder.addStatement("%M(%T.Application.Json)", CONTENT_TYPE_MEMBER, CONTENT_TYPE_CLASS)
                builder.addStatement("%M(request)", SET_BODY_MEMBER)
            }

            // Query parameters
            if (hasQueryParams) {
                info.queryProperties.forEach { property ->
                    val paramName = property.simpleName.asString()
                    builder.addStatement("$paramName?.let { %M(%S, it) }", PARAMETER_MEMBER, paramName)
                }
            }

            builder.unindent()
            builder.addStatement("}")
        } else {
            // Simple request with no body, query params, or auth
            builder.addStatement($$"val httpResponse = client.%L(\"$baseUrl%L\")", httpMethodName, urlPath)
        }

        builder.add("\n")

        // If error type is specified, return sealed result type
        if (info.errorType != null) {
            val resultTypeName = getResultTypeName(info.requestClass.simpleName.asString())
            val resultClass = ClassName(clientPackage, resultTypeName)
            val successClass = resultClass.nestedClass("Success")
            val errorClass = resultClass.nestedClass("Error")

            builder.addStatement("return if (httpResponse.status.isSuccess()) {")
            builder.indent()
            builder.addStatement("%T(", successClass)
            builder.indent()
            builder.addStatement("data = httpResponse.%M(),", BODY_MEMBER)
            builder.addStatement("response = httpResponse,")
            builder.unindent()
            builder.addStatement(")")
            builder.unindent()
            builder.addStatement("} else {")
            builder.indent()
            builder.beginControlFlow("try")
            builder.addStatement("%T(", errorClass)
            builder.indent()
            builder.addStatement("data = httpResponse.%M(),", BODY_MEMBER)
            builder.addStatement("response = httpResponse,")
            builder.unindent()
            builder.addStatement(")")
            builder.nextControlFlow("catch (e: %T)", EXCEPTION_CLASS)

            // Get the error type for the fallback
            val errorTypeDeclaration = info.errorType.declaration
            val errorClassName =
                ClassName(
                    errorTypeDeclaration.packageName.asString(),
                    errorTypeDeclaration.simpleName.asString(),
                )
            builder.addStatement(
                "throw %T(%S + %T::class.simpleName + %S + httpResponse.status.value + %S + (e.message ?: %S))",
                ILLEGAL_STATE_EXCEPTION_CLASS,
                "Failed to parse error response as ",
                errorClassName,
                " (HTTP ",
                "): ",
                "unknown error",
            )
            builder.endControlFlow()
            builder.unindent()
            builder.addStatement("}")
        } else {
            // Return ApiResponse
            builder.addStatement("return %T(", API_RESPONSE_CLASS)
            builder.indent()
            builder.addStatement("status = httpResponse.status,")
            builder.addStatement("headers = httpResponse.headers,")
            builder.addStatement("contentType = httpResponse.%M(),", CONTENT_TYPE_MEMBER)

            if (info.isEmptyResponse) {
                builder.addStatement("body = Unit,")
            } else {
                builder.addStatement("body = httpResponse.%M(),", BODY_MEMBER)
            }
            builder.unindent()
            builder.addStatement(")")
        }

        return builder.build()
    }

    private fun generateTestHarness(apiInfo: ApiSpecInfo) {
        val apiSpecClassName = ClassName(apiInfo.packageName, apiInfo.apiSpec.simpleName.asString())
        val clientClassName =
            ClassName(
                "${apiInfo.packageName}.generated",
                "${apiInfo.apiSpec.simpleName.asString().removeSuffix("ApiSpec")}Client",
            )
        val fileName = "${apiInfo.apiSpec.simpleName.asString().removeSuffix("ApiSpec")}TestHarness"
        val functionName = "${apiInfo.apiName}ApiTest"

        // Simple test harness function (no setUp/tearDown)
        val simpleFunSpec =
            FunSpec
                .builder(functionName)
                .addParameter(
                    ParameterSpec
                        .builder("baseUrl", String::class)
                        .defaultValue("%T.baseUrl", apiSpecClassName)
                        .build(),
                ).addParameter(
                    ParameterSpec
                        .builder("client", HTTP_CLIENT_CLASS)
                        .defaultValue("%M", DEFAULT_HTTP_CLIENT_MEMBER)
                        .build(),
                ).addParameter(
                    "block",
                    LambdaTypeName
                        .get(
                            receiver = clientClassName,
                            returnType = Unit::class.asTypeName(),
                        ).copy(suspending = true),
                ).returns(Unit::class)
                .addCode(
                    CodeBlock
                        .builder()
                        .addStatement(
                            "%M(api = %T(client, baseUrl), block = block)",
                            API_TEST_MEMBER,
                            clientClassName,
                        ).build(),
                ).build()

        // setUp/tearDown test harness function
        val typeVariableT = TypeVariableName("T")
        val setupTeardownFunSpec =
            FunSpec
                .builder(functionName)
                .addTypeVariable(typeVariableT)
                .addParameter(
                    ParameterSpec
                        .builder("baseUrl", String::class)
                        .defaultValue("%T.baseUrl", apiSpecClassName)
                        .build(),
                ).addParameter(
                    ParameterSpec
                        .builder("client", HTTP_CLIENT_CLASS)
                        .defaultValue("%M", DEFAULT_HTTP_CLIENT_MEMBER)
                        .build(),
                ).addParameter(
                    "setUp",
                    LambdaTypeName
                        .get(
                            receiver = clientClassName,
                            returnType = typeVariableT,
                        ).copy(suspending = true),
                ).addParameter(
                    ParameterSpec
                        .builder(
                            "tearDown",
                            LambdaTypeName
                                .get(
                                    receiver = clientClassName,
                                    parameters = listOf(ParameterSpec.unnamed(typeVariableT)),
                                    returnType = Unit::class.asTypeName(),
                                ).copy(suspending = true),
                        ).defaultValue("{}")
                        .build(),
                ).addParameter(
                    "block",
                    LambdaTypeName
                        .get(
                            receiver = clientClassName,
                            parameters = listOf(ParameterSpec.unnamed(typeVariableT)),
                            returnType = Unit::class.asTypeName(),
                        ).copy(suspending = true),
                ).returns(Unit::class)
                .addCode(
                    CodeBlock
                        .builder()
                        .addStatement(
                            "%M(api = %T(client, baseUrl), setUp = setUp, tearDown = tearDown, block = block)",
                            API_TEST_MEMBER,
                            clientClassName,
                        ).build(),
                ).build()

        val fileSpec =
            FileSpec
                .builder("${apiInfo.packageName}.generated", fileName)
                .addFileComment("Code generated by kolibrium-codegen. Do not edit.")
                .addFunction(simpleFunSpec)
                .addFunction(setupTeardownFunSpec)
                .build()

        codeGenerator
            .createNewFile(
                Dependencies(false, apiInfo.apiSpec.containingFile!!),
                fileSpec.packageName,
                fileSpec.name,
            ).writer()
            .use { writer -> fileSpec.writeTo(writer) }
    }

    private fun getReturnTypeName(
        info: RequestClassInfo,
        clientPackage: String,
    ): TypeName {
        // If error type is specified, return the sealed result type
        if (info.errorType != null) {
            val resultTypeName = getResultTypeName(info.requestClass.simpleName.asString())
            return ClassName(clientPackage, resultTypeName)
        }

        // Standard return type without error handling
        return if (info.isEmptyResponse) {
            EMPTY_RESPONSE_CLASS
        } else {
            val returnTypeDeclaration = info.returnType.declaration
            val returnClassName =
                ClassName(
                    returnTypeDeclaration.packageName.asString(),
                    returnTypeDeclaration.simpleName.asString(),
                )
            API_RESPONSE_CLASS.parameterizedBy(returnClassName)
        }
    }

    private fun getResultTypeName(requestClassName: String): String {
        // Convert "*Request" to "*Result"
        return requestClassName.removeSuffix("Request") + "Result"
    }

    private fun generateResultType(
        info: RequestClassInfo,
        clientPackage: String,
    ): TypeSpec {
        val resultTypeName = getResultTypeName(info.requestClass.simpleName.asString())

        val successTypeDeclaration = info.returnType.declaration
        val successClassName =
            ClassName(
                successTypeDeclaration.packageName.asString(),
                successTypeDeclaration.simpleName.asString(),
            )

        val errorTypeDeclaration = info.errorType!!.declaration
        val errorClassName =
            ClassName(
                errorTypeDeclaration.packageName.asString(),
                errorTypeDeclaration.simpleName.asString(),
            )

        // Build sealed interface with Success and Error data classes
        val sealedInterface =
            TypeSpec
                .interfaceBuilder(resultTypeName)
                .addModifiers(KModifier.SEALED)
                .addFunction(
                    FunSpec
                        .builder("requireSuccess")
                        .returns(ClassName(clientPackage, resultTypeName).nestedClass("Success"))
                        .addCode(
                            CodeBlock
                                .builder()
                                .beginControlFlow("return when (this)")
                                .addStatement("is Success -> this")
                                .addStatement(
                                    "is Error -> throw %T(%P)",
                                    ClassName("kotlin", "IllegalStateException"),
                                    $$"Expected success but got error: ${data}",
                                ).endControlFlow()
                                .build(),
                        ).build(),
                ).addFunction(
                    FunSpec
                        .builder("requireError")
                        .returns(ClassName(clientPackage, resultTypeName).nestedClass("Error"))
                        .addCode(
                            CodeBlock
                                .builder()
                                .beginControlFlow("return when (this)")
                                .addStatement(
                                    "is Success -> throw %T(%P)",
                                    ClassName("kotlin", "IllegalStateException"),
                                    $$"Expected error but got success: ${data}",
                                ).addStatement("is Error -> this")
                                .endControlFlow()
                                .build(),
                        ).build(),
                )

        // Success data class
        val successClass =
            TypeSpec
                .classBuilder("Success")
                .addModifiers(KModifier.DATA)
                .addSuperinterface(ClassName(clientPackage, resultTypeName))
                .primaryConstructor(
                    FunSpec
                        .constructorBuilder()
                        .addParameter("data", successClassName)
                        .addParameter("response", HTTP_RESPONSE_CLASS)
                        .build(),
                ).addProperty(
                    PropertySpec
                        .builder("data", successClassName)
                        .initializer("data")
                        .build(),
                ).addProperty(
                    PropertySpec
                        .builder("response", HTTP_RESPONSE_CLASS)
                        .initializer("response")
                        .build(),
                ).build()

        // Error data class
        val errorClass =
            TypeSpec
                .classBuilder("Error")
                .addModifiers(KModifier.DATA)
                .addSuperinterface(ClassName(clientPackage, resultTypeName))
                .primaryConstructor(
                    FunSpec
                        .constructorBuilder()
                        .addParameter("data", errorClassName)
                        .addParameter("response", HTTP_RESPONSE_CLASS)
                        .build(),
                ).addProperty(
                    PropertySpec
                        .builder("data", errorClassName)
                        .initializer("data")
                        .build(),
                ).addProperty(
                    PropertySpec
                        .builder("response", HTTP_RESPONSE_CLASS)
                        .initializer("response")
                        .build(),
                ).build()

        sealedInterface.addType(successClass)
        sealedInterface.addType(errorClass)

        return sealedInterface.build()
    }

    private fun buildUrlPath(info: RequestClassInfo): String {
        var path = info.path
        info.pathProperties.forEach { property ->
            val paramName = property.simpleName.asString()
            path = path.replace("{$paramName}", $$"$$$paramName")
        }
        return path
    }

    private fun KSType.toTypeName(): TypeName {
        val declaration = this.declaration
        val baseClassName =
            ClassName(
                declaration.packageName.asString(),
                declaration.simpleName.asString(),
            )

        // Handle generic types (e.g., List<String>)
        val typeArguments = this.arguments
        val typeName =
            if (typeArguments.isNotEmpty()) {
                val typeArgumentNames =
                    typeArguments.mapNotNull { typeArg ->
                        typeArg.type?.resolve()?.toTypeName()
                    }
                if (typeArgumentNames.size == typeArguments.size) {
                    baseClassName.parameterizedBy(typeArgumentNames)
                } else {
                    // Fallback if we can't resolve all type arguments
                    baseClassName
                }
            } else {
                baseClassName
            }

        // Apply nullability
        return if (this.nullability == Nullability.NULLABLE) {
            typeName.copy(nullable = true)
        } else {
            typeName
        }
    }

    private fun checkFunctionNameCollisions(
        apiInfo: ApiSpecInfo,
        requestInfos: List<RequestClassInfo>,
        errors: MutableList<Diagnostic>,
    ) {
        when (apiInfo.grouping) {
            ClientGrouping.SingleClient -> {
                // Check collisions across all requests in the API
                val functionNames = mutableMapOf<String, KSClassDeclaration>()
                requestInfos.forEach { info ->
                    val functionName = info.requestClass.toFunctionName()
                    val existing = functionNames[functionName]
                    if (existing != null) {
                        errors +=
                            Diagnostic(
                                "Generated function '$functionName' conflicts with another function in API '${apiInfo.apiName}'",
                                info.requestClass,
                            )
                    } else {
                        functionNames[functionName] = info.requestClass
                    }
                }
            }

            ClientGrouping.ByPrefix -> {
                // Check collisions per group
                val groupedRequests = groupRequestsByPrefix(requestInfos)
                groupedRequests.forEach { (groupName, groupRequests) ->
                    val functionNames = mutableMapOf<String, KSClassDeclaration>()
                    groupRequests.forEach { info ->
                        val functionName = info.requestClass.toFunctionName()
                        val existing = functionNames[functionName]
                        if (existing != null) {
                            errors +=
                                Diagnostic(
                                    "Generated function '$functionName' conflicts with another function in group '$groupName' of API '${apiInfo.apiName}'",
                                    info.requestClass,
                                )
                        } else {
                            functionNames[functionName] = info.requestClass
                        }
                    }
                }
            }
        }
    }

    private fun KSClassDeclaration.toFunctionName(): String {
        val className = simpleName.asString()
        return deriveFunctionName(className)
    }

    private fun deriveFunctionName(className: String): String {
        val withoutRequest = className.removeSuffix("Request")
        return if (withoutRequest.isEmpty()) {
            ""
        } else {
            withoutRequest.replaceFirstChar { it.lowercase() }
        }
    }

    private fun KSClassDeclaration.getClassName(): String = qualifiedName?.asString() ?: simpleName.asString()

    private fun String.isValidKotlinPackage(): Boolean = split('.').all { it.isValidKotlinIdentifier() }

    private fun String.isValidKotlinIdentifier(): Boolean {
        val regex = Regex("^[A-Za-z_][A-Za-z0-9_]*$")
        return regex.matches(this)
    }

    private fun extractPathVariables(path: String): PathVariables {
        val rawMatches = Regex("\\{([^}]+)}").findAll(path).map { it.groupValues[1] }.toList()
        val validNames = rawMatches.filter { it.isValidKotlinIdentifier() }.toSet()
        val invalidNames = rawMatches.filterNot { it.isValidKotlinIdentifier() }.toSet()
        return PathVariables(names = validNames, invalidNames = invalidNames)
    }

    private fun KSAnnotated.hasAnnotation(annotationClass: KClass<*>): Boolean =
        annotations.any { ksAnnotation ->
            val resolvedQualifiedName =
                ksAnnotation.annotationType
                    .resolve()
                    .declaration
                    .qualifiedName
                    ?.asString()

            val expectedQualifiedName = annotationClass.qualifiedName ?: annotationClass.java.name

            resolvedQualifiedName == expectedQualifiedName ||
                ksAnnotation.shortName.asString() == annotationClass.simpleName
        }

    private fun KSAnnotated.getAnnotation(annotationClass: KClass<*>): KSAnnotation? =
        annotations.firstOrNull {
            it.annotationType
                .resolve()
                .declaration.qualifiedName
                ?.asString() == annotationClass.java.name
        }

    private fun KSAnnotation.getArgumentValue(argumentName: String) =
        arguments
            .firstOrNull { it.name?.asString() == argumentName }
            ?.value

    private fun KSAnnotation.getKClassTypeArgument(name: String) =
        when (val value = getArgumentValue(name)) {
            is KSType -> value
            else -> null
        }

    private fun KSClassDeclaration.getHttpMethodAnnotations() = annotations.filter { it.toHttpMethod() != null }.toList()

    private fun KSAnnotation.toHttpMethod(): HttpMethod? {
        val qName =
            annotationType
                .resolve()
                .declaration
                .qualifiedName
                ?.asString()
                ?: return null

        return when (qName) {
            GET::class.qualifiedName -> HttpMethod.Get
            POST::class.qualifiedName -> HttpMethod.Post
            PUT::class.qualifiedName -> HttpMethod.Put
            DELETE::class.qualifiedName -> HttpMethod.Delete
            PATCH::class.qualifiedName -> HttpMethod.Patch
            else -> null
        }
    }

    private fun reportDiagnostics(
        warnings: List<Diagnostic>,
        errors: List<Diagnostic>,
    ) {
        warnings.forEach { logger.warn(it.message, it.node) }
        errors.forEach { logger.error(it.message, it.node) }
    }

    private data class ApiSpecInfo(
        val apiSpec: KSClassDeclaration,
        val apiName: String,
        val packageName: String,
        val scanPackages: List<String>,
        val grouping: ClientGrouping,
    )

    private data class RequestClassInfo(
        val requestClass: KSClassDeclaration,
        val httpMethod: HttpMethod,
        val path: String,
        val group: String,
        val returnType: KSType,
        val errorType: KSType?,
        val isEmptyResponse: Boolean,
        val pathProperties: List<KSPropertyDeclaration>,
        val queryProperties: List<KSPropertyDeclaration>,
        val bodyProperties: List<KSPropertyDeclaration>,
        val ctorDefaults: Map<String, Boolean>,
        val apiPackage: String,
        val authType: AuthType,
        val apiKeyHeader: String,
    )

    private data class PathVariables(
        val names: Set<String>,
        val invalidNames: Set<String>,
    )

    private data class Diagnostic(
        val message: String,
        val node: KSNode? = null,
    )

    private companion object {
        private const val KOTLIN_UNIT: String = "kotlin.Unit"
        private const val KOTLIN_NOTHING: String = "kotlin.Nothing"
        private const val JAVA_VOID: String = "java.lang.Void"
        private const val API_SPEC_BASE_CLASS: String = "dev.kolibrium.api.core.ApiSpec"

        private val ALLOWED_PATH_AND_QUERY_PARAMETER_TYPES: Set<String> =
            setOf(
                "kotlin.String",
                "kotlin.Int",
                "kotlin.Long",
                "kotlin.Short",
                "kotlin.Float",
                "kotlin.Double",
                "kotlin.Boolean",
            )

        private val API_RESPONSE_CLASS = ClassName("dev.kolibrium.api.core", "ApiResponse")
        private val EMPTY_RESPONSE_CLASS = ClassName("dev.kolibrium.api.core", "EmptyResponse")
        private val CONTENT_TYPE_CLASS = ClassName("io.ktor.http", "ContentType")
        private val HTTP_CLIENT_CLASS = ClassName("io.ktor.client", "HttpClient")
        private val HTTP_RESPONSE_CLASS = ClassName("io.ktor.client.statement", "HttpResponse")
        private val EXCEPTION_CLASS = ClassName("kotlin", "Exception")
        private val ILLEGAL_STATE_EXCEPTION_CLASS = ClassName("kotlin", "IllegalStateException")
        private val DEFAULT_HTTP_CLIENT_MEMBER = MemberName("dev.kolibrium.api.core", "defaultHttpClient")
        private val API_TEST_MEMBER = MemberName("dev.kolibrium.api.core", "apiTest")

        // Ktor request builder functions
        private val SET_BODY_MEMBER = MemberName("io.ktor.client.request", "setBody")
        private val PARAMETER_MEMBER = MemberName("io.ktor.client.request", "parameter")

        // Ktor response functions
        private val BODY_MEMBER = MemberName("io.ktor.client.call", "body")

        // Ktor HTTP functions
        private val CONTENT_TYPE_MEMBER = MemberName("io.ktor.http", "contentType")
        private val BEARER_AUTH_MEMBER = MemberName("io.ktor.client.request", "bearerAuth")
        private val BASIC_AUTH_MEMBER = MemberName("io.ktor.client.request", "basicAuth")
    }
}

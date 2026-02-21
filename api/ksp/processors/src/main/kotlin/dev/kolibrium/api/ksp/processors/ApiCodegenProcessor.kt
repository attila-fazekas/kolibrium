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
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import dev.kolibrium.api.ksp.annotations.ClientGrouping
import dev.kolibrium.api.ksp.annotations.GenerateApi
import kotlinx.serialization.Serializable

/**
 * Symbol processor that generates API client code from request classes.
 *
 * This processor discovers classes annotated with [@GenerateApi][dev.kolibrium.api.ksp.annotations.GenerateApi]
 * and generates corresponding HTTP client implementations based on request classes found
 * in the configured scan packages.
 *
 * ## Configuration
 *
 * Runtime configuration is provided by extending `ApiSpec`:
 * - **baseUrl**: Required. The base URL for the API endpoint
 * - **httpClient**: Optional. Custom HTTP client configuration (defaults to [defaultHttpClient][dev.kolibrium.api.core.defaultHttpClient])
 *
 * Codegen configuration is provided via the [@GenerateApi][dev.kolibrium.api.ksp.annotations.GenerateApi] annotation:
 * - **scanPackages**: Optional. Packages to scan for request classes (defaults to `<api-package>.models`)
 * - **grouping**: Optional. Client organization mode (defaults to [ClientGrouping.SingleClient])
 * - **generateTestHarness**: Optional. Whether to generate test harness functions (defaults to `true`)
 *
 * ## Client Generation Modes
 *
 * - **SingleClient**: All endpoints in one client class (default)
 * - **ByPrefix**: Endpoints grouped by API path prefix into separate client classes
 *
 * ## Validation
 *
 * The processor validates:
 * - API specification classes (must extend `ApiSpec`)
 * - Request classes (must be `@Serializable` data classes with HTTP method annotations)
 * - Path parameters (must match path variables and be annotated with `@Path` and `@Transient`)
 * - Query parameters (must be nullable and annotated with `@Query`)
 * - Body parameters (must be nullable or have defaults for DSL builder pattern)
 * - Return types (must be `@Serializable` or `Unit`)
 *
 * ## Generated Code
 *
 * For each API specification, the processor generates:
 * - Typed HTTP client classes with suspend functions
 * - Test harness functions for API testing with setUp/tearDown support
 * - Sealed result types for requests with error type specifications
 * - Automatic serialization/deserialization using Ktor's content negotiation
 *
 * Example API specification:
 * ```kotlin
 * @GenerateApi(
 *     scanPackages = ["io.github.vinylstore.models"],
 *     grouping = ClientGrouping.ByPrefix,
 * )
 * object VinylStoreApiSpec : ApiSpec() {
 *     override val baseUrl = "http://localhost:8080"
 * }
 * ```
 *
 * @param environment The symbol processor environment providing access to logging and code generation
 */
public class ApiCodegenProcessor(
    environment: SymbolProcessorEnvironment,
) : SymbolProcessor {
    private val logger: KSPLogger = environment.logger
    private val codeGenerator: CodeGenerator = environment.codeGenerator

    private val apiSpecValidator = ApiSpecValidator(logger)
    private val requestClassValidator = RequestClassValidator()
    private val parameterValidator = ParameterValidator()
    private val returnTypeValidator = ReturnTypeValidator()
    private val crossClassValidator = CrossClassValidator()
    private val clientMethodGenerator = ClientMethodGenerator()
    private val resultTypeGenerator = ResultTypeGenerator(clientMethodGenerator)
    private val clientCodeGenerator = ClientCodeGenerator(codeGenerator, clientMethodGenerator, resultTypeGenerator)
    private val testHarnessGenerator = TestHarnessGenerator(codeGenerator)

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
                apiSpecValidator.validateApiSpecClass(apiClass, errors)
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
                    requestClassValidator.validateRequestClass(requestClass, apiInfo, errors, warnings)
                }
            }

        // Parameter and Return Type Validation
        requestInfosByApi.values.flatten().forEach { info ->
            parameterValidator.validateRequestParameters(info, errors, warnings)
            returnTypeValidator.validateReturnType(info, errors)
        }

        // Check for function name collisions per API
        requestInfosByApi.forEach { (apiInfo, requests) ->
            crossClassValidator.checkFunctionNameCollisions(apiInfo, requests, errors)
        }

        // Validate grouped mode specific constraints
        requestInfosByApi.forEach { (apiInfo, requests) ->
            if (apiInfo.grouping == ClientGrouping.ByPrefix) {
                crossClassValidator.validateGroupedMode(apiInfo, requests, errors, warnings)
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
                clientCodeGenerator.generateClientClass(apiInfo, requests)
                if (apiInfo.generateTestHarness) {
                    testHarnessGenerator.generateTestHarness(apiInfo)
                }
            }
        }

        return emptyList()
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
            classesInScanPackages
                .filter { classDeclaration ->
                    classDeclaration.hasAnnotation(Serializable::class) &&
                        classDeclaration.getHttpMethodAnnotations().isNotEmpty()
                }.distinctBy { it.qualifiedName?.asString() }
        }
    }

    private fun reportDiagnostics(
        warnings: List<Diagnostic>,
        errors: List<Diagnostic>,
    ) {
        warnings.forEach { logger.warn(it.message, it.node) }
        errors.forEach { logger.error(it.message, it.node) }
    }
}

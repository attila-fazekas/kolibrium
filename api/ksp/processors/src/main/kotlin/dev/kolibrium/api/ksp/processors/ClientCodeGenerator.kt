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
import com.google.devtools.ksp.symbol.KSFile
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import dev.kolibrium.api.ksp.annotations.AuthType
import dev.kolibrium.api.ksp.annotations.ClientGrouping

// Generators are classes (not top-level functions like validators) because they share
// constructor-injected dependencies (CodeGenerator, sub-generators) across multiple methods.
internal class ClientCodeGenerator(
    private val codeGenerator: CodeGenerator,
    private val clientMethodGenerator: ClientMethodGenerator,
    private val resultTypeGenerator: ResultTypeGenerator,
) {
    fun generateClientClass(
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
        val clientClassName = "${apiInfo.clientNamePrefix}Client"
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
        val rootClientClassName = "${apiInfo.clientNamePrefix}Client"
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
            apiInfo = apiInfo,
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

        classBuilder
            .addKdoc("HTTP client for the %L API.", apiInfo.displayName)
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
                .map { resultTypeGenerator.generateResultType(it, clientPackage) }

        // Generate methods for each request
        requests.forEach { info ->
            val funSpec = clientMethodGenerator.generateClientMethod(apiInfo, info, clientPackage)
            classBuilder.addFunction(funSpec)
        }

        // Collect HTTP methods used for imports
        val httpMethodNames = clientMethodGenerator.collectHttpMethodNames(requests)

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

        // Add URL encoding import if any request has path parameters
        if (clientMethodGenerator.hasPathParameters(requests)) {
            fileSpecBuilder.addImport("io.ktor.http", "encodeURLPathPart")
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
            if (AuthType.CUSTOM in authTypes) {
                fileSpecBuilder.addImport("io.ktor.client.request", "HttpRequestBuilder")
            }
        }

        // Add header import if any request uses API_KEY auth
        val needsHeaderImport = requests.any { it.authType == AuthType.API_KEY }
        if (needsHeaderImport) {
            fileSpecBuilder.addImport("io.ktor.client.request", "header")
        }

        // Add isSuccess import if any request has error type (for sealed result handling)
        val hasErrorTypes = requests.any { it.errorType != null }
        if (hasErrorTypes) {
            fileSpecBuilder.addImport("io.ktor.http", "isSuccess")
        }

        // Add HeadersBuilder since every generated function has a `headers` parameter
        // TODO fileSpecBuilder.addImport("io.ktor.http.", "HeadersBuilder")

        val fileSpec = fileSpecBuilder.build()

        codeGenerator
            .createNewFile(
                Dependencies.ALL_FILES,
                fileSpec.packageName,
                fileSpec.name,
            ).writer()
            .use { writer -> fileSpec.writeTo(writer) }
    }

    private fun generateRootAggregatorClient(
        rootClientClassName: String,
        clientPackage: String,
        apiInfo: ApiSpecInfo,
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

        classBuilder
            .addKdoc("Aggregator client for the %L API, grouping endpoints by resource.", apiInfo.displayName)
            .primaryConstructor(constructorBuilder.build())
            .addProperty(
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
                Dependencies.ALL_FILES,
                fileSpec.packageName,
                fileSpec.name,
            ).writer()
            .use { writer -> fileSpec.writeTo(writer) }
    }
}

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
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asTypeName

internal class TestHarnessGenerator(
    private val codeGenerator: CodeGenerator,
) {
    fun generateTestHarness(apiInfo: ApiSpecInfo) {
        val apiSpecClassName = ClassName(apiInfo.packageName, apiInfo.apiSpec.simpleName.asString())
        val clientClassName =
            ClassName(
                "${apiInfo.packageName}.generated",
                "${apiInfo.displayName}Client",
            )
        val fileName = "${apiInfo.displayName}TestHarness"
        val functionName = "${apiInfo.apiName}ApiTest"

        // Simple test harness function (no setUp/tearDown)
        val simpleFunSpec =
            FunSpec
                .builder(functionName)
                .addKdoc("Runs a test against the %L API client.", apiInfo.displayName)
                .addParameter(
                    ParameterSpec
                        .builder("baseUrl", String::class)
                        .defaultValue("%T.baseUrl", apiSpecClassName)
                        .build(),
                ).addParameter(
                    ParameterSpec
                        .builder("client", HTTP_CLIENT_CLASS)
                        .defaultValue("%T.httpClient", apiSpecClassName)
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
                .addKdoc("Runs a test against the %L API client with setUp and tearDown phases.", apiInfo.displayName)
                .addParameter(
                    ParameterSpec
                        .builder("baseUrl", String::class)
                        .defaultValue("%T.baseUrl", apiSpecClassName)
                        .build(),
                ).addParameter(
                    ParameterSpec
                        .builder("client", HTTP_CLIENT_CLASS)
                        .defaultValue("%T.httpClient", apiSpecClassName)
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
                Dependencies(false, *listOfNotNull(apiInfo.apiSpec.containingFile).toTypedArray()),
                fileSpec.packageName,
                fileSpec.name,
            ).writer()
            .use { writer -> fileSpec.writeTo(writer) }
    }
}

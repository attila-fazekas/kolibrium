/*
 * Copyright 2023-2026 Attila Fazekas & contributors
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

package dev.kolibrium.selenium.ksp.processors

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asTypeName

internal class TestHarnessGenerator(
    private val codeGenerator: CodeGenerator,
) {
    fun generateTestHarness(info: SiteInfo) {
        val siteClassName = ClassName(info.packageName, info.siteName)
        val functionName = info.siteName.replaceFirstChar { it.lowercase() } + "Test"
        val fileName = "${info.siteName}TestHarness"
        val generatedPackage = "${info.packageName}.generated"
        val siteEntryType = SITE_ENTRY_CLASS.parameterizedBy(siteClassName)

        val noSetupFun =
            FunSpec
                .builder(functionName)
                .addParameter(
                    ParameterSpec
                        .builder("driverFactory", DRIVER_FACTORY_CLASS)
                        .defaultValue("{ %T() }", CHROME_DRIVER_CLASS)
                        .build(),
                ).addParameter(
                    ParameterSpec
                        .builder("keepBrowserOpen", Boolean::class)
                        .defaultValue("false")
                        .build(),
                ).addParameter(
                    "block",
                    LambdaTypeName
                        .get(
                            receiver = siteEntryType,
                            parameters = listOf(ParameterSpec.unnamed(Unit::class.asTypeName())),
                            returnType = Unit::class.asTypeName(),
                        ).copy(suspending = true),
                ).returns(Unit::class)
                .addCode(
                    CodeBlock
                        .builder()
                        .addStatement(
                            "%M(site = %T, keepBrowserOpen = keepBrowserOpen, driverFactory = driverFactory, block = block)",
                            SELENIUM_TEST_MEMBER,
                            siteClassName,
                        ).build(),
                ).build()

        val typeVariableT = TypeVariableName("T")
        val setupTeardownFun =
            FunSpec
                .builder(functionName)
                .addTypeVariable(typeVariableT)
                .addParameter(
                    ParameterSpec
                        .builder("driverFactory", DRIVER_FACTORY_CLASS)
                        .defaultValue("{ %T() }", CHROME_DRIVER_CLASS)
                        .build(),
                ).addParameter(
                    ParameterSpec
                        .builder("keepBrowserOpen", Boolean::class)
                        .defaultValue("false")
                        .build(),
                ).addParameter(
                    "setUp",
                    LambdaTypeName
                        .get(returnType = typeVariableT)
                        .copy(suspending = true),
                ).addParameter(
                    ParameterSpec
                        .builder(
                            "tearDown",
                            LambdaTypeName
                                .get(
                                    parameters = listOf(ParameterSpec.unnamed(typeVariableT)),
                                    returnType = Unit::class.asTypeName(),
                                ).copy(suspending = true),
                        ).defaultValue("{}")
                        .build(),
                ).addParameter(
                    "block",
                    LambdaTypeName
                        .get(
                            receiver = siteEntryType,
                            parameters = listOf(ParameterSpec.unnamed(typeVariableT)),
                            returnType = Unit::class.asTypeName(),
                        ).copy(suspending = true),
                ).returns(Unit::class)
                .addCode(
                    CodeBlock
                        .builder()
                        .addStatement(
                            "%M(site = %T, keepBrowserOpen = keepBrowserOpen, driverFactory = driverFactory, setUp = setUp, tearDown = tearDown, block = block)",
                            SELENIUM_TEST_MEMBER,
                            siteClassName,
                        ).build(),
                ).build()

        val fileSpec =
            FileSpec
                .builder(generatedPackage, fileName)
                .addFileComment("Code generated by kolibrium-codegen. Do not edit.")
                .addFunction(noSetupFun)
                .addFunction(setupTeardownFun)
                .build()

        codeGenerator
            .createNewFile(
                Dependencies.ALL_FILES,
                fileSpec.packageName,
                fileSpec.name,
            ).writer()
            .use { writer -> fileSpec.writeTo(writer) }
    }
}

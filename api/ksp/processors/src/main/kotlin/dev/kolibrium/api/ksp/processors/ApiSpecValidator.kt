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

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Modifier
import dev.kolibrium.api.core.ClientGrouping

internal class ApiSpecValidator(
    private val logger: KSPLogger,
) {
    fun validateApiSpecClass(
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
                    "ApiSpec implementation $className must be an object declaration or a concrete class",
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
                    "Class $className must extend $API_SPEC_BASE_CLASS",
                    apiSpecClass,
                )
            return null
        }

        // Must be in a package with a valid name
        val packageName = apiSpecClass.packageName.asString()

        if (!packageName.isValidKotlinPackage()) {
            errors +=
                Diagnostic(
                    "ApiSpec implementation $className has invalid package name: $packageName",
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
                    "ApiSpec implementation must not be named exactly 'ApiSpec'. Use a descriptive name like '<Name>ApiSpec'.",
                    apiSpecClass,
                )
            return null
        }

        // Use convention-based configuration
        // scanPackages defaults to <package-name>.models
        val scanPackages = setOf("$packageName.models")

        // grouping defaults to SingleClient
        val grouping = ClientGrouping.SingleClient

        logger.info("API '$apiName' will use grouping: $grouping")
        logger.info("API '$apiName' will scan packages: $scanPackages")

        val generateTestHarness = apiSpecClass.readBooleanProperty("generateTestHarness", defaultValue = true)

        return ApiSpecInfo(
            apiSpec = apiSpecClass,
            apiName = apiName,
            packageName = packageName,
            scanPackages = scanPackages,
            grouping = grouping,
            generateTestHarness = generateTestHarness,
        )
    }
}

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

import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Modifier
import dev.kolibrium.api.ksp.annotations.ClientGrouping
import dev.kolibrium.api.ksp.annotations.GenerateApi

internal fun validateApiSpecClass(
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
                "Class $className is annotated with @GenerateApi but does not extend $API_SPEC_BASE_CLASS",
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

    // Read codegen configuration from @GenerateApi annotation (defaults when absent)
    val generateApiAnnotation = apiSpecClass.getAnnotation(GenerateApi::class)

    val conventionDefault = setOf("$packageName.models")

    val scanPackages =
        if (generateApiAnnotation != null) {
            @Suppress("UNCHECKED_CAST")
            val declared = generateApiAnnotation.getArgumentValue("scanPackages") as? List<String> ?: emptyList()
            if (declared.isEmpty()) conventionDefault else declared.toSet()
        } else {
            conventionDefault
        }

    val invalidPackages = scanPackages.filterNot { it.isValidKotlinPackage() }
    if (invalidPackages.isNotEmpty()) {
        invalidPackages.forEach { pkg ->
            errors +=
                Diagnostic(
                    "Invalid scan package '$pkg' in @GenerateApi on $className",
                    apiSpecClass,
                )
        }
        return null
    }

    val grouping =
        if (generateApiAnnotation != null) {
            when (val groupingArg = generateApiAnnotation.getArgumentValue("grouping")) {
                is KSType -> {
                    val name = groupingArg.declaration.simpleName.asString()
                    ClientGrouping.entries.firstOrNull { it.name == name }
                        ?: run {
                            errors +=
                                Diagnostic(
                                    "Unknown grouping '$name' in @GenerateApi on $className",
                                    apiSpecClass,
                                )
                            return null
                        }
                }

                is KSClassDeclaration -> {
                    val name = groupingArg.simpleName.asString()
                    ClientGrouping.entries.firstOrNull { it.name == name }
                        ?: run {
                            errors +=
                                Diagnostic(
                                    "Unknown grouping '$name' in @GenerateApi on $className",
                                    apiSpecClass,
                                )
                            return null
                        }
                }

                else -> {
                    ClientGrouping.SingleClient
                }
            }
        } else {
            ClientGrouping.SingleClient
        }

    val generateTestHarness = generateApiAnnotation.getBooleanArg("generateTestHarness", default = true)

    val annotationDisplayName = generateApiAnnotation?.getArgumentValue("displayName") as? String
    val displayName =
        if (!annotationDisplayName.isNullOrBlank()) {
            annotationDisplayName
        } else {
            deriveDisplayName(simpleName)
        }

    return ApiSpecInfo(
        apiSpec = apiSpecClass,
        apiName = apiName,
        packageName = packageName,
        scanPackages = scanPackages,
        grouping = grouping,
        generateTestHarness = generateTestHarness,
        displayName = displayName,
    )
}

private val DISPLAY_NAME_SUFFIXES: List<String> = listOf("ApiSpec", "Spec")

private fun deriveDisplayName(simpleName: String): String {
    for (suffix in DISPLAY_NAME_SUFFIXES) {
        if (simpleName.endsWith(suffix) && simpleName.length > suffix.length) {
            return simpleName.removeSuffix(suffix)
        }
    }
    return simpleName
}

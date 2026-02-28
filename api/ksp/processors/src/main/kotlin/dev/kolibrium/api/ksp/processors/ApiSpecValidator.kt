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

internal fun validateApiSpecClass(apiSpecClass: KSClassDeclaration): ValidationResult<ApiSpecInfo> {
    val className = apiSpecClass.getClassName()

    // Must be an object or concrete class
    val isObject = apiSpecClass.classKind == ClassKind.OBJECT
    val isClass = apiSpecClass.classKind == ClassKind.CLASS
    val isAbstract = apiSpecClass.modifiers.contains(Modifier.ABSTRACT)

    if (!isObject && !(isClass && !isAbstract)) {
        return ValidationResult.Invalid(
            listOf(
                Diagnostic(
                    "ApiSpec implementation $className must be an object declaration or a concrete class",
                    apiSpecClass,
                ),
            ),
        )
    }

    // Must extend dev.kolibrium.api.core.ApiSpec
    val extendsApiSpec =
        apiSpecClass.superTypes.any { superType ->
            val resolvedType = superType.resolve()
            resolvedType.declaration.qualifiedName?.asString() == API_SPEC_BASE_CLASS
        }

    if (!extendsApiSpec) {
        return ValidationResult.Invalid(
            listOf(
                Diagnostic(
                    "Class $className is annotated with @GenerateApi but does not extend $API_SPEC_BASE_CLASS",
                    apiSpecClass,
                ),
            ),
        )
    }

    // Must be in a package with a valid name
    val packageName = apiSpecClass.packageName.asString()

    if (!packageName.isValidKotlinPackage()) {
        return ValidationResult.Invalid(
            listOf(
                Diagnostic(
                    "ApiSpec implementation $className has invalid package name: $packageName",
                    apiSpecClass,
                ),
            ),
        )
    }

    // Extract display name and API name from class simple name
    val simpleName = apiSpecClass.simpleName.asString()

    val clientNamePrefix = deriveClientNamePrefix(simpleName)

    if (!clientNamePrefix.isValidKotlinIdentifier()) {
        return ValidationResult.Invalid(
            listOf(
                Diagnostic(
                    "Derived client name prefix '$clientNamePrefix' from class '$simpleName' is not a valid Kotlin " +
                        "identifier. Class names with backtick escaping that produce identifiers starting with a digit " +
                        "or containing special characters are not supported. The derived prefix must start with a " +
                        "letter and contain only letters, digits, or underscores.",
                    apiSpecClass,
                ),
            ),
        )
    }

    // Read codegen configuration from @GenerateApi annotation
    val generateApiAnnotation =
        requireNotNull(apiSpecClass.getAnnotation(GenerateApi::class)) {
            "Expected @GenerateApi annotation on ${apiSpecClass.getClassName()}"
        }

    val conventionDefault = setOf("$packageName.models")

    @Suppress("UNCHECKED_CAST")
    val declared = generateApiAnnotation.getArgumentValue("scanPackages") as? List<String> ?: emptyList()

    val scanPackages = if (declared.isEmpty()) conventionDefault else declared.toSet()

    val invalidPackages = scanPackages.filterNot { it.isValidKotlinPackage() }
    if (invalidPackages.isNotEmpty()) {
        return ValidationResult.Invalid(
            invalidPackages.map { pkg ->
                Diagnostic(
                    "Invalid scan package '$pkg' in @GenerateApi on $className",
                    apiSpecClass,
                )
            },
        )
    }

    val grouping =
        when (val groupingArg = generateApiAnnotation.getArgumentValue("grouping")) {
            is KSClassDeclaration -> {
                val name = groupingArg.simpleName.asString()
                ClientGrouping.entries.firstOrNull { it.name == name }
                    ?: return ValidationResult.Invalid(
                        listOf(
                            Diagnostic(
                                "Unknown grouping '$name' in @GenerateApi on $className",
                                apiSpecClass,
                            ),
                        ),
                    )
            }

            else -> {
                ClientGrouping.SingleClient
            }
        }

    val generateTestHarness = generateApiAnnotation.getBooleanArg("generateTestHarness", default = true)

    val annotationDisplayName = generateApiAnnotation?.getArgumentValue("displayName") as? String
    val displayName =
        if (!annotationDisplayName.isNullOrBlank()) {
            annotationDisplayName
        } else {
            deriveClientNamePrefix(simpleName)
        }

    return ValidationResult.Valid(
        ApiSpecInfo(
            apiSpec = apiSpecClass,
            clientNamePrefix = clientNamePrefix,
            scanPackages = scanPackages,
            grouping = grouping,
            generateTestHarness = generateTestHarness,
            displayName = displayName,
        ),
    )
}

// Ordering matters: "ApiSpec" must be checked before "Spec" to avoid partial stripping
private val NAME_SUFFIXES: List<String> = listOf("ApiSpec", "Spec", "Api")

private fun deriveClientNamePrefix(simpleName: String): String {
    if (simpleName in NAME_SUFFIXES) return simpleName
    for (suffix in NAME_SUFFIXES) {
        if (simpleName.endsWith(suffix)) {
            return simpleName.removeSuffix(suffix)
        }
    }
    return simpleName
}

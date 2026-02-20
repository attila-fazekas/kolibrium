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

import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Nullability
import dev.kolibrium.api.ksp.annotations.Path
import dev.kolibrium.api.ksp.annotations.Query
import io.ktor.http.HttpMethod

internal class ParameterValidator {
    fun validateRequestParameters(
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

    fun validatePathParameters(
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

            if (property.hasJvmTransientOnly()) {
                errors +=
                    Diagnostic(
                        "@Path parameter '$propertyName' uses @kotlin.jvm.Transient, which does not affect kotlinx.serialization. Use @kotlinx.serialization.Transient instead",
                        property,
                    )
            } else if (!property.hasTransientAnnotation()) {
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

    fun validateQueryParameters(
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
            if (property.hasJvmTransientOnly()) {
                errors +=
                    Diagnostic(
                        "@Query parameter '$propertyName' uses @kotlin.jvm.Transient, which does not affect kotlinx.serialization. Use @kotlinx.serialization.Transient instead",
                        property,
                    )
            } else if (!property.hasTransientAnnotation()) {
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

    fun validateBodyParameters(
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

    private fun isValidQueryParameterType(type: KSType): Boolean {
        val declaration = type.declaration
        val qualifiedName = declaration.qualifiedName?.asString() ?: return false

        // Check if it's a simple type
        if (qualifiedName in ALLOWED_PATH_AND_QUERY_PARAMETER_TYPES) {
            return true
        }

        // Check if it's a List<T> where T is an allowed type
        if (qualifiedName == KOTLIN_COLLECTIONS_LIST) {
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
}

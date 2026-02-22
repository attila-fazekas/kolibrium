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
import io.ktor.http.HttpMethod

internal class ParameterValidator {
    fun validateRequestParameters(
        info: RequestClassInfo,
        errors: MutableList<Diagnostic>,
        warnings: MutableList<Diagnostic>,
    ) {
        validatePathParameters(info, info.pathProperties, errors)
        validateQueryParameters(info, info.queryProperties, errors)
        validateHeaderParameters(info, info.headerProperties, errors, warnings)
        validateBodyParameters(info, info.bodyProperties, errors, warnings)
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

            validateTransientAnnotation(property, "@Path", errors)

            val typeQualifiedName =
                property.type
                    .resolve()
                    .declaration.qualifiedName
                    ?.asString()
            if (typeQualifiedName !in ALLOWED_PARAMETER_TYPES) {
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

            validateTransientAnnotation(property, "@Query", errors)

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

            validateDefaultValue(property, "@Query", info.ctorDefaults, errors)
        }
    }

    fun validateHeaderParameters(
        info: RequestClassInfo,
        headerProperties: List<KSPropertyDeclaration>,
        errors: MutableList<Diagnostic>,
        warnings: MutableList<Diagnostic>,
    ) {
        if (headerProperties.isEmpty()) return

        headerProperties.forEach { property ->
            val propertyName = property.simpleName.asString()

            validateTransientAnnotation(property, "@Header", errors)

            val resolvedType = property.type.resolve()

            if (resolvedType.nullability != Nullability.NULLABLE) {
                errors +=
                    Diagnostic(
                        "@Header parameter '$propertyName' must be nullable",
                        property,
                    )
            }

            val typeQualifiedName = resolvedType.declaration.qualifiedName?.asString()
            if (typeQualifiedName !in ALLOWED_PARAMETER_TYPES) {
                errors +=
                    Diagnostic(
                        "@Header parameter '$propertyName' must be String, Int, Long, Short, Float, Double, or Boolean",
                        property,
                    )
            }

            val headerName = extractHeaderName(property) ?: propertyName
            if (!headerName.isValidHttpHeaderName()) {
                errors +=
                    Diagnostic(
                        "Invalid HTTP header name '$headerName' for @Header parameter '$propertyName'",
                        property,
                    )
            }

            if (headerName.lowercase() in RESERVED_HEADER_NAMES) {
                warnings +=
                    Diagnostic(
                        "@Header parameter '$propertyName' uses reserved HTTP header name '$headerName'. Setting this header may cause silent failures or protocol-level bugs depending on the HTTP client",
                        property,
                    )
            }

            validateDefaultValue(property, "@Header", info.ctorDefaults, errors)
        }

        // Detect duplicate header names (case-insensitive per RFC 7230)
        val resolvedHeaders =
            headerProperties.map { property ->
                val headerName = extractHeaderName(property) ?: property.simpleName.asString()
                headerName to property
            }
        val seen = mutableMapOf<String, KSPropertyDeclaration>()
        resolvedHeaders.forEach { (headerName, property) ->
            val normalized = headerName.lowercase()
            val existing = seen[normalized]
            if (existing != null) {
                errors +=
                    Diagnostic(
                        "Duplicate HTTP header name '$headerName': properties '${existing.simpleName.asString()}' and '${property.simpleName.asString()}' resolve to the same header (case-insensitive)",
                        property,
                    )
            } else {
                seen[normalized] = property
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

    private fun validateDefaultValue(
        property: KSPropertyDeclaration,
        annotationLabel: String,
        ctorDefaults: Map<String, Boolean>,
        errors: MutableList<Diagnostic>,
    ) {
        val propertyName = property.simpleName.asString()
        val hasDefault = ctorDefaults[propertyName] == true
        if (!hasDefault) {
            errors +=
                Diagnostic(
                    "$annotationLabel parameter '$propertyName' must have a default value (e.g., = null)",
                    property,
                )
        }
    }

    private fun validateTransientAnnotation(
        property: KSPropertyDeclaration,
        annotationLabel: String,
        errors: MutableList<Diagnostic>,
    ) {
        val propertyName = property.simpleName.asString()
        if (property.hasJvmTransientOnly()) {
            errors +=
                Diagnostic(
                    "$annotationLabel parameter '$propertyName' uses @kotlin.jvm.Transient, which does not affect kotlinx.serialization. Use @kotlinx.serialization.Transient instead",
                    property,
                )
        } else if (!property.hasTransientAnnotation()) {
            errors +=
                Diagnostic(
                    "$annotationLabel parameter '$propertyName' must be annotated with @Transient",
                    property,
                )
        }
    }

    private fun isValidQueryParameterType(type: KSType): Boolean {
        val declaration = type.declaration
        val qualifiedName = declaration.qualifiedName?.asString() ?: return false

        // Check if it's a simple type
        if (qualifiedName in ALLOWED_PARAMETER_TYPES) {
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
                return typeArgQualifiedName in ALLOWED_PARAMETER_TYPES
            }
        }

        return false
    }
}

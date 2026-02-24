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

import com.google.devtools.ksp.symbol.KSClassDeclaration
import kotlinx.serialization.Serializable

internal fun validateReturnType(info: RequestClassInfo): ValidationResult<Unit> {
    val errors = mutableListOf<Diagnostic>()
    val warnings = mutableListOf<Diagnostic>()

    val requestClass = info.requestClass
    val className = requestClass.getClassName()

    // Validate no self-referential return type
    val returnQualified =
        info.returnType.declaration.qualifiedName
            ?.asString()
    val requestQualified = info.requestClass.qualifiedName?.asString()
    if (returnQualified != null && returnQualified == requestQualified) {
        warnings +=
            Diagnostic(
                "Request class $className uses itself as its own return type — this will deserialize the response body as the request class",
                requestClass,
            )
    }

    // Validate success type
    val returnType = info.returnType
    if (returnType.isError) {
        errors += Diagnostic("Success type for $className could not be resolved", requestClass)
        return ValidationResult.Invalid(errors, warnings)
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
            return ValidationResult.Invalid(errors, warnings)
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
            return ValidationResult.Invalid(errors, warnings)
        }

        val errorClass = errorType.declaration as? KSClassDeclaration
        if (errorClass == null) {
            errors +=
                Diagnostic(
                    "Error type '${errorType.declaration.qualifiedName?.asString() ?: errorType}' for $className must be a concrete class type",
                    requestClass,
                )
            return ValidationResult.Invalid(errors, warnings)
        }

        if (!errorClass.hasAnnotation(Serializable::class)) {
            errors +=
                Diagnostic(
                    "Error type ${errorClass.qualifiedName?.asString() ?: errorClass.simpleName.asString()} for $className is not @Serializable",
                    requestClass,
                )
        }
    }

    return if (errors.isEmpty()) {
        ValidationResult.Valid(Unit, warnings)
    } else {
        ValidationResult.Invalid(errors, warnings)
    }
}

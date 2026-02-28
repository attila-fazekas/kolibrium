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
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import dev.kolibrium.api.ksp.annotations.AuthType
import dev.kolibrium.api.ksp.annotations.ClientGrouping
import io.ktor.http.HttpMethod

internal data class ApiSpecInfo(
    val apiSpec: KSClassDeclaration,
    val clientNamePrefix: String,
    val scanPackages: Set<String>,
    val grouping: ClientGrouping,
    val generateTestHarness: Boolean,
    val displayName: String,
) {
    val apiName: String get() = clientNamePrefix.replaceFirstChar { it.lowercase() }
    val packageName: String get() = apiSpec.packageName.asString()
}

internal data class RequestClassInfo(
    val requestClass: KSClassDeclaration,
    val httpMethod: HttpMethod,
    val path: String,
    val returnType: KSType,
    val errorType: KSType?,
    val pathProperties: List<KSPropertyDeclaration>,
    val queryProperties: List<KSPropertyDeclaration>,
    val bodyProperties: List<KSPropertyDeclaration>,
    val ctorDefaults: Map<String, Boolean>,
    val authType: AuthType,
    val apiKeyHeader: String,
) {
    val endpointName: String get() = requestClass.simpleName.asString().removeSuffix("Request")
    val isEmptyResponse: Boolean get() = returnType.declaration.qualifiedName?.asString() == KOTLIN_UNIT
}

internal data class PathVariables(
    val names: Set<String>,
    val invalidNames: Set<String>,
    val duplicateNames: Set<String>,
)

internal data class Diagnostic(
    val message: String,
    val node: KSNode? = null,
)

internal sealed interface ValidationResult<out T> {
    val warnings: List<Diagnostic>

    data class Valid<T>(
        val value: T,
        override val warnings: List<Diagnostic> = emptyList(),
    ) : ValidationResult<T>

    data class Invalid(
        val errors: List<Diagnostic>,
        override val warnings: List<Diagnostic> = emptyList(),
    ) : ValidationResult<Nothing>
}

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
import dev.kolibrium.api.core.AuthType
import dev.kolibrium.api.core.ClientGrouping
import io.ktor.http.HttpMethod

internal data class ApiSpecInfo(
    val apiSpec: KSClassDeclaration,
    val apiName: String,
    val packageName: String,
    val scanPackages: Set<String>,
    val grouping: ClientGrouping,
)

internal data class RequestClassInfo(
    val requestClass: KSClassDeclaration,
    val httpMethod: HttpMethod,
    val path: String,
    val group: String,
    val returnType: KSType,
    val errorType: KSType?,
    val isEmptyResponse: Boolean,
    val pathProperties: List<KSPropertyDeclaration>,
    val queryProperties: List<KSPropertyDeclaration>,
    val bodyProperties: List<KSPropertyDeclaration>,
    val ctorDefaults: Map<String, Boolean>,
    val apiPackage: String,
    val authType: AuthType,
    val apiKeyHeader: String,
)

internal data class PathVariables(
    val names: Set<String>,
    val invalidNames: Set<String>,
)

internal data class Diagnostic(
    val message: String,
    val node: KSNode? = null,
)

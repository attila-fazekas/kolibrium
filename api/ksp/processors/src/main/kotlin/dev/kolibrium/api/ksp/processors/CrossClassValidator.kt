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
import dev.kolibrium.api.core.ClientGrouping

internal class CrossClassValidator {
    fun checkFunctionNameCollisions(
        apiInfo: ApiSpecInfo,
        requestInfos: List<RequestClassInfo>,
        errors: MutableList<Diagnostic>,
    ) {
        when (apiInfo.grouping) {
            ClientGrouping.SingleClient -> {
                // Check collisions across all requests in the API
                val functionNames = mutableMapOf<String, KSClassDeclaration>()
                requestInfos.forEach { info ->
                    val functionName = info.requestClass.toFunctionName()
                    val existing = functionNames[functionName]
                    if (existing != null) {
                        errors +=
                            Diagnostic(
                                "Generated function '$functionName' conflicts with another function in API '${apiInfo.apiName}'",
                                info.requestClass,
                            )
                    } else {
                        functionNames[functionName] = info.requestClass
                    }
                }
            }

            ClientGrouping.ByPrefix -> {
                // Check collisions per group
                val groupedRequests = groupRequestsByPrefix(requestInfos)
                groupedRequests.forEach { (groupName, groupRequests) ->
                    val functionNames = mutableMapOf<String, KSClassDeclaration>()
                    groupRequests.forEach { info ->
                        val functionName = info.requestClass.toFunctionName()
                        val existing = functionNames[functionName]
                        if (existing != null) {
                            errors +=
                                Diagnostic(
                                    "Generated function '$functionName' conflicts with another function in group '$groupName' of API '${apiInfo.apiName}'",
                                    info.requestClass,
                                )
                        } else {
                            functionNames[functionName] = info.requestClass
                        }
                    }
                }
            }
        }
    }

    fun validateGroupedMode(
        apiInfo: ApiSpecInfo,
        requests: List<RequestClassInfo>,
        errors: MutableList<Diagnostic>,
        warnings: MutableList<Diagnostic>,
    ) {
        val groupedRequests = groupRequestsByPrefix(requests)

        // Check for empty groups (defensive - shouldn't happen)
        if (groupedRequests.isEmpty()) {
            warnings +=
                Diagnostic(
                    "API '${apiInfo.apiName}' with ByPrefix grouping has no groupable endpoints",
                    apiInfo.apiSpec,
                )
            return
        }

        groupedRequests.forEach { (groupName, groupRequests) ->
            // Validate group name is a valid Kotlin identifier
            if (!groupName.isValidKotlinIdentifier()) {
                errors +=
                    Diagnostic(
                        "Group name '$groupName' in API '${apiInfo.apiName}' is not a valid Kotlin identifier",
                        apiInfo.apiSpec,
                    )
            }

            // Warn if a group contains only one endpoint
            if (groupRequests.size == 1) {
                warnings +=
                    Diagnostic(
                        "Group '$groupName' in API '${apiInfo.apiName}' contains only one endpoint; consider using SingleClient mode",
                        groupRequests.first().requestClass,
                    )
            }
        }
    }
}

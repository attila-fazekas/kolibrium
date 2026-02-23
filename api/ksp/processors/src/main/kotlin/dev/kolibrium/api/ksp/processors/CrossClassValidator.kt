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
import dev.kolibrium.api.ksp.annotations.ClientGrouping

internal fun checkFunctionNameCollisions(
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

internal fun validateGroupedMode(
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

    // Warn when all or most endpoints collapse into the fallback "root" group
    val rootGroup = groupedRequests[ROOT_GROUP_NAME]
    if (rootGroup != null && rootGroup.size == requests.size) {
        warnings +=
            Diagnostic(
                "All ${requests.size} endpoints in API '${apiInfo.apiName}' fall into the '$ROOT_GROUP_NAME' fallback group — ByPrefix grouping has no effect. Consider using SingleClient or restructuring your paths",
                apiInfo.apiSpec,
            )
    } else if (rootGroup != null && rootGroup.size > requests.size / 2) {
        warnings +=
            Diagnostic(
                "${rootGroup.size} of ${requests.size} endpoints in API '${apiInfo.apiName}' fall into the '$ROOT_GROUP_NAME' fallback group — ByPrefix grouping may not be useful",
                apiInfo.apiSpec,
            )
    }

    // Detect collision between the "root" fallback and a literal /root/... prefix
    if (groupedRequests.containsKey(ROOT_GROUP_NAME)) {
        val hasLiteralRoot =
            requests.any { info ->
                val firstSegment =
                    info.path
                        .trimStart('/')
                        .split('/')
                        .firstOrNull { it.isNotEmpty() }
                firstSegment == ROOT_GROUP_NAME
            }
        val hasFallbackRoot =
            requests.any { info ->
                val segments =
                    info.path
                        .trimStart('/')
                        .split('/')
                        .filter { it.isNotEmpty() }
                segments.firstOrNull { !it.startsWith("{") } == null
            }
        if (hasLiteralRoot && hasFallbackRoot) {
            warnings +=
                Diagnostic(
                    "API '${apiInfo.apiName}' has both a literal '/root/...' prefix and endpoints that fall back to the '$ROOT_GROUP_NAME' group — these will be merged into the same client class",
                    apiInfo.apiSpec,
                )
        }
    }

    groupedRequests.forEach { (groupName, _) ->
        // Validate group name is a valid Kotlin identifier
        if (!groupName.isValidKotlinIdentifier()) {
            errors +=
                Diagnostic(
                    "Group name '$groupName' in API '${apiInfo.apiName}' is not a valid Kotlin identifier",
                    apiInfo.apiSpec,
                )
        }
    }
}

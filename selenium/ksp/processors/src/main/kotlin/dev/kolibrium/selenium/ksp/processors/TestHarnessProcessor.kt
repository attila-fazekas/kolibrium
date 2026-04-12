/*
 * Copyright 2023-2026 Attila Fazekas & contributors
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

package dev.kolibrium.selenium.ksp.processors

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import dev.kolibrium.selenium.ksp.annotations.GenerateTestHarness

internal class TestHarnessProcessor(
    environment: SymbolProcessorEnvironment,
) : SymbolProcessor {
    private val logger = environment.logger
    private val generator = TestHarnessGenerator(environment.codeGenerator)

    override fun process(resolver: Resolver): List<KSAnnotated> {
        // Discover @GenerateTestHarness annotated classes directly via annotation
        val symbols =
            resolver
                .getSymbolsWithAnnotation(GenerateTestHarness::class.qualifiedName!!)
                .filterIsInstance<KSClassDeclaration>()
                .toList()

        if (symbols.isEmpty()) {
            logger.logging("No @GenerateTestHarness classes found, skipping code generation")
            return emptyList()
        }

        logger.logging("Discovered ${symbols.size} @GenerateTestHarness class(es)")

        val validInfos = mutableListOf<SiteInfo>()

        for (declaration in symbols) {
            if (declaration.classKind != ClassKind.OBJECT) {
                logger.error(
                    "@GenerateTestHarness can only be applied to object declarations",
                    declaration,
                )
                continue
            }

            val extendsSeleniumSite =
                declaration.superTypes.any { superTypeRef ->
                    superTypeRef
                        .resolve()
                        .declaration.qualifiedName
                        ?.asString() == SELENIUM_SITE_BASE_CLASS
                }

            if (!extendsSeleniumSite) {
                logger.error(
                    "@GenerateTestHarness requires the class to directly extend SeleniumSite",
                    declaration,
                )
                continue
            }

            validInfos +=
                SiteInfo(
                    siteDeclaration = declaration,
                    siteName = declaration.simpleName.asString(),
                    packageName = declaration.packageName.asString(),
                )
        }

        val duplicates =
            validInfos
                .groupBy { it.siteName.lowercase() }
                .filter { it.value.size > 1 }

        for ((_, infos) in duplicates) {
            for (info in infos) {
                logger.error(
                    "Duplicate generated function name for '${info.siteName}'",
                    info.siteDeclaration,
                )
            }
        }

        val duplicateNames = duplicates.keys
        val uniqueInfos = validInfos.filter { it.siteName.lowercase() !in duplicateNames }

        for (info in uniqueInfos) {
            generator.generateTestHarness(info)
        }

        return emptyList()
    }
}

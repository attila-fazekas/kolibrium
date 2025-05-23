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

package dev.kolibrium.ksp.processors

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.ExperimentalKotlinPoetApi
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.asTypeName
import dev.kolibrium.ksp.annotations.PageDsl
import org.openqa.selenium.WebDriver
import java.util.Locale

private const val KOLIBRIUM_DSL_PACKAGE_NAME = "dev.kolibrium.dsl.selenium"

/**
 * Symbol processor that generates a navigation function for Page Object classes.
 *
 * This processor scans for classes annotated with [PageDsl] and generates an extension function
 * for `WebDriver` that enable type-safe navigation and interaction with the annotated page.
 *
 * ### How It Works
 * The processor looks for all classes annotated with the `@PageDsl` annotation, extracts the
 * page's URL (if specified), and generates an extension function for `WebDriver` named after
 * the annotated class. This function allows developers to navigate to the page and interact
 * with it using a block of code applied to the page object.
 *
 * ### Generated Output
 * For each annotated class, a corresponding function is generated in the `generated` package.
 * If a URL path is provided in the annotation, the generated function includes that path in
 * the navigation logic; otherwise, it navigates to the current URL.
 */
public class PageProcessor(
    environment: SymbolProcessorEnvironment,
) : SymbolProcessor {
    private val codeGen = environment.codeGenerator
    private val useDsl = environment.options["kolibriumKsp.useDsl"]?.toBoolean() == true

    // process function returns a list of KSAnnotated objects, which represent symbols that
    // the processor can't currently process and need to be deferred to another round
    override fun process(resolver: Resolver): List<KSAnnotated> {
        // retrieve all class declarations annotated with Page
        val symbols =
            resolver
                .getSymbolsWithAnnotation(PageDsl::class.java.name)
                .filterIsInstance<KSClassDeclaration>()

        // in case there are no symbols that can be processed — exit
        if (symbols.iterator().hasNext().not()) return emptyList()

        symbols.forEach { it.accept(PageVisitor(), Unit) }

        // return deferred symbols that the processor can't process but in theory we have none
        return emptyList()
    }

    private inner class PageVisitor : KSVisitorVoid() {
        @OptIn(ExperimentalKotlinPoetApi::class)
        override fun visitClassDeclaration(
            classDeclaration: KSClassDeclaration,
            data: Unit,
        ) {
            val className = classDeclaration.simpleName.asString()
            val relativePath = classDeclaration.getAnnotation(PageDsl::class)?.getArgument("value")?.value as String

            val navigateStatement =
                CodeBlock.builder().apply {
                    if (relativePath.isNotBlank()) {
                        if (useDsl) {
                            addStatement(
                                "%T(\"$relativePath\")",
                                ClassName("${KOLIBRIUM_DSL_PACKAGE_NAME}.interactions", "navigateTo"),
                            )
                        } else {
                            addStatement("get(%P)", "\${currentUrl}$relativePath")
                        }
                    }
                }

            val function =
                FunSpec
                    .builder(
                        className.replaceFirstChar { it.lowercase(Locale.getDefault()) },
                    ).contextParameter("driver", WebDriver::class)
                    .addParameter(
                        "block",
                        LambdaTypeName.get(
                            receiver = ClassName(classDeclaration.packageName.asString(), className),
                            returnType = Unit::class.asTypeName(),
                        ),
                    ).addCode(
                        navigateStatement
                            .add("%N(driver).block()", className)
                            .build(),
                    ).build()

            val fileSpec =
                FileSpec
                    .builder(classDeclaration.generatedPackageName, className)
                    .addFunction(function)

            codeGen.writeToFile(classDeclaration, fileSpec)
        }
    }
}

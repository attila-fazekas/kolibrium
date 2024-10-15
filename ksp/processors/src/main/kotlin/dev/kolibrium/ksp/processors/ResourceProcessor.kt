/*
 * Copyright 2023-2024 Attila Fazekas & contributors
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

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.asTypeName
import dev.kolibrium.ksp.annotations.Resource
import java.util.Locale

public class ResourceProcessor(
    private val codeGen: CodeGenerator,
    private val logger: KSPLogger,
) : SymbolProcessor {
    // process function returns a list of KSAnnotated objects, which represent symbols that
    // the processor can't currently process and need to be deferred to another round
    override fun process(resolver: Resolver): List<KSAnnotated> {
        // retrieve all class declarations annotated with Resource
        val symbols =
            resolver
                .getSymbolsWithAnnotation(Resource::class.java.name)
                .filterIsInstance<KSClassDeclaration>()

        // in case there are no symbols that can be processed — exit
        if (symbols.iterator().hasNext().not()) return emptyList()

        symbols.forEach { it.accept(ResourceVisitor(), Unit) }

        // return deferred symbols that the processor can't process but in theory we have none
        return emptyList()
    }

    private inner class ResourceVisitor : KSVisitorVoid() {
        override fun visitClassDeclaration(
            classDeclaration: KSClassDeclaration,
            data: Unit,
        ) {
            val className = classDeclaration.simpleName.asString()
            val resource = classDeclaration.getAnnotation(Resource::class)?.getArgument("value")?.value as? String

            val function =
                FunSpec
                    .builder(
                        className.replaceFirstChar { it.lowercase(Locale.getDefault()) },
                    ).receiver(ClassName(SELENIUM_PACKAGE_NAME, "WebDriver"))
                    .addParameter(
                        "block",
                        LambdaTypeName.get(
                            receiver = ClassName(classDeclaration.packageName.asString(), className),
                            returnType = Unit::class.asTypeName(),
                        ),
                    ).addCode(
                        CodeBlock
                            .builder()
                            .addStatement("get(%P)", "\${currentUrl}$resource")
                            .add(
                                """
                                with($className()) {
                                    block()
                                }
                                """.trimIndent(),
                            ).build(),
                    ).build()

            val fileSpec =
                FileSpec
                    .builder(classDeclaration.generatedPackageName, className)
                    .addFunction(function)

            codeGen.writeToFile(classDeclaration, fileSpec)
        }
    }
}

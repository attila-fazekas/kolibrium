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

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec

internal class ResultTypeGenerator(
    private val clientMethodGenerator: ClientMethodGenerator,
) {
    fun generateResultType(
        info: RequestClassInfo,
        clientPackage: String,
    ): TypeSpec {
        val resultTypeName = clientMethodGenerator.getResultTypeName(info.requestClass.simpleName.asString())

        val successTypeDeclaration = info.returnType.declaration
        val successClassName =
            ClassName(
                successTypeDeclaration.packageName.asString(),
                successTypeDeclaration.simpleName.asString(),
            )

        val errorTypeDeclaration = info.errorType!!.declaration
        val errorClassName =
            ClassName(
                errorTypeDeclaration.packageName.asString(),
                errorTypeDeclaration.simpleName.asString(),
            )

        // Build sealed interface with Success and Error data classes
        val sealedInterface =
            TypeSpec
                .interfaceBuilder(resultTypeName)
                .addModifiers(KModifier.SEALED)
                .addFunction(
                    FunSpec
                        .builder("requireSuccess")
                        .returns(ClassName(clientPackage, resultTypeName).nestedClass("Success"))
                        .addCode(
                            CodeBlock
                                .builder()
                                .beginControlFlow("return when (this)")
                                .addStatement("is Success -> this")
                                .addStatement(
                                    "is Error -> throw %T(%L)",
                                    ClassName("kotlin", "IllegalStateException"),
                                    $$"\"Expected success but got error: $body\"",
                                ).endControlFlow()
                                .build(),
                        ).build(),
                ).addFunction(
                    FunSpec
                        .builder("requireError")
                        .returns(ClassName(clientPackage, resultTypeName).nestedClass("Error"))
                        .addCode(
                            CodeBlock
                                .builder()
                                .beginControlFlow("return when (this)")
                                .addStatement(
                                    "is Success -> throw %T(%L)",
                                    ClassName("kotlin", "IllegalStateException"),
                                    $$"\"Expected error but got success: $body\"",
                                ).addStatement("is Error -> this")
                                .endControlFlow()
                                .build(),
                        ).build(),
                )

        // Success data class
        val successClass =
            TypeSpec
                .classBuilder("Success")
                .addModifiers(KModifier.DATA)
                .addSuperinterface(ClassName(clientPackage, resultTypeName))
                .primaryConstructor(
                    FunSpec
                        .constructorBuilder()
                        .addParameter("body", successClassName)
                        .addParameter("response", HTTP_RESPONSE_CLASS)
                        .build(),
                ).addProperty(
                    PropertySpec
                        .builder("body", successClassName)
                        .initializer("body")
                        .build(),
                ).addProperty(
                    PropertySpec
                        .builder("response", HTTP_RESPONSE_CLASS)
                        .initializer("response")
                        .build(),
                ).build()

        // Error data class
        val errorClass =
            TypeSpec
                .classBuilder("Error")
                .addModifiers(KModifier.DATA)
                .addSuperinterface(ClassName(clientPackage, resultTypeName))
                .primaryConstructor(
                    FunSpec
                        .constructorBuilder()
                        .addParameter("body", errorClassName)
                        .addParameter("response", HTTP_RESPONSE_CLASS)
                        .build(),
                ).addProperty(
                    PropertySpec
                        .builder("body", errorClassName)
                        .initializer("body")
                        .build(),
                ).addProperty(
                    PropertySpec
                        .builder("response", HTTP_RESPONSE_CLASS)
                        .initializer("response")
                        .build(),
                ).build()

        sealedInterface.addType(successClass)
        sealedInterface.addType(errorClass)

        return sealedInterface.build()
    }
}

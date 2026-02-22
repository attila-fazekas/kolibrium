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
        generateKDoc: Boolean,
    ): TypeSpec {
        val resultTypeName = clientMethodGenerator.getResultTypeName(info)

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

        val sealedInterface =
            TypeSpec
                .interfaceBuilder(resultTypeName)
                .addModifiers(KModifier.SEALED)
                .also {
                    if (generateKDoc) {
                        it.addKdoc("Sealed result type for the %L endpoint.", info.endpointName)
                    }
                }.addFunction(buildRequireSuccess(clientPackage, resultTypeName, generateKDoc))
                .addFunction(buildRequireError(clientPackage, resultTypeName, generateKDoc))
                .addType(buildSuccessClass(clientPackage, resultTypeName, successClassName, generateKDoc))
                .addType(buildErrorClass(clientPackage, resultTypeName, errorClassName, generateKDoc))
                .build()

        return sealedInterface
    }

    private fun buildRequireSuccess(
        clientPackage: String,
        resultTypeName: String,
        generateKDoc: Boolean,
    ): FunSpec =
        FunSpec
            .builder("requireSuccess")
            .returns(ClassName(clientPackage, resultTypeName).nestedClass("Success"))
            .also {
                if (generateKDoc) {
                    it.addKdoc("Returns this as [Success] or throws [IllegalStateException] if this is [Error].")
                }
            }.addCode(
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
            ).build()

    private fun buildRequireError(
        clientPackage: String,
        resultTypeName: String,
        generateKDoc: Boolean,
    ): FunSpec =
        FunSpec
            .builder("requireError")
            .returns(ClassName(clientPackage, resultTypeName).nestedClass("Error"))
            .also {
                if (generateKDoc) {
                    it.addKdoc("Returns this as [Error] or throws [IllegalStateException] if this is [Success].")
                }
            }.addCode(
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
            ).build()

    private fun buildSuccessClass(
        clientPackage: String,
        resultTypeName: String,
        successClassName: ClassName,
        generateKDoc: Boolean,
    ): TypeSpec =
        TypeSpec
            .classBuilder("Success")
            .addModifiers(KModifier.DATA)
            .addSuperinterface(ClassName(clientPackage, resultTypeName))
            .also {
                if (generateKDoc) {
                    it.addKdoc("Successful response with a [%T] body.", successClassName)
                }
            }.primaryConstructor(
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

    private fun buildErrorClass(
        clientPackage: String,
        resultTypeName: String,
        errorClassName: ClassName,
        generateKDoc: Boolean,
    ): TypeSpec =
        TypeSpec
            .classBuilder("Error")
            .addModifiers(KModifier.DATA)
            .addSuperinterface(ClassName(clientPackage, resultTypeName))
            .also {
                if (generateKDoc) {
                    it.addKdoc("Error response with a [%T] body.", errorClassName)
                }
            }.primaryConstructor(
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
}

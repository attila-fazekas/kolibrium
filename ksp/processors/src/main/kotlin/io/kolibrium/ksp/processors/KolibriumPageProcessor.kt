/*
 * Copyright 2023 Attila Fazekas
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

package io.kolibrium.ksp.processors

import com.google.devtools.ksp.closestClassDeclaration
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.ExperimentalKotlinPoetApi
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import io.kolibrium.ksp.annotations.Css
import io.kolibrium.ksp.annotations.Id
import io.kolibrium.ksp.annotations.KolibriumPage
import io.kolibrium.ksp.annotations.LinkText
import io.kolibrium.ksp.annotations.Name
import io.kolibrium.ksp.annotations.PartialLinkText
import io.kolibrium.ksp.annotations.TagName
import io.kolibrium.ksp.annotations.Xpath
import kotlin.reflect.KClass

private const val KOLIBRIUM_CORE_PACKAGE_NAME = "io.kolibrium.core"
private const val SELENIUM_PACKAGE_NAME = "org.openqa.selenium"

public class KolibriumPageProcessor(private val codeGen: CodeGenerator, private val logger: KSPLogger) :
    SymbolProcessor {

    // process function returns a list of KSAnnotated objects, which represent symbols that
    // the processor can't currently process and need to be deferred to another round
    override fun process(resolver: Resolver): List<KSAnnotated> {
        // retrieve all class declarations annotated with KolibriumPage
        val symbols =
            resolver
                .getSymbolsWithAnnotation(KolibriumPage::class.java.name)
                .filterIsInstance<KSClassDeclaration>()

        // in case there are no symbols that can be processed — exit
        if (symbols.iterator().hasNext().not()) return emptyList()

        symbols.forEach { it.accept(KolibriumPageVisitor(), Unit) }

        // return deferred symbols that the processor can't process but in theory we have none
        return emptyList()
    }

    private inner class KolibriumPageVisitor : KSVisitorVoid() {

        @OptIn(ExperimentalKotlinPoetApi::class)
        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            if (classDeclaration.classKind != ClassKind.ENUM_CLASS) {
                logger.error(
                    "Only enum classes can be annotated with @KolibriumPage. " +
                        "Please make sure \"$classDeclaration\" is an enum class."
                )
            }

            if (classDeclaration.getEnumEntries().count() == 0) {
                logger.error("At least one enum shall be defined")
            }

            val className = classDeclaration.simpleName.asString()
            val typeBuilder = TypeSpec.classBuilder(className)
                .contextReceivers(ClassName(SELENIUM_PACKAGE_NAME, "WebDriver"))

            classDeclaration.getEnumEntries().forEach {
                it.accept(KolibriumEnumEntryVisitor(typeBuilder), Unit)
            }

            val pckName = classDeclaration.packageName.asString() + ".generated"
            val fileSpec = FileSpec.builder(pckName, className)
                .addType(typeBuilder.build())
                .addImport(KOLIBRIUM_CORE_PACKAGE_NAME, "result")
                .build()

            codeGen.createNewFile(
                Dependencies(false, classDeclaration.containingFile!!),
                fileSpec.packageName,
                fileSpec.name
            ).writer().use { writer -> fileSpec.writeTo(writer) }
        }

        private fun KSClassDeclaration.getEnumEntries(): Sequence<KSDeclaration> =
            declarations.filter { it.closestClassDeclaration()?.classKind == ClassKind.ENUM_ENTRY }
    }

    private inner class KolibriumEnumEntryVisitor(private val typeSpecBuilder: TypeSpec.Builder) : KSVisitorVoid() {

        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            val locatorAnnotations = listOf(
                io.kolibrium.ksp.annotations.ClassName::class,
                Css::class,
                Id::class,
                LinkText::class,
                Name::class,
                PartialLinkText::class,
                TagName::class,
                Xpath::class
            )

            val annotationsPresent = locatorAnnotations.filter {
                classDeclaration.getAnnotation(it) != null
            }
            val enumEntryName = classDeclaration.simpleName.asString()

            if (annotationsPresent.size > 1) {
                val message = "More than one locator annotation found on \"$enumEntryName\": " +
                    annotationsPresent.joinToString { "@" + it.simpleName }
                logger.error(message, classDeclaration)
            } else if (annotationsPresent.size == 1) {
                val locatorAnnotation = classDeclaration.getAnnotation(annotationsPresent[0])!!
                val collectToListValue = locatorAnnotation.getArgumentValue("collectToList").toBoolean()

                generateProperties(getClassNames(collectToListValue), enumEntryName) {
                    val locator =
                        locatorAnnotation.getArgumentValue("locator").ifEmpty { enumEntryName }
                    val locatorStrategyClassName = ClassName(
                        KOLIBRIUM_CORE_PACKAGE_NAME,
                        getLocatorStrategy(locatorAnnotation)
                    )
                    val className = if (collectToListValue) "WebElements" else "WebElement"
                    add("%T<$className>(\"$locator\")", locatorStrategyClassName)
                }
            } else { // fallback to idOrName
                generateProperties(getClassNames(), enumEntryName) {
                    add(
                        "%T<WebElement>(\"$enumEntryName\")",
                        ClassName(KOLIBRIUM_CORE_PACKAGE_NAME, "idOrName")
                    )
                }
            }
        }

        private fun generateProperties(
            className: Pair<ClassName, ClassName>,
            enumEntryName: String,
            block: CodeBlock.Builder.() -> Unit
        ) {
            val (leftClassName, rightClassName) = className
            val delegateTypeClassName = ClassName("arrow.core", "Either")
                .parameterizedBy(leftClassName, rightClassName)

            typeSpecBuilder.addProperties(
                listOf(
                    PropertySpec.builder(
                        "_$enumEntryName",
                        delegateTypeClassName
                    ).addModifiers(KModifier.PRIVATE)
                        .delegate(
                            CodeBlock.builder().apply {
                                block()
                            }.build()
                        ).build(),
                    PropertySpec.builder(
                        enumEntryName,
                        rightClassName
                    ).getter(
                        FunSpec.getterBuilder()
                            .addStatement("return %N.result()", "_$enumEntryName")
                            .build()
                    ).build()
                )
            )
        }

        private fun KSDeclaration.getAnnotation(klass: KClass<*>): KSAnnotation? = this.annotations.firstOrNull {
            it.shortName.asString() == klass.simpleName
        }

        private fun KSAnnotation.getArgumentValue(arg: String) =
            arguments.first { it.name!!.asString() == arg }.value.toString()

        private fun getClassNames(collectToListValue: Boolean = false): Pair<ClassName, ClassName> {
            val leftClassName = ClassName(KOLIBRIUM_CORE_PACKAGE_NAME, "Error")
            val rightClassName = if (collectToListValue) {
                ClassName(KOLIBRIUM_CORE_PACKAGE_NAME, "WebElements")
            } else {
                ClassName(SELENIUM_PACKAGE_NAME, "WebElement")
            }

            return leftClassName to rightClassName
        }

        private fun getLocatorStrategy(annotation: KSAnnotation) = annotation.toString()
            .removePrefix("@")
            .replaceFirstChar {
                it.lowercaseChar()
            }
    }
}

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

import com.google.devtools.ksp.closestClassDeclaration
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.samskivert.mustache.Mustache
import com.samskivert.mustache.Template
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.ExperimentalKotlinPoetApi
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import dev.kolibrium.ksp.annotations.ClassNames
import dev.kolibrium.ksp.annotations.CssSelector
import dev.kolibrium.ksp.annotations.CssSelectors
import dev.kolibrium.ksp.annotations.Id
import dev.kolibrium.ksp.annotations.Ids
import dev.kolibrium.ksp.annotations.LinkText
import dev.kolibrium.ksp.annotations.LinkTexts
import dev.kolibrium.ksp.annotations.Locators
import dev.kolibrium.ksp.annotations.Name
import dev.kolibrium.ksp.annotations.Names
import dev.kolibrium.ksp.annotations.PartialLinkText
import dev.kolibrium.ksp.annotations.PartialLinkTexts
import dev.kolibrium.ksp.annotations.TagName
import dev.kolibrium.ksp.annotations.TagNames
import dev.kolibrium.ksp.annotations.XPath
import dev.kolibrium.ksp.annotations.XPaths
import kotlin.reflect.KClass

private const val KOLIBRIUM_SELENIUM_PACKAGE_NAME = "dev.kolibrium.selenium"

/**
 * A symbol processor that generates a repository class for locators.
 *
 * This processor scans for enum classes annotated with [Locators] and generates classes containing `WebElement`
 * and `WebElements` properties, using locator delegate functions from the kolibrium-selenium module.
 *
 * The generated classes facilitate the access and management of locators in a type-safe manner.
 *
 * ### How It Works
 * The processor looks for all classes annotated with the `@Locators` annotation. It analyzes the enum entries
 * within these classes to identify the locator annotations applied to each entry. Based on this analysis, it generates
 * corresponding properties in the output class that utilize the appropriate locator delegate functions.
 *
 * ### Generated Output
 * For each annotated class, a corresponding class is generated in the `generated` package. This generated class
 * contains properties for each locator entry, enabling seamless access to the `WebElement` and `WebElements`
 * associated with each locator. The properties are defined using the appropriate locator delegate functions,
 * ensuring type safety and ease of use.
 */
public class LocatorsProcessor(
    private val codeGen: CodeGenerator,
    private val logger: KSPLogger,
) : SymbolProcessor {
    private val singleElementLocatorAnnotations =
        listOf(
            dev.kolibrium.ksp.annotations.ClassName::class,
            CssSelector::class,
            Id::class,
            LinkText::class,
            Name::class,
            PartialLinkText::class,
            TagName::class,
            XPath::class,
        )

    private val multipleElementLocatorAnnotations =
        listOf(
            ClassNames::class,
            CssSelectors::class,
            Ids::class,
            LinkTexts::class,
            Names::class,
            PartialLinkTexts::class,
            TagNames::class,
            XPaths::class,
        )

    // process function returns a list of KSAnnotated objects, which represent symbols that
    // the processor can't currently process and need to be deferred to another round
    override fun process(resolver: Resolver): List<KSAnnotated> {
        // retrieve all class declarations annotated with Locators
        val symbols =
            resolver
                .getSymbolsWithAnnotation(Locators::class.java.name)
                .filterIsInstance<KSClassDeclaration>()

        // in case there are no symbols that can be processed — exit
        if (symbols.iterator().hasNext().not()) return emptyList()

        symbols.forEach { it.accept(LocatorsVisitor(), Unit) }

        // return deferred symbols that the processor can't process but in theory we have none
        return emptyList()
    }

    private inner class LocatorsVisitor : KSVisitorVoid() {
        @OptIn(ExperimentalKotlinPoetApi::class)
        override fun visitClassDeclaration(
            classDeclaration: KSClassDeclaration,
            data: Unit,
        ) {
            validate(classDeclaration)

            val className = classDeclaration.simpleName.asString()

            val typeBuilder =
                TypeSpec
                    .classBuilder(className)
                    .contextReceivers(ClassName(SELENIUM_PACKAGE_NAME, "WebDriver"))

            classDeclaration.getEnumEntries().forEach {
                it.accept(EnumEntryVisitor(typeBuilder), Unit)
            }

            val fileSpec =
                FileSpec
                    .builder(classDeclaration.generatedPackageName, className)
                    .addType(typeBuilder.build())

            codeGen.writeToFile(classDeclaration, fileSpec)
        }

        private fun validate(classDeclaration: KSClassDeclaration) {
            if (classDeclaration.classKind != ClassKind.ENUM_CLASS) {
                logger.error(
                    """
                    Only enum classes can be annotated with @Locators. Please make sure "$classDeclaration" is an enum class.
                    """.trimIndent(),
                )
            }

            if (classDeclaration.getEnumEntries().count() == 0) {
                logger.error("At least one enum shall be defined in \"$classDeclaration\".")
            }
        }

        private fun KSClassDeclaration.getEnumEntries(): Sequence<KSDeclaration> =
            declarations.filter { it.closestClassDeclaration()?.classKind == ClassKind.ENUM_ENTRY }
    }

    private inner class EnumEntryVisitor(
        private val typeSpecBuilder: TypeSpec.Builder,
    ) : KSVisitorVoid() {
        override fun visitClassDeclaration(
            classDeclaration: KSClassDeclaration,
            data: Unit,
        ) {
            val locatorAnnotations =
                (singleElementLocatorAnnotations + multipleElementLocatorAnnotations).filter {
                    classDeclaration.getAnnotation(it) != null
                }
            val enumEntryName = classDeclaration.simpleName.asString()

            if (locatorAnnotations.size > 1) {
                val message =
                    "More than one locator annotation found on \"$enumEntryName\": " +
                        locatorAnnotations.joinToString { "@" + it.simpleName }
                logger.error(message, classDeclaration)
            }

            if (locatorAnnotations.size == 1) {
                val locatorAnnotation = classDeclaration.getAnnotation(locatorAnnotations.first())!!
                val locator =
                    (locatorAnnotation.getArgument("locator").value as String).ifEmpty { enumEntryName }
                val locatorStrategyClassName =
                    ClassName(
                        KOLIBRIUM_SELENIUM_PACKAGE_NAME,
                        getLocatorStrategy(locatorAnnotation),
                    )
                val delegateReturnType = getDelegateReturnType(locatorAnnotations.first())
                val mustacheTemplateParser = MustacheTemplateParser(locator)

                if (mustacheTemplateParser.templateVariables.isEmpty()) {
                    generateProperty(enumEntryName, delegateReturnType) {
                        val cacheLookup = (locatorAnnotation.getArgument("cacheLookup").value as Boolean)

                        if (cacheLookup) {
                            add(
                                "%T(%S)",
                                locatorStrategyClassName,
                                locator,
                            )
                        } else {
                            add(
                                "%T(locator = %S, cacheLookup = %L)",
                                locatorStrategyClassName,
                                locator,
                                false,
                            )
                        }
                    }
                } else {
                    generateDynamicLocatorFunction(
                        enumEntryName,
                        mustacheTemplateParser,
                        locatorStrategyClassName,
                        delegateReturnType,
                    )
                }
            } else { // fallback to idOrName
                generateProperty(enumEntryName, ClassName(SELENIUM_PACKAGE_NAME, "WebElement")) {
                    add(
                        "%T(%S)",
                        ClassName(KOLIBRIUM_SELENIUM_PACKAGE_NAME, "idOrName"),
                        enumEntryName,
                    )
                }
            }
        }

        private fun getDelegateReturnType(annotation: KClass<out Annotation>) =
            if (annotation in singleElementLocatorAnnotations) {
                ClassName(SELENIUM_PACKAGE_NAME, "WebElement")
            } else {
                ClassName(KOLIBRIUM_SELENIUM_PACKAGE_NAME, "WebElements")
            }

        private fun generateProperty(
            enumEntryName: String,
            delegateReturnType: ClassName,
            block: CodeBlock.Builder.() -> Unit,
        ) {
            typeSpecBuilder.addProperty(
                PropertySpec
                    .builder(
                        enumEntryName,
                        delegateReturnType,
                    ).delegate(
                        CodeBlock
                            .builder()
                            .apply {
                                block()
                            }.build(),
                    ).build(),
            )
        }

        private fun generateDynamicLocatorFunction(
            enumEntryName: String,
            mustacheTemplateParser: MustacheTemplateParser,
            locatorStrategyClassName: ClassName,
            delegateReturnType: ClassName,
        ) {
            typeSpecBuilder.addFunction(
                FunSpec
                    .builder(enumEntryName)
                    .addParameters(
                        mustacheTemplateParser.templateVariables.map { templateVariable ->
                            ParameterSpec.builder(templateVariable, String::class).build()
                        },
                    ).addCode(
                        CodeBlock.of(
                            """
                            val locator = %P
                            val element: WebElement by %T(locator)
                            return element
                            """.trimIndent(),
                            mustacheTemplateParser.visitedTexts.joinToString(separator = ""),
                            locatorStrategyClassName,
                        ),
                    ).returns(delegateReturnType)
                    .build(),
            )
        }

        private fun getLocatorStrategy(annotation: KSAnnotation) =
            annotation
                .toString()
                .removePrefix("@")
                .replaceFirstChar {
                    it.lowercaseChar()
                }
    }
}

private class MustacheTemplateParser(
    locator: String,
) {
    private val template: Template = Mustache.compiler().compile(locator)
    val templateVariables = mutableListOf<String>()
    val visitedTexts = mutableListOf<String>()

    init {
        template.visit(
            object : Mustache.Visitor {
                override fun visitText(text: String) {
                    visitedTexts.add(text)
                }

                override fun visitVariable(name: String) {
                    addToCollections(name)
                }

                override fun visitInclude(name: String): Boolean {
                    addToCollections(name)
                    return true
                }

                override fun visitSection(name: String): Boolean {
                    addToCollections(name)
                    return true
                }

                override fun visitInvertedSection(name: String): Boolean {
                    addToCollections(name)
                    return true
                }

                private fun addToCollections(name: String) {
                    visitedTexts.add("$" + name)
                    templateVariables.add(name)
                }
            },
        )
    }
}

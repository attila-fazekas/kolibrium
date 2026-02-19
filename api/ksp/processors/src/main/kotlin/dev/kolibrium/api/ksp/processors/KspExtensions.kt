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

import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import dev.kolibrium.api.ksp.annotations.DELETE
import dev.kolibrium.api.ksp.annotations.GET
import dev.kolibrium.api.ksp.annotations.PATCH
import dev.kolibrium.api.ksp.annotations.POST
import dev.kolibrium.api.ksp.annotations.PUT
import io.ktor.http.HttpMethod
import kotlin.reflect.KClass

internal fun KSAnnotated.hasAnnotation(annotationClass: KClass<*>): Boolean =
    annotations.any { ksAnnotation ->
        val resolvedQualifiedName =
            ksAnnotation.annotationType
                .resolve()
                .declaration
                .qualifiedName
                ?.asString()

        val expectedQualifiedName = annotationClass.qualifiedName ?: annotationClass.java.name

        resolvedQualifiedName == expectedQualifiedName ||
            ksAnnotation.shortName.asString() == annotationClass.simpleName
    }

internal fun KSAnnotated.hasAnnotation(qualifiedName: String): Boolean =
    annotations.any { ksAnnotation ->
        ksAnnotation.annotationType
            .resolve()
            .declaration
            .qualifiedName
            ?.asString() == qualifiedName
    }

internal fun KSAnnotated.hasTransientAnnotation(): Boolean = hasAnnotation(KOTLINX_SERIALIZATION_TRANSIENT)

internal fun KSAnnotated.hasJvmTransientOnly(): Boolean =
    hasAnnotation(KOTLIN_JVM_TRANSIENT) && !hasAnnotation(KOTLINX_SERIALIZATION_TRANSIENT)

internal fun KSAnnotated.getAnnotation(annotationClass: KClass<*>): KSAnnotation? =
    annotations.firstOrNull {
        it.annotationType
            .resolve()
            .declaration.qualifiedName
            ?.asString() == annotationClass.java.name
    }

internal fun KSAnnotation.getArgumentValue(argumentName: String): Any? =
    arguments
        .firstOrNull { it.name?.asString() == argumentName }
        ?.value

internal fun KSAnnotation.getKClassTypeArgument(name: String): KSType? =
    when (val value = getArgumentValue(name)) {
        is KSType -> value
        else -> null
    }

internal fun KSClassDeclaration.getHttpMethodAnnotations(): List<KSAnnotation> = annotations.filter { it.toHttpMethod() != null }.toList()

internal fun KSAnnotation.toHttpMethod(): HttpMethod? {
    val qName =
        annotationType
            .resolve()
            .declaration
            .qualifiedName
            ?.asString()
            ?: return null

    return when (qName) {
        GET::class.qualifiedName -> HttpMethod.Get
        POST::class.qualifiedName -> HttpMethod.Post
        PUT::class.qualifiedName -> HttpMethod.Put
        DELETE::class.qualifiedName -> HttpMethod.Delete
        PATCH::class.qualifiedName -> HttpMethod.Patch
        else -> null
    }
}

internal fun KSClassDeclaration.getClassName(): String = qualifiedName?.asString() ?: simpleName.asString()

internal fun String.isValidKotlinPackage(): Boolean = split('.').all { it.isValidKotlinIdentifier() }

internal fun String.isValidKotlinIdentifier(): Boolean {
    val regex = Regex("^[A-Za-z_][A-Za-z0-9_]*$")
    return regex.matches(this)
}

internal fun String.isValidHttpHeaderName(): Boolean {
    // RFC 7230: token = 1*tchar
    // tchar = "!" / "#" / "$" / "%" / "&" / "'" / "*" / "+" / "-" / "." /
    //         "0"-"9" / "A"-"Z" / "^" / "_" / "`" / "a"-"z" / "|" / "~"
    val regex = Regex("^[!#$%&'*+\\-.0-9A-Z^_`a-z|~]+$")
    return isNotEmpty() && regex.matches(this)
}

internal fun validatePathFormat(
    path: String,
    className: String,
    node: com.google.devtools.ksp.symbol.KSNode?,
    errors: MutableList<Diagnostic>,
) {
    if (path.contains("//")) {
        errors += Diagnostic("Path '$path' in $className contains empty segments (double slashes)", node)
    }
    if (path.contains('?')) {
        errors += Diagnostic("Path '$path' in $className must not contain query strings", node)
    }
    if (path.contains('#')) {
        errors += Diagnostic("Path '$path' in $className must not contain fragment identifiers", node)
    }

    // Check for unclosed or nested braces
    var braceDepth = 0
    for (char in path) {
        when (char) {
            '{' -> {
                braceDepth++
                if (braceDepth > 1) {
                    errors += Diagnostic("Path '$path' in $className contains nested braces", node)
                    return
                }
            }

            '}' -> {
                braceDepth--
                if (braceDepth < 0) {
                    errors += Diagnostic("Path '$path' in $className contains unmatched closing brace", node)
                    return
                }
            }
        }
    }
    if (braceDepth != 0) {
        errors += Diagnostic("Path '$path' in $className contains unclosed brace", node)
    }

    // Check for empty braces
    if (path.contains("{}")) {
        errors += Diagnostic("Path '$path' in $className contains empty braces", node)
    }

    // Check for whitespace inside braces
    val whitespaceInBraces = Regex("\\{[^}]*\\s[^}]*}")
    if (whitespaceInBraces.containsMatchIn(path)) {
        errors += Diagnostic("Path '$path' in $className contains whitespace inside path variable braces", node)
    }

    // Check for duplicate path variable names
    val variableNames = Regex("\\{([^}]+)}").findAll(path).map { it.groupValues[1].trim() }.toList()
    val duplicates = variableNames.groupBy { it }.filter { it.value.size > 1 }.keys
    if (duplicates.isNotEmpty()) {
        errors +=
            Diagnostic(
                "Path '$path' in $className contains duplicate path variable(s): ${duplicates.joinToString(", ")}",
                node,
            )
    }
}

internal fun extractPathVariables(path: String): PathVariables {
    val rawMatches = Regex("\\{([^}]+)}").findAll(path).map { it.groupValues[1] }.toList()
    val validNames = rawMatches.filter { it.isValidKotlinIdentifier() }.toSet()
    val invalidNames = rawMatches.filterNot { it.isValidKotlinIdentifier() }.toSet()
    return PathVariables(names = validNames, invalidNames = invalidNames)
}

internal fun KSClassDeclaration.toFunctionName(): String {
    val className = simpleName.asString()
    return deriveFunctionName(className)
}

internal fun deriveFunctionName(className: String): String {
    val withoutRequest = className.removeSuffix("Request")
    return if (withoutRequest.isEmpty()) {
        ""
    } else {
        withoutRequest.replaceFirstChar { it.lowercase() }
    }
}

internal fun extractGroupByApiPrefix(path: String): String {
    val trimmedPath = path.trimStart('/')
    if (trimmedPath.isEmpty()) return "root"

    val firstSegment = trimmedPath.substringBefore('/')
    return if (firstSegment.startsWith("{")) {
        "root"
    } else {
        firstSegment
    }
}

internal fun groupRequestsByPrefix(requests: List<RequestClassInfo>): Map<String, List<RequestClassInfo>> =
    requests.groupBy { extractGroupByApiPrefix(it.path) }

/**
 * Reads a Boolean property value from a class declaration by parsing the source file.
 *
 * Since KSP doesn't provide direct access to property initializer values, this function
 * reads the source file and parses the property declaration to extract the Boolean value.
 *
 * @param propertyName the name of the Boolean property to read
 * @param defaultValue the value to return if the property is not declared in the class
 * @return the Boolean value of the property, or [defaultValue] if not found
 */
internal fun KSClassDeclaration.readBooleanProperty(
    propertyName: String,
    defaultValue: Boolean,
): Boolean {
    // First check if the property is declared in this class
    val property =
        getDeclaredProperties().find { it.simpleName.asString() == propertyName }
            ?: return defaultValue

    // Read the source file to find the property value
    val sourceFile = property.containingFile ?: return defaultValue
    val filePath = sourceFile.filePath
    val sourceText =
        try {
            java.io.File(filePath).readText()
        } catch (_: Exception) {
            return defaultValue
        }

    // Find the property declaration - supports two styles:
    // 1. Direct assignment: override val propertyName = false
    //                       override val propertyName: Boolean = false
    // 2. Getter syntax:     override val propertyName: Boolean
    //                           get() = false

    // Pattern for direct assignment (= true/false on same line as property declaration)
    val directAssignmentPattern = Regex("""(?:override\s+)?val\s+$propertyName(?:\s*:\s*Boolean)?\s*=\s*(true|false)""")
    val directMatch = directAssignmentPattern.find(sourceText)
    if (directMatch != null) {
        return directMatch.groupValues[1].toBoolean()
    }

    // Pattern for getter syntax (get() = true/false, possibly on next line)
    // First find the property declaration, then look for its getter
    val getterPattern = Regex("""(?:override\s+)?val\s+$propertyName\s*:\s*Boolean\s*\n\s*get\(\)\s*=\s*(true|false)""")
    val getterMatch = getterPattern.find(sourceText)
    if (getterMatch != null) {
        return getterMatch.groupValues[1].toBoolean()
    }

    return defaultValue
}

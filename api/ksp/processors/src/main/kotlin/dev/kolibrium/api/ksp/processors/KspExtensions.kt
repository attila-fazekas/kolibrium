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

import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import dev.kolibrium.api.ksp.annotations.DELETE
import dev.kolibrium.api.ksp.annotations.GET
import dev.kolibrium.api.ksp.annotations.Header
import dev.kolibrium.api.ksp.annotations.PATCH
import dev.kolibrium.api.ksp.annotations.POST
import dev.kolibrium.api.ksp.annotations.PUT
import io.ktor.http.HttpMethod
import kotlin.reflect.KClass

private fun KClass<*>.resolvedName(): String = qualifiedName ?: java.name

internal fun KSAnnotated.hasAnnotation(annotationClass: KClass<*>): Boolean =
    annotations.any { ksAnnotation ->
        ksAnnotation.annotationType
            .resolve()
            .declaration
            .qualifiedName
            ?.asString() == annotationClass.resolvedName()
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
            ?.asString() == annotationClass.resolvedName()
    }

internal fun KSAnnotation.getArgumentValue(argumentName: String): Any? =
    arguments
        .firstOrNull { it.name?.asString() == argumentName }
        ?.value

internal fun KSAnnotation?.getBooleanArg(
    name: String,
    default: Boolean,
): Boolean = this?.getArgumentValue(name) as? Boolean ?: default

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

private val KOTLIN_HARD_KEYWORDS: Set<String> =
    setOf(
        "as",
        "break",
        "class",
        "continue",
        "do",
        "else",
        "false",
        "for",
        "fun",
        "if",
        "in",
        "interface",
        "is",
        "null",
        "object",
        "package",
        "return",
        "super",
        "this",
        "throw",
        "true",
        "try",
        "typealias",
        "typeof",
        "val",
        "var",
        "when",
        "while",
    )

internal fun String.isValidKotlinIdentifier(): Boolean {
    val regex = Regex("^[A-Za-z_][A-Za-z0-9_]*$")
    return regex.matches(this) && this !in KOTLIN_HARD_KEYWORDS
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
    requestClass: KSClassDeclaration,
    errors: MutableList<Diagnostic>,
): Boolean {
    val className = requestClass.simpleName.asString()
    val initialErrorCount = errors.size

    if (!path.startsWith("/")) {
        errors += Diagnostic("Path '$path' in $className must start with '/'", requestClass)
    }

    if (path.contains("//")) {
        errors += Diagnostic("Path '$path' in $className contains empty segments (double slashes)", requestClass)
    }

    if (path.contains("{}")) {
        errors += Diagnostic("Path '$path' in $className contains empty braces", requestClass)
    }

    // Nested braces: /users/{{id}}
    if (Regex("\\{[^}]*\\{").containsMatchIn(path)) {
        errors += Diagnostic("Path '$path' in $className contains nested braces", requestClass)
    }

    // Reversed braces: /users/}id{  — match }...{ within the same segment (no / between)
    if (Regex("}[^{/]*\\{").containsMatchIn(path)) {
        errors += Diagnostic("Path '$path' in $className contains reversed braces", requestClass)
    }

    // Unclosed/mismatched braces
    val openCount = path.count { it == '{' }
    val closeCount = path.count { it == '}' }
    if (openCount != closeCount) {
        errors += Diagnostic("Path '$path' in $className has mismatched braces", requestClass)
    }

    return errors.size == initialErrorCount
}

internal fun extractPathVariables(path: String): PathVariables {
    val rawMatches = Regex("\\{([^}]+)}").findAll(path).map { it.groupValues[1] }.toList()
    val invalidNames = rawMatches.filterNot { it.isValidKotlinIdentifier() }.toSet()
    val duplicateNames =
        rawMatches
            .groupingBy { it }
            .eachCount()
            .filter { it.value > 1 }
            .keys
    val validNames = rawMatches.filter { it.isValidKotlinIdentifier() }.toSet() - duplicateNames
    return PathVariables(names = validNames, invalidNames = invalidNames, duplicateNames = duplicateNames)
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

/**
 * Extracts the first literal (non-path-variable) path segment to use as a group name
 * for ByPrefix client grouping.
 *
 * Returns `"root"` as the fallback group name when there is no literal segment
 * (e.g., path is `"/"`, `"/{id}"`, or `"/{a}/{b}"`).
 */
internal fun extractGroupByApiPrefix(path: String): String {
    val segments = path.trimStart('/').split('/').filter { it.isNotEmpty() }
    val firstLiteralSegment = segments.firstOrNull { !it.startsWith("{") }
    return firstLiteralSegment ?: ROOT_GROUP_NAME
}

internal fun extractHeaderName(property: KSPropertyDeclaration): String? {
    val annotation = property.getAnnotation(Header::class) ?: return null
    val name = annotation.getArgumentValue("name") as? String
    return if (name.isNullOrBlank()) null else name
}

internal fun groupRequestsByPrefix(requests: List<RequestClassInfo>): Map<String, List<RequestClassInfo>> =
    requests.groupBy { extractGroupByApiPrefix(it.path) }

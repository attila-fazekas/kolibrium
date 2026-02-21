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

        resolvedQualifiedName == expectedQualifiedName
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
    className: String,
    node: com.google.devtools.ksp.symbol.KSNode?,
    errors: MutableList<Diagnostic>,
) {
    if (path.contains("//")) {
        errors += Diagnostic("Path '$path' in $className contains empty segments (double slashes)", node)
    }

    if (path.contains("{}")) {
        errors += Diagnostic("Path '$path' in $className contains empty braces", node)
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

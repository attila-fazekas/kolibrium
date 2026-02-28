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
import dev.kolibrium.api.ksp.annotations.PATCH
import dev.kolibrium.api.ksp.annotations.POST
import dev.kolibrium.api.ksp.annotations.PUT
import io.ktor.http.HttpMethod
import jdk.internal.joptsimple.util.RegexMatcher.regex
import jdk.internal.net.http.common.Log.errors
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

internal fun KSAnnotation.getKClassTypeArgument(name: String): KSType? = getArgumentValue(name) as? KSType

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
    val regex = Regex("""^[!#$%&'*+\-.0-9A-Z^_`a-z|~]+$""")
    return isNotEmpty() && regex.matches(this)
}

internal fun extractPathVariables(path: String): PathVariables {
    val rawMatches = Regex("""\{([^}]+)}""").findAll(path).map { it.groupValues[1] }.toList()
    val invalidNames = rawMatches.filterNot { it.isValidKotlinIdentifier() }.toSet()
    val duplicateNames =
        rawMatches
            .groupingBy { it }
            .eachCount()
            .filter { it.value > 1 }
            .keys
    val validNames = rawMatches.toSet() - invalidNames - duplicateNames
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

internal fun getResultTypeName(info: RequestClassInfo): String = info.endpointName + "Result"

internal fun groupRequestsByPrefix(requests: List<RequestClassInfo>): Map<String, List<RequestClassInfo>> =
    requests.groupBy { extractGroupByApiPrefix(it.path) }

/**
 * Extracts the first literal (non-path-variable) path segment to use as a group name
 * for ByPrefix client grouping.
 *
 * Returns `"root"` as the fallback group name when there is no literal segment
 * (e.g., path is `"/"`, `"/{id}"`, or `"/{a}/{b}"`).
 */
internal fun extractGroupByApiPrefix(path: String): String =
    path.trimStart('/').split('/').firstOrNull { it.isNotEmpty() && !it.startsWith('{') } ?: ROOT_GROUP_NAME

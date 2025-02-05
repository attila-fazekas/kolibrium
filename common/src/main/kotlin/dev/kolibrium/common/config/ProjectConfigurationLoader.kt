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

package dev.kolibrium.common.config

import dev.kolibrium.common.InternalKolibriumApi
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.reflect.KClass
import kotlin.text.Typography.bullet

private val logger = KotlinLogging.logger { }

@InternalKolibriumApi
public interface ProjectConfiguration

@InternalKolibriumApi
public object ProjectConfigurationLoader {
    /**
     * Load configuration for a specific configuration type.
     * Only Kotlin objects implementing ProjectConfiguration are supported.
     * Regular classes (including those with companion objects) are not supported
     * and will result in a ProjectConfigException.
     *
     * @param kClass The Kotlin class type representing the ProjectConfiguration interface to be loaded
     * @return The instantiated configuration object of type T, or null if no implementation is found
     * @throws ProjectConfigurationException if:
     * - Multiple configuration objects are found
     * - The implementation is not a Kotlin object (regular classes are not allowed)
     * - There's an instantiation error
     */
    @InternalKolibriumApi
    public fun <T : ProjectConfiguration> loadConfiguration(kClass: KClass<T>): T? {
        val implementingObjects = findImplementingObjects(kClass)

        return when {
            implementingObjects.isEmpty() -> null
            implementingObjects.size == 1 -> {
                val configObject = implementingObjects.first()
                val configObjectName = configObject.qualifiedName
                logger.info { "Loading project configuration from $configObjectName" }
                try {
                    instantiateObject(configObject)
                } catch (e: Exception) {
                    throw ProjectConfigurationException("Failed to instantiate configuration $configObjectName", e)
                }
            }
            else -> {
                val objectNames = implementingObjects.joinToString(separator = "\n") { " $bullet ${it.qualifiedName}" }
                throw ProjectConfigurationException(
                    """More than one project configuration implementing ${kClass.simpleName} was found:
                       |$objectNames
                       |Please make sure that only one object implements  ${kClass.simpleName}.
                       |
                    """.trimMargin(),
                )
            }
        }
    }

    /**
     * Find all implementing Kotlin objects for a specific configuration type.
     * Only Kotlin objects are considered valid implementations.
     * Regular classes implementing the interface will be rejected with ProjectConfigException.
     *
     * @throws ProjectConfigurationException if:
     * - There's an error loading implementations
     * - An implementation is not a Kotlin object
     */
    @InternalKolibriumApi
    private fun <T : ProjectConfiguration> findImplementingObjects(kClass: KClass<T>): List<KClass<out T>> {
        val implementingObjects = mutableListOf<KClass<out T>>()
        val serviceFileName = "META-INF/services/${kClass.java.name}"

        Thread.currentThread().contextClassLoader.getResources(serviceFileName).asSequence().forEach { url ->
            try {
                url.openStream().bufferedReader().use { reader ->
                    reader
                        .lineSequence()
                        .map { it.trim() }
                        .filter { it.isNotEmpty() && !it.startsWith("#") }
                        .forEach { className ->
                            try {
                                @Suppress("UNCHECKED_CAST")
                                val configClass = Class.forName(className).kotlin as KClass<out T>
                                // This will throw exception if not a valid object
                                verifyKotlinObject(configClass, className)
                                // Only add to the list if verification passes
                                implementingObjects.add(configClass)
                            } catch (e: Exception) {
                                if (e is IllegalAccessException && e.message?.contains("cannot access a member") == true) {
                                    throw ProjectConfigurationException(
                                        """Configuration object $className must be public.
                                           |Please make sure that your object declaration has public visibility.
                                        """.trimMargin(),
                                    )
                                }
                                throw e
                            }
                        }
                }
            } catch (e: Exception) {
                when (e) {
                    is ProjectConfigurationException -> throw e
                    else -> logger.warn { "Failed to read service file $url: ${e.message}" }
                }
            }
        }

        return implementingObjects
    }

    /**
     * Verifies that the given class is a Kotlin object declaration.
     * This method specifically checks that the class is a Kotlin object (not a regular class)
     * using Kotlin's reflection API.
     *
     * @throws ProjectConfigurationException if the class is not a Kotlin object
     */
    private fun verifyKotlinObject(
        configClass: KClass<*>,
        className: String,
    ) {
        if (!configClass.isCompanion && configClass.objectInstance == null) {
            throw ProjectConfigurationException(
                """Configuration class $className must be a Kotlin object, found a regular class.
                   |Regular classes (including those with companion objects) are not supported.
                   |Please convert your implementation to a Kotlin object:
                   |
                   |object $className : ProjectConfiguration {
                   |    // your implementation
                   |}
                """.trimMargin(),
            )
        }
    }

    private fun <T : ProjectConfiguration> instantiateObject(kClass: KClass<out T>): T = kClass.objectInstance as T
}

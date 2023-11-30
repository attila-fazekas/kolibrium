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

package io.kolibrium.junit.config

import io.github.oshai.kotlinlogging.KotlinLogging
import io.kolibrium.core.Browser
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.edge.EdgeDriver
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.safari.SafariDriver
import java.util.*
import kotlin.text.Typography.bullet

private val logger = KotlinLogging.logger { }

public abstract class AbstractProjectConfiguration {

    public open val defaultBrowser: Browser? = null

    public open val keepBrowserOpen: Boolean? = null

    public open val verbose: Boolean? = null

    public open val chromeDriver: (() -> ChromeDriver)? = null

    public open val safariDriver: (() -> SafariDriver)? = null

    public open val edgeDriver: (() -> EdgeDriver)? = null

    public open val firefoxDriver: (() -> FirefoxDriver)? = null
}

internal fun actualConfig(): AbstractProjectConfiguration {
    val config = loadProjectConfigFromClassName()
    return config?.applyConfig() ?: ProjectConfiguration()
}

internal fun loadProjectConfigFromClassName(): AbstractProjectConfiguration? {
    val classes = findImplementingClasses()

    return if (classes.isEmpty()) {
        null
    } else if (classes.size == 1) {
        val configFileName = classes.first().name
        logger.info { "Loading project configuration from $configFileName" }
        Class.forName(configFileName).getDeclaredConstructor().newInstance() as AbstractProjectConfiguration?
    } else {
        val classNames = classes.map {
            "$bullet ${it.name}"
        }.joinToString(separator = "\n")

        throw ProjectConfigurationException(
            """
                More than one project configuration found in the following classes: $classNames
            """.trimIndent()
        )
    }
}

@SuppressWarnings("SwallowedException")
internal fun findImplementingClasses(): List<Class<out AbstractProjectConfiguration>> {
    val implementingClasses = mutableListOf<Class<out AbstractProjectConfiguration>>()

    val serviceLoader = ServiceLoader.load(AbstractProjectConfiguration::class.java)

    try {
        for (provider in serviceLoader) {
            implementingClasses.add(provider::class.java)
        }
    } catch (e: ServiceConfigurationError) {
        throw ProjectConfigurationException(
            """
                Implementing class must have a public no argument constructor.
            """.trimIndent()
        )
    }

    return implementingClasses
}

internal fun AbstractProjectConfiguration.applyConfig(): AbstractProjectConfiguration = apply {
    this.keepBrowserOpen?.let { ProjectConfiguration.keepBrowserOpen = it }
    this.chromeDriver?.let { ProjectConfiguration.chromeDriver = it }
    this.edgeDriver?.let { ProjectConfiguration.edgeDriver = it }
    this.firefoxDriver?.let { ProjectConfiguration.firefoxDriver = it }
    this.safariDriver?.let { ProjectConfiguration.safariDriver = it }
    this.defaultBrowser?.let { ProjectConfiguration.defaultBrowser = it }
    this.verbose?.let { ProjectConfiguration.verbose = it }
}

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

package dev.kolibrium.junit

import dev.kolibrium.core.Browser
import dev.kolibrium.core.Browser.CHROME
import dev.kolibrium.core.Browser.EDGE
import dev.kolibrium.core.Browser.FIREFOX
import dev.kolibrium.core.Browser.SAFARI
import dev.kolibrium.junit.config.AbstractProjectConfiguration
import dev.kolibrium.junit.config.ProjectConfiguration
import dev.kolibrium.junit.config.actualConfig
import io.github.oshai.kotlinlogging.KotlinLogging
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolutionException
import org.junit.jupiter.api.extension.ParameterResolver
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.edge.EdgeDriver
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.remote.RemoteWebDriver
import org.openqa.selenium.safari.SafariDriver
import kotlin.reflect.KClass
import kotlin.text.Typography.bullet

private const val KOLIBRIUM_STORE = "KOLIBRIUM_STORE"

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
@ExtendWith(KolibriumExtension::class)
public annotation class Kolibrium

private val logger = KotlinLogging.logger { }

public class KolibriumExtension(private val driver: (() -> WebDriver)? = null) : ParameterResolver, AfterEachCallback {
    private val actualConfig: AbstractProjectConfiguration by lazy { actualConfig() }

    override fun supportsParameter(
        paramCtx: ParameterContext,
        extCtx: ExtensionContext,
    ): Boolean {
        val driver = paramCtx.parameter.type
        val supportedDriverClasses =
            listOf(
                WebDriver::class.java,
                ChromeDriver::class.java,
                SafariDriver::class.java,
                EdgeDriver::class.java,
                FirefoxDriver::class.java,
            )

        if (!supportedDriverClasses.contains(driver)) {
            throw ParameterResolutionException(
                """
                    |
                    |${driver.simpleName} class cannot be injected into ${extCtx.testClass.get().name} tests.
                    |Only the following classes are supported:
                    |$bullet WebDriver
                    |$bullet ChromeDriver
                    |$bullet SafariDriver
                    |$bullet EdgeDriver
                    |$bullet FirefoxDriver
                """.trimMargin(),
            )
        }

        return true
    }

    override fun resolveParameter(
        paramCtx: ParameterContext,
        extCtx: ExtensionContext,
    ): WebDriver {
        val constructorDriverClass = paramCtx.parameter.type
        val currentTestClass = extCtx.testClass.get().name

        val driver =
            this.driver?.let {
                val instantiatedDriver = it()
                logger.trace {
                    "${instantiatedDriver::class.simpleName} created with Programmatic Extension Registration in" +
                        " $currentTestClass class"
                }
                instantiatedDriver
            } ?: run {
                // defaultBrowser was overridden in configuration
                actualConfig.defaultBrowser?.let {
                    val defaultBrowserDriverClass = it.driverClass().java
                    if (RemoteWebDriver::class.java.isAssignableFrom(constructorDriverClass) &&
                        constructorDriverClass != defaultBrowserDriverClass
                    ) {
                        createDriver(
                            Browser.valueOf(constructorDriverClass.simpleName.substringBefore("Driver").uppercase()),
                        )
                    } else {
                        createDriver(
                            Browser.valueOf(
                                defaultBrowserDriverClass.simpleName
                                    .substringBefore("Driver").uppercase(),
                            ),
                        )
                    }
                } ?: run { // otherwise get the driver from default config
                    if (RemoteWebDriver::class.java.isAssignableFrom(constructorDriverClass) &&
                        constructorDriverClass != ProjectConfiguration.defaultBrowser.driverClass()::class.java
                    ) {
                        createDriver(
                            Browser.valueOf(constructorDriverClass.simpleName.substringBefore("Driver").uppercase()),
                        )
                    } else {
                        createDriver(ProjectConfiguration.defaultBrowser)
                    }
                }
            }

        extCtx.store().put(Thread.currentThread().threadId(), driver)
        driver.get(actualConfig.baseUrl ?: ProjectConfiguration.baseUrl)
        return driver
    }

    private fun Browser.driverClass(): KClass<out WebDriver> {
        return when (this) {
            CHROME -> ChromeDriver::class
            SAFARI -> SafariDriver::class
            EDGE -> EdgeDriver::class
            FIREFOX -> FirefoxDriver::class
        }
    }

    override fun afterEach(extCtx: ExtensionContext) {
        val driver: WebDriver = extCtx.store().get(Thread.currentThread().threadId()) as WebDriver
        if (!ProjectConfiguration.keepBrowserOpen) {
            driver.quit()
        }
    }

    private fun createDriver(browser: Browser): WebDriver =
        when (browser) {
            CHROME -> actualConfig.chromeDriver ?: ProjectConfiguration.chromeDriver
            SAFARI -> actualConfig.safariDriver ?: ProjectConfiguration.safariDriver
            EDGE -> actualConfig.edgeDriver ?: ProjectConfiguration.edgeDriver
            FIREFOX -> actualConfig.firefoxDriver ?: ProjectConfiguration.firefoxDriver
        }()

    private fun ExtensionContext.store() = getStore(ExtensionContext.Namespace.create(KOLIBRIUM_STORE))
}

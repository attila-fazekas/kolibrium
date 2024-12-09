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
import dev.kolibrium.junit.configuration.AbstractJUnitProjectConfiguration
import dev.kolibrium.junit.configuration.DefaultJUnitProjectConfiguration
import dev.kolibrium.junit.configuration.JUnitProjectConfiguration
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

/**
 * Marks a test class to be managed by the Kolibrium JUnit extension.
 *
 * This annotation enables automatic WebDriver injection and lifecycle management for Selenium WebDriver instances
 * in JUnit 5 test classes. When applied to a test class, it automatically registers [KolibriumExtension].
 *
 * Example usage:
 * ```kotlin
 * @Kolibrium
 * class MySeleniumTest {
 *     @Test
 *     fun `test with chrome driver`(driver: ChromeDriver) {
 *         // Test implementation
 *     }
 * }
 * ```
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
@ExtendWith(KolibriumExtension::class)
public annotation class Kolibrium

private val logger = KotlinLogging.logger { }

/**
 * JUnit 5 extension for managing WebDriver instances in Selenium tests.
 *
 * This extension provides automatic WebDriver injection and lifecycle management for Selenium tests.
 * It supports injection of WebDriver, ChromeDriver, SafariDriver, EdgeDriver, and FirefoxDriver instances.
 *
 * Features:
 * - Automatic WebDriver creation and cleanup
 * - Configurable browser selection
 * - Support for custom WebDriver initialization
 * - Automatic navigation to base URL
 * - Thread-safe WebDriver management
 *
 * Example usage with constructor-based configuration:
 * ```kotlin
 * @ExtendWith(KolibriumExtension::class)
 * class MyTest {
 *     @Test
 *     fun `test with chrome`(driver: ChromeDriver) {
 *         // Test implementation
 *     }
 * }
 * ```
 *
 * @param driver Optional factory function to create custom WebDriver instances.
 *               When provided, this function takes precedence over other configuration methods.
 *
 * @throws ParameterResolutionException if an unsupported driver type is requested for injection.
 */
public class KolibriumExtension(
    private val driver: (() -> WebDriver)? = null,
) : ParameterResolver,
    AfterEachCallback {
    private val actualConfig: AbstractJUnitProjectConfiguration by lazy { JUnitProjectConfiguration.actualConfig() }

    /**
     * Checks if the requested parameter type is supported for injection.
     *
     * @param paramCtx The parameter context containing information about the parameter to be injected.
     * @param extCtx The extension context.
     * @return true if the parameter type is supported, false otherwise.
     * @throws ParameterResolutionException if an unsupported driver type is requested.
     */
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

    /**
     * Creates and configures a WebDriver instance for injection.
     *
     * The driver is created based on the following precedence:
     * 1. Constructor-provided driver factory
     * 2. Project configuration override
     * 3. Default configuration
     *
     * The created driver automatically navigates to the configured base URL.
     *
     * @param paramCtx The parameter context containing information about the parameter to be injected.
     * @param extCtx The extension context.
     * @return A configured WebDriver instance.
     */
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
                                    .substringBefore("Driver")
                                    .uppercase(),
                            ),
                        )
                    }
                } ?: run {
                    // otherwise get the driver from default config
                    if (RemoteWebDriver::class.java.isAssignableFrom(constructorDriverClass) &&
                        constructorDriverClass != DefaultJUnitProjectConfiguration.defaultBrowser.driverClass()::class.java
                    ) {
                        createDriver(
                            Browser.valueOf(constructorDriverClass.simpleName.substringBefore("Driver").uppercase()),
                        )
                    } else {
                        createDriver(DefaultJUnitProjectConfiguration.defaultBrowser)
                    }
                }
            }

        extCtx.store().put(Thread.currentThread().threadId(), driver)
        driver.get(actualConfig.baseUrl ?: DefaultJUnitProjectConfiguration.baseUrl)
        return driver
    }

    private fun Browser.driverClass(): KClass<out WebDriver> =
        when (this) {
            CHROME -> ChromeDriver::class
            SAFARI -> SafariDriver::class
            EDGE -> EdgeDriver::class
            FIREFOX -> FirefoxDriver::class
        }

    /**
     * Performs cleanup after each test execution.
     *
     * Quits the WebDriver instance unless `keepBrowserOpen` is set to true in the configuration.
     *
     * @param extCtx The extension context.
     */
    override fun afterEach(extCtx: ExtensionContext) {
        val driver: WebDriver = extCtx.store().get(Thread.currentThread().threadId()) as WebDriver
        val keepBrowserOpen = actualConfig.keepBrowserOpen ?: DefaultJUnitProjectConfiguration.keepBrowserOpen

        if (!keepBrowserOpen) {
            driver.quit()
        }
    }

    private fun createDriver(browser: Browser): WebDriver =
        when (browser) {
            CHROME -> actualConfig.chromeDriver ?: DefaultJUnitProjectConfiguration.chromeDriver
            SAFARI -> actualConfig.safariDriver ?: DefaultJUnitProjectConfiguration.safariDriver
            EDGE -> actualConfig.edgeDriver ?: DefaultJUnitProjectConfiguration.edgeDriver
            FIREFOX -> actualConfig.firefoxDriver ?: DefaultJUnitProjectConfiguration.firefoxDriver
        }()

    private fun ExtensionContext.store() = getStore(ExtensionContext.Namespace.create(KOLIBRIUM_STORE))
}

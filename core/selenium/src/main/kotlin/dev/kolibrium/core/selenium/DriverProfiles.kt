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

package dev.kolibrium.core.selenium

import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.edge.EdgeDriver
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.safari.SafariDriver

/**
 * Defines a factory for producing a Selenium [WebDriver].
 *
 * Implementations encapsulate how a concrete browser driver is created and configured
 * (e.g. capabilities, or options) while keeping call sites simple.
 */
public fun interface DriverProfile {
    /**
     * Create and return a new [WebDriver] instance.
     *
     * Implementations are expected to return a fresh driver (not shared) and leave ownership
     * of its lifecycle to the caller.
     */
    public fun getDriver(): WebDriver
}

/**
 * Default Chrome driver profile.
 *
 * Creates a vanilla [ChromeDriver] with Selenium defaults. Override or provide your own profile
 * if you need custom options or capabilities.
 */
public object DefaultChromeDriverProfile : DriverProfile {
    override fun getDriver(): ChromeDriver = ChromeDriver()
}

/**
 * Default Microsoft Edge driver profile.
 *
 * Creates a vanilla [EdgeDriver] with Selenium defaults. Override or provide your own profile
 * if you need custom options or capabilities.
 */
public object DefaultEdgeDriverProfile : DriverProfile {
    override fun getDriver(): EdgeDriver = EdgeDriver()
}

/**
 * Default Firefox driver profile.
 *
 * Creates a vanilla Firefox driver with Selenium defaults. Override or provide your own profile
 * if you need custom options or capabilities.
 */
public object DefaultFirefoxDriverProfile : DriverProfile {
    override fun getDriver(): FirefoxDriver = FirefoxDriver()
}

/**
 * Default Safari driver profile.
 *
 * Creates a vanilla [SafariDriver] with Selenium defaults. Override or provide your own profile
 * if you need custom options or capabilities.
 */
public object DefaultSafariDriverProfile : DriverProfile {
    override fun getDriver(): SafariDriver = SafariDriver()
}

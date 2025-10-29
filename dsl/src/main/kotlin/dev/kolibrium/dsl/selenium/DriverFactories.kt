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

package dev.kolibrium.dsl.selenium

import dev.kolibrium.dsl.selenium.creation.Arguments.Chrome.headless
import dev.kolibrium.dsl.selenium.creation.Arguments.Chrome.incognito
import dev.kolibrium.dsl.selenium.creation.Arguments.Edge.inPrivate
import dev.kolibrium.dsl.selenium.creation.chromeDriver
import dev.kolibrium.dsl.selenium.creation.edgeDriver
import dev.kolibrium.dsl.selenium.creation.firefoxDriver
import dev.kolibrium.dsl.selenium.creation.safariDriver
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.firefox.FirefoxDriver
import dev.kolibrium.dsl.selenium.creation.Arguments.Edge.headless as edgeHeadless
import dev.kolibrium.dsl.selenium.creation.Arguments.Firefox.headless as firefoxHeadless
import dev.kolibrium.dsl.selenium.creation.Arguments.Firefox.incognito as firefoxIncognito

/**
 * Factory function that creates a new WebDriver instance for use by Kolibrium DSL helpers such as [dev.kolibrium.dsl.selenium.webTest].
 *
 * Prefer the predefined factories in dev.kolibrium.dsl.selenium.DriverFactories for common setups
 * (e.g., headlessChrome, incognitoFirefox).
 */
public typealias DriverFactory = () -> WebDriver

/**
 * Create a [DriverFactory] that launches a Selenium-backed Chrome session using default options.
 */
public fun chrome(): DriverFactory = { ChromeDriver() }

/**
 * Create a [DriverFactory] that launches a Selenium-backed Firefox session using default options.
 */
public fun firefox(): DriverFactory = { FirefoxDriver() }

/** A DriverFactory that creates a plain ChromeDriver with default Kolibrium options. */
public val chrome: DriverFactory = { chromeDriver { } }

/** A DriverFactory that creates a plain FirefoxDriver with default Kolibrium options. */
public val firefox: DriverFactory = { firefoxDriver { } }

/** A DriverFactory that creates a plain EdgeDriver with default Kolibrium options. */
public val edge: DriverFactory = { edgeDriver { } }

/** A DriverFactory that creates a plain SafariDriver with default Kolibrium options. */
public val safari: DriverFactory = { safariDriver { } }

/** Chrome in headless mode. Useful for CI environments. */
public val headlessChrome: DriverFactory = {
    chromeDriver {
        options {
            arguments {
                +headless
            }
        }
    }
}

/** Chrome in incognito mode. */
public val incognitoChrome: DriverFactory = {
    chromeDriver {
        options {
            arguments {
                +incognito
            }
        }
    }
}

/** Firefox in headless mode. Useful for CI environments. */
public val headlessFirefox: DriverFactory = {
    firefoxDriver {
        options {
            arguments {
                +firefoxHeadless
            }
        }
    }
}

/** Firefox in private/incognito mode. */
public val incognitoFirefox: DriverFactory = {
    firefoxDriver {
        options {
            arguments {
                +firefoxIncognito
            }
        }
    }
}

/** Microsoft Edge in headless mode. Useful for CI environments. */
public val headlessEdge: DriverFactory = {
    edgeDriver {
        options {
            arguments {
                +edgeHeadless
            }
        }
    }
}

/** Microsoft Edge in InPrivate mode. */
public val inPrivateEdge: DriverFactory = {
    edgeDriver {
        options {
            arguments {
                +inPrivate
            }
        }
    }
}

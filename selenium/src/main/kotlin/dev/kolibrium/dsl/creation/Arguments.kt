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

package dev.kolibrium.dsl.creation

/**
 * Base interface for browser command-line arguments.
 */
public sealed interface Argument {
    /**
     * The string value of the argument.
     */
    public val value: String
}

/**
 * Value class representing a Chrome browser command-line argument.
 *
 * @property value The string value of the argument, must start with "--".
 */
@JvmInline
public value class ChromeArgument(
    override val value: String,
) : Argument {
    init {
        require(value.startsWith("--")) { "Chrome argument \"$value\" must start with \"--\"" }
    }
}

/**
 * Value class representing a Firefox browser command-line argument.
 *
 * @property value The string value of the argument, must start with "--".
 */
@JvmInline
public value class FirefoxArgument(
    override val value: String,
) : Argument {
    init {
        require(value.startsWith("--")) { "FirefoxArgument argument \"$value\" must start with \"--\"" }
    }
}

/**
 * Value class representing an Edge browser command-line argument.
 *
 * @property value The string value of the argument, must start with "--".
 */
@JvmInline
public value class EdgeArgument(
    override val value: String,
) : Argument {
    init {
        require(value.startsWith("--")) { "EdgeArgument argument \"$value\" must start with \"--\"" }
    }
}

/**
 * Collection of predefined command line arguments.
 */
public object Arguments {
    /**
     * Collection of predefined Chrome command line arguments.
     *
     * List of arguments are available [here](https://peter.sh/experiments/chromium-command-line-switches).
     */
    public object Chrome {
        /**
         * Disables the use of /dev/shm for browser memory.
         */
        public val disable_dev_shm_usage: ChromeArgument = ChromeArgument("--disable-dev-shm-usage")

        /**
         * Disables browser extensions.
         */
        public val disable_extensions: ChromeArgument = ChromeArgument("--disable-extensions")

        /**
         * Disables GPU hardware acceleration.
         */
        public val disable_gpu: ChromeArgument = ChromeArgument("--disable-gpu")

        /**
         * Disables the popup blocking feature.
         */
        public val disable_popup_blocking: ChromeArgument = ChromeArgument("--disable-popup-blocking")

        /**
         * Disables browser notifications.
         */
        public val disable_notifications: ChromeArgument = ChromeArgument("--disable-notifications")

        /**
         * Disables the search engine choice screen.
         */
        public val disable_search_engine_choice_screen: ChromeArgument =
            ChromeArgument(
                "--disable-search-engine-choice-screen",
            )

        /**
         * Enables headless mode using the new implementation.
         */
        public val headless: ChromeArgument = ChromeArgument("--headless=new")

        /**
         * Ignores SSL certificate errors.
         */
        public val ignore_certificate_errors: ChromeArgument = ChromeArgument("--ignore-certificate-errors")

        /**
         * Launches the browser in incognito mode.
         */
        public val incognito: ChromeArgument = ChromeArgument("--incognito")

        /**
         * Disables the sandbox security feature.
         */
        public val no_sandbox: ChromeArgument = ChromeArgument("--no-sandbox")

        /**
         * Allows remote connections from any origin.
         */
        public val remote_allow_origins: ChromeArgument = ChromeArgument("--remote-allow-origins=*")

        /**
         * Starts the browser maximized.
         */
        public val start_maximized: ChromeArgument = ChromeArgument("--start-maximized")
    }

    /**
     * Collection of predefined Firefox command line arguments.
     */
    public object Firefox {
        /**
         * Enables headless mode.
         */
        public val headless: FirefoxArgument = FirefoxArgument("--headless")

        /**
         * Launches browser in private browsing mode.
         */
        public val incognito: FirefoxArgument = FirefoxArgument("--incognito")

        /**
         * Sets the browser window height.
         */
        public val height: FirefoxArgument = FirefoxArgument("--height")

        /**
         * Sets the browser window width.
         */
        public val width: FirefoxArgument = FirefoxArgument("--width")
    }

    /**
     * Collection of predefined Edge command line arguments.
     */
    public object Edge {
        /**
         * Enables headless mode.
         */
        public val headless: EdgeArgument = EdgeArgument("--headless")

        /**
         * Launches browser in private mode.
         */
        public val inPrivate: EdgeArgument = EdgeArgument("--inprivate")
    }
}

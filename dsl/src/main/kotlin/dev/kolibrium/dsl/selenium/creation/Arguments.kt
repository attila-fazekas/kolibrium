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

package dev.kolibrium.dsl.selenium.creation

/**
 * Base interface for browser command-line arguments.
 */
internal sealed interface Argument {
    val value: String
}

/**
 * Value class representing a Chrome browser command-line argument.
 *
 * @property value The string value of the argument, must start with "--".
 */
@JvmInline
public value class ChromeArgument private constructor(
    override val value: String,
) : Argument {
    /**
     * Provides factory method for creating Chrome browser command-line arguments.
     *
     * @see ChromeArgument
     */
    public companion object {
        /**
         * Creates a new Chrome command-line argument.
         *
         * @param value The argument string, must start with "--".
         * @throws IllegalArgumentException if the value doesn't start with "--".
         * @return A new [ChromeArgument] instance.
         */
        public fun of(value: String): ChromeArgument {
            require(value.startsWith("--")) { "Chrome argument \"$value\" must start with \"--\"" }
            return ChromeArgument(value)
        }
    }
}

/**
 * Value class representing a Firefox browser command-line argument.
 *
 * @property value The string value of the argument, must start with "--".
 */
@JvmInline
public value class FirefoxArgument private constructor(
    override val value: String,
) : Argument {
    /**
     * Provides factory method for creating Firefox browser command-line arguments.
     *
     * @see FirefoxArgument
     */
    public companion object {
        /**
         * Creates a new Firefox command-line argument.
         *
         * @param value The argument string, must start with "--".
         * @throws IllegalArgumentException if the value doesn't start with "--".
         * @return A new [FirefoxArgument] instance.
         */
        public fun of(value: String): FirefoxArgument {
            require(value.startsWith("--")) { "Firefox argument \"$value\" must start with \"--\"" }
            return FirefoxArgument(value)
        }
    }
}

/**
 * Value class representing an Edge browser command-line argument.
 *
 * @property value The string value of the argument, must start with "--".
 */
@JvmInline
public value class EdgeArgument private constructor(
    override val value: String,
) : Argument {
    /**
     * Provides factory method for creating Edge browser command-line arguments.
     *
     * @see EdgeArgument
     */
    public companion object {
        /**
         * Creates a new Edge command-line argument.
         *
         * @param value The argument string, must start with "--".
         * @throws IllegalArgumentException if the value doesn't start with "--".
         * @return A new [EdgeArgument] instance.
         */
        public fun of(value: String): EdgeArgument {
            require(value.startsWith("--")) { "Edge argument \"$value\" must start with \"--\"" }
            return EdgeArgument(value)
        }
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
        @KolibriumPropertyDsl
        public val disable_dev_shm_usage: ChromeArgument = ChromeArgument.of("--disable-dev-shm-usage")

        /**
         * Disables browser extensions.
         */
        @KolibriumPropertyDsl
        public val disable_extensions: ChromeArgument = ChromeArgument.of("--disable-extensions")

        /**
         * Disables GPU hardware acceleration.
         */
        @KolibriumPropertyDsl
        public val disable_gpu: ChromeArgument = ChromeArgument.of("--disable-gpu")

        /**
         * Disables the popup blocking feature.
         */
        @KolibriumPropertyDsl
        public val disable_popup_blocking: ChromeArgument = ChromeArgument.of("--disable-popup-blocking")

        /**
         * Disables browser notifications.
         */
        @KolibriumPropertyDsl
        public val disable_notifications: ChromeArgument = ChromeArgument.of("--disable-notifications")

        /**
         * Disables the search engine choice screen.
         */
        @KolibriumPropertyDsl
        public val disable_search_engine_choice_screen: ChromeArgument =
            ChromeArgument.of(
                "--disable-search-engine-choice-screen",
            )

        /**
         * Enables headless mode using the new implementation.
         */
        @KolibriumPropertyDsl
        public val headless: ChromeArgument = ChromeArgument.of("--headless=new")

        /**
         * Launches the browser in incognito mode.
         */
        @KolibriumPropertyDsl
        public val incognito: ChromeArgument = ChromeArgument.of("--incognito")

        /**
         * Disables the sandbox security feature.
         */
        @KolibriumPropertyDsl
        public val no_sandbox: ChromeArgument = ChromeArgument.of("--no-sandbox")

        /**
         * Allows remote connections from any origin.
         */
        @KolibriumPropertyDsl
        public val remote_allow_origins: ChromeArgument = ChromeArgument.of("--remote-allow-origins=*")

        /**
         * Starts the browser maximized.
         */
        @KolibriumPropertyDsl
        public val start_maximized: ChromeArgument = ChromeArgument.of("--start-maximized")
    }

    /**
     * Collection of predefined Firefox command line arguments.
     */
    public object Firefox {
        /**
         * Enables headless mode.
         */
        @KolibriumPropertyDsl
        public val headless: FirefoxArgument = FirefoxArgument.of("--headless")

        /**
         * Launches browser in private browsing mode.
         */
        @KolibriumPropertyDsl
        public val incognito: FirefoxArgument = FirefoxArgument.of("--incognito")

        /**
         * Sets the browser window height.
         */
        @KolibriumPropertyDsl
        public val height: FirefoxArgument = FirefoxArgument.of("--height")

        /**
         * Sets the browser window width.
         */
        @KolibriumPropertyDsl
        public val width: FirefoxArgument = FirefoxArgument.of("--width")
    }

    /**
     * Collection of predefined Edge command line arguments.
     */
    public object Edge {
        /**
         * Enables headless mode.
         */
        @KolibriumPropertyDsl
        public val headless: EdgeArgument = EdgeArgument.of("--headless")

        /**
         * Launches browser in private mode.
         */
        @KolibriumPropertyDsl
        public val inPrivate: EdgeArgument = EdgeArgument.of("--inprivate")
    }
}

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

public sealed interface Argument {
    public val value: String
}

@JvmInline
public value class ChromeArgument private constructor(
    override val value: String,
) : Argument {
    public companion object {
        public fun of(value: String): ChromeArgument {
            require(value.startsWith("--")) { "Chrome argument \"$value\" must start with \"--\"" }
            return ChromeArgument(value)
        }
    }
}

@JvmInline
public value class FirefoxArgument private constructor(
    override val value: String,
) : Argument {
    public companion object {
        public fun of(value: String): FirefoxArgument {
            require(value.startsWith("--")) { "Firefox argument \"$value\" must start with \"--\"" }
            return FirefoxArgument(value)
        }
    }
}

@JvmInline
public value class EdgeArgument private constructor(
    override val value: String,
) : Argument {
    public companion object {
        public fun of(value: String): EdgeArgument {
            require(value.startsWith("--")) { "Edge argument \"$value\" must start with \"--\"" }
            return EdgeArgument(value)
        }
    }
}

public object Arguments {
    // list of arguments: https://peter.sh/experiments/chromium-command-line-switches/
    public object Chrome {
        @KolibriumPropertyDsl
        public val disable_dev_shm_usage: ChromeArgument = ChromeArgument.of("--disable-dev-shm-usage")

        @KolibriumPropertyDsl
        public val disable_extensions: ChromeArgument = ChromeArgument.of("--disable-extensions")

        @KolibriumPropertyDsl
        public val disable_gpu: ChromeArgument = ChromeArgument.of("--disable-gpu")

        @KolibriumPropertyDsl
        public val disable_popup_blocking: ChromeArgument = ChromeArgument.of("--disable-popup-blocking")

        @KolibriumPropertyDsl
        public val disable_notifications: ChromeArgument = ChromeArgument.of("--disable-notifications")

        @KolibriumPropertyDsl
        public val disable_search_engine_choice_screen: ChromeArgument =
            ChromeArgument.of(
                "--disable-search-engine-choice-screen",
            )

        @KolibriumPropertyDsl
        public val headless: ChromeArgument = ChromeArgument.of("--headless=new")

        @KolibriumPropertyDsl
        public val incognito: ChromeArgument = ChromeArgument.of("--incognito")

        @KolibriumPropertyDsl
        public val no_sandbox: ChromeArgument = ChromeArgument.of("--no-sandbox")

        @KolibriumPropertyDsl
        public val remote_allow_origins: ChromeArgument = ChromeArgument.of("--remote-allow-origins=*")

        @KolibriumPropertyDsl
        public val start_maximized: ChromeArgument = ChromeArgument.of("--start-maximized")
    }

    public object Firefox {
        @KolibriumPropertyDsl
        public val headless: FirefoxArgument = FirefoxArgument.of("--headless")

        @KolibriumPropertyDsl
        public val incognito: FirefoxArgument = FirefoxArgument.of("--incognito")

        @KolibriumPropertyDsl
        public val height: FirefoxArgument = FirefoxArgument.of("--height")

        @KolibriumPropertyDsl
        public val width: FirefoxArgument = FirefoxArgument.of("--width")
    }

    public object Edge {
        @KolibriumPropertyDsl
        public val headless: EdgeArgument = EdgeArgument.of("--headless")

        @KolibriumPropertyDsl
        public val inPrivate: EdgeArgument = EdgeArgument.of("--inprivate")
    }
}

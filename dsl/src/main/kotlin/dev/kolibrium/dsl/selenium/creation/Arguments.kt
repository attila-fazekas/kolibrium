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

package dev.kolibrium.dsl.selenium.creation

@JvmInline
public value class Argument<T : Browser>(public val name: String) {
    init {
        require(name.startsWith("--")) {
            """
                Argument "$name" must start with "--" prefix
            """.trimIndent()
        }
    }
}

public object Arguments {

    // list of arguments: https://peter.sh/experiments/chromium-command-line-switches/
    public object Chrome {
        public val disable_dev_shm_usage: Argument<dev.kolibrium.dsl.selenium.creation.Chrome> =
            Argument("--disable-dev-shm-usage")
        public val disable_extensions: Argument<dev.kolibrium.dsl.selenium.creation.Chrome> =
            Argument("--disable-extensions")
        public val disable_gpu: Argument<dev.kolibrium.dsl.selenium.creation.Chrome> =
            Argument("--disable-gpu")
        public val disable_popup_blocking: Argument<dev.kolibrium.dsl.selenium.creation.Chrome> =
            Argument("--disable-popup-blocking")
        public val disable_notifications: Argument<dev.kolibrium.dsl.selenium.creation.Chrome> =
            Argument("--disable-notifications")
        public val headless: Argument<dev.kolibrium.dsl.selenium.creation.Chrome> =
            Argument("--headless=new")
        public val incognito: Argument<dev.kolibrium.dsl.selenium.creation.Chrome> =
            Argument("--incognito")
        public val no_sandbox: Argument<dev.kolibrium.dsl.selenium.creation.Chrome> =
            Argument("--no-sandbox")
        public val remote_allow_origins: Argument<dev.kolibrium.dsl.selenium.creation.Chrome> =
            Argument("--remote-allow-origins=*")
        public val start_maximized: Argument<dev.kolibrium.dsl.selenium.creation.Chrome> =
            Argument("--start-maximized")
    }

    public object Firefox {
        public val headless: Argument<dev.kolibrium.dsl.selenium.creation.Firefox> =
            Argument("--headless")
        public val incognito: Argument<dev.kolibrium.dsl.selenium.creation.Firefox> =
            Argument("--incognito")
        public val height: Argument<dev.kolibrium.dsl.selenium.creation.Firefox> =
            Argument("--height")
        public val width: Argument<dev.kolibrium.dsl.selenium.creation.Firefox> =
            Argument("--width")
    }

    public object Edge {
        public val headless: Argument<dev.kolibrium.dsl.selenium.creation.Edge> =
            Argument("--headless")
        public val inPrivate: Argument<dev.kolibrium.dsl.selenium.creation.Edge> =
            Argument("--inprivate")
    }
}

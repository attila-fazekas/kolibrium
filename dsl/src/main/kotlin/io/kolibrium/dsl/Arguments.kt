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

package io.kolibrium.dsl

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
        public val disable_dev_shm_usage: Argument<io.kolibrium.dsl.Chrome> = Argument("--disable-dev-shm-usage")
        public val disable_extensions: Argument<io.kolibrium.dsl.Chrome> = Argument("--disable-extensions")
        public val disable_gpu: Argument<io.kolibrium.dsl.Chrome> = Argument("--disable-gpu")
        public val disable_popup_blocking: Argument<io.kolibrium.dsl.Chrome> = Argument("--disable-popup-blocking")
        public val disable_notifications: Argument<io.kolibrium.dsl.Chrome> = Argument("--disable-notifications")
        public val headless: Argument<io.kolibrium.dsl.Chrome> = Argument("--headless=new")
        public val incognito: Argument<io.kolibrium.dsl.Chrome> = Argument("--incognito")
        public val no_sandbox: Argument<io.kolibrium.dsl.Chrome> = Argument("--no-sandbox")
        public val remote_allow_origins: Argument<io.kolibrium.dsl.Chrome> = Argument("--remote-allow-origins=*")
        public val start_maximized: Argument<io.kolibrium.dsl.Chrome> = Argument("--start-maximized")
    }

    public object Firefox {
        public val headless: Argument<io.kolibrium.dsl.Firefox> = Argument("--headless")
        public val incognito: Argument<io.kolibrium.dsl.Firefox> = Argument("--incognito")
        public val height: Argument<io.kolibrium.dsl.Firefox> = Argument("--height")
        public val width: Argument<io.kolibrium.dsl.Firefox> = Argument("--width")
    }

    public object Edge {
        public val headless: Argument<io.kolibrium.dsl.Edge> = Argument("--headless")
        public val inPrivate: Argument<io.kolibrium.dsl.Edge> = Argument("--inprivate")
    }
}

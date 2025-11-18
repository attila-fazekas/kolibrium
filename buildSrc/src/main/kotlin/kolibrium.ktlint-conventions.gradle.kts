import gradle.kotlin.dsl.accessors._4a33a12d8cb39904dd14f27ebecea85c.ktlintKotlinScriptCheck
import gradle.kotlin.dsl.accessors._4a33a12d8cb39904dd14f27ebecea85c.ktlintKotlinScriptFormat
import gradle.kotlin.dsl.accessors._4a33a12d8cb39904dd14f27ebecea85c.ktlintMainSourceSetCheck
import gradle.kotlin.dsl.accessors._4a33a12d8cb39904dd14f27ebecea85c.ktlintMainSourceSetFormat
import gradle.kotlin.dsl.accessors._4a33a12d8cb39904dd14f27ebecea85c.ktlintTestSourceSetCheck
import gradle.kotlin.dsl.accessors._4a33a12d8cb39904dd14f27ebecea85c.ktlintTestSourceSetFormat
import org.gradle.kotlin.dsl.invoke

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

plugins {
    id("org.jlleitschuh.gradle.ktlint")
}

ktlint {
    version = "1.7.1"
    debug = true
    verbose = true
    outputToConsole = true
    outputColorName = "RED"
    ignoreFailures = false
    enableExperimentalRules = false
}

tasks.ktlintMainSourceSetCheck {
    dependsOn(tasks.ktlintMainSourceSetFormat)
}

tasks.ktlintTestSourceSetCheck {
    dependsOn(tasks.ktlintTestSourceSetFormat)
}

tasks.ktlintKotlinScriptCheck {
    dependsOn(tasks.ktlintKotlinScriptFormat)
}
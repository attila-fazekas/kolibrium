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
    id("kolibrium.detekt-conventions")
    id("kolibrium.kotlin-conventions")
    id("kolibrium.ktlint-conventions")
}

dependencies {
    testImplementation("com.lemonappdev:konsist:_")
    testImplementation(Testing.junit.jupiter.api)
    runtimeOnly(Testing.junit.jupiter.engine)
}

tasks.withType<Test> {
    useJUnitPlatform()
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

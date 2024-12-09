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

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("kolibrium.library-conventions")
    id("kolibrium.test-conventions")
}

dependencies {
    api(project(":core"))
    implementation("commons-validator:commons-validator:_")
    testImplementation("com.titusfortner:selenium-logger:_")
}

tasks.withType<KotlinCompile> {
    compilerOptions.freeCompilerArgs =
        listOf(
            "-Xcontext-receivers",
            "-opt-in=dev.kolibrium.core.InternalKolibriumApi",
        )
}

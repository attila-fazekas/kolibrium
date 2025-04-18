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
    id("kolibrium.test-conventions")
    id("com.google.devtools.ksp")
}

dependencies {
    implementation(project(":core:selenium"))
    implementation(project(":dsl"))
    implementation(project(":junit"))
    implementation(project(":ksp:annotations"))
    implementation(Testing.kotest.assertions.core)
    ksp(project(":ksp:processors"))
    ksp("dev.zacsweers.autoservice:auto-service-ksp:_")
    testImplementation("com.titusfortner:selenium-logger:_")
}

ksp {
    arg("kolibriumKsp.useDsl", "false")
}

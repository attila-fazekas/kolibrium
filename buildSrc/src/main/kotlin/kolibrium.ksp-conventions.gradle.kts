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
    id("com.google.devtools.ksp")
    id("kolibrium.kotlin-conventions")
}

dependencies {
    implementation("com.google.auto.service:auto-service-annotations:_")
    implementation("com.google.devtools.ksp:symbol-processing-api:_")
    implementation("com.squareup:kotlinpoet:2.2.0-SNAPSHOT")
    implementation("com.squareup:kotlinpoet-ksp:2.2.0-SNAPSHOT")
    ksp("dev.zacsweers.autoservice:auto-service-ksp:_")
    testImplementation("dev.zacsweers.kctfork:core:_")
    testImplementation("dev.zacsweers.kctfork:ksp:_")
}

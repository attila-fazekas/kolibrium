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

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    idea
    id("com.diffplug.spotless")
}

group = "dev.kolibrium"
version = "0.8.0-SNAPSHOT"

repositories {
    mavenCentral()
}

spotless {
    kotlin {
        target("**/*.kt")
        endWithNewline()
        trimTrailingWhitespace()
        licenseHeaderFile(rootProject.file("spotless/copyright.kt"))
    }
    kotlinGradle {
        target("**/*.kts")
        targetExclude("settings.gradle.kts")
        endWithNewline()
        trimTrailingWhitespace()
        licenseHeaderFile(rootProject.file("spotless/copyright.kt"), "(package |@file|import |fun )|buildscript |plugins |subprojects |spotless ")
    }
}

tasks.spotlessCheck {
    dependsOn(tasks.spotlessApply)
}

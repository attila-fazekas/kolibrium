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

plugins {
    `kotlin-dsl`
    id("com.diffplug.spotless") version "6.25.0"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.diffplug.spotless:spotless-plugin-gradle:_")
    implementation("com.google.devtools.ksp:symbol-processing-gradle-plugin:_")
    implementation("com.jaredsburrows:gradle-license-plugin:_")
    implementation("dev.adamko.dokkatoo:dev.adamko.dokkatoo.gradle.plugin:_")
    implementation("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:_")
    implementation("io.kotest:io.kotest.gradle.plugin:_")
    implementation("org.jetbrains.kotlin.jvm:org.jetbrains.kotlin.jvm.gradle.plugin:_")
    implementation("org.jetbrains.kotlinx.binary-compatibility-validator:org.jetbrains.kotlinx.binary-compatibility-validator.gradle.plugin:_")
    implementation("org.jlleitschuh.gradle:ktlint-gradle:_")
    implementation("org.jreleaser:jreleaser-gradle-plugin:_")
}

kotlin {
    jvmToolchain(17)
}

spotless {
    kotlin {
        target("**/*.kt")
        endWithNewline()
        trimTrailingWhitespace()
        licenseHeaderFile(file("${project.rootDir}/../spotless/copyright.kt"))
    }
    kotlinGradle {
        target("**/*.kts")
        targetExclude("settings.gradle.kts")
        endWithNewline()
        trimTrailingWhitespace()
        licenseHeaderFile(file("${project.rootDir}/../spotless/copyright.kt"), "(package |@file|import |fun )|buildscript |plugins |subprojects |spotless ")
    }
}

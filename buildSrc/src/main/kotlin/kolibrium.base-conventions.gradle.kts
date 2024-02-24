import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    idea
    id("com.diffplug.spotless")
}

group = "dev.kolibrium"
version = "0.1.1-SNAPSHOT"

repositories {
    mavenCentral()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.freeCompilerArgs = listOf(
        "-Xcontext-receivers",
    )
}

spotless {
    kotlin {
        target("**/*.kt")
        endWithNewline()
        licenseHeaderFile(rootProject.file("spotless/copyright.kt"))
    }
    kotlinGradle {
        target("**/*.kts")
        endWithNewline()
        licenseHeaderFile(rootProject.file("spotless/copyright.kt"), "(package |@file|import |fun )|buildscript |plugins |subprojects |spotless ")
    }
}

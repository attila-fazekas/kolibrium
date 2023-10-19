import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    idea
    id("io.gitlab.arturbosch.detekt")
    id("org.jetbrains.dokka")
    id("org.jetbrains.kotlinx.binary-compatibility-validator")
    id("org.jetbrains.kotlinx.kover")
    id("org.jlleitschuh.gradle.ktlint")
}

group = "io.kolibrium"

repositories {
    mavenCentral()
}

dependencies {
    implementation("ch.qos.logback:logback-classic:_")
    implementation("io.github.oshai:kotlin-logging-jvm:_")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.freeCompilerArgs = listOf(
        "-Xcontext-receivers",
    )
}

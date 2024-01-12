import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    idea
    id("dev.adamko.dokkatoo-html")
    id("io.gitlab.arturbosch.detekt")
    id("org.jetbrains.kotlinx.binary-compatibility-validator")
    id("org.jlleitschuh.gradle.ktlint")
}

group = "dev.kolibrium"
version = "0.1.0"

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

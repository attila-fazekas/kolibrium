plugins {
    kotlin("jvm")
    idea
    id("org.jetbrains.kotlinx.binary-compatibility-validator")
}

group = "io.kolibrium"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("ch.qos.logback:logback-classic:_")
    implementation("io.github.microutils:kotlin-logging:_")
}
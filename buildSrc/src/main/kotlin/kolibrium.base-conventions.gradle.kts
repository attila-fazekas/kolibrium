plugins {
    kotlin("jvm")
    idea
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
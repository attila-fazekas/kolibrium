import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    idea
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

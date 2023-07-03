plugins {
    `kotlin-dsl`
}

dependencies {
    implementation("com.github.johnrengelman.shadow:com.github.johnrengelman.shadow.gradle.plugin:_")
    implementation("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:_")
    implementation("io.kotest:io.kotest.gradle.plugin:_")
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:_")
    implementation("org.jetbrains.kotlin.jvm:org.jetbrains.kotlin.jvm.gradle.plugin:_")
    implementation("org.jetbrains.kotlinx.binary-compatibility-validator:org.jetbrains.kotlinx.binary-compatibility-validator.gradle.plugin:_")
    implementation("org.jlleitschuh.gradle:ktlint-gradle:_")
}

kotlin {
    jvmToolchain(17)
}
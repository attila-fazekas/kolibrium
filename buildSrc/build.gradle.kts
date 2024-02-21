plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

dependencies {
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

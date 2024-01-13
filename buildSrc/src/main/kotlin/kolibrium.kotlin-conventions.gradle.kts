plugins {
    kotlin("jvm")
    id("dev.adamko.dokkatoo-html")
    id("io.gitlab.arturbosch.detekt")
    id("kolibrium.base-conventions")
    id("org.jetbrains.kotlinx.binary-compatibility-validator")
    id("org.jlleitschuh.gradle.ktlint")
}

dependencies {
    implementation("ch.qos.logback:logback-classic:_")
    implementation("io.github.oshai:kotlin-logging-jvm:_")
}

plugins {
    kotlin("jvm")
    id("dev.adamko.dokkatoo-html")
    id("io.gitlab.arturbosch.detekt")
    id("kolibrium.base-conventions")
    id("org.jetbrains.kotlinx.binary-compatibility-validator")
    id("org.jlleitschuh.gradle.ktlint")
}

dependencies {
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:_")
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-rules-libraries:_")
    implementation("ch.qos.logback:logback-classic:_")
    implementation("io.github.oshai:kotlin-logging-jvm:_")
}

detekt {
    config.setFrom(files(rootProject.file("detekt.yml")))
}

ktlint {
    version = "1.1.1"
    debug = true
    verbose = true
    outputToConsole = true
    outputColorName = "RED"
    ignoreFailures = false
    enableExperimentalRules = false
}
plugins {
    id("io.gitlab.arturbosch.detekt")
}

dependencies {
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:_")
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-rules-libraries:_")
}

detekt {
    config.setFrom(files(rootProject.file("detekt.yml")))
}

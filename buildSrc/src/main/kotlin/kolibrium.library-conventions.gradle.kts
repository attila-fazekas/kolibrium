plugins {
    `java-library`
    `maven-publish`
    id("kolibrium.base-conventions")
    id("com.github.johnrengelman.shadow")
    id("org.jreleaser")
}

kotlin {
    explicitApi()
}

java {
    withJavadocJar()
    withSourcesJar()
}

val javadocJar by tasks.register<Jar>("dokkaHtmlJar") {
    group = "documentation"
    dependsOn(tasks.dokkaHtml)
    from(tasks.dokkaHtml.flatMap { it.outputDirectory })
    archiveClassifier.set("javadoc")
}

val sourcesJar by tasks.named("sourcesJar")

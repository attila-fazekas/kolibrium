import dev.kolibrium.PublicationProperties.PROJECT_GIT_URL
import dev.kolibrium.PublicationProperties.SCM

plugins {
    `java-library`
    id("dev.adamko.dokkatoo-html")
    id("kolibrium.kotlin-conventions")
    id("kolibrium.publication-conventions")
    id("kolibrium.static-analysis-conventions")
    id("org.jetbrains.kotlinx.binary-compatibility-validator")
}

kotlin {
    explicitApi()
}

tasks.named("compileTestKotlin") {
    dependsOn(tasks.named("ktlintFormat"))
}

val sourcesJar by tasks.register<Jar>("sourcesJar") {
    archiveClassifier = "sources"
    from(sourceSets.main.get().allSource)
}

val javadocJar by tasks.register<Jar>("javadocJar") {
    description = "Assembles a JAR containing the Javadoc documentation."
    group = "documentation"

    dependsOn(tasks.dokkatooGeneratePublicationHtml)
    from(tasks.dokkatooGeneratePublicationHtml.flatMap { it.outputDirectory })
    archiveClassifier = "javadoc"
}

val stagingDir = layout.buildDirectory.dir("staging-deploy")

publishing {
    publications {
        create<MavenPublication>("kolibrium") {
            from(components["kotlin"])
            artifact(javadocJar)
            artifact(sourcesJar)
            artifactId = if (project.name.contains("processors")) "ksp" else project.name
            artifactId = "$name-$artifactId"
            pom {
                name = rootProject.name
                description = "\"${project.name}\" module of Kolibrium"
                inceptionYear = "2023"
                url = PROJECT_GIT_URL
                licenses {
                    license {
                        name = "Apache-2.0"
                        url = "http://www.apache.org/licenses/LICENSE-2.0"
                    }
                }
                developers {
                    developer {
                        id = "attila-fazekas"
                        name = "Attila Fazekas"
                    }
                }
                issueManagement {
                    system = "GitHub"
                    url = "$PROJECT_GIT_URL/issues"
                }
                scm {
                    connection = SCM
                    developerConnection = SCM
                    url = PROJECT_GIT_URL
                }
            }
        }
        repositories {
            maven {
                url = uri(stagingDir)
            }
        }
    }
}

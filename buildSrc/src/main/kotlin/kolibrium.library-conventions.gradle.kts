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

val projectGitUrl = "https://github.com/attila-fazekas/kolibrium"
val scm = "scm:git:$projectGitUrl.git"
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
                description = "Kotlin library for Selenium tests"
                inceptionYear = "2023"
                url = projectGitUrl
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
                    url = "$projectGitUrl/issues"
                }
                scm {
                    connection = scm
                    developerConnection = scm
                    url = projectGitUrl
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

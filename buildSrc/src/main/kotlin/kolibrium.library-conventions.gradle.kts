import org.jreleaser.model.Active.ALWAYS

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

val projectGitUrl = "https://github.com/attila-fazekas/kolibrium"
val scm = "scm:git:$projectGitUrl.git"
val stagingDir = layout.buildDirectory.dir("staging-deploy")

val javadocJar by tasks.register<Jar>("dokkaHtmlJar") {
    group = "documentation"
    dependsOn(tasks.dokkaHtml)
    from(tasks.dokkaHtml.flatMap { it.outputDirectory })
    archiveClassifier.set("javadoc")
}

val sourcesJar by tasks.named("sourcesJar")

publishing {
    publications {
        create<MavenPublication>("kolibrium") {
            from(components["kotlin"])
            artifact(javadocJar)
            artifact(sourcesJar)
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

jreleaser {
    dryrun = true
    project {
        author("Attila Fazekas")
        copyright = "Copyright © 2023 Attila Fazekas. All rights reserved."
        description = "Kotlin library for Selenium tests"
        gitRootSearch = true
        inceptionYear = "2023"
        license = "Apache-2.0"
        links {
            documentation = projectGitUrl
            homepage = projectGitUrl
            license = "http://www.apache.org/licenses/LICENSE-2.0"
        }
    }
    release {
        github {
            changelog {
                contributors {
                    enabled = false
                }
                format = "{{commitShortHash}} {{commitTitle}}"
                formatted = ALWAYS
                skipMergeCommits = true
            }
            overwrite = true
            releaseName = "Kolibrium {{projectVersionNumber}}"
            repoOwner = "attila-fazekas"
        }
    }
    signing {
        active = ALWAYS
        armored = true
    }
    deploy {
        maven {
            nexus2 {
                register("maven-central") {
                    active = ALWAYS
                    group = "io.github.attila-fazekas"
                    closeRepository = true
                    releaseRepository = true
                    snapshotUrl = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
                    url = "https://s01.oss.sonatype.org/service/local"
                    stagingRepository(stagingDir.get().toString())
                }
            }
        }
    }
}

import org.jreleaser.model.Active
import java.nio.file.Files

plugins {
    `java-library`
    `maven-publish`
    id("kolibrium.base-conventions")
    id("org.jreleaser")
}

kotlin {
    explicitApi()
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

val projectGitUrl = "https://github.com/attila-fazekas/kolibrium"
val scm = "scm:git:$projectGitUrl.git"
val stagingDir = layout.buildDirectory.dir("staging-deploy") //

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
                description = provider { "Module \"${project.name}\" of Kolibrium" }
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
                formatted = Active.ALWAYS
                skipMergeCommits = true
            }
            overwrite = true
            releaseName = "Kolibrium {{projectVersionNumber}}"
            repoOwner = "attila-fazekas"
        }
    }
    signing {
        active = Active.ALWAYS
        armored = true
    }
    deploy {
        maven {
            nexus2 {
                register("maven-central") {
                    active = Active.ALWAYS
                    description = provider { "Module \"${project.name}\" of Kolibrium" }.toString()
                    closeRepository = false
                    releaseRepository = false
                    snapshotUrl = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
                    url = "https://s01.oss.sonatype.org/service/local"
                    stagingRepository(stagingDir.get().toString())
                }
            }
        }
    }
}

tasks.register("jreleaserCreateDirectory") {
    group = "jreleaser"
    val directory = file(layout.buildDirectory.dir("jreleaser"))
    doLast {
        Files.createDirectories(directory.toPath())
    }
}

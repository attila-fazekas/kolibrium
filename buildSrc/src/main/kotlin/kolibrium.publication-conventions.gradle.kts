import dev.kolibrium.PublicationProperties
import org.jreleaser.model.Active
import java.nio.file.Files

plugins {
    `maven-publish`
    id("org.jreleaser")
}

val stagingDir = layout.buildDirectory.dir("staging-deploy")

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
            documentation = PublicationProperties.PROJECT_GIT_URL
            homepage = PublicationProperties.PROJECT_GIT_URL
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
                    description = "\"${project.name}\" module of Kolibrium"
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

import org.jreleaser.model.Active

plugins {
    id("kolibrium.test-conventions")
    id("kolibrium.library-conventions")
}

version = "0.2.0-SNAPSHOT"

dependencies {
    api("org.seleniumhq.selenium:selenium-java:_")
}

val projectGitUrl = "https://github.com/attila-fazekas/kolibrium"
val scm = "scm:git:$projectGitUrl.git"
val stagingDir: Provider<Directory> = layout.buildDirectory.dir("staging-deploy")

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
                    description = "Core module of Kolibrium"
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

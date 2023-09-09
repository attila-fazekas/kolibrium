import org.jreleaser.model.Active

plugins {
    id("kolibrium.junit-conventions")
    id("kolibrium.kotest-conventions")
    id("kolibrium.ksp-conventions")
    id("kolibrium.library-conventions")
}

version = "0.2.0-SNAPSHOT"

dependencies {
    implementation(project(":ksp:annotations"))
    implementation("com.samskivert:jmustache:_")
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
    signing {
        active = Active.ALWAYS
        armored = true
    }
    deploy {
        maven {
            nexus2 {
                register("maven-central") {
                    active = Active.ALWAYS
                    description = "KSP module of Kolibrium"
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

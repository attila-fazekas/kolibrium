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
            documentation = "https://github.com/attila-fazekas/kolibrium"
            homepage = "https://github.com/attila-fazekas/kolibrium"
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
            tagName = "v{{projectVersionNumber}}"
        }
    }
    distributions {
        subprojects.forEach {
            create(it.name) {
                artifact {
                    path.set(file("/build/libs/{{distributionName}}-{{projectVersion}}.jar"))
                }
            }
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("kolibrium") {
            artifactId = "$name-$artifactId"
            from(components["kotlin"])
        }
    }
}
import dev.kolibrium.PublicationProperties.PROJECT_GIT_URL
import dev.kolibrium.PublicationProperties.SCM

plugins {
    `java-platform`
    id("kolibrium.base-conventions")
    id("kolibrium.publication-conventions")
}

dependencies {
    constraints {
        api(project(":ksp:annotations"))
        api(project(":core"))
        api(project(":dsl"))
        api(project(":junit"))
        api(project(":selenium"))
    }
}

val stagingDir: Provider<Directory> = layout.buildDirectory.dir("staging-deploy")

publishing {
    publications {
        create<MavenPublication>("kolibrium") {
            from(components["javaPlatform"])
            artifactId = "$name-${project.name}"
            pom {
                name = "Kolibrium BOM (Bill of Materials)"
                description = "Bill of materials to make sure a consistent set of versions is used for Kolibrium"
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
            repositories {
                maven {
                    url = uri(stagingDir)
                }
            }
        }
    }
}

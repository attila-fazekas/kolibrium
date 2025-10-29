/*
 * Copyright 2023-2025 Attila Fazekas & contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import dev.kolibrium.PublicationProperties.PROJECT_GIT_URL
import dev.kolibrium.PublicationProperties.SCM

plugins {
    `java-platform`
    id("kolibrium.base-conventions")
    id("kolibrium.publication-conventions")
}

dependencies {
    constraints {
        api(project(":core:selenium"))
        api(project(":dsl"))
        api(project(":ksp:annotations"))
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
                inceptionYear = "2024"
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

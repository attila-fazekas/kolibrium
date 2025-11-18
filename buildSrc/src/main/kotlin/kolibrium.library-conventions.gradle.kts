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
import dev.kolibrium.SharedFunctions
import java.io.FileOutputStream

plugins {
    `java-library`
    id("com.jaredsburrows.license")
    id("kolibrium.dokka-conventions")
    id("kolibrium.kotlin-conventions")
    id("kolibrium.publication-conventions")
    id("kolibrium.static-analysis-conventions")
    id("org.jetbrains.kotlinx.binary-compatibility-validator")
}

kotlin {
    explicitApi()
}

val sourcesJar by tasks.register<Jar>("sourcesJar") {
    archiveClassifier = "sources"
    from(sourceSets.main.get().allSource)
}

val dokkaJar by tasks.register<Jar>("dokkaJar") {
    description = "Assembles a JAR containing the Dokka HTML documentation."
    group = "documentation"

    dependsOn(tasks.dokkaGenerate)
    from(tasks.dokkaGeneratePublicationHtml.flatMap { it.outputDirectory })
    archiveClassifier = "javadoc"
}

val stagingDir: Provider<Directory> = layout.buildDirectory.dir("staging-deploy")

publishing {
    publications {
        create<MavenPublication>("kolibrium") {
            from(components["kotlin"])
            artifact(dokkaJar)
            artifact(sourcesJar)
            val moduleName = SharedFunctions.getModuleName(project)
            artifactId = "$name-$moduleName"
            pom {
                name = rootProject.name
                description = "\"$moduleName\" module of Kolibrium"
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

licenseReport {
    generateCsvReport = false
    generateHtmlReport = false
    generateJsonReport = false
    generateTextReport = true
}

//tasks.register("createNoticeFile") {
//    group = "notice"
//
//    if (!File("NOTICE").exists()) {
//        val header = """
//            Kolibrium includes work under the Apache License v2.0 (given in full in LICENSE file) requiring this NOTICE file to be
//            provided.
//
//            This software bundles unchanged copies of third-party libraries, to which different licenses may apply.
//            Please see below for the list of third-party libraries organized by modules, along with their respective licenses.
//        """.trimIndent()
//        File("NOTICE").writeText(header)
//    }
//
//    val licenseFile = file(layout.buildDirectory.dir("reports/licenses/licenseReport.txt"))
//
//    FileOutputStream("NOTICE", true).use { output ->
//        val header = """
//            ${System.lineSeparator()}
//            ==========================
//            ${project.projectDir.name} module
//            ==========================
//             ${System.lineSeparator()}
//        """.trimIndent()
//        output.write(header.toByteArray())
//        licenseFile.forEachBlock { buffer, bytesRead ->
//            output.write(buffer, 0, bytesRead)
//        }
//    }
//}

import dev.kolibrium.PublicationProperties.PROJECT_GIT_URL
import dev.kolibrium.PublicationProperties.SCM
import java.io.FileOutputStream

plugins {
    `java-library`
    id("dev.adamko.dokkatoo-html")
    id("com.jaredsburrows.license")
    id("kolibrium.kotlin-conventions")
    id("kolibrium.publication-conventions")
    id("kolibrium.static-analysis-conventions")
    id("org.jetbrains.kotlinx.binary-compatibility-validator")
}

kotlin {
    explicitApi()
}

tasks.ktlintMainSourceSetCheck {
    dependsOn(tasks.ktlintMainSourceSetFormat)
}

tasks.ktlintTestSourceSetCheck {
    dependsOn(tasks.ktlintTestSourceSetFormat)
}

tasks.ktlintKotlinScriptCheck {
    dependsOn(tasks.ktlintKotlinScriptFormat)
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

val stagingDir: Provider<Directory> = layout.buildDirectory.dir("staging-deploy")

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
                description = "\"${project.name}\" module of Kolibrium"
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

tasks.register("createNoticeFile") {
    group = "notice"

    if (!File("NOTICE").exists()) {
        val header = """
            Kolibrium includes work under the Apache License v2.0 (given in full in LICENSE file) requiring this NOTICE file to be
            provided.

            This software bundles unchanged copies of third-party libraries, to which different licenses may apply.
            Please see below for the list of third-party libraries organized by modules, along with their respective licenses.
        """.trimIndent()
        File("NOTICE").writeText(header)
    }

    val licenseFile = file(layout.buildDirectory.dir("reports/licenses/licenseReport.txt"))

    FileOutputStream("NOTICE", true).use { output ->
        val header = """
            ${System.lineSeparator()}
            ==========================
            ${project.projectDir.name} module
            ==========================
             ${System.lineSeparator()}
        """.trimIndent()
        output.write(header.toByteArray())
        licenseFile.forEachBlock { buffer, bytesRead ->
            output.write(buffer, 0, bytesRead)
        }
    }
}

dokkatoo.pluginsConfiguration.html {
    footerMessage.set("Copyright 2024 Attila Fazekas & contributors")
}

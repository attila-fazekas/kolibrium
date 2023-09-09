plugins {
    id("org.jetbrains.dokka")
}

repositories {
    mavenCentral()
}

tasks.dokkaHtmlMultiModule {
    removeChildTasks(
        project(":ksp:processors"),
    )
}

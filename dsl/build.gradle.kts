import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("kolibrium.library-conventions")
    id("kolibrium.test-conventions")
}

dependencies {
    implementation(project(":core"))
    implementation("commons-validator:commons-validator:_")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.freeCompilerArgs =
        listOf(
            "-opt-in=dev.kolibrium.core.InternalKolibriumApi",
        )
}

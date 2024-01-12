plugins {
    id("kolibrium.test-conventions")
    id("kolibrium.library-conventions")
}

dependencies {
    implementation(project(":core"))
    implementation(Testing.junit.jupiter.api)
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.20")
}

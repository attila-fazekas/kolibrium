plugins {
    id("kolibrium.library-conventions")
    id("kolibrium.test-conventions")
}

dependencies {
    implementation(project(":core"))
    implementation(Testing.junit.jupiter.api)
}

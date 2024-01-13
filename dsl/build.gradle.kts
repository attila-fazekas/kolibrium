plugins {
    id("kolibrium.library-conventions")
    id("kolibrium.test-conventions")
}

dependencies {
    implementation(project(":core"))
    implementation("commons-validator:commons-validator:_")
}

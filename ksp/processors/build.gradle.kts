plugins {
    id("kolibrium.test-conventions")
    id("kolibrium.ksp-conventions")
    id("kolibrium.library-conventions")
}

dependencies {
    implementation(project(":ksp:annotations"))
    implementation("commons-validator:commons-validator:_")
    implementation("com.samskivert:jmustache:_")
}

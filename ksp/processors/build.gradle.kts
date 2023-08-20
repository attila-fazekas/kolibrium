plugins {
    id("kolibrium.junit-conventions")
    id("kolibrium.kotest-conventions")
    id("kolibrium.ksp-conventions")
    id("kolibrium.library-conventions")
}

dependencies {
    implementation(project(":ksp:annotations"))
}

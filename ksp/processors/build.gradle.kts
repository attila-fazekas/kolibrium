plugins {
    id("kolibrium.library-conventions")
    id("kolibrium.junit-conventions")
    id("kolibrium.kotest-conventions")
    id("kolibrium.ksp-conventions")
}

dependencies {
    implementation(project(":ksp:annotations"))
}

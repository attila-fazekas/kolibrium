plugins {
    id("kolibrium.junit-conventions")
    id("kolibrium.kotest-conventions")
    id("kolibrium.library-conventions")
}

dependencies {
    api("org.seleniumhq.selenium:selenium-java:_")
    implementation("io.arrow-kt:arrow-core:_")
}

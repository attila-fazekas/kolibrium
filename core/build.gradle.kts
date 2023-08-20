plugins {
    id("kolibrium.junit-conventions")
    id("kolibrium.kotest-conventions")
    id("kolibrium.library-conventions")
}

dependencies {
    api("org.seleniumhq.selenium:selenium-java:_")
    api("io.arrow-kt:arrow-core:_")
}

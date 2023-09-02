plugins {
    id("kolibrium.base-conventions")
    id("io.kotest")
}

dependencies {
    testImplementation(Testing.kotest.assertions.core)
}
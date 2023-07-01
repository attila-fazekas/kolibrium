plugins {
    id("kolibrium.base-conventions")
    id("io.kotest")
}

dependencies {
    testImplementation(Testing.kotest.assertions.core)
    testImplementation(Testing.kotest.framework.api)
    testImplementation(Testing.kotest.runner.junit5)
}
plugins {
    id("kolibrium.base-conventions")
    id("io.kotest")
}

dependencies {
    testImplementation(Testing.kotest.assertions.core)
    testImplementation(Testing.junit.jupiter.api)
    testImplementation(Testing.junit.jupiter.params)
    testRuntimeOnly(Testing.junit.jupiter.engine)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    maxParallelForks = Runtime.getRuntime().availableProcessors() / 2 + 1
}

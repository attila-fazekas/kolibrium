plugins {
    id("io.kotest")
    id("kolibrium.kotlin-conventions")
}

dependencies {
    testImplementation(Testing.junit.jupiter.api)
    testImplementation(Testing.junit.jupiter.params)
    testImplementation(Testing.kotest.assertions.core)
    testRuntimeOnly(Testing.junit.jupiter.engine)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    maxParallelForks = Runtime.getRuntime().availableProcessors() / 2 + 1
}

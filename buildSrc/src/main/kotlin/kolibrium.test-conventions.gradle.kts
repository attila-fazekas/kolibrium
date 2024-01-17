plugins {
    id("io.kotest")
    id("kolibrium.kotlin-conventions")
    `jvm-test-suite`
}

dependencies {
    testImplementation(Testing.junit.jupiter.api)
    testImplementation(Testing.junit.jupiter.params)
    testImplementation(Testing.kotest.assertions.core)
    testRuntimeOnly(Testing.junit.jupiter.engine)
}

testing {
    suites {
        register("konsistTest", JvmTestSuite::class) {
            dependencies {
                implementation("com.lemonappdev:konsist:_")
            }
        }
    }
}

tasks.named("check") {
    dependsOn(testing.suites.named("konsistTest"))
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    maxParallelForks = Runtime.getRuntime().availableProcessors() / 2 + 1
}

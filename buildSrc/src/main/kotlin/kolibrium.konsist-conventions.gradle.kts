plugins {
    `jvm-test-suite`
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

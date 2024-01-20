plugins {
    id("org.jlleitschuh.gradle.ktlint")
}

ktlint {
    version = "1.1.1"
    debug = true
    verbose = true
    outputToConsole = true
    outputColorName = "RED"
    ignoreFailures = false
    enableExperimentalRules = false
}

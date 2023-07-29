import de.fayard.refreshVersions.core.StabilityLevel

rootProject.name = "kolibrium"
include("core")

plugins {
    id("de.fayard.refreshVersions") version "0.51.0"
}

refreshVersions {
    rejectVersionIf {
        candidate.stabilityLevel.isLessStableThan(current.stabilityLevel)
    }
}

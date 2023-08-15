rootProject.name = "kolibrium"
include("core")
include("ksp")
include("ksp:annotations")
findProject(":ksp:annotations")?.name = "annotations"
include("ksp:processors")
findProject(":ksp:processors")?.name = "processors"

plugins {
    id("de.fayard.refreshVersions") version "0.51.0"
}

refreshVersions {
    rejectVersionIf {
        candidate.stabilityLevel.isLessStableThan(current.stabilityLevel)
    }
}

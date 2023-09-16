rootProject.name = "kolibrium"
include("core")
include("dsl")
include("ksp")
include("ksp:annotations")
findProject(":ksp:annotations")?.name = "annotations"
include("ksp:processors")

findProject(":ksp:processors")?.name = "processors"

plugins {
    id("de.fayard.refreshVersions") version "0.60.2"
}
refreshVersions {
    rejectVersionIf {
        candidate.stabilityLevel.isLessStableThan(current.stabilityLevel)
    }
}

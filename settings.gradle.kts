rootProject.name = "kolibrium"

include("bom")
include("core")
include("dsl")
include("junit")
include("konsistTest")
include("ksp")
include("ksp:annotations")
findProject(":ksp:annotations")?.name = "annotations"
include("ksp:processors")
findProject(":ksp:processors")?.name = "processors"
include("selenium")

gradle.startParameter.isContinueOnFailure = true

plugins {
    id("de.fayard.refreshVersions") version "0.60.3"
////                            # available:"0.60.4"
}

refreshVersions {
    rejectVersionIf {
        candidate.stabilityLevel.isLessStableThan(current.stabilityLevel)
    }
}

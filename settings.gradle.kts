/*
 * Copyright 2023-2025 Attila Fazekas & contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

rootProject.name = "kolibrium"

include("bom")
include("common")
include("core")
include("core:selenium")
findProject(":core:selenium")?.name = "selenium"
include("dokka")
include("dsl")
include("konsistTest")
include("ksp")
include("ksp:annotations")
findProject(":ksp:annotations")?.name = "annotations"
include("ksp:processors")
findProject(":ksp:processors")?.name = "processors"
include("test")
include("test:with-config")
include("test:without-config")

gradle.startParameter.isContinueOnFailure = true

plugins {
    id("de.fayard.refreshVersions") version "0.60.5"
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

refreshVersions {
    rejectVersionIf {
        candidate.stabilityLevel.isLessStableThan(current.stabilityLevel)
    }
}

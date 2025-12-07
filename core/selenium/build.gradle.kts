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

import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

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

plugins {
    id("kolibrium.library-conventions")
    id("kolibrium.test-conventions")
}

dependencies {
    api("org.seleniumhq.selenium:selenium-java:_")
    testImplementation("com.titusfortner:selenium-logger:_")
}

testing {
    suites {
        register("unitTest", JvmTestSuite::class) {
            dependencies {
                implementation("io.mockk:mockk:1.14.6")
            }
        }
        register("integrationTest", JvmTestSuite::class)
    }

    suites.withType(JvmTestSuite::class).configureEach {
        dependencies {
            implementation(project())
            implementation(Testing.kotest.assertions.core)
        }
    }
}

tasks.withType<KotlinCompile> {
    compilerOptions.freeCompilerArgs =
        listOf(
            "-Xcontext-parameters",
            "-opt-in=dev.kolibrium.core.selenium.InternalKolibriumApi",
        )
}

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

package dev.kolibrium.dsl.creation

import dev.kolibrium.dsl.Browser
import dev.kolibrium.dsl.Browser.Chrome
import dev.kolibrium.dsl.Browser.Edge
import dev.kolibrium.dsl.Browser.Firefox
import dev.kolibrium.dsl.Browser.Safari
import org.junit.jupiter.api.condition.OS
import org.junit.jupiter.api.condition.OS.LINUX
import org.junit.jupiter.api.condition.OS.MAC
import org.openqa.selenium.remote.service.DriverService
import java.nio.file.Path

fun DriverService.invokeMethod(methodName: String): Any {
    val method = DriverService::class.java.getDeclaredMethod(methodName)
    method.isAccessible = true
    return method.invoke(this)
}

enum class Channel {
    BETA,
    STABLE,
}

fun getExecutablePath(
    browser: Browser,
    channel: Channel = Channel.STABLE,
): String {
    val pathToExecutables = "src/test/resources/executables/"

    val distributionType =
        when (OS.current()) {
            MAC -> "mac-x64/"
            LINUX -> "linux64/"
            else -> throw DslConfigurationException("Unsupported platform")
        }

    val filename =
        when (browser) {
            Chrome -> "chromedriver"
            Firefox -> "geckodriver"
            Safari -> throw DslConfigurationException("Safari doesn't need driver executable")
            Edge -> "msedgedriver"
        }

    val pathToDistribution = pathToExecutables + distributionType
    val path = Path.of(pathToDistribution + "${browser.name.lowercase()}/${channel.name.lowercase()}/$filename")
    return path.toAbsolutePath().toString()
}

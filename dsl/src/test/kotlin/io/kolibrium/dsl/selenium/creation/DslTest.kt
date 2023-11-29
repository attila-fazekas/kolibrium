/*
 * Copyright 2023 Attila Fazekas
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

package io.kolibrium.dsl.selenium.creation

import io.kolibrium.core.Browser
import io.kolibrium.core.Browser.CHROME
import io.kolibrium.core.Browser.EDGE
import io.kolibrium.core.Browser.FIREFOX
import io.kolibrium.core.Browser.SAFARI
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

fun DriverService.getField(fieldName: String): Any {
    val field = DriverService::class.java.getDeclaredField(fieldName)
    field.isAccessible = true
    return field.get(this)
}

enum class Channel {
    BETA,
    STABLE
}

fun getExecutablePath(browser: Browser, channel: Channel = Channel.STABLE): String {
    val pathToExecutables = "src/test/resources/executables/"

    val distributionType = when (OS.current()) {
        MAC -> "mac-x64/"
        LINUX -> "linux64/"
        else -> throw DslConfigurationException("Unsupported platform")
    }

    val filename = when (browser) {
        CHROME -> "chromedriver"
        FIREFOX -> "geckodriver"
        SAFARI -> throw DslConfigurationException("Safari doesn't need driver executable")
        EDGE -> "msedgedriver"
    }

    val pathToDistribution = pathToExecutables + distributionType
    val path = Path.of(pathToDistribution + "${browser.name.lowercase()}/${channel.name.lowercase()}/$filename")
    return path.toAbsolutePath().toString()
}

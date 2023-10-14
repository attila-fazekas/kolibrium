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

package io.kolibrium.dsl

import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.openqa.selenium.chromium.ChromiumDriverLogLevel.DEBUG
import org.openqa.selenium.firefox.FirefoxDriverLogLevel.TRACE
import org.openqa.selenium.remote.service.DriverService
import java.io.File
import java.nio.file.Path
import java.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

@Suppress("UNCHECKED_CAST")
class DriverServiceTests : DslTest() {
    lateinit var ds: DriverService

    @AfterEach
    fun stopDriverService() {
        ds.stop()
    }

    @Test
    @Disabled("Due to CI")
    fun `empty driverService block should create default DriverService`() {
        ds = chromeDriverService {
        }

        ds.start()

        ds.executable shouldContain(".cache/selenium/chromedriver")

        val timeout = ds.invokeMethod("getTimeout") as Duration
        timeout shouldBe 20.seconds.toJavaDuration()

        val args = ds.invokeMethod("getArgs") as List<String>
        args shouldHaveSize 1

        val environment = ds.invokeMethod("getEnvironment") as Map<String, String>
        environment.shouldBeEmpty()
    }

    @Disabled("Due to CI is Linux machine but the used executable is Mac distribution")
    @Test
    fun `custom ChromeDriverService shall be created`(@TempDir tempDir: Path) {
        val logFilePath = tempDir.resolve("chrome.log").toString()
        val executablePath = Path.of("src/test/resources/executables/chromedriver").toAbsolutePath().toString()

        ds = chromeDriverService {
            appendLog = true
            buildCheckDisabled = true
            executable = executablePath
            logFile = logFilePath
            logLevel = DEBUG
            port = 7000
            readableTimestamp = true
            timeout = 5.seconds
            allowedIps {
                +"192.168.0.50"
                +"192.168.0.51"
            }
            environment {
                +("key1" to "value1")
                +("key2" to "value2")
            }
        }

        ds.start()

        ds.executable shouldBe executablePath

        val args = ds.invokeMethod("getArgs") as List<String>
        args shouldHaveSize 7
        args.shouldContainExactlyInAnyOrder(
            "--allowed-ips=192.168.0.50, 192.168.0.51",
            "--append-log",
            "--disable-build-check",
            "--log-level=DEBUG",
            "--log-path=$logFilePath",
            "--port=7000",
            "--readable-timestamp"
        )

        val timeout = ds.invokeMethod("getTimeout") as Duration
        timeout shouldBe 5.seconds.toJavaDuration()

        val environment = ds.invokeMethod("getEnvironment") as Map<String, String>
        environment shouldBe mapOf("key1" to "value1", "key2" to "value2")
    }

    @Test
    fun `custom GeckoDriverService shall be created`(@TempDir tempDir: Path) {
        val logFilePath = tempDir.resolve("firefox.log").toString()
        val executablePath = Path.of("src/test/resources/executables/geckodriver").toAbsolutePath().toString()

        ds = geckoDriverService {
            executable = executablePath
            logFile = logFilePath
            logLevel = TRACE
            port = 7001
            profileRoot = tempDir.toString()
            truncatedLogs = false
//            allowedHosts {
//                +"localhost"
//            }
            environment {
                +("key1" to "value1")
                +("key2" to "value2")
            }
        }

        ds.start()

        val args = ds.invokeMethod("getArgs") as List<String>
        args shouldHaveSize 11
        args.shouldContainAll(
            "--port=7001",
            "--log",
            "trace",
            "--log-no-truncate",
            "--profile-root",
            tempDir.toString()
        )

        ds.executable shouldBe executablePath

        val environment = ds.invokeMethod("getEnvironment") as Map<String, String>
        environment shouldBe mapOf("key1" to "value1", "key2" to "value2")

        File(logFilePath).exists() shouldBe true
    }

    @Disabled("Temporarily disabled due to CI runs a Linux machine")
    @Test
    fun `custom SafariDriverService shall be created`() {
        ds = safariDriverService {
            logging = true
            port = 7002
            environment {
                +("key1" to "value1")
                +("key2" to "value2")
            }
        }

        ds.start()

        val args = ds.invokeMethod("getArgs") as List<String>
        args shouldHaveSize 3
        args.shouldContainExactly("--port", "7002", "--diagnose")

        val environment = ds.invokeMethod("getEnvironment") as Map<String, String>
        environment shouldBe mapOf("key1" to "value1", "key2" to "value2")
    }

    @Disabled("Due to CI is Linux machine but the used executable is Mac distribution")
    @Test
    fun `custom EdgeDriverService shall be created`(@TempDir tempDir: Path) {
        val logFilePath = tempDir.resolve("edge.log").toString()
        val executablePath = Path.of("src/test/resources/executables/msedgedriver").toAbsolutePath().toString()

        ds = edgeDriverService {
            appendLog = true
            buildCheckDisabled = true
            executable = executablePath
            logFile = logFilePath
            logLevel = DEBUG
            port = 7003
            readableTimestamp = true
            timeout = 5.seconds
            allowedIps {
                +"192.168.0.50"
                +"192.168.0.51"
            }
            environment {
                +("key1" to "value1")
                +("key2" to "value2")
            }
        }

        ds.start()

        ds.executable shouldBe executablePath

        val args = ds.invokeMethod("getArgs") as List<String>
        args shouldHaveSize 7
        args.shouldContainExactlyInAnyOrder(
            "--allowed-ips=192.168.0.50, 192.168.0.51",
            "--append-log",
            "--disable-build-check",
            "--log-level=DEBUG",
            "--log-path=$logFilePath",
            "--port=7003",
            "--readable-timestamp"
        )

        val timeout = ds.invokeMethod("getTimeout") as Duration
        timeout shouldBe 5.seconds.toJavaDuration()

        val environment = ds.invokeMethod("getEnvironment") as Map<String, String>
        environment shouldBe mapOf("key1" to "value1", "key2" to "value2")
    }
}

/*
 * Copyright 2023-2026 Attila Fazekas & contributors
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

package dev.kolibrium.webdriver.descriptors

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import java.nio.file.Files
import java.nio.file.Path

abstract class AbstractElementDescriptorIntegrationTest {
    protected lateinit var driver: WebDriver
    private lateinit var htmlFile: Path

    @BeforeEach
    fun startDriver() {
        driver =
            ChromeDriver(
                ChromeOptions().apply {
                    addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage")
                },
            )
        htmlFile = writeHtml(html())
        driver.get("file://$htmlFile")
    }

    @AfterEach
    fun stopDriver() {
        driver.quit()
        Files.deleteIfExists(htmlFile)
    }

    protected abstract fun html(): String

    private fun writeHtml(content: String): Path =
        Files
            .createTempFile("kolibrium-wd-it-", ".html")
            .also { Files.writeString(it, content) }
}

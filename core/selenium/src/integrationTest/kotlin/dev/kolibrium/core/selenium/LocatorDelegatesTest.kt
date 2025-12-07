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

package dev.kolibrium.core.selenium

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import java.nio.file.Files
import java.nio.file.Path
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Integration tests using real WebDriver instances.
 * Requires ChromeDriver to be available on the system PATH.
 */
class LocatorDelegatesTest {
    private lateinit var driver: WebDriver
    private lateinit var testHtmlPath: Path

    @BeforeEach
    fun setup() {
        // Create a headless Chrome instance
        val options =
            ChromeOptions().apply {
                addArguments("--headless=new")
                addArguments("--no-sandbox")
                addArguments("--disable-dev-shm-usage")
                addArguments("--disable-gpu")
            }
        driver = ChromeDriver(options)

        // Create test HTML file
        testHtmlPath = createTestHtmlFile()
        driver.get("file://$testHtmlPath")
    }

    @AfterEach
    fun tearDown() {
        driver.quit()
        Files.deleteIfExists(testHtmlPath)
    }

    // Convenience to execute each test with a contextual WebDriver
    private inline fun <T> withPage(block: () -> T): T = withDriver(driver, block)

    @Test
    fun `should find element by id`(): Unit =
        withPage {
            // Given
            val page = TestPage()

            // When
            val element = page.headerById

            // Then
            element.text shouldBe "Test Page"
            element.tagName shouldBe "h1"
        }

    @Test
    fun `should find element by css selector`(): Unit =
        withPage {
            // Given
            val page = TestPage()

            // When
            val element = page.submitButton

            // Then
            element.text shouldBe "Submit"
            element.getAttribute("type") shouldBe "submit"
        }

    @Test
    fun `should find element by data-test attribute`(): Unit =
        withPage {
            // Given
            val page = TestPage()

            // When
            val element = page.usernameInput

            // Then
            element.getAttribute("name") shouldBe "username"
            element.tagName shouldBe "input"
        }

    @Test
    fun `should find multiple elements`(): Unit =
        withPage {
            // Given
            val page = TestPage()

            // When
            val elements = page.listItems

            // Then
            elements shouldHaveSize 3
            elements[0].text shouldBe "Item 1"
            elements[1].text shouldBe "Item 2"
            elements[2].text shouldBe "Item 3"
        }

    @Test
    fun `should cache element lookup when cacheLookup is true`(): Unit =
        withPage {
            // Given
            val page = TestPage()

            // When - Get element multiple times
            val first = page.cachedElement
            val firstId = first.getAttribute("data-instance-id")

            val second = page.cachedElement
            val secondId = second.getAttribute("data-instance-id")

            // Then - Should be the same element reference
            firstId shouldBe secondId
        }

    @Test
    fun `should not cache element when cacheLookup is false`(): Unit =
        withPage {
            // Given
            val page = TestPage()

            // When
            val first = page.nonCachedElement
            val second = page.nonCachedElement

            // Then - Both lookups should succeed (no caching means fresh lookup each time)
            first.text shouldBe "Dynamic Content"
            second.text shouldBe "Dynamic Content"
        }

    @Test
    fun `should wait for element to become displayed`(): Unit =
        withPage {
            // Given
            val page = TestPage()

            // Trigger delayed element display
            (driver as JavascriptExecutor).executeScript(
                """
            setTimeout(function() {
                document.getElementById('delayed').style.display = 'block';
            }, 500);
        """,
            )

            // When
            val element = page.delayedElement

            // Then
            element.isDisplayed shouldBe true
            element.text shouldBe "Delayed Content"
        }

    @Test
    fun `should respect custom readyWhen condition`(): Unit =
        withPage {
            // Given
            val page = TestPage()

            // Enable button after delay
            (driver as JavascriptExecutor).executeScript(
                """
            setTimeout(function() {
                document.getElementById('disabled-btn').disabled = false;
            }, 300);
        """,
            )

            // When
            val button = page.enabledButton

            // Then
            button.isEnabled shouldBe true
        }

    @Test
    fun `should find elements within another element`(): Unit =
        withPage {
            // Given
            val page = TestPage()

            // When - Find list, then find items within it
            val listContainer = page.listContainer
            val itemsInList = listContainer.cssSelectors("li")

            // Then
            itemsInList.get() shouldHaveSize 3
        }

    @Test
    fun `should work with xpath locators`(): Unit =
        withPage {
            // Given
            val page = TestPage()

            // When
            val element = page.elementByXpath

            // Then
            element.text shouldBe "Found by XPath"
        }

    @Test
    fun `should find links by text`(): Unit =
        withPage {
            // Given
            val page = TestPage()

            // When
            val link = page.googleLink

            // Then
            link.text shouldBe "Go to Google"
            link.getAttribute("href") shouldContain "google.com"
        }

    @Test
    fun `should find links by partial text`(): Unit =
        withPage {
            // Given
            val page = TestPage()

            // When
            val link = page.partialLink

            // Then
            link.text shouldContain "Documentation"
        }

    @Test
    fun `should throw timeout exception when element not found`(): Unit =
        withPage {
            // Given
            val page = TestPage()

            // When/Then
            shouldThrow<org.openqa.selenium.TimeoutException> {
                page.nonExistentElement
            }
        }

    @Test
    fun `should work with multiple data attribute conventions`(): Unit =
        withPage {
            // Given
            val page = TestPage()

            // When
            val byDataTest = page.elementByDataTest
            val byDataTestId = page.elementByDataTestId
            val byDataQa = page.elementByDataQa

            // Then
            byDataTest.text shouldBe "Test Data"
            byDataTestId.text shouldBe "TestId Data"
            byDataQa.text shouldBe "QA Data"
        }

    @Test
    fun `should handle stale elements by retrying`(): Unit =
        withPage {
            // Given
            val page = TestPage()

            // Get initial element
            val initial = page.dynamicContent
            initial.text shouldBe "Initial Content"

            // Replace the element in DOM
            (driver as JavascriptExecutor).executeScript(
                """
            var old = document.getElementById('dynamic-content');
            var newEl = document.createElement('div');
            newEl.id = 'dynamic-content';
            newEl.setAttribute('data-testid', 'dynamic');
            newEl.textContent = 'Replaced Content';
            old.parentNode.replaceChild(newEl, old);
        """,
            )

            // When - Access through non-cached descriptor should get new element
            val refreshed = page.dynamicContentNonCached

            // Then
            refreshed.text shouldBe "Replaced Content"
        }

    @Test
    fun `should work with Site context and custom wait config`(): Unit =
        withPage {
            // Given
            val site = TestSite()
            SessionContext.withSession(Session(driver, site)) {
                val page = TestPageWithSite()

                // When
                val element = page.patientElement

                // Then
                element.text shouldBe "Test Page"
            }
        }

    @Test
    fun `should find all checkboxes and verify state`(): Unit =
        withPage {
            // Given
            val page = TestPage()

            // When
            val checkboxes = page.allCheckboxes

            // Then
            checkboxes shouldHaveSize 2
            checkboxes[0].isSelected shouldBe false
            checkboxes[1].isSelected shouldBe false
        }

    @Test
    fun `property delegation should work in Page class`(): Unit =
        withPage {
            // Given
            val page = TestPage()

            // When - Access via property delegation
            val header = page.headerById
            val items = page.listItems

            // Then
            header.text shouldBe "Test Page"
            items shouldHaveSize 3
        }

    private fun createTestHtmlFile(): Path {
        val html =
            """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Test Page</title>
                <style>
                    #delayed { display: none; }
                </style>
            </head>
            <body>
                <h1 id="header">Test Page</h1>

                <form id="test-form">
                    <input type="text" name="username" data-test="username-input" />
                    <input type="password" name="password" />
                    <button type="submit" class="btn-primary">Submit</button>
                    <button id="disabled-btn" disabled>Disabled Button</button>
                </form>

                <ul id="list-container">
                    <li class="list-item">Item 1</li>
                    <li class="list-item">Item 2</li>
                    <li class="list-item">Item 3</li>
                </ul>

                <div id="cached" data-instance-id="instance-1">Cached Element</div>
                <div id="non-cached">Dynamic Content</div>

                <div id="delayed">Delayed Content</div>

                <div data-test="test-data">Test Data</div>
                <div data-testid="testid-data">TestId Data</div>
                <div data-qa="qa-data">QA Data</div>

                <div id="dynamic-content" data-testid="dynamic">Initial Content</div>

                <div class="special">
                    <span>Found by XPath</span>
                </div>

                <a href="https://google.com">Go to Google</a>
                <a href="/docs">Read the Documentation</a>

                <input type="checkbox" name="option1" value="1" />
                <input type="checkbox" name="option2" value="2" />
            </body>
            </html>
            """.trimIndent()

        val tempFile = Files.createTempFile("kolibrium-test-", ".html")
        Files.writeString(tempFile, html)
        return tempFile
    }

    // Test Page Object
    private class TestPage : Page<Site>() {
        val headerById by id("header")
        val submitButton by cssSelector("button.btn-primary")
        val usernameInput by dataTest("username-input")
        val listItems by cssSelectors(".list-item")
        val cachedElement by id("cached", cacheLookup = true)
        val nonCachedElement by id("non-cached", cacheLookup = false)

        val delayedElement by id(
            "delayed",
            waitConfig =
                WaitConfig(
                    timeout = 2.seconds,
                    pollingInterval = 100.milliseconds,
                ),
        )

        val enabledButton by id(
            "disabled-btn",
            cacheLookup = false,
            waitConfig =
                WaitConfig(
                    timeout = 1.seconds,
                    pollingInterval = 100.milliseconds,
                ),
            readyWhen = { isEnabled },
        )

        val listContainer by id("list-container")
        val elementByXpath by xpath("//div[@class='special']/span")
        val googleLink by linkText("Go to Google")
        val partialLink by partialLinkText("Documentation")

        val nonExistentElement by id(
            "does-not-exist",
            waitConfig =
                WaitConfig(
                    timeout = 500.milliseconds,
                    pollingInterval = 100.milliseconds,
                ),
        )

        val elementByDataTest by dataTest("test-data")
        val elementByDataTestId by dataTestId("testid-data")
        val elementByDataQa by dataQa("qa-data")

        val dynamicContent by dataTestId("dynamic", cacheLookup = true)
        val dynamicContentNonCached by dataTestId("dynamic", cacheLookup = false)

        val allCheckboxes by cssSelectors("input[type='checkbox']")
    }

    // Test Page with Site context
    private class TestPageWithSite : Page<TestSite>() {
        val patientElement by id("header")
    }

    // Test Site
    private class TestSite : Site("file://test") {
        override val waitConfig = WaitConfig.Patient
    }
}

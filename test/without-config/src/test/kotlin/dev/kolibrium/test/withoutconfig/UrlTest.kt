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

package dev.kolibrium.test.withoutconfig

import dev.kolibrium.common.InternalKolibriumApi
import dev.kolibrium.common.config.ProjectConfigurationException
import dev.kolibrium.core.selenium.browserTest
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.html.respondHtml
import io.ktor.server.netty.Netty
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import java.nio.file.Paths
import kotlinx.html.body
import kotlinx.html.h1
import kotlinx.html.head
import kotlinx.html.title
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

@OptIn(InternalKolibriumApi::class)
class UrlTest {

    @BeforeEach
    fun startServer() {

    }

    @Test
    fun `baseUrl is not set`() {
        val exception = shouldThrow<ProjectConfigurationException> {
            browserTest {
            }
        }

        exception.message shouldBe "\"baseUrl\" was neither configured through project-level settings nor provided as a parameter!"
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "http:/www.kolibrium.dev/",
            "https://kolibriumdev",
            "http//www.kolibrium.dev/",
        ]
    )
    fun `baseUrl is invalid`(url: String) {
        val exception = shouldThrow<ProjectConfigurationException> {
            browserTest(url = url) {
            }
        }

        exception.message shouldBe "Provided $url URL is invalid!"
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "kolibrium.dev",
            "www.kolibrium.dev",
            "http://www.kolibrium.dev/",
            "https://kolibrium.dev",
            "http://www.kolibrium.dev/",
            "http://kolibrium.dev",
        ]
    )
    fun `baseUrl is valid`(url: String) {
        shouldNotThrow<ProjectConfigurationException> {
            browserTest(url = url) {
            }
        }
    }

    fun Application.module() {
        routing {
            get("/") {
                val name = "Ktor"
                call.respondHtml(OK) {
                    head {

                    }
                    body {
                        h1 {
                            +"Hello from $name!"
                        }
                    }
                }
            }
        }
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "localhost",
            "http://localhost",
            "127.0.0.1",
            "http://127.0.0.1",
            "http://127.0.0.1",
        ]
    )
    fun `baseUrl is valid - needs server`(url: String) {
        val server = embeddedServer(Netty, port = 0) {
            routing {
                get("/") {
                    call.respondHtml(OK) {
                        head {
                            title {
                                +"Kolibrium"
                            }
                        }
                        body {
                            h1 {
                                +"Hello from Kolibrium!"
                            }
                        }
                    }
                }
            }
        }.start(wait = false)

        val port = server.engineConfig.connectors.first().port

        shouldNotThrow<ProjectConfigurationException> {
            browserTest(url = "$url:$port") {
            }
        }

        server.stop()
    }


    @Test
    fun `baseUrl is set to file system URL`() {
        shouldNotThrow<ProjectConfigurationException> {
            browserTest(url = getHomePage()) {
            }
        }
    }

    private fun getHomePage() =
        Paths
            .get("")
            .toAbsolutePath()
            .parent
            .parent
            .resolve("pages/home.html")
            .toUri()
            .toString()
}

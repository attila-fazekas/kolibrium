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

package dev.kolibrium.test.withconfig

import dev.kolibrium.core.selenium.browserTest
import dev.kolibrium.test.pages.generated.absoluteUrlKolibriumPage
import dev.kolibrium.test.pages.generated.relativeUrlKolibriumPage
import dev.kolibrium.test.pages.generated.rootRelativeUrlKolibriumPage
import io.kotest.matchers.shouldBe
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.engine.embeddedServer
import io.ktor.server.html.respondHtml
import io.ktor.server.netty.Netty
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.html.body
import kotlinx.html.h1
import kotlinx.html.head
import kotlinx.html.title
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class UrlTest {
    @Test
    fun `baseUrl is set in project-level settings`() {
        browserTest {
            title shouldBe "Kolibrium test page"
        }
    }

    @Test
    fun `baseUrl set in project-level settings and overwritten in url param`() {
        browserTest(
            url = "https://kolibrium.dev"
        ) {
            title shouldBe "Kotlin library for Selenium tests | Kolibrium"
        }
    }

    @Test
    fun `baseUrl is set in project-level - an absolute URL combined with the baseUrl`() {
        browserTest {
            absoluteUrlKolibriumPage {
                title shouldBe "Kotlin library for Selenium tests | Kolibrium"
            }
        }
    }

    @Test
    fun `baseUrl is set in project-level - a relative URL combined with the baseUrl`() {
        browserTest {
            relativeUrlKolibriumPage {
                title shouldBe "Kolibrium - this is a relative URL"
            }
        }
    }

    @Test
    fun `baseUrl is set in project-level - a root-relative URL combined with the baseUrl`() {
        browserTest {
            rootRelativeUrlKolibriumPage {
                title shouldBe "Kolibrium - this is a root-relative URL"
            }
        }
    }

    companion object {
        val server =
            embeddedServer(Netty, port = 3003) {
                routing {
                    get("/") {
                        call.respondHtml(OK) {
                            head {
                                title {
                                    +"Kolibrium test page"
                                }
                            }
                            body {
                                h1 {
                                    +"Kolibrium test page"
                                }
                            }
                        }
                    }
                    get("/hello.html") {
                        call.respondHtml(OK) {
                            head {
                                title {
                                    +"Kolibrium - this is a relative URL"
                                }
                            }
                            body {
                                h1 {
                                    +"Hello from relative URL!"
                                }
                            }
                        }
                    }
                    get("/hello") {
                        call.respondHtml(OK) {
                            head {
                                title {
                                    +"Kolibrium - this is a root-relative URL"
                                }
                            }
                            body {
                                h1 {
                                    +"Hello from root-relative URL!"
                                }
                            }
                        }
                    }
                }
            }

        @BeforeAll
        @JvmStatic
        fun setUp() {
            server.start(wait = false)
        }

        @AfterAll
        @JvmStatic
        fun tearDown() {
            server.stop()
        }
    }
}

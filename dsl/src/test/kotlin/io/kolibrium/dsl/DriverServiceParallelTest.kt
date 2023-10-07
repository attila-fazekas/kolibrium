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

import io.kolibrium.dsl.chromium.chrome.logLevel
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.openqa.selenium.chromium.ChromiumDriverLogLevel
import org.openqa.selenium.remote.service.DriverService

@Suppress("UNCHECKED_CAST")
class DriverServiceParallelTest : DslTest() {
    lateinit var ds: DriverService

    @AfterEach
    fun stopDriverService() {
        ds.stop()
    }

    @Test
    fun test1() {
        ds = chromeDriverService {
            logLevel = ChromiumDriverLogLevel.DEBUG
            environment {
                +("key1" to "value1")
            }
        }
        ds.start()

        val args = ds.invokeMethod("getArgs") as List<String>
        args shouldHaveSize 2
        args.shouldContain("--log-level=DEBUG")

        val environment = ds.invokeMethod("getEnvironment") as Map<String, String>
        environment shouldBe mapOf("key1" to "value1")
    }

    @Test
    fun test2() {
        ds = chromeDriverService {
            logLevel = ChromiumDriverLogLevel.INFO
            environment {
                +("key2" to "value2")
            }
        }
        ds.start()

        val args = ds.invokeMethod("getArgs") as List<String>
        args shouldHaveSize 2
        args.shouldContain("--log-level=INFO")

        val environment = ds.invokeMethod("getEnvironment") as Map<String, String>
        environment shouldBe mapOf("key2" to "value2")
    }

    @Test
    fun test3() {
        ds = chromeDriverService {
            logLevel = ChromiumDriverLogLevel.WARNING
            environment {
                +("key3" to "value3")
            }
        }
        ds.start()

        val args = ds.invokeMethod("getArgs") as List<String>
        args shouldHaveSize 2
        args.shouldContain("--log-level=WARNING")

        val environment = ds.invokeMethod("getEnvironment") as Map<String, String>
        environment shouldBe mapOf("key3" to "value3")
    }

    @Test
    fun test4() {
        ds = chromeDriverService {
            logLevel = ChromiumDriverLogLevel.ALL
            environment {
                +("key4" to "value4")
            }
        }
        ds.start()

        val args = ds.invokeMethod("getArgs") as List<String>
        args shouldHaveSize 2
        args.shouldContain("--log-level=ALL")

        val environment = ds.invokeMethod("getEnvironment") as Map<String, String>
        environment shouldBe mapOf("key4" to "value4")
    }

    @Test
    fun test5() {
        ds = chromeDriverService {
            logLevel = ChromiumDriverLogLevel.SEVERE
            environment {
                +("key5" to "value5")
            }
        }
        ds.start()

        val args = ds.invokeMethod("getArgs") as List<String>
        args shouldHaveSize 2
        args.shouldContain("--log-level=SEVERE")

        val environment = ds.invokeMethod("getEnvironment") as Map<String, String>
        environment shouldBe mapOf("key5" to "value5")
    }

    @Test
    fun test6() {
        ds = chromeDriverService {
            logLevel = ChromiumDriverLogLevel.DEBUG
            environment {
                +("key6" to "value6")
            }
        }
        ds.start()

        val args = ds.invokeMethod("getArgs") as List<String>
        args shouldHaveSize 2
        args.shouldContain("--log-level=DEBUG")

        val environment = ds.invokeMethod("getEnvironment") as Map<String, String>
        environment shouldBe mapOf("key6" to "value6")
    }

    @Test
    fun test7() {
        ds = chromeDriverService {
            logLevel = ChromiumDriverLogLevel.INFO
            environment {
                +("key7" to "value7")
            }
        }
        ds.start()

        val args = ds.invokeMethod("getArgs") as List<String>
        args shouldHaveSize 2
        args.shouldContain("--log-level=INFO")

        val environment = ds.invokeMethod("getEnvironment") as Map<String, String>
        environment shouldBe mapOf("key7" to "value7")
    }

    @Test
    fun test8() {
        ds = chromeDriverService {
            logLevel = ChromiumDriverLogLevel.WARNING
            environment {
                +("key8" to "value8")
            }
        }
        ds.start()

        val args = ds.invokeMethod("getArgs") as List<String>
        args shouldHaveSize 2
        args.shouldContain("--log-level=WARNING")

        val environment = ds.invokeMethod("getEnvironment") as Map<String, String>
        environment shouldBe mapOf("key8" to "value8")
    }

    @Test
    fun test9() {
        ds = chromeDriverService {
            logLevel = ChromiumDriverLogLevel.DEBUG
            environment {
                +("key9" to "value9")
            }
        }
        ds.start()

        val args = ds.invokeMethod("getArgs") as List<String>
        args shouldHaveSize 2
        args.shouldContain("--log-level=DEBUG")

        val environment = ds.invokeMethod("getEnvironment") as Map<String, String>
        environment shouldBe mapOf("key9" to "value9")
    }

    @Test
    fun test10() {
        ds = chromeDriverService {
            logLevel = ChromiumDriverLogLevel.ALL
            environment {
                +("key10" to "value10")
            }
        }
        ds.start()

        val args = ds.invokeMethod("getArgs") as List<String>
        args shouldHaveSize 2
        args.shouldContain("--log-level=ALL")

        val environment = ds.invokeMethod("getEnvironment") as Map<String, String>
        environment shouldBe mapOf("key10" to "value10")
    }

    @Test
    fun test11() {
        ds = chromeDriverService {
            logLevel = ChromiumDriverLogLevel.SEVERE
            environment {
                +("key11" to "value11")
            }
        }
        ds.start()

        val args = ds.invokeMethod("getArgs") as List<String>
        args shouldHaveSize 2
        args.shouldContain("--log-level=SEVERE")

        val environment = ds.invokeMethod("getEnvironment") as Map<String, String>
        environment shouldBe mapOf("key11" to "value11")
    }

    @Test
    fun test12() {
        ds = chromeDriverService {
            logLevel = ChromiumDriverLogLevel.DEBUG
            environment {
                +("key12" to "value12")
            }
        }
        ds.start()

        val args = ds.invokeMethod("getArgs") as List<String>
        args shouldHaveSize 2
        args.shouldContain("--log-level=DEBUG")

        val environment = ds.invokeMethod("getEnvironment") as Map<String, String>
        environment shouldBe mapOf("key12" to "value12")
    }

    @Test
    fun test13() {
        ds = chromeDriverService {
            logLevel = ChromiumDriverLogLevel.INFO
            environment {
                +("key13" to "value13")
            }
        }
        ds.start()

        val args = ds.invokeMethod("getArgs") as List<String>
        args shouldHaveSize 2
        args.shouldContain("--log-level=INFO")

        val environment = ds.invokeMethod("getEnvironment") as Map<String, String>
        environment shouldBe mapOf("key13" to "value13")
    }

    @Test
    fun test14() {
        ds = chromeDriverService {
            logLevel = ChromiumDriverLogLevel.WARNING
            environment {
                +("key14" to "value14")
            }
        }
        ds.start()

        val args = ds.invokeMethod("getArgs") as List<String>
        args shouldHaveSize 2
        args.shouldContain("--log-level=WARNING")

        val environment = ds.invokeMethod("getEnvironment") as Map<String, String>
        environment shouldBe mapOf("key14" to "value14")
    }

    @Test
    fun test15() {
        ds = chromeDriverService {
            logLevel = ChromiumDriverLogLevel.ALL
            environment {
                +("key15" to "value15")
            }
        }
        ds.start()

        val args = ds.invokeMethod("getArgs") as List<String>
        args shouldHaveSize 2
        args.shouldContain("--log-level=ALL")

        val environment = ds.invokeMethod("getEnvironment") as Map<String, String>
        environment shouldBe mapOf("key15" to "value15")
    }

    @Test
    fun test16() {
        ds = chromeDriverService {
            logLevel = ChromiumDriverLogLevel.SEVERE
            environment {
                +("key16" to "value16")
            }
        }
        ds.start()

        val args = ds.invokeMethod("getArgs") as List<String>
        args shouldHaveSize 2
        args.shouldContain("--log-level=SEVERE")

        val environment = ds.invokeMethod("getEnvironment") as Map<String, String>
        environment shouldBe mapOf("key16" to "value16")
    }

    @Test
    fun test17() {
        ds = chromeDriverService {
            logLevel = ChromiumDriverLogLevel.INFO
            environment {
                +("key17" to "value17")
            }
        }
        ds.start()

        val args = ds.invokeMethod("getArgs") as List<String>
        args shouldHaveSize 2
        args.shouldContain("--log-level=INFO")

        val environment = ds.invokeMethod("getEnvironment") as Map<String, String>
        environment shouldBe mapOf("key17" to "value17")
    }

    @Test
    fun test18() {
        ds = chromeDriverService {
            logLevel = ChromiumDriverLogLevel.WARNING
            environment {
                +("key18" to "value18")
            }
        }
        ds.start()

        val args = ds.invokeMethod("getArgs") as List<String>
        args shouldHaveSize 2
        args.shouldContain("--log-level=WARNING")

        val environment = ds.invokeMethod("getEnvironment") as Map<String, String>
        environment shouldBe mapOf("key18" to "value18")
    }

    @Test
    fun test19() {
        ds = chromeDriverService {
            logLevel = ChromiumDriverLogLevel.ALL
            environment {
                +("key19" to "value19")
            }
        }
        ds.start()

        val args = ds.invokeMethod("getArgs") as List<String>
        args shouldHaveSize 2
        args.shouldContain("--log-level=ALL")

        val environment = ds.invokeMethod("getEnvironment") as Map<String, String>
        environment shouldBe mapOf("key19" to "value19")
    }

    @Test
    fun test20() {
        ds = chromeDriverService {
            logLevel = ChromiumDriverLogLevel.SEVERE
            environment {
                +("key20" to "value20")
            }
        }
        ds.start()

        val args = ds.invokeMethod("getArgs") as List<String>
        args shouldHaveSize 2
        args.shouldContain("--log-level=SEVERE")

        val environment = ds.invokeMethod("getEnvironment") as Map<String, String>
        environment shouldBe mapOf("key20" to "value20")
    }
}

/*
 * Copyright 2023-2024 Attila Fazekas & contributors
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

package dev.kolibrium.dsl.selenium.creation

import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.RepeatedTest
import org.openqa.selenium.chromium.ChromiumDriverLogLevel.ALL
import org.openqa.selenium.chromium.ChromiumDriverLogLevel.DEBUG
import org.openqa.selenium.chromium.ChromiumDriverLogLevel.INFO
import org.openqa.selenium.chromium.ChromiumDriverLogLevel.SEVERE
import org.openqa.selenium.chromium.ChromiumDriverLogLevel.WARNING
import org.openqa.selenium.remote.service.DriverService

@Suppress("UNCHECKED_CAST")
@Disabled
class DriverServiceParallelTest {
    lateinit var ds: DriverService

    @AfterEach
    fun stopDriverService() {
        ds.stop()
    }

    @RepeatedTest(10)
    fun test1() {
        ds =
            chromeDriverService {
                logLevel = DEBUG
                environments {
                    environment("key1", "value1")
                }
            }
        ds.start()

        val args = ds.invokeMethod("getArgs") as List<String>
        args shouldHaveSize 2
        args.shouldContain("--log-level=DEBUG")

        val environment = ds.invokeMethod("getEnvironment") as Map<String, String>
        environment shouldBe mapOf("key1" to "value1")
    }

    @RepeatedTest(10)
    fun test2() {
        ds =
            chromeDriverService {
                logLevel = INFO
                environments {
                    environment("key2", "value2")
                }
            }
        ds.start()

        val args = ds.invokeMethod("getArgs") as List<String>
        args shouldHaveSize 2
        args.shouldContain("--log-level=INFO")

        val environment = ds.invokeMethod("getEnvironment") as Map<String, String>
        environment shouldBe mapOf("key2" to "value2")
    }

    @RepeatedTest(10)
    fun test3() {
        ds =
            chromeDriverService {
                logLevel = WARNING
                environments {
                    environment("key3", "value3")
                }
            }
        ds.start()

        val args = ds.invokeMethod("getArgs") as List<String>
        args shouldHaveSize 2
        args.shouldContain("--log-level=WARNING")

        val environment = ds.invokeMethod("getEnvironment") as Map<String, String>
        environment shouldBe mapOf("key3" to "value3")
    }

    @RepeatedTest(10)
    fun test4() {
        ds =
            chromeDriverService {
                logLevel = ALL
                environments {
                    environment("key4", "value4")
                }
            }
        ds.start()

        val args = ds.invokeMethod("getArgs") as List<String>
        args shouldHaveSize 2
        args.shouldContain("--log-level=ALL")

        val environment = ds.invokeMethod("getEnvironment") as Map<String, String>
        environment shouldBe mapOf("key4" to "value4")
    }

    @RepeatedTest(10)
    fun test5() {
        ds =
            chromeDriverService {
                logLevel = SEVERE
                environments {
                    environment("key5", "value5")
                }
            }
        ds.start()

        val args = ds.invokeMethod("getArgs") as List<String>
        args shouldHaveSize 2
        args.shouldContain("--log-level=SEVERE")

        val environment = ds.invokeMethod("getEnvironment") as Map<String, String>
        environment shouldBe mapOf("key5" to "value5")
    }

    @RepeatedTest(10)
    fun test6() {
        ds =
            chromeDriverService {
                logLevel = DEBUG
                environments {
                    environment("key6", "value6")
                }
            }
        ds.start()

        val args = ds.invokeMethod("getArgs") as List<String>
        args shouldHaveSize 2
        args.shouldContain("--log-level=DEBUG")

        val environment = ds.invokeMethod("getEnvironment") as Map<String, String>
        environment shouldBe mapOf("key6" to "value6")
    }

    @RepeatedTest(10)
    fun test7() {
        ds =
            chromeDriverService {
                logLevel = INFO
                environments {
                    environment("key7", "value7")
                }
            }
        ds.start()

        val args = ds.invokeMethod("getArgs") as List<String>
        args shouldHaveSize 2
        args.shouldContain("--log-level=INFO")

        val environment = ds.invokeMethod("getEnvironment") as Map<String, String>
        environment shouldBe mapOf("key7" to "value7")
    }

    @RepeatedTest(10)
    fun test8() {
        ds =
            chromeDriverService {
                logLevel = WARNING
                environments {
                    environment("key8", "value8")
                }
            }
        ds.start()

        val args = ds.invokeMethod("getArgs") as List<String>
        args shouldHaveSize 2
        args.shouldContain("--log-level=WARNING")

        val environment = ds.invokeMethod("getEnvironment") as Map<String, String>
        environment shouldBe mapOf("key8" to "value8")
    }

    @RepeatedTest(10)
    fun test9() {
        ds =
            chromeDriverService {
                logLevel = DEBUG
                environments {
                    environment("key9", "value9")
                }
            }
        ds.start()

        val args = ds.invokeMethod("getArgs") as List<String>
        args shouldHaveSize 2
        args.shouldContain("--log-level=DEBUG")

        val environment = ds.invokeMethod("getEnvironment") as Map<String, String>
        environment shouldBe mapOf("key9" to "value9")
    }

    @RepeatedTest(10)
    fun test10() {
        ds =
            chromeDriverService {
                logLevel = ALL
                environments {
                    environment("key10", "value10")
                }
            }
        ds.start()

        val args = ds.invokeMethod("getArgs") as List<String>
        args shouldHaveSize 2
        args.shouldContain("--log-level=ALL")

        val environment = ds.invokeMethod("getEnvironment") as Map<String, String>
        environment shouldBe mapOf("key10" to "value10")
    }

    @RepeatedTest(10)
    fun test11() {
        ds =
            chromeDriverService {
                logLevel = SEVERE
                environments {
                    environment("key11", "value11")
                }
            }
        ds.start()

        val args = ds.invokeMethod("getArgs") as List<String>
        args shouldHaveSize 2
        args.shouldContain("--log-level=SEVERE")

        val environment = ds.invokeMethod("getEnvironment") as Map<String, String>
        environment shouldBe mapOf("key11" to "value11")
    }

    @RepeatedTest(10)
    fun test12() {
        ds =
            chromeDriverService {
                logLevel = DEBUG
                environments {
                    environment("key12", "value12")
                }
            }
        ds.start()

        val args = ds.invokeMethod("getArgs") as List<String>
        args shouldHaveSize 2
        args.shouldContain("--log-level=DEBUG")

        val environment = ds.invokeMethod("getEnvironment") as Map<String, String>
        environment shouldBe mapOf("key12" to "value12")
    }

    @RepeatedTest(10)
    fun test13() {
        ds =
            chromeDriverService {
                logLevel = INFO
                environments {
                    environment("key13", "value13")
                }
            }
        ds.start()

        val args = ds.invokeMethod("getArgs") as List<String>
        args shouldHaveSize 2
        args.shouldContain("--log-level=INFO")

        val environment = ds.invokeMethod("getEnvironment") as Map<String, String>
        environment shouldBe mapOf("key13" to "value13")
    }

    @RepeatedTest(10)
    fun test14() {
        ds =
            chromeDriverService {
                logLevel = WARNING
                environments {
                    environment("key14", "value14")
                }
            }
        ds.start()

        val args = ds.invokeMethod("getArgs") as List<String>
        args shouldHaveSize 2
        args.shouldContain("--log-level=WARNING")

        val environment = ds.invokeMethod("getEnvironment") as Map<String, String>
        environment shouldBe mapOf("key14" to "value14")
    }

    @RepeatedTest(10)
    fun test15() {
        ds =
            chromeDriverService {
                logLevel = ALL
                environments {
                    environment("key15", "value15")
                }
            }
        ds.start()

        val args = ds.invokeMethod("getArgs") as List<String>
        args shouldHaveSize 2
        args.shouldContain("--log-level=ALL")

        val environment = ds.invokeMethod("getEnvironment") as Map<String, String>
        environment shouldBe mapOf("key15" to "value15")
    }

    @RepeatedTest(10)
    fun test16() {
        ds =
            chromeDriverService {
                logLevel = SEVERE
                environments {
                    environment("key16", "value16")
                }
            }
        ds.start()

        val args = ds.invokeMethod("getArgs") as List<String>
        args shouldHaveSize 2
        args.shouldContain("--log-level=SEVERE")

        val environment = ds.invokeMethod("getEnvironment") as Map<String, String>
        environment shouldBe mapOf("key16" to "value16")
    }

    @RepeatedTest(10)
    fun test17() {
        ds =
            chromeDriverService {
                logLevel = INFO
                environments {
                    environment("key17", "value17")
                }
            }
        ds.start()

        val args = ds.invokeMethod("getArgs") as List<String>
        args shouldHaveSize 2
        args.shouldContain("--log-level=INFO")

        val environment = ds.invokeMethod("getEnvironment") as Map<String, String>
        environment shouldBe mapOf("key17" to "value17")
    }

    @RepeatedTest(10)
    fun test18() {
        ds =
            chromeDriverService {
                logLevel = WARNING
                environments {
                    environment("key18", "value18")
                }
            }
        ds.start()

        val args = ds.invokeMethod("getArgs") as List<String>
        args shouldHaveSize 2
        args.shouldContain("--log-level=WARNING")

        val environment = ds.invokeMethod("getEnvironment") as Map<String, String>
        environment shouldBe mapOf("key18" to "value18")
    }

    @RepeatedTest(10)
    fun test19() {
        ds =
            chromeDriverService {
                logLevel = ALL
                environments {
                    environment("key19", "value19")
                }
            }
        ds.start()

        val args = ds.invokeMethod("getArgs") as List<String>
        args shouldHaveSize 2
        args.shouldContain("--log-level=ALL")

        val environment = ds.invokeMethod("getEnvironment") as Map<String, String>
        environment shouldBe mapOf("key19" to "value19")
    }

    @RepeatedTest(10)
    fun test20() {
        ds =
            chromeDriverService {
                logLevel = SEVERE
                environments {
                    environment("key20", "value20")
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

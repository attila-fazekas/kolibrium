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

package dev.kolibrium.dsl.selenium.creation

import dev.kolibrium.core.Browser
import dev.kolibrium.dsl.selenium.creation.Arguments.Chrome.incognito
import dev.kolibrium.dsl.selenium.creation.Arguments.Firefox.headless
import dev.kolibrium.dsl.selenium.creation.ExperimentalFlags.cookies_without_same_site_must_be_secure
import dev.kolibrium.dsl.selenium.creation.ExperimentalFlags.same_site_by_default_cookies
import dev.kolibrium.dsl.selenium.creation.Preferences.Firefox.browser_download_folderList
import dev.kolibrium.dsl.selenium.creation.Preferences.Firefox.browser_download_manager_alertOnEXEOpen
import dev.kolibrium.dsl.selenium.creation.Preferences.Firefox.browser_download_manager_closeWhenDone
import dev.kolibrium.dsl.selenium.creation.Preferences.Firefox.browser_download_manager_focusWhenStarting
import dev.kolibrium.dsl.selenium.creation.Preferences.Firefox.browser_download_manager_showAlertOnComplete
import dev.kolibrium.dsl.selenium.creation.Preferences.Firefox.browser_download_manager_showWhenStarting
import dev.kolibrium.dsl.selenium.creation.Preferences.Firefox.browser_download_manager_useWindow
import dev.kolibrium.dsl.selenium.creation.Preferences.Firefox.browser_download_useDownloadDir
import dev.kolibrium.dsl.selenium.creation.Preferences.Firefox.browser_helperApps_alwaysAsk_force
import dev.kolibrium.dsl.selenium.creation.Preferences.Firefox.browser_helperApps_neverAsk_saveToDisk
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeEmpty
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.openqa.selenium.PageLoadStrategy.EAGER
import org.openqa.selenium.Platform.MAC
import org.openqa.selenium.Proxy
import org.openqa.selenium.Proxy.ProxyType.MANUAL
import org.openqa.selenium.UnexpectedAlertBehaviour.DISMISS
import kotlin.time.Duration.Companion.seconds

@Suppress("UNCHECKED_CAST")
class OptionsTests {
    @ParameterizedTest
    @EnumSource(Browser::class)
    fun optionsTest(browser: Browser) {
        options(browser) {
            acceptInsecureCerts = true
            browserVersion = "109.0.5414.46"
            pageLoadStrategy = EAGER
            platform = MAC
            strictFileInteractability = true
            unhandledPromptBehaviour = DISMISS
            proxy {
                proxyType = MANUAL
                autodetect = false
                socks {
                    address = "socks5://192.168.10.100:8888"
                    version = 5
                    username = "username"
                    password = "password"
                }
            }
            timeouts {
                implicitWait = 5.seconds
                pageLoad = 3.seconds
                script = 2.seconds
            }
        }
    }

    @Test
    fun `empty options block should create default ChromeOptions`() {
        val options =
            chromeOptions {
                arguments {
                }
                experimentalOptions {
                    excludeSwitches {
                    }
                    localState {
                        browserEnabledLabsExperiments {
                        }
                    }
                    preferences {
                    }
                }
                extensions {
                }
                proxy {
                }
                timeouts {
                }
            }

        val mappedOptions = options.asMap()
        mappedOptions["browserName"] shouldBe "chrome"

        val googChromeOptions: Map<String, String> = mappedOptions["goog:chromeOptions"] as Map<String, String>
        googChromeOptions shouldHaveSize 2
        (googChromeOptions["extensions"] as List<String>).shouldBeEmpty()
    }

    @Test
    fun `ChromeOptions with custom settings should be created`() {
        val options =
            chromeOptions {
                acceptInsecureCerts = true
                binary = "/Applications/Google Chrome Beta 2.app/Contents/MacOS/Google Chrome Beta"
                browserVersion = "109.0.5414.46"
                pageLoadStrategy = EAGER
                platform = MAC
                strictFileInteractability = true
                unhandledPromptBehaviour = DISMISS
            }

        val mappedOptions = options.asMap()
        mappedOptions shouldHaveSize 8
        mappedOptions["acceptInsecureCerts"] shouldBe true
        mappedOptions["browserName"] shouldBe "chrome"
        mappedOptions["browserVersion"] shouldBe "109.0.5414.46"
        mappedOptions["pageLoadStrategy"].toString() shouldBe "eager"
        mappedOptions["platformName"] shouldBe MAC
        mappedOptions["strictFileInteractability"] shouldBe true
        mappedOptions["unhandledPromptBehavior"].toString() shouldBe "dismiss"

        val googChromeOptions: Map<String, String> = mappedOptions["goog:chromeOptions"] as Map<String, String>
        googChromeOptions shouldHaveSize 3
        googChromeOptions["binary"] shouldBe "/Applications/Google Chrome Beta 2.app/Contents/MacOS/Google Chrome Beta"
        (googChromeOptions["extensions"] as List<String>).shouldBeEmpty()
    }

    @Test
    fun `ChromeOptions with arguments should be created`() {
        val options =
            chromeOptions {
                arguments {
                    +Arguments.Chrome.headless
                    +incognito
                    windowSize {
                        width = 1800
                        height = 1000
                    }
                }
            }

        val mappedOptions = options.asMap()
        mappedOptions shouldHaveSize 2

        val googChromeOptions: Map<String, String> = mappedOptions["goog:chromeOptions"] as Map<String, String>
        googChromeOptions shouldHaveSize 2
        (googChromeOptions["args"] as List<String>).shouldContainExactlyInAnyOrder(
            "--headless=new",
            "--incognito",
            "--window-size=1800,1000",
        )
        (googChromeOptions["extensions"] as List<String>).shouldBeEmpty()
    }

    @Test
    fun `ChromeOptions with experimentalOptions should be created`() {
        val options =
            chromeOptions {
                experimentalOptions {
                    excludeSwitches {
                        +Switches.enable_automation
                    }
                    localState {
                        browserEnabledLabsExperiments {
                            +same_site_by_default_cookies
                            +cookies_without_same_site_must_be_secure
                        }
                    }
                    preferences {
                        +(Preferences.Chromium.download_default_directory to "~/Downloads/TestAuto")
                        +(Preferences.Chromium.safebrowsing_enabled to false)
                    }
                }
            }

        val mappedOptions = options.asMap()
        val googChromeOptions: Map<String, String> = mappedOptions["goog:chromeOptions"] as Map<String, String>
        (googChromeOptions["prefs"] as Map<String, Any>).shouldContainExactly(
            mapOf(
                "download.default_directory" to "~/Downloads/TestAuto",
                "safebrowsing.enabled" to false,
            ),
        )
        (googChromeOptions["excludeSwitches"] as Set<String>).shouldContainExactly("enable-automation")
        (googChromeOptions["localState"] as Map<String, Any>).shouldContainExactly(
            mapOf(
                "browser.enabled_labs_experiments" to
                    setOf(
                        "same-site-by-default-cookies@2",
                        "cookies-without-same-site-must-be-secure@2",
                    ),
            ),
        )
        (googChromeOptions["extensions"] as List<String>).shouldBeEmpty()
    }

    @Test
    fun `ChromeOptions with extensions should be created`() {
        val options =
            chromeOptions {
                extensions {
                    +Extension("src/test/resources/extensions/webextensions-selenium-example.crx")
                }
            }

        val mappedOptions = options.asMap()
        val googChromeOptions: Map<String, String> = mappedOptions["goog:chromeOptions"] as Map<String, String>
        (googChromeOptions["extensions"] as List<String>) shouldHaveSize 1
    }

    @Test
    fun `ChromeOptions with timeouts should be created`() {
        val options =
            chromeOptions {
                timeouts {
                    implicitWait = 5.seconds
                    pageLoad = 3.seconds
                    script = 2.seconds
                }
            }

        val mappedOptions = options.asMap()
        val timeouts: Map<String, String> = mappedOptions["timeouts"] as Map<String, String>
        timeouts.entries.toString().shouldBe("[implicit=5000, pageLoad=3000, script=2000]")
        val googChromeOptions: Map<String, String> = mappedOptions["goog:chromeOptions"] as Map<String, String>
        (googChromeOptions["extensions"] as List<String>).shouldBeEmpty()
    }

    @Test
    fun `ChromeOptions with socks proxy should be created`() {
        val options =
            chromeOptions {
                proxy {
                    proxyType = MANUAL
                    autodetect = false
                    socks {
                        address = "socks5://192.168.10.100:8888"
                        version = 5
                        username = "username"
                        password = "password"
                    }
                }
            }

        val mappedOptions = options.asMap()
        val proxy = mappedOptions["proxy"] as Proxy
        with(proxy) {
            proxyType shouldBe MANUAL
            isAutodetect shouldBe false
            socksProxy shouldBe "socks5://192.168.10.100:8888"
            socksVersion shouldBe 5
            socksUsername shouldBe "username"
            socksPassword shouldBe "password"
        }

        val googChromeOptions: Map<String, String> = mappedOptions["goog:chromeOptions"] as Map<String, String>
        (googChromeOptions["extensions"] as List<String>).shouldBeEmpty()
    }

    @Test
    fun `ChromeOptions with proxy should be created`() {
        val options =
            chromeOptions {
                proxy {
                    proxyType = MANUAL
                    ftpProxy = "localhost:8888"
                    httpProxy = "localhost:8888"
                }
            }

        val mappedOptions = options.asMap()

        val proxy = mappedOptions["proxy"] as Proxy
        with(proxy) {
            assertSoftly {
                proxyType shouldBe MANUAL
                ftpProxy shouldBe "localhost:8888"
                httpProxy shouldBe "localhost:8888"
            }
        }
    }

    @Test
    fun `FirefoxOptions with custom binary should be set`() {
        val options =
            firefoxOptions {
                binary = "/Applications/Firefox Developer Edition.app/Contents/MacOS/firefox"
            }

        val mappedOptions = options.asMap()
        mappedOptions["acceptInsecureCerts"] shouldBe true
        mappedOptions["browserName"] shouldBe "firefox"
        mappedOptions["moz:debuggerAddress"] shouldBe true

        val mozFirefoxOptions: Map<String, String> = mappedOptions["moz:firefoxOptions"] as Map<String, String>
        mozFirefoxOptions["binary"] shouldBe "/Applications/Firefox Developer Edition.app/Contents/MacOS/firefox"
    }

    @Test
    fun `FirefoxOptions with headless should be set`() {
        val options =
            firefoxOptions {
                arguments {
                    +headless
                    +Arguments.Firefox.incognito
                }
            }

        val mappedOptions = options.asMap()
        val mozfirefoxOptions: Map<String, String> = mappedOptions["moz:firefoxOptions"] as Map<String, String>
        (mozfirefoxOptions["args"] as List<String>).shouldContainExactlyInAnyOrder("--headless", "--incognito")
    }

    @Test
    fun `FirefoxOptions with preferences should be set`() {
        val options =
            firefoxOptions {
                preferences {
                    +(Preferences.Firefox.network_automatic_ntlm_auth_trusted_uris to "http://,https://")
                    +(Preferences.Firefox.network_automatic_ntlm_auth_allow_non_fqdn to false)
                    +(Preferences.Firefox.network_negotiate_auth_delegation_uris to "http://,https://")
                    +(Preferences.Firefox.network_negotiate_auth_trusted_uris to "http://,https://")
                    +(Preferences.Firefox.network_http_phishy_userpass_length to 255)
                    +(Preferences.Firefox.network_proxy_no_proxies_on to "")
                    +(Preferences.Firefox.security_csp_enable to false)
                }
            }

        val mappedOptions = options.asMap()
        val mozfirefoxOptions: Map<String, String> = mappedOptions["moz:firefoxOptions"] as Map<String, String>
        (mozfirefoxOptions["prefs"] as Map<String, Any>).shouldContainExactly(
            mapOf(
                "network.automatic-ntlm-auth.allow-non-fqdn" to false,
                "network.automatic-ntlm-auth.trusted-uris" to "http://,https://",
                "network.http.phishy-userpass-length" to 255,
                "network.negotiate-auth.delegation-uris" to "http://,https://",
                "network.negotiate-auth.trusted-uris" to "http://,https://",
                "network.proxy.no_proxies_on" to "",
                "security.csp.enable" to false,
            ),
        )
    }

    @Disabled("Can't run on CI")
    @Test
    fun `FirefoxOptions with profile from directory should be set`() {
        val firefoxProfileDir = System.getenv("FIREFOX_PROFILE_DIR")
        val options =
            firefoxOptions {
                profileDir = firefoxProfileDir
            }

        val mappedOptions = options.asMap()
        val mozfirefoxOptions: Map<String, String> = mappedOptions["moz:firefoxOptions"] as Map<String, String>
        mozfirefoxOptions["profile"].shouldNotBeEmpty()
    }

    @Test
    fun `FirefoxOptions with profile should be set`() {
        val options =
            firefoxOptions {
                profile {
                    +(browser_download_folderList to 1)
                    +(browser_download_manager_showWhenStarting to false)
                    +(browser_download_manager_focusWhenStarting to false)
                    +(browser_download_useDownloadDir to true)
                    +(browser_helperApps_alwaysAsk_force to false)
                    +(browser_download_manager_alertOnEXEOpen to false)
                    +(browser_download_manager_closeWhenDone to true)
                    +(browser_download_manager_showAlertOnComplete to false)
                    +(browser_download_manager_useWindow to false)
                    +(browser_helperApps_neverAsk_saveToDisk to "application/octet-stream")
                }
            }

        val mappedOptions = options.asMap()
        val mozfirefoxOptions: Map<String, String> = mappedOptions["moz:firefoxOptions"] as Map<String, String>
        mozfirefoxOptions["profile"].shouldNotBeEmpty()
    }

    @Test
    fun `FirefoxOptions with windowSize should be set`() {
        val options =
            firefoxOptions {
                arguments {
                    windowSize {
                        width = 1800
                        height = 1000
                    }
                }
            }

        val mappedOptions = options.asMap()
        val mozfirefoxOptions: Map<String, String> = mappedOptions["moz:firefoxOptions"] as Map<String, String>
        (mozfirefoxOptions["args"] as List<String>).shouldContainExactly(
            "--height=1800",
            "--width=1000",
        )
    }

    @Test
    fun `SafariOptions with custom settings should be created`() {
        val options =
            safariOptions {
                automaticInspection = true
                automaticProfiling = true
                useTechnologyPreview = true
                timeouts {
                    implicitWait = 5.seconds
                    pageLoad = 3.seconds
                    script = 2.seconds
                }
            }

        val mappedOptions = options.asMap()
        mappedOptions shouldHaveSize 4
        mappedOptions["browserName"] shouldBe "Safari Technology Preview"
        mappedOptions["safari:automaticInspection"] shouldBe true
        mappedOptions["safari:automaticProfiling"] shouldBe true

        val timeouts: Map<String, String> = mappedOptions["timeouts"] as Map<String, String>
        timeouts.entries.toString().shouldBe("[implicit=5000, pageLoad=3000, script=2000]")
    }

    @Test
    fun `EdgeOptions with custom settings should be created`() {
        val options =
            edgeOptions {
                acceptInsecureCerts = true
                binary = "/Applications/Microsoft Edge Beta.app/Contents/MacOS/Microsoft Edge Beta"
                browserVersion = "118.0.2088.17"
                pageLoadStrategy = EAGER
                platform = MAC
                strictFileInteractability = true
                unhandledPromptBehaviour = DISMISS
                useWebView = true
            }

        val mappedOptions = options.asMap()
        mappedOptions shouldHaveSize 8
        mappedOptions["acceptInsecureCerts"] shouldBe true
        mappedOptions["browserName"] shouldBe "webview2"
        mappedOptions["browserVersion"] shouldBe "118.0.2088.17"
        mappedOptions["pageLoadStrategy"].toString() shouldBe "eager"
        mappedOptions["platformName"] shouldBe MAC
        mappedOptions["strictFileInteractability"] shouldBe true
        mappedOptions["unhandledPromptBehavior"].toString() shouldBe "dismiss"

        val googChromeOptions: Map<String, String> = mappedOptions["ms:edgeOptions"] as Map<String, String>
        googChromeOptions shouldHaveSize 3
        googChromeOptions["binary"] shouldBe "/Applications/Microsoft Edge Beta.app/Contents/MacOS/Microsoft Edge Beta"
        (googChromeOptions["extensions"] as List<String>).shouldBeEmpty()
    }

    @Test
    fun `EdgeOptions with arguments should be created`() {
        val options =
            edgeOptions {
                arguments {
                    +Arguments.Edge.headless
                    +Arguments.Edge.inPrivate
                    windowSize {
                        width = 1800
                        height = 1000
                    }
                }
            }

        val mappedOptions = options.asMap()
        mappedOptions shouldHaveSize 2

        val googChromeOptions: Map<String, String> = mappedOptions["ms:edgeOptions"] as Map<String, String>
        googChromeOptions shouldHaveSize 2
        (googChromeOptions["args"] as List<String>).shouldContainExactlyInAnyOrder(
            "--headless",
            "--inprivate",
            "--window-size=1800,1000",
        )
        (googChromeOptions["extensions"] as List<String>).shouldBeEmpty()
    }

    @Test
    fun `EdgeOptions with experimentalOptions should be created`() {
        val options =
            edgeOptions {
                experimentalOptions {
                    preferences {
                        +(Preferences.Chromium.download_default_directory to "~/Downloads/TestAuto")
                        +(Preferences.Chromium.safebrowsing_enabled to false)
                    }
                    excludeSwitches {
                        +Switches.enable_automation
                    }
                    localState {
                        browserEnabledLabsExperiments {
                            +same_site_by_default_cookies
                            +cookies_without_same_site_must_be_secure
                        }
                    }
                }
            }

        val mappedOptions = options.asMap()
        val googChromeOptions: Map<String, String> = mappedOptions["ms:edgeOptions"] as Map<String, String>
        (googChromeOptions["prefs"] as Map<String, Any>).shouldContainExactly(
            mapOf(
                "download.default_directory" to "~/Downloads/TestAuto",
                "safebrowsing.enabled" to false,
            ),
        )
        (googChromeOptions["excludeSwitches"] as Set<String>).shouldContainExactly("enable-automation")
        (googChromeOptions["localState"] as Map<String, Any>).shouldContainExactly(
            mapOf(
                "browser.enabled_labs_experiments" to
                    setOf(
                        "same-site-by-default-cookies@2",
                        "cookies-without-same-site-must-be-secure@2",
                    ),
            ),
        )
        (googChromeOptions["extensions"] as List<String>).shouldBeEmpty()
    }

    @Test
    fun `EdgeOptions with extensions should be created`() {
        val options =
            edgeOptions {
                extensions {
                    +Extension("src/test/resources/extensions/webextensions-selenium-example.crx")
                }
            }

        val mappedOptions = options.asMap()
        val googChromeOptions: Map<String, String> = mappedOptions["ms:edgeOptions"] as Map<String, String>
        (googChromeOptions["extensions"] as List<String>) shouldHaveSize 1
    }

    @Test
    fun `EdgeOptions with timeouts should be created`() {
        val options =
            edgeOptions {
                timeouts {
                    implicitWait = 5.seconds
                    pageLoad = 3.seconds
                    script = 2.seconds
                }
            }

        val mappedOptions = options.asMap()
        val timeouts: Map<String, String> = mappedOptions["timeouts"] as Map<String, String>
        timeouts.entries.toString().shouldBe("[implicit=5000, pageLoad=3000, script=2000]")
        val googChromeOptions: Map<String, String> = mappedOptions["ms:edgeOptions"] as Map<String, String>
        (googChromeOptions["extensions"] as List<String>).shouldBeEmpty()
    }

    @Test
    fun `EdgeOptions with socks proxy should be created`() {
        val options =
            edgeOptions {
                proxy {
                    proxyType = MANUAL
                    autodetect = false
                    socks {
                        address = "socks5://192.168.10.100:8888"
                        version = 5
                        username = "username"
                        password = "password"
                    }
                }
            }

        val mappedOptions = options.asMap()
        val proxy = mappedOptions["proxy"] as Proxy
        with(proxy) {
            proxyType shouldBe MANUAL
            isAutodetect shouldBe false
            socksProxy shouldBe "socks5://192.168.10.100:8888"
            socksVersion shouldBe 5
            socksUsername shouldBe "username"
            socksPassword shouldBe "password"
        }

        val googChromeOptions: Map<String, String> = mappedOptions["ms:edgeOptions"] as Map<String, String>
        (googChromeOptions["extensions"] as List<String>).shouldBeEmpty()
    }

    @Test
    fun `EdgeOptions with proxy should be created`() {
        val options =
            edgeOptions {
                proxy {
                    proxyType = MANUAL
                    ftpProxy = "localhost:8888"
                    httpProxy = "localhost:8888"
                }
            }

        val mappedOptions = options.asMap()

        val proxy = mappedOptions["proxy"] as Proxy
        with(proxy) {
            assertSoftly {
                proxyType shouldBe MANUAL
                ftpProxy shouldBe "localhost:8888"
                httpProxy shouldBe "localhost:8888"
            }
        }
    }
}

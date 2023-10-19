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

package io.kolibrium.dsl.creation

import io.kolibrium.dsl.creation.BrowserType.CHROME
import io.kolibrium.dsl.creation.BrowserType.EDGE
import io.kolibrium.dsl.creation.BrowserType.FIREFOX
import io.kolibrium.dsl.creation.Channel.BETA
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.openqa.selenium.PageLoadStrategy.NORMAL
import org.openqa.selenium.Platform.MAC
import org.openqa.selenium.UnexpectedAlertBehaviour.DISMISS
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chromium.ChromiumDriverLogLevel.DEBUG
import org.openqa.selenium.firefox.FirefoxDriverLogLevel.CONFIG
import java.nio.file.Files
import java.nio.file.Path
import java.util.regex.Pattern
import kotlin.io.path.absolutePathString
import kotlin.time.Duration.Companion.seconds

@Disabled("Temporarily disabled due to CI does not have browsers installed")
@SuppressWarnings("MaxLineLength", "LongMethod")
class DriverTests {

    private lateinit var driver: WebDriver

    @AfterEach
    fun quitDriver() {
        driver.quit()
    }

    @ParameterizedTest
    @EnumSource(BrowserType::class)
    fun driverTest(browser: BrowserType) {
        driver = driver(browser) {
            driverService {
                timeout = 30.seconds
            }
            options {
                acceptInsecureCerts = true
                pageLoadStrategy = NORMAL
                platform = MAC
                strictFileInteractability = true
                unhandledPromptBehaviour = DISMISS
            }
        }
    }

    @Test
    fun chromeTest(@TempDir tempDir: Path) {
        val logFile = tempDir.resolve("chrome.log").toString()
        val downloadDir = tempDir.absolutePathString()
        val executablePath = getExecutablePath(CHROME, BETA)

        driver = chromeDriver {
            driverService {
                buildCheckDisabled = true
                executable = executablePath
                this.logFile = logFile
                logLevel = DEBUG
                port = 7899
                readableTimestamp = true
                timeout = 30.seconds
                allowedIps {
                    +"192.168.0.50"
                    +"192.168.0.51"
                }
            }
            options {
                acceptInsecureCerts = true
                binary = "/Applications/Google Chrome Beta 2.app/Contents/MacOS/Google Chrome Beta"
                browserVersion = "116.0.5845.110"
                pageLoadStrategy = NORMAL
                platform = MAC
                strictFileInteractability = true
                unhandledPromptBehaviour = DISMISS
                arguments {
                    +Arguments.Chrome.headless
                    +Arguments.Chrome.incognito
                    windowSize {
                        width = 1800
                        height = 1000
                    }
                }
                experimentalOptions {
                    preferences {
                        +(Preferences.Chromium.download_default_directory to downloadDir)
                        +(Preferences.Chromium.download_prompt_for_download to false)
                        +(Preferences.Chromium.safebrowsing_enabled to false)
                    }
                    excludeSwitches {
                        +Switches.enable_automation
                    }
                    localState {
                        browserEnabledLabsExperiments {
                            +ExperimentalFlags.same_site_by_default_cookies
                            +ExperimentalFlags.cookies_without_same_site_must_be_secure
                        }
                    }
                }
                extensions {
                    +Extension("src/test/resources/extensions/webextensions-selenium-example.crx")
                }
                proxy {
                    ftpProxy = "192.168.0.1"
                    httpProxy = "192.168.0.1"
                }
                timeouts {
                    implicitWait = 5.seconds
                    pageLoad = 3.seconds
                    script = 2.seconds
                }
            }
        }

        val fileContent = String(Files.readAllBytes(Path.of(logFile)))

        fileContent shouldContain "[WARNING]: You are using an unsupported command-line switch: --disable-build-check"
        fileContent shouldContain "Starting ChromeDriver 119.0.6045.21"
        fileContent shouldContain "[DEBUG]:"
        fileContent shouldContain "on port 7899"
        fileContent shouldContain """"acceptInsecureCerts": true"""
        fileContent shouldContain """"binary": "/Applications/Google Chrome Beta 2.app/Contents/MacOS/Google Chrome Beta""""
        fileContent shouldContain """"browserVersion": "116.0.5845.110""""
        fileContent shouldContain """"pageLoadStrategy": "normal""""
        fileContent shouldContain """"platformName": "mac""""
        fileContent shouldContain """"strictFileInteractability": true"""
        fileContent shouldContain """"unhandledPromptBehavior": "dismiss""""
        fileContent shouldContain """"args": [ "--headless=new", "--incognito", "--window-size=1800,1000" ]"""
        fileContent shouldContain """"download.default_directory": "$tempDir""""
        fileContent shouldContain """"download.prompt_for_download": false"""
        fileContent shouldContain """"safebrowsing.enabled": false"""
        fileContent shouldContain """"excludeSwitches": [ "enable-automation" ]"""
        fileContent shouldContain """"browser.enabled_labs_experiments": [ "same-site-by-default-cookies@2", "cookies-without-same-site-must-be-secure@2" ]"""
        fileContent shouldContain """ "extensions": [ "Q3IyNAMAAABFAgAAEqwECqYCMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAmuE5LlzdWEMz9iBNPjCjUum5KWguTJRvZ3HzbJv1tkAFjHEckOHbOfg9JN2HMMB2zsRHLrsPMAMFAXTwEzq1A5lN8GEtewCu61Ku/gA2LZozaTHCeSmIdLuRehERmM8F9dFJkw1QvK+6nFlqbS8twA/1mEBFvOQTELMuF0AaAm6IAvTl4SknHGUpJCVAdn0wQ5qiTlZwgx9ur/a3lG8/5tBgkA8mMjFSNDZziGi/RKklizaBIUCYL+YqeKHYY/9GyaIwcZDy6um81wV/utk7s9xIoI0p+1gRV+d3zqDrcGKgPt1TNp0fBK7ZI0r2gbxscuf0K19oHL71FKjJdEmScwIDAQABEoACMOkR5n8xXFyjcm5pWHbt/ob/xKQ4u0WSP9OxQAnvbkanT+jbG/2TeONHNfxNbBiXHlBNPaXk/W9BUQTi+jJ383xyX7K1Wu6T3RAldxVtxT7vhXtqSaek2JCCovMADks3rK0Hw1xunzwbPAKiEkwr44NWn4Qq4N2cLMsZShxtQAq4UKIukJAR5ctBVeVqnBd8CpBZzIzUXZXjasi5l7gsQn5ulKGS2IQqIi2U388d6MImoo6NVbvMdsJ+C1OG2qKorT6FmozjSZyXpj/BvYGHFbkcE1L2hnFEvxUs9jUoifNKyxEj7u8W/Sb0ITgFoCoq/LrYineDGXmwejHL5cMueILxBBIKEMLJZg3MGH+hXYhMqVIaDYVQSwMEFAAACAgAY1GUSgeTd7jqAAAArwEAAA0AAABtYW5pZmVzdC5qc29uhVA9T8NADN3zKyyPVZoAYycWkJgZGFBVXS8OcZv7UM40QVX+O5e7FDZYfHr3nv2efS0A0CjLLQU5XGgI7Czu4KFcCKsMRYAjHWkSsgsZtoF6svxptjQp43vCpG0o6IG95H58sSfSAgoavsDI0gE38PccEAcxAbdfIJ0SeKPj048aRjecgS0880Ctm+rXtRsh2f9Gx7vqPkfSzsZ2OeRgIXLv8RvgmmpaXHRHicBOxIddXW/qDZYJrWBf3uSnrOS0WhXRPjFzrEmEyvuetVpusEiTD36QPrsbipib/0/6uL6VdgaL7DEXc/ENUEsDBBQAAAgIAGNRlEpa2OzxkwAAAOgAAAAJAAAAaW5qZWN0LmpzfY8xDsIwDEV3TmF1STK0F0BMFQdJ448ISp2qTUsrxN0xiIqN7dt+9v+29jJLKDGL5RzmHlIcPQ5Eix+J40In2vtNGOELzgnvyhqdGndUVEUTWUlzR4e1QCa9N9UTEiTOfY3V90OC+bJFmTaLckWXql1GuSEUMHUb/T9UfVz3WF3mrfHDAOH2GhNb9dBcT/f7yB1eUEsBAgAAFAAACAgAY1GUSgeTd7jqAAAArwEAAA0AAAAAAAAAAQAAAAAAAAAAAG1hbmlmZXN0Lmpzb25QSwECAAAUAAAICABjUZRKWtjs8ZMAAADoAAAACQAAAAAAAAABAAAAAAAVAQAAaW5qZWN0LmpzUEsFBgAAAAACAAIAcgAAAM8BAAAAAA==" ]"""
        fileContent shouldContain """"ftpProxy": "192.168.0.1""""
        fileContent shouldContain """"httpProxy": "192.168.0.1""""
        fileContent shouldContain """"implicit": 5000"""
        fileContent shouldContain """"pageLoad": 3000"""
        fileContent shouldContain """"script": 2000"""
        val timestampPattern: Pattern = Pattern.compile("\\[\\d\\d-\\d\\d-\\d\\d\\d\\d", Pattern.CASE_INSENSITIVE)
        timestampPattern.matcher(fileContent).find() shouldBe true
    }

    @Test
    fun firefoxTest(@TempDir tempDir: Path) {
        val logFile = tempDir.resolve("firefox.log").toString()
        val executablePath = getExecutablePath(FIREFOX)

        driver = firefoxDriver {
            driverService {
                executable = executablePath
                this.logFile = logFile
                logLevel = CONFIG
                port = 7900
                truncatedLogs = false
                timeout = 30.seconds
                allowedHosts {
                    +"localhost"
                }
            }
            options {
                acceptInsecureCerts = false
                binary = "/Applications/Firefox Developer Edition.app/Contents/MacOS/firefox"
                arguments {
                    +Arguments.Firefox.headless
                    windowSize {
                        width = 1800
                        height = 1000
                    }
                }
                preferences {
                    +(Preferences.Firefox.network_automatic_ntlm_auth_trusted_uris to "http://,https://")
                    +(Preferences.Firefox.network_automatic_ntlm_auth_allow_non_fqdn to false)
                    +(Preferences.Firefox.network_negotiate_auth_delegation_uris to "http://,https://")
                    +(Preferences.Firefox.network_negotiate_auth_trusted_uris to "http://,https://")
                    +(Preferences.Firefox.network_http_phishy_userpass_length to 255)
                    +(Preferences.Firefox.network_proxy_no_proxies_on to "")
                    +(Preferences.Firefox.security_csp_enable to false)
                }
                profile {
                    +(Preferences.Firefox.browser_download_folderList to 1)
                    +(Preferences.Firefox.browser_download_manager_showWhenStarting to false)
                    +(Preferences.Firefox.browser_download_manager_focusWhenStarting to false)
                    +(Preferences.Firefox.browser_download_useDownloadDir to true)
                    +(Preferences.Firefox.browser_helperApps_alwaysAsk_force to false)
                    +(Preferences.Firefox.browser_download_manager_alertOnEXEOpen to false)
                    +(Preferences.Firefox.browser_download_manager_closeWhenDone to true)
                    +(Preferences.Firefox.browser_download_manager_showAlertOnComplete to false)
                    +(Preferences.Firefox.browser_download_manager_useWindow to false)
                    +(Preferences.Firefox.browser_helperApps_neverAsk_saveToDisk to "application/octet-stream")
                }
                timeouts {
                    implicitWait = 5.seconds
                    pageLoad = 3.seconds
                    script = 2.seconds
                }
            }
        }

        val fileContent = String(Files.readAllBytes(Path.of(logFile)))

        fileContent shouldContain "geckodriver\tINFO\tListening on 127.0.0.1:7900"
        fileContent shouldContain """"moz:geckodriverVersion":"0.33.0""""
        fileContent shouldContain """"acceptInsecureCerts": false"""
        fileContent shouldContain """"binary": "\u002fApplications\u002fFirefox Developer Edition.app\u002fContents\u002fMacOS\u002ffirefox""""
        fileContent shouldContain """"--height=1800""""
        fileContent shouldContain """"--width=1000""""
        fileContent shouldContain """"network.automatic-ntlm-auth.trusted-uris": "http:\u002f\u002f,https:\u002f\u002f""""
        fileContent shouldContain """"network.negotiate-auth.delegation-uris": "http:\u002f\u002f,https:\u002f\u002f""""
        fileContent shouldContain """"network.negotiate-auth.trusted-uris": "http:\u002f\u002f,https:\u002f\u002f""""
        fileContent shouldContain """"network.http.phishy-userpass-length": 255"""
        fileContent shouldContain """"network.proxy.no_proxies_on": """""
        fileContent shouldContain """"security.csp.enable": false"""
        fileContent shouldContain """"implicit": 5000"""
        fileContent shouldContain """"pageLoad": 3000"""
        fileContent shouldContain """"script": 2000"""
    }

    @Test
    fun safariTest() {
        driver = safariDriver {
            driverService {
                port = 7901
                timeout = 30.seconds
                logging = true
            }
            options {
                automaticInspection = true
                automaticProfiling = true
            }
        }
    }

    @Test
    fun edgeTest(@TempDir tempDir: Path) {
        val logFile = tempDir.resolve("edge.log").toString()
        val downloadDir = tempDir.absolutePathString()
        val executablePath = getExecutablePath(EDGE, BETA)

        driver = edgeDriver {
            driverService {
                buildCheckDisabled = true
                executable = executablePath
                this.logFile = logFile
                logLevel = DEBUG
                port = 7902
                readableTimestamp = true
                timeout = 30.seconds
                allowedIps {
                    +"192.168.0.50"
                    +"192.168.0.51"
                }
            }
            options {
                acceptInsecureCerts = true
                binary = "/Applications/Microsoft Edge Beta.app/Contents/MacOS/Microsoft Edge Beta"
                browserVersion = "117.0.2045.47"
                pageLoadStrategy = NORMAL
                platform = MAC
                strictFileInteractability = true
                unhandledPromptBehaviour = DISMISS
                arguments {
                    +Arguments.Edge.headless
                    +Arguments.Edge.inPrivate
                    windowSize {
                        width = 1800
                        height = 1000
                    }
                }
                experimentalOptions {
                    preferences {
                        +(Preferences.Chromium.download_default_directory to downloadDir)
                        +(Preferences.Chromium.download_prompt_for_download to false)
                        +(Preferences.Chromium.safebrowsing_enabled to false)
                    }
                    excludeSwitches {
                        +Switches.enable_automation
                    }
                    localState {
                        browserEnabledLabsExperiments {
                            +ExperimentalFlags.same_site_by_default_cookies
                            +ExperimentalFlags.cookies_without_same_site_must_be_secure
                        }
                    }
                }
                extensions {
                    +Extension("src/test/resources/extensions/webextensions-selenium-example.crx")
                }
                proxy {
                    ftpProxy = "192.168.0.1"
                    httpProxy = "192.168.0.1"
                }
                timeouts {
                    implicitWait = 5.seconds
                    pageLoad = 3.seconds
                    script = 2.seconds
                }
            }
        }

        val fileContent = String(Files.readAllBytes(Path.of(logFile)))

        fileContent shouldContain "[WARNING]: You are using an unsupported command-line switch: --disable-build-check"
        fileContent shouldContain "Starting Microsoft Edge WebDriver 118.0.2088.46"
        fileContent shouldContain "[DEBUG]:"
        fileContent shouldContain "on port 7902"
        fileContent shouldContain """"acceptInsecureCerts": true"""
        fileContent shouldContain """"binary": "/Applications/Microsoft Edge Beta.app/Contents/MacOS/Microsoft Edge Beta""""
        fileContent shouldContain """"browserVersion": "117.0.2045.47""""
        fileContent shouldContain """"pageLoadStrategy": "normal""""
        fileContent shouldContain """"platformName": "mac""""
        fileContent shouldContain """"strictFileInteractability": true"""
        fileContent shouldContain """"unhandledPromptBehavior": "dismiss""""
        fileContent shouldContain """"args": [ "--headless", "--inprivate", "--window-size=1800,1000" ]"""
        fileContent shouldContain """"download.default_directory": "$tempDir""""
        fileContent shouldContain """"download.prompt_for_download": false"""
        fileContent shouldContain """"safebrowsing.enabled": false"""
        fileContent shouldContain """"excludeSwitches": [ "enable-automation" ]"""
        fileContent shouldContain """"browser.enabled_labs_experiments": [ "same-site-by-default-cookies@2", "cookies-without-same-site-must-be-secure@2" ]"""
        fileContent shouldContain """ "extensions": [ "Q3IyNAMAAABFAgAAEqwECqYCMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAmuE5LlzdWEMz9iBNPjCjUum5KWguTJRvZ3HzbJv1tkAFjHEckOHbOfg9JN2HMMB2zsRHLrsPMAMFAXTwEzq1A5lN8GEtewCu61Ku/gA2LZozaTHCeSmIdLuRehERmM8F9dFJkw1QvK+6nFlqbS8twA/1mEBFvOQTELMuF0AaAm6IAvTl4SknHGUpJCVAdn0wQ5qiTlZwgx9ur/a3lG8/5tBgkA8mMjFSNDZziGi/RKklizaBIUCYL+YqeKHYY/9GyaIwcZDy6um81wV/utk7s9xIoI0p+1gRV+d3zqDrcGKgPt1TNp0fBK7ZI0r2gbxscuf0K19oHL71FKjJdEmScwIDAQABEoACMOkR5n8xXFyjcm5pWHbt/ob/xKQ4u0WSP9OxQAnvbkanT+jbG/2TeONHNfxNbBiXHlBNPaXk/W9BUQTi+jJ383xyX7K1Wu6T3RAldxVtxT7vhXtqSaek2JCCovMADks3rK0Hw1xunzwbPAKiEkwr44NWn4Qq4N2cLMsZShxtQAq4UKIukJAR5ctBVeVqnBd8CpBZzIzUXZXjasi5l7gsQn5ulKGS2IQqIi2U388d6MImoo6NVbvMdsJ+C1OG2qKorT6FmozjSZyXpj/BvYGHFbkcE1L2hnFEvxUs9jUoifNKyxEj7u8W/Sb0ITgFoCoq/LrYineDGXmwejHL5cMueILxBBIKEMLJZg3MGH+hXYhMqVIaDYVQSwMEFAAACAgAY1GUSgeTd7jqAAAArwEAAA0AAABtYW5pZmVzdC5qc29uhVA9T8NADN3zKyyPVZoAYycWkJgZGFBVXS8OcZv7UM40QVX+O5e7FDZYfHr3nv2efS0A0CjLLQU5XGgI7Czu4KFcCKsMRYAjHWkSsgsZtoF6svxptjQp43vCpG0o6IG95H58sSfSAgoavsDI0gE38PccEAcxAbdfIJ0SeKPj048aRjecgS0880Ctm+rXtRsh2f9Gx7vqPkfSzsZ2OeRgIXLv8RvgmmpaXHRHicBOxIddXW/qDZYJrWBf3uSnrOS0WhXRPjFzrEmEyvuetVpusEiTD36QPrsbipib/0/6uL6VdgaL7DEXc/ENUEsDBBQAAAgIAGNRlEpa2OzxkwAAAOgAAAAJAAAAaW5qZWN0LmpzfY8xDsIwDEV3TmF1STK0F0BMFQdJ448ISp2qTUsrxN0xiIqN7dt+9v+29jJLKDGL5RzmHlIcPQ5Eix+J40In2vtNGOELzgnvyhqdGndUVEUTWUlzR4e1QCa9N9UTEiTOfY3V90OC+bJFmTaLckWXql1GuSEUMHUb/T9UfVz3WF3mrfHDAOH2GhNb9dBcT/f7yB1eUEsBAgAAFAAACAgAY1GUSgeTd7jqAAAArwEAAA0AAAAAAAAAAQAAAAAAAAAAAG1hbmlmZXN0Lmpzb25QSwECAAAUAAAICABjUZRKWtjs8ZMAAADoAAAACQAAAAAAAAABAAAAAAAVAQAAaW5qZWN0LmpzUEsFBgAAAAACAAIAcgAAAM8BAAAAAA==" ]"""
        fileContent shouldContain """"ftpProxy": "192.168.0.1""""
        fileContent shouldContain """"httpProxy": "192.168.0.1""""
        fileContent shouldContain """"implicit": 5000"""
        fileContent shouldContain """"pageLoad": 3000"""
        fileContent shouldContain """"script": 2000"""
        val timestampPattern: Pattern = Pattern.compile("\\[\\d\\d-\\d\\d-\\d\\d\\d\\d", Pattern.CASE_INSENSITIVE)
        timestampPattern.matcher(fileContent).find() shouldBe true
    }
}

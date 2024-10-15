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

public sealed interface Preference {
    public val value: String
}

@JvmInline
public value class ChromiumPreference(
    internal val value: String,
) {
    public companion object {
        public fun of(value: String): ChromiumPreference = ChromiumPreference(value)
    }
}

@JvmInline
public value class FirefoxPreference(
    internal val value: String,
) {
    public companion object {
        public fun of(value: String): FirefoxPreference = FirefoxPreference(value)
    }
}

public object Preferences {
    public object Chromium {
        @KolibriumPropertyDsl
        public val download_default_directory: ChromiumPreference = ChromiumPreference.of("download.default_directory")

        @KolibriumPropertyDsl
        public val download_prompt_for_download: ChromiumPreference =
            ChromiumPreference.of(
                "download.prompt_for_download",
            )

        @KolibriumPropertyDsl
        public val safebrowsing_enabled: ChromiumPreference = ChromiumPreference.of("safebrowsing.enabled")
    }

    public object Firefox {
        @KolibriumPropertyDsl
        public val network_automatic_ntlm_auth_trusted_uris: FirefoxPreference =
            FirefoxPreference.of("network.automatic-ntlm-auth.trusted-uris")

        @KolibriumPropertyDsl
        public val network_automatic_ntlm_auth_allow_non_fqdn: FirefoxPreference =
            FirefoxPreference.of("network.automatic-ntlm-auth.allow-non-fqdn")

        @KolibriumPropertyDsl
        public val network_negotiate_auth_delegation_uris: FirefoxPreference =
            FirefoxPreference.of("network.negotiate-auth.delegation-uris")

        @KolibriumPropertyDsl
        public val network_negotiate_auth_trusted_uris: FirefoxPreference =
            FirefoxPreference.of("network.negotiate-auth.trusted-uris")

        @KolibriumPropertyDsl
        public val network_http_phishy_userpass_length: FirefoxPreference =
            FirefoxPreference.of("network.http.phishy-userpass-length")

        @KolibriumPropertyDsl
        public val security_csp_enable: FirefoxPreference =
            FirefoxPreference.of("security.csp.enable")

        @KolibriumPropertyDsl
        public val network_proxy_no_proxies_on: FirefoxPreference =
            FirefoxPreference.of("network.proxy.no_proxies_on")

        @KolibriumPropertyDsl
        public val browser_download_folderList: FirefoxPreference =
            FirefoxPreference.of("browser.download.folderList")

        @KolibriumPropertyDsl
        public val browser_download_manager_showWhenStarting: FirefoxPreference =
            FirefoxPreference.of("browser.download.manager.showWhenStarting")

        @KolibriumPropertyDsl
        public val browser_download_manager_focusWhenStarting: FirefoxPreference =
            FirefoxPreference.of("browser.download.manager.focusWhenStarting")

        @KolibriumPropertyDsl
        public val browser_download_useDownloadDir: FirefoxPreference =
            FirefoxPreference.of("browser.download.useDownloadDir")

        @KolibriumPropertyDsl
        public val browser_download_manager_alertOnEXEOpen: FirefoxPreference =
            FirefoxPreference.of("browser.download.manager.alertOnEXEOpen")

        @KolibriumPropertyDsl
        public val browser_download_manager_closeWhenDone: FirefoxPreference =
            FirefoxPreference.of("browser.download.manager.closeWhenDone")

        @KolibriumPropertyDsl
        public val browser_download_manager_showAlertOnComplete:
            FirefoxPreference =
            FirefoxPreference.of("browser.download.manager.showAlertOnComplete")

        @KolibriumPropertyDsl
        public val browser_download_manager_useWindow: FirefoxPreference =
            FirefoxPreference.of("browser.download.manager.useWindow")

        @KolibriumPropertyDsl
        public val browser_helperApps_alwaysAsk_force: FirefoxPreference =
            FirefoxPreference.of("browser.helperApps.alwaysAsk.force")

        @KolibriumPropertyDsl
        public val browser_helperApps_neverAsk_saveToDisk: FirefoxPreference =
            FirefoxPreference.of("browser.helperApps.neverAsk.saveToDisk")
    }
}

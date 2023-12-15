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

@JvmInline
public value class Preference<T : Browser>(public val name: String)

public object Preferences {

    public object Chromium {
        public val download_default_directory: Preference<dev.kolibrium.dsl.selenium.creation.Chromium> =
            Preference("download.default_directory")
        public val download_prompt_for_download: Preference<dev.kolibrium.dsl.selenium.creation.Chromium> =
            Preference("download.prompt_for_download")
        public val safebrowsing_enabled: Preference<dev.kolibrium.dsl.selenium.creation.Chromium> =
            Preference("safebrowsing.enabled")
    }

    public object Firefox {
        public val network_automatic_ntlm_auth_trusted_uris: Preference<dev.kolibrium.dsl.selenium.creation.Firefox> =
            Preference("network.automatic-ntlm-auth.trusted-uris")
        public val network_automatic_ntlm_auth_allow_non_fqdn: Preference<dev.kolibrium.dsl.selenium.creation.Firefox> =
            Preference("network.automatic-ntlm-auth.allow-non-fqdn")
        public val network_negotiate_auth_delegation_uris: Preference<dev.kolibrium.dsl.selenium.creation.Firefox> =
            Preference("network.negotiate-auth.delegation-uris")
        public val network_negotiate_auth_trusted_uris: Preference<dev.kolibrium.dsl.selenium.creation.Firefox> =
            Preference("network.negotiate-auth.trusted-uris")
        public val network_http_phishy_userpass_length: Preference<dev.kolibrium.dsl.selenium.creation.Firefox> =
            Preference("network.http.phishy-userpass-length")
        public val security_csp_enable: Preference<dev.kolibrium.dsl.selenium.creation.Firefox> =
            Preference("security.csp.enable")
        public val network_proxy_no_proxies_on: Preference<dev.kolibrium.dsl.selenium.creation.Firefox> =
            Preference("network.proxy.no_proxies_on")

        public val browser_download_folderList: Preference<dev.kolibrium.dsl.selenium.creation.Firefox> =
            Preference("browser.download.folderList")
        public val browser_download_manager_showWhenStarting: Preference<dev.kolibrium.dsl.selenium.creation.Firefox> =
            Preference("browser.download.manager.showWhenStarting")
        public val browser_download_manager_focusWhenStarting: Preference<dev.kolibrium.dsl.selenium.creation.Firefox> =
            Preference("browser.download.manager.focusWhenStarting")
        public val browser_download_useDownloadDir: Preference<dev.kolibrium.dsl.selenium.creation.Firefox> =
            Preference("browser.download.useDownloadDir")
        public val browser_helperApps_alwaysAsk_force: Preference<dev.kolibrium.dsl.selenium.creation.Firefox> =
            Preference("browser.helperApps.alwaysAsk.force")
        public val browser_download_manager_alertOnEXEOpen: Preference<dev.kolibrium.dsl.selenium.creation.Firefox> =
            Preference("browser.download.manager.alertOnEXEOpen")
        public val browser_download_manager_closeWhenDone: Preference<dev.kolibrium.dsl.selenium.creation.Firefox> =
            Preference("browser.download.manager.closeWhenDone")
        public val browser_download_manager_showAlertOnComplete:
            Preference<dev.kolibrium.dsl.selenium.creation.Firefox> =
                Preference("browser.download.manager.showAlertOnComplete")
        public val browser_download_manager_useWindow: Preference<dev.kolibrium.dsl.selenium.creation.Firefox> =
            Preference("browser.download.manager.useWindow")
        public val browser_helperApps_neverAsk_saveToDisk: Preference<dev.kolibrium.dsl.selenium.creation.Firefox> =
            Preference("browser.helperApps.neverAsk.saveToDisk")
    }
}

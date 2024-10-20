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

/**
 * Value class representing a Chromium browser preference.
 *
 * @property value The string key of the preference.
 */
@JvmInline
public value class ChromiumPreference(
    internal val value: String,
) {
    /**
     * Provides factory method for creating Chromium browser preferences.
     *
     * @see ChromiumPreference
     */
    public companion object {
        /**
         * Creates a new Chromium preference.
         *
         * @param value The preference key.
         * @return A new [ChromiumPreference] instance.
         */
        public fun of(value: String): ChromiumPreference = ChromiumPreference(value)
    }
}

/**
 * Value class representing a Firefox browser preference.
 *
 * @property value The string key of the preference.
 */
@JvmInline
public value class FirefoxPreference(
    internal val value: String,
) {
    /**
     * Provides factory method for creating Firefox browser preferences.
     *
     * @see FirefoxPreference
     */
    public companion object {
        /**
         * Creates a new Firefox preference.
         *
         * @param value The preference key.
         * @return A new [FirefoxPreference] instance.
         */
        public fun of(value: String): FirefoxPreference = FirefoxPreference(value)
    }
}

/**
 * Collection of predefined browser preferences.
 */
public object Preferences {
    /**
     * Collection of predefined Chromium browser preferences.
     */
    public object Chromium {
        /**
         * Sets the default download directory.
         */
        @KolibriumPropertyDsl
        public val download_default_directory: ChromiumPreference = ChromiumPreference.of("download.default_directory")

        /**
         * Controls whether to show download prompt.
         */
        @KolibriumPropertyDsl
        public val download_prompt_for_download: ChromiumPreference =
            ChromiumPreference.of(
                "download.prompt_for_download",
            )

        /**
         * Enables or disables SafeBrowsing feature.
         */
        @KolibriumPropertyDsl
        public val safebrowsing_enabled: ChromiumPreference = ChromiumPreference.of("safebrowsing.enabled")
    }

    /**
     * Collection of predefined Firefox browser preferences.
     */
    public object Firefox {
        /**
         * Configures trusted URIs for NTLM authentication.
         */
        @KolibriumPropertyDsl
        public val network_automatic_ntlm_auth_trusted_uris: FirefoxPreference =
            FirefoxPreference.of("network.automatic-ntlm-auth.trusted-uris")

        /**
         * Allows non-FQDN for NTLM authentication.
         */
        @KolibriumPropertyDsl
        public val network_automatic_ntlm_auth_allow_non_fqdn: FirefoxPreference =
            FirefoxPreference.of("network.automatic-ntlm-auth.allow-non-fqdn")

        /**
         * URI patterns to allow for Negotiate/Kerberos delegation.
         */
        @KolibriumPropertyDsl
        public val network_negotiate_auth_delegation_uris: FirefoxPreference =
            FirefoxPreference.of("network.negotiate-auth.delegation-uris")

        /**
         * URI patterns to allow for NTLM/Kerberos authentication.
         */
        @KolibriumPropertyDsl
        public val network_negotiate_auth_trusted_uris: FirefoxPreference =
            FirefoxPreference.of("network.negotiate-auth.trusted-uris")

        /**
         * Maximum length allowed for usernames/passwords in URLs.
         */
        @KolibriumPropertyDsl
        public val network_http_phishy_userpass_length: FirefoxPreference =
            FirefoxPreference.of("network.http.phishy-userpass-length")

        /**
         * Enables/disables Content Security Policy.
         */
        @KolibriumPropertyDsl
        public val security_csp_enable: FirefoxPreference =
            FirefoxPreference.of("security.csp.enable")

        /**
         * Domains to bypass proxy settings.
         */
        @KolibriumPropertyDsl
        public val network_proxy_no_proxies_on: FirefoxPreference =
            FirefoxPreference.of("network.proxy.no_proxies_on")

        /**
         * Download directory setting (0=Desktop, 1=Downloads, 2=Custom).
         */
        @KolibriumPropertyDsl
        public val browser_download_folderList: FirefoxPreference =
            FirefoxPreference.of("browser.download.folderList")

        /**
         * Show download manager window when starting download.
         */
        @KolibriumPropertyDsl
        public val browser_download_manager_showWhenStarting: FirefoxPreference =
            FirefoxPreference.of("browser.download.manager.showWhenStarting")

        /**
         * Focus the download manager window when starting download.
         */
        @KolibriumPropertyDsl
        public val browser_download_manager_focusWhenStarting: FirefoxPreference =
            FirefoxPreference.of("browser.download.manager.focusWhenStarting")

        /**
         * Use the default download directory.
         */
        @KolibriumPropertyDsl
        public val browser_download_useDownloadDir: FirefoxPreference =
            FirefoxPreference.of("browser.download.useDownloadDir")

        /**
         * Show alert when opening executable files.
         */
        @KolibriumPropertyDsl
        public val browser_download_manager_alertOnEXEOpen: FirefoxPreference =
            FirefoxPreference.of("browser.download.manager.alertOnEXEOpen")

        /**
         * Close download manager when downloads complete.
         */
        @KolibriumPropertyDsl
        public val browser_download_manager_closeWhenDone: FirefoxPreference =
            FirefoxPreference.of("browser.download.manager.closeWhenDone")

        /**
         * Show alert when downloads complete.
         */
        @KolibriumPropertyDsl
        public val browser_download_manager_showAlertOnComplete:
            FirefoxPreference =
            FirefoxPreference.of("browser.download.manager.showAlertOnComplete")

        /**
         * Use download manager window.
         */
        @KolibriumPropertyDsl
        public val browser_download_manager_useWindow: FirefoxPreference =
            FirefoxPreference.of("browser.download.manager.useWindow")

        /**
         * Force "always ask" for file types.
         */
        @KolibriumPropertyDsl
        public val browser_helperApps_alwaysAsk_force: FirefoxPreference =
            FirefoxPreference.of("browser.helperApps.alwaysAsk.force")

        /**
         * MIME types to save without asking.
         */
        @KolibriumPropertyDsl
        public val browser_helperApps_neverAsk_saveToDisk: FirefoxPreference =
            FirefoxPreference.of("browser.helperApps.neverAsk.saveToDisk")
    }
}

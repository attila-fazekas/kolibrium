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

package dev.kolibrium.dsl.selenium.creation

/**
 * Base interface for browser preferences.
 */
public sealed interface Preference {
    /**
     * The string value of the browser preference.
     */
    public val value: String
}

/**
 * Value class representing a Chromium browser preference.
 *
 * @property value The string key of the preference.
 */
@JvmInline
public value class ChromiumPreference(
    public override val value: String,
) : Preference

/**
 * Value class representing a Firefox browser preference.
 *
 * @property value The string key of the preference.
 */
@JvmInline
public value class FirefoxPreference(
    public override val value: String,
) : Preference

/**
 * Collection of predefined browser preferences.
 */
public object Preferences {
    /**
     * Collection of predefined Chromium browser preferences.
     */
    public object Chromium {
        /**
         * Enables or disables the built‑in credentials (password) service integration.
         *
         * When set to false, Chrome will not offer to save or automatically fill in passwords.
         */
        public val credentials_enable_service: ChromiumPreference = ChromiumPreference("credentials_enable_service")

        /**
         * Sets the default download directory.
         */
        public val download_default_directory: ChromiumPreference = ChromiumPreference("download.default_directory")

        /**
         * Controls whether to use the upgraded download directory dialog.
         */
        public val download_directory_upgrade: ChromiumPreference =
            ChromiumPreference("download.directory_upgrade")

        /**
         * Controls whether to show download prompt.
         */
        public val download_prompt_for_download: ChromiumPreference =
            ChromiumPreference("download.prompt_for_download")

        /**
         * Enables or disables the password manager UI for the current profile.
         *
         * When set to false, Chrome will not prompt to save passwords and autofill suggestions are suppressed.
         */
        public val password_manager_enabled: ChromiumPreference =
            ChromiumPreference("profile.password_manager_enabled")

        /**
         * Enables real‑time password leak detection checks for saved credentials.
         *
         * When enabled, Chrome compares saved passwords against known data breaches and warns the user if a compromise is detected.
         */
        public val password_manager_leak_detection: ChromiumPreference =
            ChromiumPreference("profile.password_manager_leak_detection")

        /**
         * Enables or disables SafeBrowsing feature.
         */
        public val safebrowsing_enabled: ChromiumPreference = ChromiumPreference("safebrowsing.enabled")
    }

    /**
     * Collection of predefined Firefox browser preferences.
     */
    public object Firefox {
        /**
         * Configures trusted URIs for NTLM authentication.
         */
        public val network_automatic_ntlm_auth_trusted_uris: FirefoxPreference =
            FirefoxPreference("network.automatic-ntlm-auth.trusted-uris")

        /**
         * Allows non-FQDN for NTLM authentication.
         */
        public val network_automatic_ntlm_auth_allow_non_fqdn: FirefoxPreference =
            FirefoxPreference("network.automatic-ntlm-auth.allow-non-fqdn")

        /**
         * URI patterns to allow for Negotiate/Kerberos delegation.
         */
        public val network_negotiate_auth_delegation_uris: FirefoxPreference =
            FirefoxPreference("network.negotiate-auth.delegation-uris")

        /**
         * URI patterns to allow for NTLM/Kerberos authentication.
         */
        public val network_negotiate_auth_trusted_uris: FirefoxPreference =
            FirefoxPreference("network.negotiate-auth.trusted-uris")

        /**
         * Maximum length allowed for usernames/passwords in URLs.
         */
        public val network_http_phishy_userpass_length: FirefoxPreference =
            FirefoxPreference("network.http.phishy-userpass-length")

        /**
         * Enables/disables Content Security Policy.
         */
        public val security_csp_enable: FirefoxPreference =
            FirefoxPreference("security.csp.enable")

        /**
         * Domains to bypass proxy settings.
         */
        public val network_proxy_no_proxies_on: FirefoxPreference =
            FirefoxPreference("network.proxy.no_proxies_on")

        /**
         * Download directory setting (0=Desktop, 1=Downloads, 2=Custom).
         */
        public val browser_download_folderList: FirefoxPreference =
            FirefoxPreference("browser.download.folderList")

        /**
         * Show download manager window when starting download.
         */
        public val browser_download_manager_showWhenStarting: FirefoxPreference =
            FirefoxPreference("browser.download.manager.showWhenStarting")

        /**
         * Focus the download manager window when starting download.
         */
        public val browser_download_manager_focusWhenStarting: FirefoxPreference =
            FirefoxPreference("browser.download.manager.focusWhenStarting")

        /**
         * Use the default download directory.
         */
        public val browser_download_useDownloadDir: FirefoxPreference =
            FirefoxPreference("browser.download.useDownloadDir")

        /**
         * Show alert when opening executable files.
         */
        public val browser_download_manager_alertOnEXEOpen: FirefoxPreference =
            FirefoxPreference("browser.download.manager.alertOnEXEOpen")

        /**
         * Close download manager when downloads complete.
         */
        public val browser_download_manager_closeWhenDone: FirefoxPreference =
            FirefoxPreference("browser.download.manager.closeWhenDone")

        /**
         * Show alert when downloads complete.
         */
        public val browser_download_manager_showAlertOnComplete:
            FirefoxPreference =
            FirefoxPreference("browser.download.manager.showAlertOnComplete")

        /**
         * Use download manager window.
         */
        public val browser_download_manager_useWindow: FirefoxPreference =
            FirefoxPreference("browser.download.manager.useWindow")

        /**
         * Force "always ask" for file types.
         */
        public val browser_helperApps_alwaysAsk_force: FirefoxPreference =
            FirefoxPreference("browser.helperApps.alwaysAsk.force")

        /**
         * MIME types to save without asking.
         */
        public val browser_helperApps_neverAsk_saveToDisk: FirefoxPreference =
            FirefoxPreference("browser.helperApps.neverAsk.saveToDisk")
    }
}

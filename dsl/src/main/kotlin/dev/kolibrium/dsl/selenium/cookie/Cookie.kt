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

package dev.kolibrium.dsl.selenium.cookie

import dev.kolibrium.dsl.selenium.creation.KolibriumDsl
import org.openqa.selenium.Cookie
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebDriver.Options
import java.util.Date

/**
 * Function to manage cookies in the context of a [WebDriver] instance.
 *
 * @param block The configuration block that defines cookie operations within the [CookiesScope].
 */
@KolibriumDsl
public fun WebDriver.cookies(block: CookiesScope.() -> Unit) {
    val options = this@WebDriver.manage()
    CookiesScope(options).apply(block)
}

/**
 * Scope class that provides configuration for managing cookies in a [WebDriver].
 *
 * @constructor Creates a [CookiesScope] with the provided [options].
 * @param options The cookie management options of the current [WebDriver].
 */
@KolibriumDsl
public class CookiesScope(
    private val options: Options,
) {
    /**
     * Adds a cookie to the current [WebDriver] session.
     *
     * This method allows creating and configuring a cookie with various optional parameters like domain, path,
     * expiration date, security settings, HttpOnly flag, and SameSite policy. The cookie is added to the
     * [WebDriver]'s cookie store.
     *
     * @param name The name of the cookie.
     * @param value The value of the cookie.
     * @param domain (Optional) The domain for which the cookie is valid.
     * @param path (Optional) The path for which the cookie is valid.
     * @param expiresOn (Optional) The expiration date of the cookie.
     * @param isSecure (Optional) If true, the cookie is marked as secure.
     * @param isHttpOnly (Optional) If true, the cookie is marked as HttpOnly.
     * @param sameSite (Optional) The SameSite policy for the cookie.
     */
    @KolibriumDsl
    public fun cookie(
        name: String,
        value: String,
        domain: String? = null,
        path: String? = null,
        expiresOn: Date? = null,
        isSecure: Boolean? = null,
        isHttpOnly: Boolean? = null,
        sameSite: SameSite? = null,
    ) {
        val cookie = Cookie.Builder(name, value)
        domain?.let { cookie.domain(it) }
        path?.let { cookie.path(it) }
        expiresOn?.let { cookie.expiresOn(it) }
        isSecure?.let { cookie.isSecure(it) }
        isHttpOnly?.let { cookie.isSecure(it) }
        sameSite?.let { cookie.sameSite(it.type) }
        options.addCookie(cookie.build())
    }

    /**
     * Returns a string representation of the [CookiesScope], primarily for debugging purposes.
     */
    override fun toString(): String = "CookiesScope(options=${options.cookies})"
}

/**
 * Specifies the SameSite attribute for cookies in the [CookiesScope] class.
 *
 * @property type The string representation of the SameSite policy ("Strict", "Lax", or "None") used in the cookie
 * configuration.
 */
@KolibriumDsl
public enum class SameSite(
    internal val type: String,
) {
    /**
     * Strict SameSite policy, which restricts the cookie to same-site requests only.
     */
    STRICT("Strict"),

    /**
     * Lax SameSite policy, which allows the cookie to be sent with top-level navigations and some cross-site requests.
     */
    LAX("Lax"),

    /**
     * No SameSite restriction, allowing the cookie to be sent with any cross-site request.
     */
    NONE("None"),
}

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
 * @receiver The WebDriver instance on which to perform cookie operations.
 * @param block The configuration block that defines cookie operations within the [CookiesScope].
 */
@KolibriumDsl
public fun WebDriver.cookies(block: CookiesScope.() -> Unit) {
    val options = manage()
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
     * This function creates a new cookie with the specified parameters and adds it to the browser's cookie store.
     * You can optionally specify details such as domain, path, expiration date, security flags, HttpOnly flag,
     * and the SameSite policy.
     *
     * @param name The name of the cookie.
     * @param value The value of the cookie.
     * @param domain (Optional) The domain to which the cookie applies. Defaults to the current domain if not specified.
     * @param path (Optional) The path to which the cookie applies. Defaults to "/" if not specified.
     * @param expiresOn (Optional) The expiration date of the cookie. If not provided, the cookie will be a session cookie.
     * @param isSecure (Optional) If `true`, the cookie is marked as secure, meaning it will only be sent over HTTPS.
     * @param isHttpOnly (Optional) If `true`, the cookie is marked as HttpOnly, preventing client-side JavaScript access.
     * @param sameSite (Optional) Specifies the SameSite policy for the cookie, controlling cross-site request behavior.
     * @return The created [Cookie] object that was added to the browser session.
     */
    @KolibriumDsl
    public fun addCookie(
        name: String,
        value: String,
        domain: String? = null,
        path: String? = null,
        expiresOn: Date? = null,
        isSecure: Boolean? = null,
        isHttpOnly: Boolean? = null,
        sameSite: SameSite? = null,
    ): Cookie {
        val cookieBuilder = Cookie.Builder(name, value)
        domain?.let { cookieBuilder.domain(it) }
        path?.let { cookieBuilder.path(it) }
        expiresOn?.let { cookieBuilder.expiresOn(it) }
        isSecure?.let { cookieBuilder.isSecure(it) }
        isHttpOnly?.let { cookieBuilder.isSecure(it) }
        sameSite?.let { cookieBuilder.sameSite(it.type) }
        val cookie = cookieBuilder.build()
        options.addCookie(cookie)
        return cookie
    }

    /**
     * Retrieves a cookie by its name from the current [WebDriver] session.
     *
     * @param name The name of the cookie to retrieve.
     * @return The [Cookie] object if found, or `null` if no cookie with the specified name exists.
     */
    @KolibriumDsl
    public fun getCookie(name: String): Cookie? = options.getCookieNamed(name)

    /**
     * Retrieves all cookies from the current [WebDriver] session.
     *
     * @return A [Set] containing all cookies currently stored in the browser.
     */
    @KolibriumDsl
    public fun getCookies(): Set<Cookie> = options.cookies

    /**
     * Deletes the specified cookie from the current [WebDriver] session.
     *
     * @param cookie The [Cookie] object to remove from the browser.
     */
    @KolibriumDsl
    public fun deleteCookie(cookie: Cookie): Unit = options.deleteCookie(cookie)

    /**
     * Deletes a cookie by its name from the current [WebDriver] session.
     *
     * @param name The name of the cookie to delete.
     */
    @KolibriumDsl
    public fun deleteCookie(name: String): Unit = options.deleteCookieNamed(name)

    /**
     * Deletes all cookies from the current [WebDriver] session.
     */
    @KolibriumDsl
    public fun deleteCookies(): Unit = options.deleteAllCookies()

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

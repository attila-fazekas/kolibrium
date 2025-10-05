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

package dev.kolibrium.dsl.selenium.interactions

import dev.kolibrium.dsl.selenium.KolibriumDsl
import org.openqa.selenium.Cookie
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebDriver.Options
import java.util.Date

/**
 * Function to manage cookies in the context of a [WebDriver] instance.
 *
 * @receiver The [WebDriver] instance on which to perform cookie operations.
 * @param refreshPage Whether to refresh the page after performing cookie operations. Defaults to false.
 * @param block The configuration block that defines cookie operations within the [CookiesScope].
 * @return The [WebDriver] instance for method chaining.
 */
@KolibriumDsl
public fun WebDriver.cookies(
    refreshPage: Boolean = false,
    block: CookiesScope.() -> Unit,
): WebDriver {
    val options = manage()
    CookiesScope(options).apply(block)
    if (refreshPage) navigate().refresh()
    return this
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
     * Notes:
     * - Selenium restricts adding cookies to the current origin. Ensure the driver is already on the target domain.
     *
     * @param name The name of the cookie.
     * @param value The value of the cookie.
     * @param domain (Optional) The domain to which the cookie applies. Defaults to the current domain if not specified.
     * @param path (Optional) The path to which the cookie applies. If `null`, the builder's default is used.
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
        isHttpOnly?.let { cookieBuilder.isHttpOnly(it) }
        sameSite?.let { cookieBuilder.sameSite(it.toString()) }
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
     * Add all provided Selenium [Cookie] instances to the current session.
     */
    @KolibriumDsl
    public fun addAll(vararg cookies: Cookie) {
        cookies.forEach(options::addCookie)
    }

    /**
     * Convenience for adding a simple session cookie with a name/value pair.
     */
    @KolibriumDsl
    public fun put(
        name: String,
        value: String,
    ): Cookie = addCookie(name, value)

    /**
     * Convenience for adding multiple name/value cookies.
     */
    @KolibriumDsl
    public fun putAll(pairs: Map<String, String>) {
        pairs.forEach { (k, v) -> addCookie(k, v) }
    }

    /**
     * Returns a string representation of the [CookiesScope], primarily for debugging purposes.
     */
    override fun toString(): String = "CookiesScope(options=${options.cookies})"
}

/**
 * Specifies the SameSite attribute for cookies in the [CookiesScope] class.
 *
 */
@KolibriumDsl
public enum class SameSite {
    /** Strict SameSite policy: cookie is sent only for same-site requests. */
    Strict,

    /** Lax SameSite policy: cookie is sent with top-level navigations and some cross-site GETs. */
    Lax,

    /** No SameSite restriction: cookie may be sent with any cross-site request. */
    None,
}

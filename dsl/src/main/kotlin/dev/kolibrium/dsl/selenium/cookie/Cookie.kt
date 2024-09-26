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

@KolibriumDsl
public fun WebDriver.cookies(block: CookiesScope.() -> Unit) {
    val options = this@WebDriver.manage()
    CookiesScope(options).apply(block)
}

@KolibriumDsl
public class CookiesScope(private val options: Options) {
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
}

public enum class SameSite(internal val type: String) {
    STRICT("Strict"),
    LAX("Lax"),
    NONE("None"),
}

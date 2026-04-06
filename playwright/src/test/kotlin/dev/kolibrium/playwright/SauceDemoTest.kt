/*
 * Copyright 2023-2026 Attila Fazekas & contributors
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

package dev.kolibrium.playwright

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserType.LaunchOptions
import dev.kolibrium.playwright.pages.CartPage
import dev.kolibrium.playwright.pages.InventoryPage
import dev.kolibrium.playwright.pages.LoginPage
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class SauceDemoTest {
    @Test
    fun `login and verify products`() =
        sauceDemoTest(
            config = KolibriumConfig(recordTrace = true),
        ) {
            on(::LoginPage) {
                login()
            }.on(::InventoryPage) {
                titleText() shouldBe "Products"
            }
        }

    @Test
    fun `add item to cart`() =
        sauceDemoTest {
            on(::LoginPage) {
                login()
            }.on(::InventoryPage) {
                addToCart("sauce-labs-backpack")
                openCart()
            }.on(::CartPage) {
                itemCount() shouldBe 1
            }
        }

    @Test
    fun `verify inventory then come back`() =
        sauceDemoTest {
            on(::LoginPage) {
                login()
            }.on(::InventoryPage) {
                titleText() shouldBe "Products"
                addToCart("sauce-labs-backpack")
            }.then {
                // still on InventoryPage — verify cart badge updated
                cartBadgeCount() shouldBe "1"
            }
        }

//    @Test
//    fun `with setUp and tearDown`() = playwrightTest(
//        site = SauceDemo,
//        setUp = { createTestUser() },
//        tearDown = { user -> deleteTestUser(user) },
//    ) { user ->
//        on(::LoginPage) {
//            login(user.username, user.password)
//        }.on(::InventoryPage) {
//            titleText() shouldBe "Products"
//        }
//    }

    // TODO Generate it with KSP
    fun sauceDemoTest(
        browserType: BrowserType = BrowserType.Chromium,
        launchOptions: LaunchOptions? = null,
        contextOptions: Browser.NewContextOptions? = null,
        config: KolibriumConfig = KolibriumConfig(),
        block: SiteScope<SauceDemo>.(Unit) -> Unit,
    ) = playwrightTest(
        site = SauceDemo,
        browserType = browserType,
        launchOptions = launchOptions,
        contextOptions = contextOptions,
        config = config,
        block = block,
    )
}

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

package dev.kolibrium.selenium.ksp.processors

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName

internal const val SELENIUM_SITE_BASE_CLASS = "dev.kolibrium.selenium.core.SeleniumSite"

internal val SELENIUM_TEST_MEMBER = MemberName("dev.kolibrium.selenium.dsl", "seleniumTest")
internal val SITE_SCOPE_CLASS = ClassName("dev.kolibrium.selenium.dsl", "SiteScope")
internal val DRIVER_FACTORY_CLASS = ClassName("dev.kolibrium.selenium.dsl", "DriverFactory")
internal val CHROME_DRIVER_CLASS = ClassName("org.openqa.selenium.chrome", "ChromeDriver")

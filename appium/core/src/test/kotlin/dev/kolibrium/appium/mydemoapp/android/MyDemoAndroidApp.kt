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

package dev.kolibrium.appium.mydemoapp.android

import dev.kolibrium.appium.AndroidApp
import dev.kolibrium.appium.AndroidDriverFactory
import dev.kolibrium.appium.appiumService
import io.appium.java_client.Location
import io.appium.java_client.android.AndroidDriver
import io.appium.java_client.android.options.UiAutomator2Options
import org.openqa.selenium.ScreenOrientation

object MyDemoAndroidApp : AndroidApp(
    appPackage = "com.saucelabs.mydemoapp.android",
    appActivity = ".view.activities.SplashActivity",
    service =
        appiumService {
            port = 4723
            logLevel = "info"
        },
) {
    override fun configureOptions(options: UiAutomator2Options) {
        options.apply {
            setUdid("emulator-5554")
        }
    }

    override fun onSessionReady(driver: AndroidDriver) {
        driver.apply {
            rotate(ScreenOrientation.PORTRAIT)
            location = Location(39.4666667, -0.3666667, 0.0) // Valencia, Spain
        }
    }
}

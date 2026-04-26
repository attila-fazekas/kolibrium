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

package dev.kolibrium.appium.mydemoapprn

import dev.kolibrium.appium.AndroidApp
import dev.kolibrium.appium.CrossPlatformApp
import dev.kolibrium.appium.IosApp
import dev.kolibrium.appium.appiumService
import io.appium.java_client.Location
import io.appium.java_client.android.AndroidDriver
import org.openqa.selenium.ScreenOrientation

object MyDemoAppRnAndroidApp : AndroidApp(
    appPackage = "com.saucelabs.mydemoapp.rn",
    appActivity = ".MainActivity",
    service =
        appiumService {
            port = 4723
            logLevel = "info"
        },
) {
    override fun onSessionReady(driver: AndroidDriver) {
        driver.apply {
            rotate(ScreenOrientation.PORTRAIT)
            location = Location(39.4666667, -0.3666667, 0.0) // Valencia, Spain
        }
    }
}

object MyDemoAppRnIosApp : IosApp(
    bundleId = "com.example.ios",
)

object MyDemoAppRn : CrossPlatformApp(
    android = MyDemoAppRnAndroidApp,
    ios = MyDemoAppRnIosApp,
)

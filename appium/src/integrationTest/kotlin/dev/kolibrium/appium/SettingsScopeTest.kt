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

package dev.kolibrium.appium

import dev.kolibrium.appium.android.SettingsScope
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class SettingsScopeTest {
    @Test
    fun `toMap should return empty map when no settings are set`() {
        SettingsScope().toMap().shouldBeEmpty()
    }

    @Test
    fun `toMap should include all non-null settings with correct keys`() {
        val scope =
            SettingsScope().apply {
                actionAcknowledgmentTimeout = 1000L
                allowInvisibleElements = true
                ignoreUnimportantViews = false
                elementResponseAttributes = "name,text"
                enableMultiWindows = true
                enableTopmostWindowFromActivePackage = true
                enableNotificationListener = false
                keyInjectionDelay = 50L
                scrollAcknowledgmentTimeout = 500L
                shouldUseCompactResponses = false
                waitForIdleTimeout = 20000L
                waitForSelectorTimeout = 15000L
                normalizeTagNames = true
                shutdownOnPowerDisconnect = false
                simpleBoundsCalculation = true
                trackScrollEvents = false
                wakeLockTimeout = 3600000L
                serverPort = 1234
                mjpegServerPort = 5678
                mjpegServerFramerate = 30
                mjpegScalingFactor = 75
                mjpegServerScreenshotQuality = 80
                mjpegBilinearFiltering = true
                useResourcesForOrientationDetection = true
                enforceXPath1 = true
                limitXPathContextScope = false
                disableIdLocatorAutocompletion = true
                alwaysTraversableViewClasses = "com.example.View"
                includeExtrasInPageSource = true
                includeA11yActionsInPageSource = true
                snapshotMaxDepth = 100
                currentDisplayId = 1
            }

        val map = scope.toMap()
        map shouldHaveSize 32
        map["actionAcknowledgmentTimeout"] shouldBe 1000L
        map["allowInvisibleElements"] shouldBe true
        map["ignoreUnimportantViews"] shouldBe false
        map["elementResponseAttributes"] shouldBe "name,text"
        map["enableMultiWindows"] shouldBe true
        map["enableTopmostWindowFromActivePackage"] shouldBe true
        map["enableNotificationListener"] shouldBe false
        map["keyInjectionDelay"] shouldBe 50L
        map["scrollAcknowledgmentTimeout"] shouldBe 500L
        map["shouldUseCompactResponses"] shouldBe false
        map["waitForIdleTimeout"] shouldBe 20000L
        map["waitForSelectorTimeout"] shouldBe 15000L
        map["normalizeTagNames"] shouldBe true
        map["shutdownOnPowerDisconnect"] shouldBe false
        map["simpleBoundsCalculation"] shouldBe true
        map["trackScrollEvents"] shouldBe false
        map["wakeLockTimeout"] shouldBe 3600000L
        map["serverPort"] shouldBe 1234
        map["mjpegServerPort"] shouldBe 5678
        map["mjpegServerFramerate"] shouldBe 30
        map["mjpegScalingFactor"] shouldBe 75
        map["mjpegServerScreenshotQuality"] shouldBe 80
        map["mjpegBilinearFiltering"] shouldBe true
        map["useResourcesForOrientationDetection"] shouldBe true
        map["enforceXPath1"] shouldBe true
        map["limitXPathContextScope"] shouldBe false
        map["disableIdLocatorAutocompletion"] shouldBe true
        map["alwaysTraversableViewClasses"] shouldBe "com.example.View"
        map["includeExtrasInPageSource"] shouldBe true
        map["includeA11yActionsInPageSource"] shouldBe true
        map["snapshotMaxDepth"] shouldBe 100
        map["currentDisplayId"] shouldBe 1
    }
}

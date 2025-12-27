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

package dev.kolibrium.examples.selenium.browserstack

enum class Product(
    val displayName: String,
) {
    IPHONE_12("iPhone 12"),
    IPHONE_12_MINI("iPhone 12 Mini"),
    IPHONE_12_PRO_MAX("iPhone 12 Pro Max"),
    IPHONE_12_PRO("iPhone 12 Pro"),
    IPHONE_11("iPhone 11"),
    IPHONE_11_PRO("iPhone 11 Pro"),
    IPHONE_XS("iPhone XS"),
    IPHONE_XR("iPhone XR"),
    IPHONE_XS_MAX("iPhone XS Max"),
    GALAXY_S20("Galaxy S20"),
    GALAXY_S20_PLUS("Galaxy S20+"),
    GALAXY_S20_ULTRA("Galaxy S20 Ultra"),
    GALAXY_S10("Galaxy S10"),
    GALAXY_S9("Galaxy S9"),
    GALAXY_NOTE_20("Galaxy Note 20"),
    GALAXY_NOTE_20_ULTRA("Galaxy Note 20 Ultra"),
    PIXEL_4("Pixel 4"),
    PIXEL_3("Pixel 3"),
    PIXEL_2("Pixel 2"),
    ONE_PLUS_8("One Plus 8"),
    ONE_PLUS_8T("One Plus 8T"),
    ONE_PLUS_8_PRO("One Plus 8 Pro"),
    ONE_PLUS_7T("One Plus 7T"),
    ONE_PLUS_7("One Plus 7"),
    ONE_PLUS_6T("One Plus 6T"),
}

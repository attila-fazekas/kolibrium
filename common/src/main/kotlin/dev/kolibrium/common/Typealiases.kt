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

package dev.kolibrium.common

import org.openqa.selenium.Cookie
import org.openqa.selenium.WebElement

/**
 * A typealias for a list of [WebElement]s.
 *
 * This provides a more readable and concise way to refer to a list of web elements in the code.
 */
public typealias WebElements = List<WebElement>

/**
 * A typealias for a set of [Cookie]s.
 *
 * This provides a more readable and concise way to refer to a set of cookies in the code.
 */
public typealias Cookies = Set<Cookie>

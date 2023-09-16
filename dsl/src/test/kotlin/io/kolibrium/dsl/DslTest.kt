/*
 * Copyright 2023 Attila Fazekas
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

package io.kolibrium.dsl

import org.openqa.selenium.remote.service.DriverService

abstract class DslTest {
    protected fun DriverService.invokeMethod(methodName: String): Any {
        val method = DriverService::class.java.getDeclaredMethod(methodName)
        method.isAccessible = true
        return method.invoke(this)
    }

    protected fun DriverService.getField(fieldName: String): Any {
        val field = DriverService::class.java.getDeclaredField(fieldName)
        field.isAccessible = true
        return field.get(this)
    }
}

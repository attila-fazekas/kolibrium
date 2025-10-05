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

package dev.kolibrium.dsl.selenium

import dev.kolibrium.core.selenium.ElementReadyCheck
import dev.kolibrium.core.selenium.HasBy
import dev.kolibrium.core.selenium.ReadinessCondition
import dev.kolibrium.core.selenium.ReadinessDescriptor
import dev.kolibrium.core.selenium.WebElementDescriptor
import dev.kolibrium.core.selenium.WebElementsDescriptor

// Bridge functions to convert DSL descriptors to core descriptors

/**
 * Creates a [ReadinessDescriptor] from a single-element locator descriptor.
 *
 * This maps the descriptor's [HasBy.by] and [WebElementDescriptor.waitConfig] to a core [ReadinessDescriptor].
 * You can optionally override the built-in [condition] or provide a [custom] predicate.
 */
public fun WebElementDescriptor.toReadinessDescriptor(
    condition: ReadinessCondition = ReadinessCondition.IsDisplayed,
    custom: ElementReadyCheck? = null,
): ReadinessDescriptor =
    ReadinessDescriptor(
        by = this.by,
        waitConfig = this.waitConfig,
        condition = condition,
        custom = custom,
    )

/**
 * Creates a [ReadinessDescriptor] from a multi-element locator descriptor.
 *
 * Note: collection-level readiness predicates are not carried over; this simply bridges locator and wait config.
 */
public fun WebElementsDescriptor.toReadinessDescriptor(
    condition: ReadinessCondition = ReadinessCondition.IsDisplayed,
    custom: ElementReadyCheck? = null,
): ReadinessDescriptor =
    ReadinessDescriptor(
        by = this.by,
        waitConfig = this.waitConfig,
        condition = condition,
        custom = custom,
    )

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

package dev.kolibrium.api.ksp.processors

import com.google.auto.service.AutoService
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

/**
 * Service provider for the API code generation symbol processor.
 *
 * This provider is automatically discovered by KSP through the [AutoService] annotation
 * and creates instances of [ApiCodegenProcessor] to generate API client code from
 * annotated interfaces.
 */
@AutoService(SymbolProcessorProvider::class)
public class ApiCodegenProcessorProvider : SymbolProcessorProvider {
    /**
     * Creates a new instance of the API code generation processor.
     *
     * @param environment The symbol processor environment providing access to logging,
     *                    code generation, and other KSP facilities
     * @return A new [ApiCodegenProcessor] instance
     */
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor = ApiCodegenProcessor(environment)
}

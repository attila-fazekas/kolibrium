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

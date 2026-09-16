package uk.co.compendiumdev.thingifier.swaggerizer;

public class SwaggerGenerationConfig {

    public enum PathParameterPlacement {
        PATH,
        OPERATION
    }

    // include endpoints with only a 405 status code?
    public boolean includeMethodNotAllowedEndpoints = false;
    public boolean includeFieldValidation = true;
    public boolean strongSchemas = false;
    public PathParameterPlacement pathParameterPlacement = PathParameterPlacement.PATH;
    public OpenApiSpecificationVersion openApiSpecificationVersion =
            OpenApiSpecificationVersion.OPENAPI_3_1;

    public SwaggerGenerationConfig copy() {
        final SwaggerGenerationConfig copy = new SwaggerGenerationConfig();
        copy.includeMethodNotAllowedEndpoints = includeMethodNotAllowedEndpoints;
        copy.includeFieldValidation = includeFieldValidation;
        copy.strongSchemas = strongSchemas;
        copy.pathParameterPlacement = pathParameterPlacement;
        copy.openApiSpecificationVersion = openApiSpecificationVersion;
        return copy;
    }

    public SwaggerGenerationConfig copyFor(final OpenApiSpecificationVersion version) {
        final SwaggerGenerationConfig copy = copy();
        copy.openApiSpecificationVersion = version;
        return copy;
    }
}

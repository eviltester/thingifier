package uk.co.compendiumdev.thingifier.swaggerizer;

public class SwaggerGenerationConfig {

    // include endpoints with only a 405 status code?
    public boolean includeMethodNotAllowedEndpoints = false;
    public boolean includeFieldValidation = true;
    public OpenApiSpecificationVersion openApiSpecificationVersion =
            OpenApiSpecificationVersion.OPENAPI_3_1;
}

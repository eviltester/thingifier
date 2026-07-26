package uk.co.compendiumdev.thingifier.swaggerizer;

import io.swagger.v3.oas.models.SpecVersion;

public enum OpenApiSpecificationVersion {
    OPENAPI_3_0("3.0", "3.0.1", "OpenAPI 3.0", SpecVersion.V30),
    OPENAPI_3_1("3.1", "3.1.0", "OpenAPI 3.1", SpecVersion.V31);

    private final String versionNumber;
    private final String documentVersion;
    private final String displayName;
    private final SpecVersion specVersion;

    OpenApiSpecificationVersion(
            final String versionNumber,
            final String documentVersion,
            final String displayName,
            final SpecVersion specVersion) {
        this.versionNumber = versionNumber;
        this.documentVersion = documentVersion;
        this.displayName = displayName;
        this.specVersion = specVersion;
    }

    public String versionNumber() {
        return versionNumber;
    }

    public String documentVersion() {
        return documentVersion;
    }

    public String displayName() {
        return displayName;
    }

    public SpecVersion specVersion() {
        return specVersion;
    }
}

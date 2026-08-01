package uk.co.compendiumdev.thingifier.apiconfig;

public final class WriteMethodsConfig {

    private final EntityWriteMethodConfig entityWriteMethods;
    private final RelationshipWriteMethodConfig relationshipWriteMethods;

    public WriteMethodsConfig() {
        entityWriteMethods = new EntityWriteMethodConfig();
        relationshipWriteMethods = new RelationshipWriteMethodConfig();
    }

    public EntityWriteMethodConfig entities() {
        return entityWriteMethods;
    }

    public RelationshipWriteMethodConfig relationships() {
        return relationshipWriteMethods;
    }

    public ApiConfigValidationReport validate() {
        ApiConfigValidationReport report = new ApiConfigValidationReport();
        entityWriteMethods.addValidationMessages(report, "writeMethods.entities");
        return report;
    }

    public void setFrom(final WriteMethodsConfig source) {
        entityWriteMethods.setFrom(source.entities());
        relationshipWriteMethods.setFrom(source.relationships());
    }
}

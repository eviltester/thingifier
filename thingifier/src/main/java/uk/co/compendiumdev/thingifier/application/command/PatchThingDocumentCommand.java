package uk.co.compendiumdev.thingifier.application.command;

public final class PatchThingDocumentCommand implements ThingWriteCommand {

    public enum DocumentStyle {
        JSON_MERGE_PATCH_RFC7396,
        JSON_PATCH_RFC6902
    }

    private final String entityName;
    private final String identifier;
    private final String rawDocument;
    private final DocumentStyle style;

    public PatchThingDocumentCommand(
            final String entityName,
            final String identifier,
            final String rawDocument,
            final DocumentStyle style) {
        this.entityName = entityName;
        this.identifier = identifier;
        this.rawDocument = rawDocument;
        this.style = style;
    }

    public String getEntityName() {
        return entityName;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getRawDocument() {
        return rawDocument;
    }

    public DocumentStyle getStyle() {
        return style;
    }
}

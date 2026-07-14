package uk.co.compendiumdev.thingifier.application.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.NamedValue;

public final class AmendThingCommand implements ThingWriteCommand {

    private final String entityName;
    private final String identifier;
    private final List<NamedValue> fieldValues;
    private final List<BodyFieldValue> bodyFields;
    private final boolean replaceExistingFieldsAndRelationships;
    private final List<RelationshipReference> relationships;

    public AmendThingCommand(
            final String entityName,
            final String identifier,
            final List<NamedValue> fieldValues,
            final boolean replaceExistingFieldsAndRelationships,
            final List<RelationshipReference> relationships) {
        this(
                entityName,
                identifier,
                fieldValues,
                BodyFieldValue.fromNamedValues(fieldValues),
                replaceExistingFieldsAndRelationships,
                relationships);
    }

    public AmendThingCommand(
            final String entityName,
            final String identifier,
            final List<NamedValue> fieldValues,
            final List<BodyFieldValue> bodyFields,
            final boolean replaceExistingFieldsAndRelationships,
            final List<RelationshipReference> relationships) {
        this.entityName = entityName;
        this.identifier = identifier;
        this.fieldValues = Collections.unmodifiableList(new ArrayList<>(fieldValues));
        this.bodyFields = Collections.unmodifiableList(new ArrayList<>(bodyFields));
        this.replaceExistingFieldsAndRelationships = replaceExistingFieldsAndRelationships;
        this.relationships = Collections.unmodifiableList(new ArrayList<>(relationships));
    }

    public String getEntityName() {
        return entityName;
    }

    public String getIdentifier() {
        return identifier;
    }

    public List<NamedValue> getFieldValues() {
        return fieldValues;
    }

    public List<BodyFieldValue> getBodyFields() {
        return bodyFields;
    }

    public boolean shouldReplaceExistingFieldsAndRelationships() {
        return replaceExistingFieldsAndRelationships;
    }

    public List<RelationshipReference> getRelationships() {
        return relationships;
    }
}

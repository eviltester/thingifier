package uk.co.compendiumdev.thingifier.application.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.NamedValue;

public final class CreateThingCommand implements ThingWriteCommand {

    private final String entityName;
    private final String requestedPrimaryKey;
    private final List<NamedValue> fieldValues;
    private final List<BodyFieldValue> bodyFields;
    private final List<RelationshipReference> relationships;
    private final boolean validateFinalRelationships;

    public CreateThingCommand(
            final String entityName,
            final List<NamedValue> fieldValues,
            final List<RelationshipReference> relationships,
            final boolean validateFinalRelationships) {
        this(
                entityName,
                "",
                fieldValues,
                BodyFieldValue.fromNamedValues(fieldValues),
                relationships,
                validateFinalRelationships);
    }

    public CreateThingCommand(
            final String entityName,
            final String requestedPrimaryKey,
            final List<NamedValue> fieldValues,
            final List<RelationshipReference> relationships,
            final boolean validateFinalRelationships) {
        this(
                entityName,
                requestedPrimaryKey,
                fieldValues,
                BodyFieldValue.fromNamedValues(fieldValues),
                relationships,
                validateFinalRelationships);
    }

    public CreateThingCommand(
            final String entityName,
            final String requestedPrimaryKey,
            final List<NamedValue> fieldValues,
            final List<BodyFieldValue> bodyFields,
            final List<RelationshipReference> relationships,
            final boolean validateFinalRelationships) {
        this.entityName = entityName;
        this.requestedPrimaryKey = requestedPrimaryKey == null ? "" : requestedPrimaryKey;
        this.fieldValues = Collections.unmodifiableList(new ArrayList<>(fieldValues));
        this.bodyFields = Collections.unmodifiableList(new ArrayList<>(bodyFields));
        this.relationships = Collections.unmodifiableList(new ArrayList<>(relationships));
        this.validateFinalRelationships = validateFinalRelationships;
    }

    public String getEntityName() {
        return entityName;
    }

    public boolean hasRequestedPrimaryKey() {
        return !requestedPrimaryKey.isEmpty();
    }

    public String getRequestedPrimaryKey() {
        return requestedPrimaryKey;
    }

    public List<NamedValue> getFieldValues() {
        return fieldValues;
    }

    public List<BodyFieldValue> getBodyFields() {
        return bodyFields;
    }

    public List<RelationshipReference> getRelationships() {
        return relationships;
    }

    public boolean shouldValidateFinalRelationships() {
        return validateFinalRelationships;
    }
}

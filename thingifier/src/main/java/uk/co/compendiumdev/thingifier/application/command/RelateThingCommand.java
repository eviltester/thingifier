package uk.co.compendiumdev.thingifier.application.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.NamedValue;

public final class RelateThingCommand implements ThingWriteCommand {

    private final String parentEntityName;
    private final String parentIdentifier;
    private final String relationshipName;
    private final List<NamedValue> bodyFieldValues;
    private final List<BodyFieldValue> bodyFields;
    private final List<RelationshipReference> bodyRelationships;

    public RelateThingCommand(
            final String parentEntityName,
            final String parentIdentifier,
            final String relationshipName,
            final List<NamedValue> bodyFieldValues,
            final List<RelationshipReference> bodyRelationships) {
        this(
                parentEntityName,
                parentIdentifier,
                relationshipName,
                bodyFieldValues,
                BodyFieldValue.fromNamedValues(bodyFieldValues),
                bodyRelationships);
    }

    public RelateThingCommand(
            final String parentEntityName,
            final String parentIdentifier,
            final String relationshipName,
            final List<NamedValue> bodyFieldValues,
            final List<BodyFieldValue> bodyFields,
            final List<RelationshipReference> bodyRelationships) {
        this.parentEntityName = parentEntityName;
        this.parentIdentifier = parentIdentifier;
        this.relationshipName = relationshipName;
        this.bodyFieldValues = Collections.unmodifiableList(new ArrayList<>(bodyFieldValues));
        this.bodyFields = Collections.unmodifiableList(new ArrayList<>(bodyFields));
        this.bodyRelationships = Collections.unmodifiableList(new ArrayList<>(bodyRelationships));
    }

    public String getParentEntityName() {
        return parentEntityName;
    }

    public String getParentIdentifier() {
        return parentIdentifier;
    }

    public String getRelationshipName() {
        return relationshipName;
    }

    public List<NamedValue> getBodyFieldValues() {
        return bodyFieldValues;
    }

    public List<BodyFieldValue> getBodyFields() {
        return bodyFields;
    }

    public List<RelationshipReference> getBodyRelationships() {
        return bodyRelationships;
    }
}

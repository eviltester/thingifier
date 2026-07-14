package uk.co.compendiumdev.thingifier.application.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.NamedValue;

public final class CreateAndConnectRelationshipCommand implements ThingWriteCommand {

    private final String parentEntityName;
    private final String parentIdentifier;
    private final String relationshipName;
    private final String childEntityName;
    private final List<NamedValue> childFieldValues;
    private final List<BodyFieldValue> childBodyFields;
    private final List<RelationshipReference> childRelationships;

    public CreateAndConnectRelationshipCommand(
            final String parentEntityName,
            final String parentIdentifier,
            final String relationshipName,
            final String childEntityName,
            final List<NamedValue> childFieldValues,
            final List<RelationshipReference> childRelationships) {
        this(
                parentEntityName,
                parentIdentifier,
                relationshipName,
                childEntityName,
                childFieldValues,
                BodyFieldValue.fromNamedValues(childFieldValues),
                childRelationships);
    }

    public CreateAndConnectRelationshipCommand(
            final String parentEntityName,
            final String parentIdentifier,
            final String relationshipName,
            final String childEntityName,
            final List<NamedValue> childFieldValues,
            final List<BodyFieldValue> childBodyFields,
            final List<RelationshipReference> childRelationships) {
        this.parentEntityName = parentEntityName;
        this.parentIdentifier = parentIdentifier;
        this.relationshipName = relationshipName;
        this.childEntityName = childEntityName;
        this.childFieldValues = Collections.unmodifiableList(new ArrayList<>(childFieldValues));
        this.childBodyFields = Collections.unmodifiableList(new ArrayList<>(childBodyFields));
        this.childRelationships = Collections.unmodifiableList(new ArrayList<>(childRelationships));
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

    public String getChildEntityName() {
        return childEntityName;
    }

    public List<NamedValue> getChildFieldValues() {
        return childFieldValues;
    }

    public List<BodyFieldValue> getChildBodyFields() {
        return childBodyFields;
    }

    public List<RelationshipReference> getChildRelationships() {
        return childRelationships;
    }
}

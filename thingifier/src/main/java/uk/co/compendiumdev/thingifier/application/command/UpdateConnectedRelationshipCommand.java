package uk.co.compendiumdev.thingifier.application.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.NamedValue;

/**
 * Updates an existing child that is already connected to a parent relationship.
 *
 * <p>This command backs relationship collection writes that are explicitly configured for {@code
 * UPDATE_CONNECTED}. It keeps the relationship route semantics separate from normal entity
 * amendment while reusing the same field validation and relationship-reference processing.
 */
public final class UpdateConnectedRelationshipCommand implements ThingWriteCommand {

    private final String parentEntityName;
    private final String parentIdentifier;
    private final String relationshipName;
    private final List<NamedValue> childFieldValues;
    private final List<BodyFieldValue> childBodyFields;
    private final List<RelationshipReference> childRelationships;

    /**
     * Creates a command to update a child already connected through a relationship.
     *
     * @param parentEntityName parent entity name
     * @param parentIdentifier parent route/query identifier
     * @param relationshipName relationship name from parent to child
     * @param childFieldValues parsed child field values from the request body
     * @param childBodyFields parsed child fields with source types
     * @param childRelationships relationship references supplied for the child
     */
    public UpdateConnectedRelationshipCommand(
            final String parentEntityName,
            final String parentIdentifier,
            final String relationshipName,
            final List<NamedValue> childFieldValues,
            final List<BodyFieldValue> childBodyFields,
            final List<RelationshipReference> childRelationships) {
        this.parentEntityName = parentEntityName;
        this.parentIdentifier = parentIdentifier;
        this.relationshipName = relationshipName;
        this.childFieldValues = Collections.unmodifiableList(new ArrayList<>(childFieldValues));
        this.childBodyFields = Collections.unmodifiableList(new ArrayList<>(childBodyFields));
        this.childRelationships = Collections.unmodifiableList(new ArrayList<>(childRelationships));
    }

    /**
     * Returns the parent entity name.
     *
     * @return parent entity name
     */
    public String getParentEntityName() {
        return parentEntityName;
    }

    /**
     * Returns the parent identifier from the relationship route.
     *
     * @return parent identifier
     */
    public String getParentIdentifier() {
        return parentIdentifier;
    }

    /**
     * Returns the relationship name from parent to child.
     *
     * @return relationship name
     */
    public String getRelationshipName() {
        return relationshipName;
    }

    /**
     * Returns the parsed child field values.
     *
     * @return immutable child field values
     */
    public List<NamedValue> getChildFieldValues() {
        return childFieldValues;
    }

    /**
     * Returns the parsed child body fields with original source types.
     *
     * @return immutable child body fields
     */
    public List<BodyFieldValue> getChildBodyFields() {
        return childBodyFields;
    }

    /**
     * Returns relationship references supplied for the child entity.
     *
     * @return immutable child relationship references
     */
    public List<RelationshipReference> getChildRelationships() {
        return childRelationships;
    }
}

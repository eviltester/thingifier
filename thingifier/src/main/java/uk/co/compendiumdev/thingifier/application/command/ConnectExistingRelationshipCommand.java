package uk.co.compendiumdev.thingifier.application.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.NamedValue;

public final class ConnectExistingRelationshipCommand implements ThingWriteCommand {

    private final String parentEntityName;
    private final String parentIdentifier;
    private final String relationshipName;
    private final List<NamedValue> childReferenceFields;
    private final String routeDisplay;

    public ConnectExistingRelationshipCommand(
            final String parentEntityName,
            final String parentIdentifier,
            final String relationshipName,
            final List<NamedValue> childReferenceFields,
            final String routeDisplay) {
        this.parentEntityName = parentEntityName;
        this.parentIdentifier = parentIdentifier;
        this.relationshipName = relationshipName;
        this.childReferenceFields =
                Collections.unmodifiableList(new ArrayList<>(childReferenceFields));
        this.routeDisplay = routeDisplay == null ? "" : routeDisplay;
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

    public List<NamedValue> getChildReferenceFields() {
        return childReferenceFields;
    }

    public String getRouteDisplay() {
        return routeDisplay;
    }
}

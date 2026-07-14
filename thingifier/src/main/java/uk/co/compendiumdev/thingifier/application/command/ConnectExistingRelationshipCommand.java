package uk.co.compendiumdev.thingifier.application.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.NamedValue;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

public final class ConnectExistingRelationshipCommand implements ThingWriteCommand {

    private final EntityInstance parent;
    private final EntityDefinition parentEntity;
    private final String parentIdentifier;
    private final String relationshipName;
    private final EntityInstance child;
    private final List<NamedValue> childReferenceFields;
    private final String routeDisplay;

    public ConnectExistingRelationshipCommand(
            final EntityInstance parent,
            final String relationshipName,
            final EntityInstance child) {
        this.parent = parent;
        this.parentEntity = parent.getEntity();
        this.parentIdentifier = parent.getPrimaryKeyValue();
        this.relationshipName = relationshipName;
        this.child = child;
        this.childReferenceFields = Collections.emptyList();
        this.routeDisplay = "";
    }

    public ConnectExistingRelationshipCommand(
            final EntityDefinition parentEntity,
            final String parentIdentifier,
            final String relationshipName,
            final List<NamedValue> childReferenceFields,
            final String routeDisplay) {
        this.parent = null;
        this.parentEntity = parentEntity;
        this.parentIdentifier = parentIdentifier;
        this.relationshipName = relationshipName;
        this.child = null;
        this.childReferenceFields =
                Collections.unmodifiableList(new ArrayList<>(childReferenceFields));
        this.routeDisplay = routeDisplay;
    }

    public EntityInstance getParent() {
        return parent;
    }

    public boolean hasResolvedRelationship() {
        return parent != null && child != null;
    }

    public EntityDefinition getParentEntity() {
        return parentEntity;
    }

    public String getParentIdentifier() {
        return parentIdentifier;
    }

    public String getRelationshipName() {
        return relationshipName;
    }

    public EntityInstance getChild() {
        return child;
    }

    public List<NamedValue> getChildReferenceFields() {
        return childReferenceFields;
    }

    public String getRouteDisplay() {
        return routeDisplay;
    }
}

package uk.co.compendiumdev.thingifier.application.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;

public final class CreateAndConnectRelationshipCommand implements ThingWriteCommand {

    private final EntityInstance parent;
    private final EntityDefinition parentEntity;
    private final String parentIdentifier;
    private final String relationshipName;
    private final EntityInstanceDraft childDraft;
    private final List<RelationshipReference> childRelationships;
    private final String routeDisplay;

    public CreateAndConnectRelationshipCommand(
            final EntityInstance parent,
            final String relationshipName,
            final EntityInstanceDraft childDraft,
            final List<RelationshipReference> childRelationships) {
        this.parent = parent;
        this.parentEntity = parent.getEntity();
        this.parentIdentifier = parent.getPrimaryKeyValue();
        this.relationshipName = relationshipName;
        this.childDraft = childDraft;
        this.childRelationships = Collections.unmodifiableList(new ArrayList<>(childRelationships));
        this.routeDisplay = "";
    }

    public CreateAndConnectRelationshipCommand(
            final EntityDefinition parentEntity,
            final String parentIdentifier,
            final String relationshipName,
            final EntityInstanceDraft childDraft,
            final List<RelationshipReference> childRelationships,
            final String routeDisplay) {
        this.parent = null;
        this.parentEntity = parentEntity;
        this.parentIdentifier = parentIdentifier;
        this.relationshipName = relationshipName;
        this.childDraft = childDraft;
        this.childRelationships = Collections.unmodifiableList(new ArrayList<>(childRelationships));
        this.routeDisplay = routeDisplay;
    }

    public EntityInstance getParent() {
        return parent;
    }

    public boolean hasResolvedParent() {
        return parent != null;
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

    public EntityInstanceDraft getChildDraft() {
        return childDraft;
    }

    public List<RelationshipReference> getChildRelationships() {
        return childRelationships;
    }

    public String getRouteDisplay() {
        return routeDisplay;
    }
}

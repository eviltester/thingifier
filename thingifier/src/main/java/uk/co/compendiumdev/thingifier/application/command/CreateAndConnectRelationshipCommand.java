package uk.co.compendiumdev.thingifier.application.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;

public final class CreateAndConnectRelationshipCommand implements ThingWriteCommand {

    private final EntityInstance parent;
    private final String relationshipName;
    private final EntityInstanceDraft childDraft;
    private final List<RelationshipReference> childRelationships;

    public CreateAndConnectRelationshipCommand(
            final EntityInstance parent,
            final String relationshipName,
            final EntityInstanceDraft childDraft,
            final List<RelationshipReference> childRelationships) {
        this.parent = parent;
        this.relationshipName = relationshipName;
        this.childDraft = childDraft;
        this.childRelationships = Collections.unmodifiableList(new ArrayList<>(childRelationships));
    }

    public EntityInstance getParent() {
        return parent;
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
}

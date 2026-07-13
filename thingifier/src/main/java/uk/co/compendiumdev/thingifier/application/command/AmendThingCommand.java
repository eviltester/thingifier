package uk.co.compendiumdev.thingifier.application.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;

public final class AmendThingCommand implements ThingWriteCommand {

    private final EntityInstance instance;
    private final EntityInstanceDraft draft;
    private final boolean replaceExistingFieldsAndRelationships;
    private final List<RelationshipReference> relationships;

    public AmendThingCommand(
            final EntityInstance instance,
            final EntityInstanceDraft draft,
            final boolean replaceExistingFieldsAndRelationships,
            final List<RelationshipReference> relationships) {
        this.instance = instance;
        this.draft = draft;
        this.replaceExistingFieldsAndRelationships = replaceExistingFieldsAndRelationships;
        this.relationships = Collections.unmodifiableList(new ArrayList<>(relationships));
    }

    public EntityInstance getInstance() {
        return instance;
    }

    public EntityInstanceDraft getDraft() {
        return draft;
    }

    public boolean shouldReplaceExistingFieldsAndRelationships() {
        return replaceExistingFieldsAndRelationships;
    }

    public List<RelationshipReference> getRelationships() {
        return relationships;
    }
}

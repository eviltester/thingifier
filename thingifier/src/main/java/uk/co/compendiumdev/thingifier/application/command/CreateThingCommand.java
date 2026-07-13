package uk.co.compendiumdev.thingifier.application.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;

public final class CreateThingCommand implements ThingWriteCommand {

    private final EntityInstanceDraft draft;
    private final List<RelationshipReference> relationships;
    private final boolean validateFinalRelationships;

    public CreateThingCommand(
            final EntityInstanceDraft draft,
            final List<RelationshipReference> relationships,
            final boolean validateFinalRelationships) {
        this.draft = draft;
        this.relationships = Collections.unmodifiableList(new ArrayList<>(relationships));
        this.validateFinalRelationships = validateFinalRelationships;
    }

    public EntityInstanceDraft getDraft() {
        return draft;
    }

    public List<RelationshipReference> getRelationships() {
        return relationships;
    }

    public boolean shouldValidateFinalRelationships() {
        return validateFinalRelationships;
    }
}

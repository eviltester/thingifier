package uk.co.compendiumdev.thingifier.application.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.NamedValue;

public final class PutThingCommand implements ThingWriteCommand {

    private final EntityDefinition entity;
    private final String identifier;
    private final List<NamedValue> fieldValues;
    private final List<RelationshipReference> relationships;

    public PutThingCommand(
            final EntityDefinition entity,
            final String identifier,
            final List<NamedValue> fieldValues,
            final List<RelationshipReference> relationships) {
        this.entity = entity;
        this.identifier = identifier;
        this.fieldValues = Collections.unmodifiableList(new ArrayList<>(fieldValues));
        this.relationships = Collections.unmodifiableList(new ArrayList<>(relationships));
    }

    public EntityDefinition getEntity() {
        return entity;
    }

    public String getIdentifier() {
        return identifier;
    }

    public List<NamedValue> getFieldValues() {
        return fieldValues;
    }

    public List<RelationshipReference> getRelationships() {
        return relationships;
    }
}

package uk.co.compendiumdev.thingifier.application.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.NamedValue;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;

public final class AmendThingCommand implements ThingWriteCommand {

    private final EntityInstance instance;
    private final EntityDefinition entity;
    private final String identifier;
    private final EntityInstanceDraft draft;
    private final List<NamedValue> fieldValues;
    private final boolean replaceExistingFieldsAndRelationships;
    private final List<RelationshipReference> relationships;
    private final String missingInstanceMessage;

    public AmendThingCommand(
            final EntityInstance instance,
            final EntityInstanceDraft draft,
            final boolean replaceExistingFieldsAndRelationships,
            final List<RelationshipReference> relationships) {
        this.instance = instance;
        this.entity = instance.getEntity();
        this.identifier = instance.getPrimaryKeyValue();
        this.draft = draft;
        this.fieldValues = Collections.emptyList();
        this.replaceExistingFieldsAndRelationships = replaceExistingFieldsAndRelationships;
        this.relationships = Collections.unmodifiableList(new ArrayList<>(relationships));
        this.missingInstanceMessage = "";
    }

    public AmendThingCommand(
            final EntityDefinition entity,
            final String identifier,
            final List<NamedValue> fieldValues,
            final boolean replaceExistingFieldsAndRelationships,
            final List<RelationshipReference> relationships) {
        this(
                entity,
                identifier,
                fieldValues,
                replaceExistingFieldsAndRelationships,
                relationships,
                "");
    }

    public AmendThingCommand(
            final EntityDefinition entity,
            final String identifier,
            final List<NamedValue> fieldValues,
            final boolean replaceExistingFieldsAndRelationships,
            final List<RelationshipReference> relationships,
            final String missingInstanceMessage) {
        this.instance = null;
        this.entity = entity;
        this.identifier = identifier;
        this.draft = null;
        this.fieldValues = Collections.unmodifiableList(new ArrayList<>(fieldValues));
        this.replaceExistingFieldsAndRelationships = replaceExistingFieldsAndRelationships;
        this.relationships = Collections.unmodifiableList(new ArrayList<>(relationships));
        this.missingInstanceMessage = missingInstanceMessage;
    }

    public EntityInstance getInstance() {
        return instance;
    }

    public boolean hasResolvedInstance() {
        return instance != null;
    }

    public EntityDefinition getEntity() {
        return entity;
    }

    public String getIdentifier() {
        return identifier;
    }

    public EntityInstanceDraft getDraft() {
        return draft;
    }

    public List<NamedValue> getFieldValues() {
        return fieldValues;
    }

    public boolean shouldReplaceExistingFieldsAndRelationships() {
        return replaceExistingFieldsAndRelationships;
    }

    public List<RelationshipReference> getRelationships() {
        return relationships;
    }

    public String getMissingInstanceMessage() {
        return missingInstanceMessage;
    }
}

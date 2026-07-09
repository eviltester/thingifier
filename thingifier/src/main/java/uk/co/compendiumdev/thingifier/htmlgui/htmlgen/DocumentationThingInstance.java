package uk.co.compendiumdev.thingifier.htmlgui.htmlgen;

import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;

public class DocumentationThingInstance{

    private final EntityInstanceDraft draft;
    private final EntityDefinition definition;

    public DocumentationThingInstance(final EntityDefinition eDefn) {
        this.definition = eDefn;
        this.draft = EntityInstanceDraft.forEntity(eDefn);
    }

    public EntityInstance getInstance(){
        return EntityInstance.fromDraft(this.draft);
    }

    public EntityInstance withoutIDsOrGUIDs(){
        for(String name : definition.getFieldNamesOfType(FieldType.AUTO_INCREMENT, FieldType.AUTO_GUID)){
            overrideValue(name, null);
        }
        return getInstance();
    }

    public void overrideValue(final String name, final String exampleValue) {
        Field field = definition.getField(name);
        if (field != null &&
                (field.getType() == FieldType.AUTO_INCREMENT ||
                        field.getType() == FieldType.AUTO_GUID)) {
            this.draft.withProtectedField(name, exampleValue);
            return;
        }
        this.draft.withField(name, exampleValue);
    }
}

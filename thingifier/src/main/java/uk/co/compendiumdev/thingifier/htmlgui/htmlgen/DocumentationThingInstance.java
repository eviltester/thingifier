package uk.co.compendiumdev.thingifier.htmlgui.htmlgen;

import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

public class DocumentationThingInstance{

    private final EntityInstance instance;
    private final EntityDefinition definition;

    public DocumentationThingInstance(final EntityDefinition eDefn) {
        this.definition = eDefn;
        this.instance = new EntityInstance(eDefn);
    }

    public EntityInstance getInstance(){
        return this.instance;
    }

    public EntityInstance withoutIDsOrGUIDs(){
        for(String name : definition.getFieldNamesOfType(FieldType.AUTO_INCREMENT, FieldType.AUTO_GUID)){
            overrideValue(name, null);
        }
        return instance;
    }

    public void overrideValue(final String name, final String exampleValue) {
        this.instance.overrideValue(name, exampleValue);
    }
}

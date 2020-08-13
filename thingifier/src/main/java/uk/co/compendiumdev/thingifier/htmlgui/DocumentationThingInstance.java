package uk.co.compendiumdev.thingifier.htmlgui;

import uk.co.compendiumdev.thingifier.domain.definitions.ThingDefinition;
import uk.co.compendiumdev.thingifier.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.domain.instances.ThingInstance;

public class DocumentationThingInstance{

    private final ThingInstance instance;
    private final ThingDefinition definition;

    public DocumentationThingInstance(final ThingDefinition eDefn) {
        this.definition = eDefn;
        this.instance = ThingInstance.createExampleInstance(eDefn);
    }

    public ThingInstance getInstance(){
        return this.instance;
    }

    public ThingInstance withoutIDsOrGUIDs(){
        for(String name : definition.getFieldNamesOfType(FieldType.ID, FieldType.GUID)){
            overrideValue(name, null);
        }
        return instance;
    }

    public void overrideValue(final String name, final String exampleValue) {
        this.instance.overrideValue(name, exampleValue);
    }
}

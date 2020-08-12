package uk.co.compendiumdev.thingifier.domain.instances;

import uk.co.compendiumdev.thingifier.domain.definitions.ThingDefinition;
import uk.co.compendiumdev.thingifier.domain.definitions.field.definition.FieldType;

public class DocumentationThingInstance{

    private final ThingInstance instance;
    private final ThingDefinition definition;

    public DocumentationThingInstance(final ThingDefinition eDefn) {
        this.definition = eDefn;
        this.instance = ThingInstance.getInstanceWithoutIds(eDefn);
    }

    public ThingInstance getInstance(){
        return this.instance;
    }

    public ThingInstance getInstanceWithoutProtectedFields(){
        ThingInstance duplicate = instance.createDuplicateWithRelationships();
        for(String name : definition.getFieldNamesOfType(FieldType.ID, FieldType.GUID)){
            duplicate.overrideValue(name, null);
        }
        return duplicate;
    }

    public void overrideValue(final String name, final String exampleValue) {
        this.instance.overrideValue(name, exampleValue);
    }
}

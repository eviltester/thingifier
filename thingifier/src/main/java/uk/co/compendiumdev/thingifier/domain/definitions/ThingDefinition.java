package uk.co.compendiumdev.thingifier.domain.definitions;

import uk.co.compendiumdev.thingifier.domain.FieldType;
import java.util.*;



public class ThingDefinition {
    private String name;
    private String plural;

    private DefinedFields fields;
    private DefinedRelationships definedRelationships;



    private ThingDefinition(String name, String plural) {
        this.name = name;
        this.plural = plural;
        definedRelationships = new DefinedRelationships();
        fields = new DefinedFields();

    }

    public static ThingDefinition create(String name, String plural) {
        ThingDefinition entityDefinition = new ThingDefinition(name, plural);
        entityDefinition.addField(Field.is("guid", FieldType.GUID));
        return entityDefinition;
    }

    public String toString() {
        StringBuilder output = new StringBuilder();

        output.append("\t" + name + "\n");
        output.append(fields.toString());

        return output.toString();
    }


    public String getName() {
        return name;
    }


    public String getPlural() {
        return plural;
    }


    public void addField(Field aField) {
        fields.addField(aField);
    }

    public List<String> getFieldNames() {
        return fields.getFieldNames();
    }

    public boolean hasFieldNameDefined(String fieldName) {
        return fields.hasFieldNameDefined(fieldName);
    }

    public DefinedFields getFieldDefinitions() {
        return fields;
    }

    public ThingDefinition and() {
        return this;
    }


    public ThingDefinition addFields(Field... theseFields) {
        fields.addFields(theseFields);
        return this;
    }

    public Field getField(String fieldName) {
        return fields.getField(fieldName);
    }


    public boolean hasIDField() {
        return fields.getFieldsOfType(FieldType.ID).size()>0;
    }

    // todo: this suggests there is only one, but there might be more and that could prove problematic
    public Field getIDField() {
        List<Field> ids = fields.getFieldsOfType(FieldType.ID);
        if(ids.size()>0){
            return ids.get(0);
        }
        return null;
    }

    public List<String> getProtectedFieldNamesList() {
        List<String> protectedNames = new ArrayList();
        List<Field> protectedFields = fields.getFieldsOfType(FieldType.ID, FieldType.GUID);

        for(Field field : protectedFields){
            protectedNames.add(field.getName());
        }

        return protectedNames;
    }

    public DefinedRelationships related(){
        return definedRelationships;
    }

}

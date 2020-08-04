package uk.co.compendiumdev.thingifier.domain.definitions;

import uk.co.compendiumdev.thingifier.domain.FieldType;
import uk.co.compendiumdev.thingifier.domain.instances.InstanceFields;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class ThingDefinition {
    private String name;
    private String plural;

    DefinedFields fields;
    private DefinedRelationships relationships;

    private int nextId;

    private ThingDefinition() {
        relationships = new DefinedRelationships();
        fields = new DefinedFields();
        nextId=1;
    }

    public static ThingDefinition create(String name, String plural) {
        ThingDefinition entityDefinition = new ThingDefinition();
        entityDefinition.setName(name);
        entityDefinition.setPlural(plural);
        entityDefinition.addField(Field.is("guid", FieldType.GUID));
        return entityDefinition;
    }

    public String toString() {
        StringBuilder output = new StringBuilder();

        output.append("\t" + name + "\n");
        output.append(fields.toString());

        return output.toString();
    }

    public ThingDefinition setName(String name) {
        this.name = name;
        if (plural == null || plural.length() == 0) {
            this.setPlural(name);
        }
        return this;
    }

    public String getName() {
        return name;
    }

    public ThingDefinition setPlural(String plural) {
        this.plural = plural;
        return this;
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

    public void addIdsToInstance(final InstanceFields instance) {
        List<Field>idfields = fields.getFieldsOfType(FieldType.ID);
        for(Field aField : idfields){
            if(aField.getType()==FieldType.ID){
                if(!instance.hasFieldNamed(aField.getName())) {
                    instance.addValue(aField.getName(), getNextIdValue());
                }
            }
        }
    }

    // TODO: this could support multiple ids by giving them a name and keeping them in a HashSet
    //      e.g. an IDCounter idCounter.getNext("name")
    private String getNextIdValue() {
        int id = nextId;
        nextId++;
        return String.valueOf(id);
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

    /*
        RELATIONSHIPS
     */
    public boolean hasRelationship(String relationshipName) {
        return relationships.hasRelationship(relationshipName);
    }

    public RelationshipVector getRelationship(String relationshipName, ThingDefinition toEntityDefinition) {
        return relationships.getRelationship(relationshipName, toEntityDefinition);
    }

    public void addRelationship(RelationshipVector relationship) {
        relationships.addRelationship(relationship);
    }

    public List<RelationshipVector> getRelationships(String relationshipName) {
        return relationships.getRelationships(relationshipName);
    }

    public Collection<RelationshipVector> getRelationships() {
        return relationships.getRelationships();
    }


}

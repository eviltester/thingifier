package uk.co.compendiumdev.thingifier.generic.definitions;

import uk.co.compendiumdev.thingifier.generic.FieldType;
import uk.co.compendiumdev.thingifier.generic.instances.InstanceFields;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class ThingDefinition {
    private String name;
    private Map<String, Field> fields = new ConcurrentHashMap<String, Field>();
    private String plural;

    private Map<String, List<RelationshipVector>> relationships;
    private int nextId;

    private ThingDefinition() {
        relationships = new ConcurrentHashMap<String, List<RelationshipVector>>();
        nextId=1;
    }

    public static ThingDefinition create(String name, String plural) {
        ThingDefinition entityDefinition = new ThingDefinition();
        entityDefinition.setName(name);
        entityDefinition.setPlural(plural);
        entityDefinition.addField(Field.is("guid", FieldType.STRING));
        return entityDefinition;
    }

    public String toString() {
        StringBuilder output = new StringBuilder();

        output.append("\t" + name + "\n");
        output.append("\t\tFields:\n");

        for (Field aField : fields.values()) {

            output.append("\t\t\t" + aField.getName() + "\n");
        }

        return output.toString();
    }

    public ThingDefinition setName(String name) {
        this.name = name;
        if (plural == null || plural.length() == 0) {
            this.setPlural(name);
        }
        return this;
    }

    public void addField(Field aField) {
        fields.put(aField.getName().toLowerCase(), aField);
    }

    public String getName() {
        return name;
    }

    public List<String> getFieldNames() {
        ArrayList<String> fieldNames = new ArrayList<String>();
        for (Field field : fields.values()) {
            fieldNames.add(field.getName());
        }
        return fieldNames;
    }

    public ThingDefinition setPlural(String plural) {
        this.plural = plural;
        return this;
    }

    public String getPlural() {
        return plural;
    }

    public boolean hasFieldNameDefined(String fieldName) {
        return fields.keySet().contains(fieldName.toLowerCase());
    }

    public ThingDefinition and() {
        return this;
    }


    public ThingDefinition addFields(Field... theseFields) {
        for (Field aField : theseFields) {
            addField(aField);
        }
        return this;
    }


    public Field getField(String fieldName) {
        if (hasFieldNameDefined(fieldName)) {
            return fields.get(fieldName.toLowerCase());
        }
        return null;
    }

    public boolean hasRelationship(String relationshipName) {
        return this.relationships.containsKey(relationshipName.toLowerCase());
    }

    public RelationshipVector getRelationship(String relationshipName, ThingDefinition toEntityDefinition) {

        List<RelationshipVector> relationshipsWithThisName = this.relationships.get(relationshipName.toLowerCase());
        if (relationshipsWithThisName == null) {
            // there is no relationship with this name
            return null;
        }

        for (RelationshipVector relationship : relationshipsWithThisName) {
            if (relationship.getTo().definition() == toEntityDefinition) {
                return relationship;
            }
        }

        // there is no relationship with this name between the things we want
        return null;

    }

    public void addRelationship(RelationshipVector relationship) {

        List<RelationshipVector> relationshipsWithThisName = this.relationships.get(relationship.getName());
        if (relationshipsWithThisName == null) {
            // there is no relationship with this name
            relationshipsWithThisName = new ArrayList<RelationshipVector>();
            this.relationships.put(relationship.getName(), relationshipsWithThisName);
        }

        relationshipsWithThisName.add(relationship);
    }

    public List<RelationshipVector> getRelationships(String relationshipName) {
        List<RelationshipVector> myrelationships = this.relationships.get(relationshipName.toLowerCase());
        ArrayList<RelationshipVector> retRels = new ArrayList<RelationshipVector>(myrelationships);
        return retRels;
    }

    public Collection<RelationshipVector> getRelationships() {
        // TODO: at the moment this is overly complicated because it supports duplicate named relationship which we don't use or test for
        Set<RelationshipVector> myRelationships = new HashSet<>();
        for (List<RelationshipVector> list : relationships.values()) {
            for (RelationshipVector rel : list) {
                myRelationships.add(rel);
            }

        }
        return myRelationships;
    }


    public void addIdsToInstance(final InstanceFields instance) {
        for(Map.Entry<String,Field> field : fields.entrySet()){
            Field aField = field.getValue();
            if(aField.getType()==FieldType.ID){
                if(!instance.hasFieldNamed(aField.getName())) {
                    instance.addValue(aField.getName(), getNextIdValue());
                }
            }
        }
    }

    // TODO: this could support multiple ids by giving them a name and keeping them in a HashSet
    private String getNextIdValue() {
        int id = nextId;
        nextId++;
        return String.valueOf(id);
    }

    public boolean hasIDField() {
        return getIDField()!=null;
    }

    public Field getIDField() {
        for (Field aField : fields.values()) {
            if(aField.getType()==FieldType.ID){
                return aField;
            }
        }
        return null;
    }
}

package uk.co.compendiumdev.thingifier.generic.definitions;

import uk.co.compendiumdev.thingifier.generic.FieldType;
import uk.co.compendiumdev.thingifier.generic.GUID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ThingDefinition {
    private String name;
    private Map<String, Field> fields = new HashMap<String, Field>();
    private String plural;
    private String guid;
    private Map<String, List<RelationshipDefinition>> relationships;

    private ThingDefinition(){
        guid = GUID.create();
        relationships = new HashMap<String, List<RelationshipDefinition>>();
    }

    public static ThingDefinition create(String name, String plural){
        ThingDefinition entityDefinition = new ThingDefinition();
        entityDefinition.setName(name);
        entityDefinition.setPlural(plural);
        entityDefinition.addField(Field.is("guid", FieldType.STRING));
        return entityDefinition;
    }

    public String toString(){
        StringBuilder output = new StringBuilder();

        output.append("\t" + name + "\n");
        output.append("\t\tFields:\n");

        for(Field aField : fields.values()){

            output.append("\t\t\t" + aField.getName() + "\n");
        }

        return output.toString();
    }

    public ThingDefinition setName(String name) {
        this.name = name;
        if(plural == null || plural.length()==0){
            this.setPlural(name);
        }
        return this;
    }

    public ThingDefinition defineField(String fieldName) {

        addField(Field.is(fieldName));
        return this;
    }

    private void addField(Field aField) {
        fields.put(aField.getName().toLowerCase(), aField);
    }

    public String getName() {
        return name;
    }

    public List<String> getFieldNames() {
        ArrayList<String> fieldNames = new ArrayList<String>();
        for(Field field : fields.values()){
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

    public ThingDefinition and(){
        return this;
    }


    public ThingDefinition addFields(Field... theseFields) {
        for(Field aField : theseFields){
            addField(aField);
        }
        return this;
    }




    public Field getField(String fieldName) {
        if(hasFieldNameDefined(fieldName)){
            return fields.get(fieldName.toLowerCase());
        }
        return null;
    }

    public boolean hasRelationship(String relationshipName) {
        return this.relationships.containsKey(relationshipName.toLowerCase());
    }

    public RelationshipDefinition getRelationship(String relationshipName, ThingDefinition toEntityDefinition) {

        List<RelationshipDefinition> relatinionshipsWithThisName = this.relationships.get(relationshipName.toLowerCase());
        if(relatinionshipsWithThisName==null){
            // there is no relationship with this name
            return null;
        }

        for(RelationshipDefinition relationship : relatinionshipsWithThisName){
            if(relationship.to() == toEntityDefinition){
                return relationship;
            }
        }

        // there is no relationship with this name
        return null;

    }

    public void addRelationship(RelationshipDefinition relationship) {

        List<RelationshipDefinition> relationshipsWithThisName = this.relationships.get(relationship.getName());
        if(relationshipsWithThisName==null){
            // there is no relationship with this name
            relationshipsWithThisName = new ArrayList<RelationshipDefinition>();
            this.relationships.put(relationship.getName(), relationshipsWithThisName);
        }

        relationshipsWithThisName.add(relationship);
    }

    public List<RelationshipDefinition> getRelationships(String relationshipName) {
        List<RelationshipDefinition> myrelationships = this.relationships.get(relationshipName.toLowerCase());
        ArrayList<RelationshipDefinition> retRels = new ArrayList<RelationshipDefinition>();
        retRels.addAll(myrelationships);
        return retRels;
    }

}

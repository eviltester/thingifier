package uk.co.compendiumdev.thingifier.generic.instances;

import uk.co.compendiumdev.thingifier.generic.GUID;
import uk.co.compendiumdev.thingifier.generic.definitions.Field;
import uk.co.compendiumdev.thingifier.generic.definitions.RelationshipDefinition;
import uk.co.compendiumdev.thingifier.generic.definitions.ThingDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ThingInstance {


    private final List<RelationshipInstance> relationships;
    private final ThingDefinition entityDefinition;
    private final InstanceFields instance;
    private List<RelationshipInstance> amRelatedTo;

    public ThingInstance(ThingDefinition eDefn) {
        this.entityDefinition = eDefn;
        this.instance = new InstanceFields();
        instance.addValue("guid", GUID.create());
        this.relationships = new ArrayList<RelationshipInstance>();
        this.amRelatedTo = new ArrayList<RelationshipInstance>();
    }

    public ThingInstance(ThingDefinition entityTestSession, String guid) {
        this(entityTestSession);
        instance.addValue("guid", guid);
    }

    public String toString(){

        StringBuilder output = new StringBuilder();

        output.append("\t\t\t" + entityDefinition.getName() + "\n");
        //output.append(instance.toString() + "\n");
        for(String fieldName : entityDefinition.getFieldNames()){
            output.append(String.format("\t\t\t\t %s : %s %n", fieldName, getValue(fieldName)));
        }

        if(relationships.size()>0) {
            output.append(String.format("\t\t\t\t\t Relationships:\n"));
            for (RelationshipInstance relatesTo : relationships) {
                output.append(String.format("\t\t\t\t\t %s : %s (%s)%n",
                        relatesTo.getRelationship().getName(),
                        relatesTo.getTo().getGUID(),
                        relatesTo.getTo().getEntity().getName()));
            }
        }

        return output.toString();
    }

    public String getGUID() {
        return instance.getValue("guid");
    }

    public List<String> getFieldNames() {
        return this.entityDefinition.getFieldNames();
    }

    public ThingInstance setValue(String fieldName, String value)  {
        if(this.entityDefinition.hasFieldNameDefined(fieldName)){
            Field field = entityDefinition.getField(fieldName);
            if(field.isValidValue(value)) {
                this.instance.addValue(fieldName, value);
            }else{
                throw new IllegalArgumentException(String.format("Invalid Value %s for field %s of type %s", value, field.getName(), field.getType()));
            }
        }else{
            reportError(fieldName);
        }
        return this;
    }

    public ThingInstance setFieldValuesFrom(String[] fieldValuesFrom) {
        instance.setFieldValuesFrom(fieldValuesFrom);
        return this;
    }

    public ThingInstance setFieldValuesFrom(Map<String, String> args) {

        for(Map.Entry<String, String> entry : args.entrySet()){
            setValue(entry.getKey(), entry.getValue());
        }
        return this;
    }

    private void reportError(String fieldName)  {
        throw new RuntimeException("Could not find field: " + fieldName + " on Entity " + this.entityDefinition.getName());
    }

    public String getValue(String fieldName) {
        if(this.entityDefinition.hasFieldNameDefined(fieldName)){
            String assignedValue = this.instance.getValue(fieldName);
            if(assignedValue==null){
                if(this.entityDefinition.getField(fieldName).hasDefaultValue()){
                    return getDefaultValue(fieldName);
                }else{
                    // does definition have a default value?
                    String defaultVal = this.entityDefinition.getField(fieldName).getType().getDefault();
                    if(defaultVal!=null){
                        return defaultVal;
                    }
                }
            }else{
                return assignedValue;
            }
        }

        reportError(fieldName);
        return "";
    }

    public ThingDefinition getEntity() {
        return this.entityDefinition;
    }


    public String getDefaultValue(String defaultFieldValue) {
        return entityDefinition.getField(defaultFieldValue).getDefaultValue();
    }


    public void connects(String relationshipName, ThingInstance thing) {

        // TODO: enforce cardinality

        // find relationship
        if(!entityDefinition.hasRelationship(relationshipName)){
            throw new IllegalArgumentException(String.format("Unkown Relationship %s for %s : %s", relationshipName, entityDefinition.getName(), getGUID()));
        }

        RelationshipDefinition relationship = entityDefinition.getRelationship(relationshipName, thing.entityDefinition);

        RelationshipInstance related = new RelationshipInstance(relationship, this, thing);
        this.relationships.add(related);

        thing.isNowRelatedVia(related);
    }

    private void isNowRelatedVia(RelationshipInstance relationship) {
        this.amRelatedTo.add(relationship);
    }

    public List<RelationshipInstance> connections(String relationshipName) {
        List<RelationshipInstance> theConnections = new ArrayList<RelationshipInstance>();
        for(RelationshipInstance relationship : relationships){
            if(relationship.getRelationship().getName().toLowerCase().contentEquals(relationshipName.toLowerCase())){
                theConnections.add(relationship);
            }
        }
        return theConnections;
    }

    public List<ThingInstance> connectedItems(String relationshipName) {
        List<ThingInstance> theConnectedItems = new ArrayList<ThingInstance>();
        for(RelationshipInstance relationship : relationships){
            if(relationship.getRelationship().getName().toLowerCase().contentEquals(relationshipName.toLowerCase())){
                theConnectedItems.add(relationship.getTo());
            }
        }
        return theConnectedItems;
    }

    public void tellRelatedItemsIAmDeleted() {
        for(RelationshipInstance item : amRelatedTo){
            item.getFrom().removeRelationshipsToMe(this);
        }

        // I am related to nothing for I am dead
        amRelatedTo = new ArrayList<RelationshipInstance>();
    }

    public void removeRelationshipsTo(ThingInstance thing, String relationshipName) {
        List<RelationshipInstance> toDelete = new ArrayList<RelationshipInstance>();

        for(RelationshipInstance relationship : relationships){
            if(relationship.getTo()==thing && relationship.getRelationship().getName().equalsIgnoreCase(relationshipName)){
                toDelete.add(relationship);
            }
        }

        relationships.removeAll(toDelete);
    }

    private void removeRelationshipsToMe(ThingInstance thing) {
        List<RelationshipInstance> toDelete = new ArrayList<RelationshipInstance>();

        for(RelationshipInstance relationship : relationships){
            if(relationship.getTo()==thing){
                toDelete.add(relationship);
            }
        }

        relationships.removeAll(toDelete);
    }

    public List<ThingInstance> connectedItemsOfType(String type) {
        List<ThingInstance> theConnectedItems = new ArrayList<ThingInstance>();
        for(RelationshipInstance relationship : relationships){
            if(relationship.getTo().getEntity().getName().toLowerCase().contentEquals(type.toLowerCase())){
                theConnectedItems.add(relationship.getTo());
            }
        }
        return theConnectedItems;
    }


    public void clearAllFields() {
        instance.deleteAllFieldsExcept("guid");
    }


}

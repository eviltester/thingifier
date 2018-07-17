package uk.co.compendiumdev.thingifier.generic.instances;

import uk.co.compendiumdev.thingifier.api.ValidationReport;
import uk.co.compendiumdev.thingifier.generic.GUID;
import uk.co.compendiumdev.thingifier.generic.definitions.Field;
import uk.co.compendiumdev.thingifier.generic.definitions.RelationshipVector;
import uk.co.compendiumdev.thingifier.generic.definitions.ThingDefinition;

import java.util.*;

public class ThingInstance {


    private final List<RelationshipInstance> relationships;
    private final ThingDefinition entityDefinition;
    private final InstanceFields instance;

    public ThingInstance(ThingDefinition eDefn) {
        this.entityDefinition = eDefn;
        this.instance = new InstanceFields();
        instance.addValue("guid", GUID.create());
        this.relationships = new ArrayList<RelationshipInstance>();
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


    /**
     * connect this thing to another thing using the relationship relationshipName
     */
    public void connects(String relationshipName, ThingInstance thing) {

        // TODO: enforce cardinality

        // find relationship
        if(!entityDefinition.hasRelationship(relationshipName)){
            throw new IllegalArgumentException(String.format("Unknown Relationship %s for %s : %s", relationshipName, entityDefinition.getName(), getGUID()));
        }

        RelationshipVector relationship = entityDefinition.getRelationship(relationshipName, thing.entityDefinition);

        RelationshipInstance related = new RelationshipInstance(relationship, this, thing);
        this.relationships.add(related);

        thing.isNowRelatedVia(related);

    }

    private void isNowRelatedVia(RelationshipInstance relationship) {

        // if the relationship vector has a parent that is both ways then we need to create a relationship of the reverse type to the thing that called us
        if(relationship.getRelationship().isTwoWay()){
            this.relationships.add(relationship);
        }
    }

    public Collection<ThingInstance> connectedItems(String relationshipName) {
        Set<ThingInstance> theConnectedItems = new HashSet<ThingInstance>();
        for(RelationshipInstance relationship : relationships){
            if(relationship.getRelationship().isKnownAs(relationshipName)){
                if(relationship.getTo()== this){
                    theConnectedItems.add(relationship.getFrom());
                }else{
                    theConnectedItems.add(relationship.getTo());
                }
            }
        }

        return theConnectedItems;
    }

    public void removeRelationshipsTo(ThingInstance thing, String relationshipName) {
        List<RelationshipInstance> toDelete = new ArrayList<RelationshipInstance>();

        for(RelationshipInstance relationship : relationships){
            if(relationship.getRelationship().isKnownAs(relationshipName)){
                if(relationship.getTo() == thing || relationship.getFrom()==thing){
                    toDelete.add(relationship);
                    thing.isNoLongerRelatedVia(relationship);
                }
            }
        }

        relationships.removeAll(toDelete);

    }

    public void removeAllRelationships() {

        for(RelationshipInstance item : relationships){
            if(item.getFrom()!= this) {
                item.getFrom().removeRelationshipsInvolvingMe(this);
            }else{
                item.getTo().removeRelationshipsInvolvingMe(this);
            }
        }

        relationships.clear();

    }

    private void isNoLongerRelatedVia(RelationshipInstance relationship) {
        // delete any relationship to or from
        relationships.remove(relationship);
    }

    private void removeRelationshipsInvolvingMe(ThingInstance thing) {
        List<RelationshipInstance> toDelete = new ArrayList<RelationshipInstance>();

        for(RelationshipInstance relationship : relationships){
            if(relationship.getTo()==thing){
                toDelete.add(relationship);
            }
            if(relationship.getFrom()==thing){
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


    public ValidationReport validate() {
        ValidationReport report= new ValidationReport();


        for(String fieldName : entityDefinition.getFieldNames()){
            Field field = entityDefinition.getField(fieldName);
            ValidationReport validity = field.validate(instance.getValue(fieldName));
            report.combine(validity);
        }

        return report;
    }

    public ThingInstance createDuplicateWithoutRelationships() {
        ThingInstance cloneInstance = new ThingInstance(entityDefinition);
        cloneInstance.setFieldValuesFrom(instance.asMap());
        return cloneInstance;
    }


}

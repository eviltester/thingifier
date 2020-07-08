package uk.co.compendiumdev.thingifier.generic.instances;

import uk.co.compendiumdev.thingifier.api.ValidationReport;
import uk.co.compendiumdev.thingifier.generic.FieldType;
import uk.co.compendiumdev.thingifier.generic.definitions.Field;
import uk.co.compendiumdev.thingifier.generic.definitions.RelationshipVector;
import uk.co.compendiumdev.thingifier.generic.definitions.ThingDefinition;

import java.util.*;

import static uk.co.compendiumdev.thingifier.generic.definitions.Optionality.MANDATORY_RELATIONSHIP;

final public class ThingInstance {


    private final List<RelationshipInstance> relationships;
    private final ThingDefinition entityDefinition;
    private final InstanceFields instance;

    public ThingInstance(ThingDefinition eDefn) {
        this.entityDefinition = eDefn;
        this.instance = new InstanceFields();
        instance.addValue("guid", UUID.randomUUID().toString());
        this.relationships = new ArrayList<RelationshipInstance>();
    }

    public ThingInstance(ThingDefinition entityTestSession, String guid) {
        this(entityTestSession);
        instance.addValue("guid", guid);
    }

    public String toString() {

        StringBuilder output = new StringBuilder();

        output.append("\t\t\t" + entityDefinition.getName() + "\n");
        //output.append(instance.toString() + "\n");
        for (String fieldName : entityDefinition.getFieldNames()) {
            output.append(String.format("\t\t\t\t %s : %s %n", fieldName, getValue(fieldName)));
        }

        if (relationships.size() > 0) {
            output.append(String.format("\t\t\t\t\t Relationships:%n"));
            for (RelationshipInstance relatesTo : relationships) {
                if (relatesTo.getFrom() == this) {
                    output.append(String.format("\t\t\t\t\t %s : %s (%s)%n",
                            relatesTo.getRelationship().getName(),
                            relatesTo.getTo().getGUID(),
                            relatesTo.getTo().getEntity().getName()));
                } else {
                    output.append(String.format("\t\t\t\t\t %s : %s (%s)%n",
                            relatesTo.getRelationship().getReversedRelationship().getName(),
                            relatesTo.getFrom().getGUID(),
                            relatesTo.getFrom().getEntity().getName()));
                }
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

    public ThingInstance setValue(String fieldName, String value) {
        if (this.entityDefinition.hasFieldNameDefined(fieldName)) {
            Field field = entityDefinition.getField(fieldName);
            final ValidationReport validationReport = field.validate(value);
            if (validationReport.isValid()) {

                String valueToAdd = value;

                if(field.getType()== FieldType.STRING){
                    if(field.shouldTruncate()){
                        valueToAdd = valueToAdd.substring(0,field.getMaximumAllowedLength());
                    }
                }

                this.instance.addValue(fieldName, valueToAdd);

            } else {
                throw new IllegalArgumentException(
                        validationReport.getCombinedErrorMessages());
            }
        } else {
            reportCannotFindFieldError(fieldName);
        }
        return this;
    }

    public ThingInstance setFieldValuesFrom(final Map<String, String> args) {

        for (Map.Entry<String, String> entry : args.entrySet()) {

            // Handle attempt to amend a GUID
            if (!entry.getKey().equalsIgnoreCase("guid")) {

                // set the value
                setValue(entry.getKey(), entry.getValue());

            } else {

                // if editing it then throw error, ignore if same value
                String existingGuid = instance.getValue("guid");
                if (existingGuid != null && existingGuid.trim().length() > 0) {

                    // if value is different then it is an attempt to amend it
                    if (!existingGuid.equalsIgnoreCase(entry.getValue())) {
                        throw new RuntimeException(
                                String.format("Can not amend GUID on Entity %s from %s to %s",
                                        this.entityDefinition.getName(),
                                        existingGuid,
                                        entry.getValue()));
                    }
                }
            }
        }
        return this;
    }

    public ThingInstance setCloneFieldValuesFrom(final Map<String, String> args) {

        for (Map.Entry<String, String> entry : args.entrySet()) {
            setValue(entry.getKey(), entry.getValue());
        }

        return this;
    }

    private void reportCannotFindFieldError(final String fieldName) {
        throw new RuntimeException("Could not find field: " + fieldName + " on Entity " + this.entityDefinition.getName());
    }

    public String getValue(String fieldName) {
        if (this.entityDefinition.hasFieldNameDefined(fieldName)) {
            String assignedValue = this.instance.getValue(fieldName);
            if (assignedValue == null) {
                // does definition have a default value?
                if (this.entityDefinition.getField(fieldName).hasDefaultValue()) {
                    return getDefaultValue(fieldName);
                } else {
                    // return the field type default value
                    String defaultVal = this.entityDefinition.getField(fieldName).getType().getDefault();
                    if (defaultVal != null) {
                        return defaultVal;
                    }
                }
            } else {
                return assignedValue;
            }
        }

        reportCannotFindFieldError(fieldName);
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
        if (!entityDefinition.hasRelationship(relationshipName)) {
            throw new IllegalArgumentException(String.format("Unknown Relationship %s for %s : %s", relationshipName, entityDefinition.getName(), getGUID()));
        }

        RelationshipVector relationship = entityDefinition.getRelationship(relationshipName, thing.entityDefinition);

        RelationshipInstance related = new RelationshipInstance(relationship, this, thing);
        this.relationships.add(related);

        thing.isNowRelatedVia(related);

    }

    private void isNowRelatedVia(RelationshipInstance relationship) {

        // if the relationship vector has a parent that is both ways then we need to create a relationship of the reverse type to the thing that called us
        if (relationship.getRelationship().isTwoWay()) {
            this.relationships.add(relationship);
        }
    }

    public ThingDefinition typeOfConnectedItems(String relationshipName) {

        for (RelationshipVector relationship : entityDefinition.getRelationships()) {
            if (relationship.getRelationshipDefinition().isKnownAs(relationshipName)) {
                if (relationship.getTo().definition() == this.entityDefinition) {
                    return relationship.getFrom().definition();
                } else {
                    return relationship.getTo().definition();
                }
            }
        }

        return null;
    }

    public Collection<ThingInstance> connectedItems(String relationshipName) {
        Set<ThingInstance> theConnectedItems = new HashSet<ThingInstance>();
        for (RelationshipInstance relationship : relationships) {
            if (relationship.getRelationship().isKnownAs(relationshipName)) {
                if (relationship.getTo() == this) {
                    theConnectedItems.add(relationship.getFrom());
                } else {
                    theConnectedItems.add(relationship.getTo());
                }
            }
        }

        return theConnectedItems;
    }

    public void removeRelationshipsTo(ThingInstance thing, String relationshipName) {
        List<RelationshipInstance> toDelete = new ArrayList<RelationshipInstance>();

        for (RelationshipInstance relationship : relationships) {
            if (relationship.getRelationship().isKnownAs(relationshipName)) {
                if (relationship.getTo() == thing || relationship.getFrom() == thing) {
                    toDelete.add(relationship);
                    thing.isNoLongerRelatedVia(relationship);
                }
            }
        }

        relationships.removeAll(toDelete);

    }

    public List<ThingInstance> removeAllRelationships() {

        List<ThingInstance> deleteThese = new ArrayList<>();

        for (RelationshipInstance item : relationships) {
            if (item.getFrom() != this) {
                item.getFrom().removeRelationshipsInvolvingMe(this);
                if (item.getRelationship().getOptionalityFrom() == MANDATORY_RELATIONSHIP) {
                    // I am deleted, therefor any mandatory relationship to me, must result in the related thing being
                    // deleted also
                    deleteThese.add(item.getFrom());
                }
            } else {
                item.getTo().removeRelationshipsInvolvingMe(this);

//                if (item.getRelationship().getOptionalityTo() == MANDATORY_RELATIONSHIP) {
//                    // I am being deleted therefore it does not matter if relationship to other is mandatory
//                }
            }
        }

        relationships.clear();

        return deleteThese;

    }

    private void isNoLongerRelatedVia(RelationshipInstance relationship) {
        // delete any relationship to or from
        relationships.remove(relationship);
    }

    private void removeRelationshipsInvolvingMe(ThingInstance thing) {
        List<RelationshipInstance> toDelete = new ArrayList<RelationshipInstance>();

        for (RelationshipInstance relationship : relationships) {
            if (relationship.getTo() == thing) {
                toDelete.add(relationship);
            }
            if (relationship.getFrom() == thing) {
                toDelete.add(relationship);
            }
        }

        relationships.removeAll(toDelete);
    }

    public List<ThingInstance> connectedItemsOfType(String type) {
        List<ThingInstance> theConnectedItems = new ArrayList<ThingInstance>();
        for (RelationshipInstance relationship : relationships) {
            if (relationship.getTo().getEntity().getName().toLowerCase().contentEquals(type.toLowerCase())) {
                theConnectedItems.add(relationship.getTo());
            }
        }
        return theConnectedItems;
    }


    public void clearAllFields() {
        instance.deleteAllFieldsExcept("guid");
    }


    public ValidationReport validateFields(){
        ValidationReport report = new ValidationReport();


        // Field validation

        for (String fieldName : entityDefinition.getFieldNames()) {
            Field field = entityDefinition.getField(fieldName);
            ValidationReport validity = field.validate(instance.getValue(fieldName));
            report.combine(validity);
        }

        return report;
    }


    public ValidationReport validateRelationships(){
        ValidationReport report = new ValidationReport();


        // Optionality Relationship Validation
        final Collection<RelationshipVector> theRelationshipVectors = entityDefinition.getRelationships();
        for(RelationshipVector vector : theRelationshipVectors){
            // for each definition, does it have relationships that match
            if(vector.getOptionality() == MANDATORY_RELATIONSHIP){
                boolean foundRelationship = false;
                for(RelationshipInstance relationship : relationships){
                    if(relationship.getRelationship()==vector.getRelationshipDefinition()){
                        foundRelationship=true;
                    }
                }
                if(!foundRelationship){
                    report.combine(
                            new ValidationReport().
                                    setValid(false).
                                    addErrorMessage(String.format("Mandatory Relationship not found %s", vector.getName()))
                    );
                }
            }
        }

        return report;
    }

    public ValidationReport validate() {
        ValidationReport report = new ValidationReport();

        report.combine(validateFields());
        report.combine(validateRelationships());

        return report;
    }

    public ThingInstance createDuplicateWithoutRelationships() {
        ThingInstance cloneInstance = new ThingInstance(entityDefinition);
        cloneInstance.setCloneFieldValuesFrom(instance.asMap());
        return cloneInstance;
    }


    public boolean hasAnyRelationshipInstances() {
        return (relationships.size() >0);
    }
}

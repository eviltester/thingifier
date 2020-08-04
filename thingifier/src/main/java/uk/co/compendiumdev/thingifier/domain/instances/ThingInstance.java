package uk.co.compendiumdev.thingifier.domain.instances;

import uk.co.compendiumdev.thingifier.api.ValidationReport;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.BodyParser;
import uk.co.compendiumdev.thingifier.domain.FieldType;
import uk.co.compendiumdev.thingifier.domain.definitions.Field;
import uk.co.compendiumdev.thingifier.domain.definitions.RelationshipVector;
import uk.co.compendiumdev.thingifier.domain.definitions.ThingDefinition;

import java.util.*;

import static uk.co.compendiumdev.thingifier.domain.definitions.Optionality.MANDATORY_RELATIONSHIP;

public class ThingInstance {

    // TODO: this is messy because of cloning and documentation - find a way to simplify
    // TODO split 'objectInstance' from ThingInstance i.e. without relationships and without a GUID
    private final List<RelationshipInstance> relationships;
    private final ThingDefinition entityDefinition;
    private final InstanceFields instanceFields;
    private Map<String, ThingInstance> objectFields;

    public ThingInstance(ThingDefinition eDefn) {
        this(eDefn, true);
    }

    protected static ThingInstance getInstanceWithoutIds(ThingDefinition eDefn){
        return new ThingInstance(eDefn, false);
    }

    /**
     * Without ids this would be a dangerous thing instance since
     * the ids are supposed to be global to the entity
     * without ids should only be used for creating documentation or
     * example instances
     * @param eDefn
     * @param addIds
     */
    private ThingInstance(ThingDefinition eDefn, boolean addIds) {
        this.entityDefinition = eDefn;
        this.instanceFields = new InstanceFields();
        instanceFields.addValue("guid", UUID.randomUUID().toString());
        if(addIds) {
            eDefn.addIdsToInstance(instanceFields);
        }
        this.relationships = new ArrayList<RelationshipInstance>();
    }

    public ThingInstance(ThingDefinition entityTestSession, String guid) {
        this(entityTestSession);
        instanceFields.addValue("guid", guid);
    }

    // todo: put this in a 'reporting' class
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
        return instanceFields.getValue("guid");
    }

    public List<String> getFieldNames() {
        return this.entityDefinition.getFieldNames();
    }

    public ThingInstance setValue(String fieldName, String value) {

        if (this.entityDefinition.hasFieldNameDefined(fieldName)) {

            Field field = entityDefinition.getField(fieldName);
            return setFieldValue(field, fieldName, value);

        } else {

            if(fieldName.contains(".")){
                // processing a complex set of fields
                final String[] fields = fieldName.split("\\.");
                if (this.entityDefinition.hasFieldNameDefined(fields[0])){
                    Field field = entityDefinition.getField(fields[0]);
                    if(field.getType()!= FieldType.OBJECT){
                        throw new RuntimeException(
                                "Cannot reference fields on non object fields: " + fieldName + " on Entity " + this.entityDefinition.getName());
                    }
                    final List<String> fieldNames = new ArrayList();
                    fieldNames.addAll(Arrays.asList(fields));
                    fieldNames.remove(0);
                    // need to track the instance with it
                    final ThingInstance fieldInstance = getObjectInstance(fields[0]);

                    return setFieldValue(fieldInstance, fieldNames, value);
                }else{
                    // if it is not a relationship then we should throw an error
                    if(!this.entityDefinition.hasRelationship(fields[0])) {
                        reportCannotFindFieldError(fieldName);
                    }
                }
            }else {
                reportCannotFindFieldError(fieldName);
            }
        }
        return this;
    }
    private ThingInstance setFieldValue(final ThingInstance fieldInstance, final List<String> fieldNames, final String value) {

        String fieldName = fieldNames.get(0);
        final ThingDefinition defn = fieldInstance.entityDefinition;
        if(defn==null || !defn.hasFieldNameDefined(fieldName)){
            reportCannotFindFieldError(fieldName);
        }

        final Field nextField = defn.getField(fieldName);

        if(fieldNames.size()==1){
            return fieldInstance.setFieldValue(nextField, fieldName, value);
        }else{

            if(nextField.getType()!= FieldType.OBJECT){
                throw new RuntimeException(
                        "Cannot reference fields on non object fields: " + fieldName + " on Entity " + this.entityDefinition.getName());
            }
            fieldNames.remove(0);

            final ThingInstance nextFieldInstance = fieldInstance.getObjectInstance(fieldName);
            return setFieldValue(nextFieldInstance, fieldNames, value);
        }

    }
    private ThingInstance setFieldValue(final Field field, final String fieldName, final String value) {
        final ValidationReport validationReport = field.validate(value);
        if (validationReport.isValid()) {

            String valueToAdd = value;

            if(field.getType()== FieldType.STRING){
                if(field.shouldTruncate()){
                    valueToAdd = valueToAdd.substring(0,field.getMaximumAllowedLength());
                }
            }

            if(field.getType()== FieldType.INTEGER || field.getType()==FieldType.ID){
                // enforce an int of possible
                try {
                    Double dVal = Double.parseDouble(value);
                    valueToAdd = String.valueOf(dVal.intValue());
                }catch(Exception e){
                    valueToAdd = Integer.valueOf(valueToAdd).toString();
                }
            }

//            if(field.getType()== FieldType.INTEGER){
//                valueToAdd = Integer.valueOf(valueToAdd).toString();
//            }

            if(field.getType()== FieldType.BOOLEAN){
                valueToAdd = Boolean.valueOf(valueToAdd).toString();
            }

            if(field.getType()== FieldType.FLOAT){
                valueToAdd = Float.valueOf(valueToAdd).toString();
            }

            this.instanceFields.addValue(fieldName, valueToAdd);

        } else {
            throw new IllegalArgumentException(
                    validationReport.getCombinedErrorMessages());
        }

        return this;
    }

    public ThingInstance setFieldValuesFrom(final BodyParser args) {

        // protected fields
        List<String> idOrGuidFields = entityDefinition.getProtectedFieldNamesList();

        for (Map.Entry<String, String> entry : args.getFlattenedStringMap()) {

            // Handle attempt to amend a protected field
            if (!idOrGuidFields.contains(entry.getKey())) {
                // set the value because it is not protected
                setValue(entry.getKey(), entry.getValue());
            } else {
                // if editing it then throw error, ignore if same value
                String existingValue = instanceFields.getValue(entry.getKey());
                if (existingValue != null && existingValue.trim().length() > 0) {

                    // if value is different then it is an attempt to amend it
                    if (!existingValue.equalsIgnoreCase(entry.getValue())) {
                        throw new RuntimeException(
                                String.format("Can not amend %s on Entity %s from %s to %s",
                                        entry.getKey(),
                                        this.entityDefinition.getName(),
                                        existingValue,
                                        entry.getValue()));
                    }
                }
            }
        }
        return this;
    }



    public void overrideValue(final String key, final String value) {
        // bypass all validation
        this.instanceFields.addValue(key, value);
    }

    private void reportCannotFindFieldError(final String fieldName) {
        throw new RuntimeException("Could not find field: " + fieldName + " on Entity " + this.entityDefinition.getName());
    }

    public String getValue(String fieldName) {
        if (this.entityDefinition.hasFieldNameDefined(fieldName)) {
            // if an object, just return "", we should be using this to get the value
            if(this.entityDefinition.getField(fieldName).getType()==FieldType.OBJECT){
                return "";
            }

            String assignedValue = this.instanceFields.getValue(fieldName);
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

        RelationshipInstance related = new RelationshipInstance(relationship.getRelationshipDefinition(), this, thing);
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




    public ValidationReport validateFields(){
        return validateFields(new ArrayList<>());
    }

    public ValidationReport validateFields(List<String> excluding){
        ValidationReport report = new ValidationReport();

        // Field validation

        for (String fieldName : entityDefinition.getFieldNames()) {
            if(!excluding.contains(fieldName)) {
                Field field = entityDefinition.getField(fieldName);
                ValidationReport validity = field.validate(instanceFields.getValue(fieldName));
                report.combine(validity);
            }
        }

        return report;
    }
    public ValidationReport validateNonProtectedFields() {
        List<String> excluding = getProtectedFieldNamesList(); // guid and ids
        return validateFields(excluding);
    }

    private List<String> getProtectedFieldNamesList() {
        return entityDefinition.getProtectedFieldNamesList();
    }

    public ValidationReport validateRelationships(){
        ValidationReport report = new ValidationReport();


        // TODO: relationship cardinality validation e.g. too many, not enough etc.

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




    public boolean hasAnyRelationshipInstances() {
        return (relationships.size() >0);
    }

    public boolean hasIDField() {
        return entityDefinition.hasIDField();
    }

    public String getID() {
        String value = "";
        final Field field = entityDefinition.getIDField();
        if(field!=null) {
            value = getValue(field.getName());
        }
        return value;
    }


    // Cloning and documentation


    public void clearAllFields() {
        List<String>ignoreFields = new ArrayList<>();

        ignoreFields.add("guid");
        if(hasIDField()){
            ignoreFields.add(getEntity().getIDField().getName());
        }

        instanceFields.deleteAllFieldsExcept(ignoreFields);
    }

    public ThingInstance setCloneFieldValuesFrom(final Map<String, String> args) {

        for (Map.Entry<String, String> entry : args.entrySet()) {
            overrideValue(entry.getKey(), entry.getValue());
        }

        return this;
    }

    public ThingInstance createDuplicateWithoutRelationships() {
        ThingInstance cloneInstance = new ThingInstance(entityDefinition, false);
        cloneInstance.setCloneFieldValuesFrom(instanceFields.asMap());
        return cloneInstance;
    }

    public ThingInstance createDuplicateWithRelationships() {
        ThingInstance cloneInstance = new ThingInstance(entityDefinition, false);
        cloneInstance.setCloneFieldValuesFrom(instanceFields.asMap());
        for(RelationshipInstance relationship : relationships){
            RelationshipInstance clonedRelationship = new RelationshipInstance(
                                                            relationship.getRelationship(),
                                                            relationship.getFrom(),
                                                            relationship.getTo()
            );
            cloneInstance.relationships.add(clonedRelationship);
        }
        return cloneInstance;
    }

    public ThingInstance getObjectInstance(final String objectFieldName) {
        Field objectField = entityDefinition.getField(objectFieldName);
        if(objectField.getType()==FieldType.OBJECT){
            if(objectFields==null){
                objectFields = new HashMap<>();
            }
            ThingInstance instance = objectFields.get(objectFieldName);
            if(instance==null){
                instance = new ThingInstance(objectField.getObjectDefinition());
                addObjectInstance(objectFieldName, instance);
            }
            return instance;
        }
        return null;
    }

    private void addObjectInstance(final String objectFieldName, final ThingInstance thingInstance) {
        if(objectFields==null){
            objectFields = new HashMap<>();
        }
        objectFields.put(objectFieldName, thingInstance);
    }
}
